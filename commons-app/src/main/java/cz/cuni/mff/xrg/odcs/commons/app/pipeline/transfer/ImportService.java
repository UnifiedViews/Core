/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUJarNameFormatException;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUJarUtils;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUModuleManipulator;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream.JPAXStream;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;

/**
 * Service for importing pipelines exported by {@link ExportService}.
 *
 * @author Škoda Petr
 */
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(
            ImportService.class);

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private DPUFacade dpuFacade;

    @Autowired
    private PipelineFacade pipelineFacade;

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    @Autowired(required = false)
    private PermissionUtils permissionUtils;

    @Autowired
    private DPUModuleManipulator moduleManipulator;

    @Autowired
    private AppConfig appConfig;

    @PreAuthorize("hasRole('pipeline.import') and hasRole('pipeline.create')")
    public Pipeline importPipeline(File zipFile, boolean importUserDataFile, boolean importScheduleFile) 
            throws ImportException, IOException {
        return importPipeline(zipFile, importUserDataFile, importScheduleFile, new HashMap<String, ImportStrategy>(0));
    }
    
    @PreAuthorize("hasRole('pipeline.import') and hasRole('pipeline.create')")
    public Pipeline importPipeline(File zipFile, boolean importUserDataFile, boolean importScheduleFile,
            Map<String, ImportStrategy> choosenStrategies) throws ImportException, IOException {
        final File tempDir;
        try {
            tempDir = resourceManager.getNewImportTempDir();
        } catch (MissingResourceException ex) {
            throw new ImportException(Messages.getString("ImportService.pipeline.temp.dir.fail"), ex);
        }
        return importPipeline(zipFile, tempDir, importUserDataFile, importScheduleFile, choosenStrategies);
    }

    @PreAuthorize("hasRole('pipeline.import') and hasRole('pipeline.create')")
    public Pipeline importPipeline(File zipFile, File tempDirectory, boolean importUserDataFile, boolean importScheduleFile,
            Map<String, ImportStrategy> choosenStrategies) throws ImportException, IOException {
        // delete tempDirectory
        ResourceManager.cleanupQuietly(tempDirectory);

        if (authCtx == null) {
            throw new ImportException(Messages.getString("ImportService.pipeline.authenticationContext.null"));
        }
        final User user = authCtx.getUser();
        if (user == null) {
            throw new ImportException(Messages.getString("ImportService.pipeline.unknown.user"));
        }
        final UserActor actor = this.authCtx.getUser().getUserActor();
        // unpack
        Pipeline pipe;
        try {
            ZipCommons.unpack(zipFile, tempDirectory);

            pipe = loadPipeline(tempDirectory);
            pipe.setUser(user);
            pipe.setActor(actor);
            pipe.setShareType(ShareType.PRIVATE);
            
            final List<DPUTemplateRecord> templateDPUs = loadTemplates(tempDirectory);

            for (Node node : pipe.getGraph().getNodes()) {
                final DPUInstanceRecord dpu = node.getDpuInstance();
                final DPUTemplateRecord template = dpu.getTemplate();
                final DPUTemplateRecord templateToUse;
                
                setParent(template, templateDPUs); // pipeline.xml doesn't contain info about parent DPU
                
                // prepare data for import
                final File jarFile = new File(tempDirectory,
                        ArchiveStructure.DPU_JAR.getValue() + File.separator
                        + template.getJarPath());
                final File userDataFile = new File(tempDirectory,
                        ArchiveStructure.DPU_DATA_USER.getValue() + File.separator
                        + template.getJarDirectory());
                final File globalDataFile = new File(tempDirectory,
                        ArchiveStructure.DPU_DATA_GLOBAL.getValue() + File.separator
                        + template.getJarDirectory());
                
                // import
                templateToUse = findDPUTemplate(dpu, template, user, jarFile,
                        userDataFile, globalDataFile, importUserDataFile, choosenStrategies);
                // set DPU instance
                dpu.setTemplate(templateToUse);
            }
            // save pipeline
            pipelineFacade.save(pipe);
            // add schedules
            final File scheduleFile = new File(tempDirectory,
                    ArchiveStructure.SCHEDULE.getValue());
            if (scheduleFile.exists() && importScheduleFile) {
                importSchedules(scheduleFile, pipe, user);
            }

        } catch (ImportException ex) {
            throw ex;
        } finally {
            ResourceManager.cleanupQuietly(tempDirectory);
        }
        return pipe;
    }

    private void setParent(DPUTemplateRecord template, List<DPUTemplateRecord> templateDPUs) {
        for (DPUTemplateRecord dpuTemplateRecord : templateDPUs) {
            
            if (template.getName().equals(dpuTemplateRecord.getName())
                    && haveTheSameConfig(template, dpuTemplateRecord)) {
                template.setParent(dpuTemplateRecord.getParent());
                return;
            }
        }
    }

    /**
     * @param baseDir
     * @return
     * @throws ImportException
     */
    @PreAuthorize("hasRole('pipeline.import')")
    public static Pipeline loadPipeline(File baseDir) throws ImportException {
        final XStream xStream = JPAXStream.createForPipeline(new DomDriver("UTF-8"));
        final File sourceFile = new File(baseDir, ArchiveStructure.PIPELINE
                .getValue());
        try {
            return (Pipeline) xStream.fromXML(sourceFile);
        } catch (Throwable t) {
            String msg = Messages.getString("ImportService.pipeline.xml.load.file.fail");
            LOG.error(msg);
            throw new ImportException(msg, t);
        }
    }

    @PreAuthorize("hasRole('pipeline.import')")
    public List<DpuItem> loadUsedDpus(File baseDir) throws ImportException {
        XStream xStream = new XStream(new DomDriver("UTF-8"));
        xStream.alias("dpus", List.class);
        xStream.alias("dpu", DpuItem.class);

        final File sourceFile = new File(baseDir, ArchiveStructure.USED_DPUS
                .getValue());
        if (!sourceFile.exists()) {
            LOG.warn("file: {} is not exist", sourceFile.getName());
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            List<DpuItem> result = (List<DpuItem>) xStream.fromXML(sourceFile);
            return result;
        } catch (Throwable t) {
            String msg = Messages.getString("ImportService.pipeline.dpu.file.wrong");
            LOG.error(msg);
            throw new ImportException(msg, t);
        }
    }
    
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('pipeline.import')")
    public List<DPUTemplateRecord> loadTemplates(File baseDir) throws ImportException {
        final File templatesFile = new File(baseDir, ArchiveStructure.DPU_TEMPLATE.getValue());
        if (!templatesFile.exists()) {
            LOG.warn("file: {} is not exist", templatesFile.getName());
            return null;
        }
        
        final XStream xStream = JPAXStream.createForDPUTemplate(new DomDriver("UTF-8"));
        
        try {
            return (List<DPUTemplateRecord>) xStream.fromXML(templatesFile);
        } catch (Throwable e) {
            String msg = Messages.getString("ImportService.wrong.lst.file");
            LOG.error(msg);
            throw new ImportException(msg, e);
        }
    }

    /**
     * Check if given template exist (compare for jar directory and jar name).
     * If not then import new DPU template into system. In both cases the user
     * and global DPU's data are copied into respective directories.
     *
     * @param dpu 
     * @param template
     * @param user
     * @param jarFile
     * @param userDataDir
     * @param globalDataDir
     * @param choosenStrategies 
     * @return Template that is stored in database and is equivalent to the
     *         given one.
     * @throws ImportException
     */
    private DPUTemplateRecord findDPUTemplate(DPUInstanceRecord dpu, DPUTemplateRecord template,
            User user, File jarFile, File userDataDir, File globalDataDir, boolean importUserDataFile,
            Map<String, ImportStrategy> choosenStrategies) throws ImportException {
        
        String dpuDir;
        try {
            dpuDir = DPUJarUtils.parseNameFromJarName(jarFile.getName());
        } catch (DPUJarNameFormatException e) {
            throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.fail"), e);
        }
        
        final DPUTemplateRecord parentDpu = dpuFacade.getByDirectory(dpuDir);
        final DPUTemplateRecord matchingNameDpu = dpuFacade.getByDirectoryAndName(dpuDir, template.getName());
        DPUTemplateRecord result = null;
        
        if (parentDpu == null) {
            throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.fail.not.found",
                    template.getName(),
                    jarFile.getName()));
        } else {
            checkVersion(parentDpu, jarFile);
        }
        
        if (template.getParent() == null) { // 2nd lvl dpu
            // can't use matchingNameDpu because name can be localized
            
            if (dpu.isUseTemplateConfig()) {
                result = getTemplate(parentDpu, template, user, dpu, jarFile, choosenStrategies);
            } else {
                checkPermissions(parentDpu, user);
                result = parentDpu;
            }
        } else { // 3rd lvl dpu
            if (matchingNameDpu == null) {
                throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.fail.not.found",
                        template.getName(),
                        jarFile.getName()));
            } else if (dpu.isUseTemplateConfig()) {
                result = getTemplate(matchingNameDpu, template, user, dpu, jarFile, choosenStrategies);
            } else {
                checkPermissions(matchingNameDpu, user);
                result = matchingNameDpu;
            }
        }
        
        // copy user data
        if (userDataDir.exists() && importUserDataFile) {
            if (!hasUserPermission(EntityPermissions.PIPELINE_IMPORT_USER_DATA)) {
                throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.data.permissions"));
            }
            try {
                final File dest = resourceManager
                        .getDPUDataUserDir(result, user);
                FileUtils.copyDirectory(userDataDir, dest);
            } catch (MissingResourceException ex) {
                throw new ImportException(Messages.getString("ImportService.pipeline.missing.resource"), ex);
            } catch (IOException ex) {
                throw new ImportException(Messages.getString("ImportService.pipeline.userData.copy.fail"), ex);
            }
        }

        // copy global data
        if (globalDataDir.exists()) {
            try {
                final File dest = resourceManager.getDPUDataGlobalDir(result);
                FileUtils.copyDirectory(globalDataDir, dest);
            } catch (MissingResourceException ex) {
                throw new ImportException(Messages.getString("ImportService.pipeline.missing.resource"), ex);
            } catch (IOException ex) {
                throw new ImportException(Messages.getString("ImportService.pipeline.globalData.copy.fail"), ex);
            }
        }

        return result;
    }

    private DPUTemplateRecord getTemplate(DPUTemplateRecord currentDpuTempalte, DPUTemplateRecord pipelineDpuTemplate, User user,
            DPUInstanceRecord dpu, File jarFile, Map<String, ImportStrategy> choosenStrategies)
            throws ImportException {
        if (haveTheSameConfig(currentDpuTempalte, pipelineDpuTemplate)) {
            checkPermissions(currentDpuTempalte, user);
            return currentDpuTempalte;
        } else {
            switch (choosenStrategies.get(dpu.getName())) {
                case REPLACE_INSTANCE_CONFIG:
                    dpu.setUseTemplateConfig(false);
                    dpu.setRawConf(pipelineDpuTemplate.getRawConf());
                case CHANGE_TO_EXISTING:
                    checkPermissions(currentDpuTempalte, user);
                    return currentDpuTempalte;
                default:
                    throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.fail.different.config",
                            pipelineDpuTemplate.getName(),
                            jarFile.getName()));
            }
        }
    }

    private void checkVersion(DPUTemplateRecord parentDpu, File jarFile) throws ImportException {
        try {
            int compareVersion = moduleManipulator.compareVersions(parentDpu.getJarName(), jarFile.getName());
            
            if (compareVersion < 0) {
                throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.fail.version",
                                                            jarFile.getName()));
            }
        } catch (DPUJarNameFormatException e) {
            throw new ImportException(e.getMessage(), e);
        }
    }

    private void checkPermissions(DPUTemplateRecord dpuTemplate, User user) throws ImportException {
        // check visibility
        if (dpuTemplate.getShareType() == ShareType.PRIVATE && !dpuTemplate.getOwner().equals(user)) {
            throw new ImportException(Messages.getString("ImportService.pipeline.import.dpu.fail.permission"));
        }
    }

    private boolean haveTheSameConfig(DPUTemplateRecord matchingNameDpu, DPUTemplateRecord template) {
        if (matchingNameDpu.getRawConf() == null && template.getRawConf() == null) {
            return true;
        }
        
        if (matchingNameDpu.getRawConf() == null
                || !matchingNameDpu.getRawConf().equals(template.getRawConf())) {
            return false;
        }
        
        return true;
    }

    /**
     * Load schedules from given file. The given use and pipeline is set to them
     * and then they are imported into system.
     *
     * @param scheduleFile
     *            File with schedules to load.
     * @param pipeline
     * @param user
     * @throws ImportException
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('pipeline.importScheduleRules')")
    private void importSchedules(File scheduleFile, Pipeline pipeline, User user)
            throws ImportException {
        final XStream xStream = JPAXStream.createForPipeline(new DomDriver("UTF-8"));
        final List<Schedule> schedules;
        try {
            schedules = (List<Schedule>) xStream.fromXML(scheduleFile);
        } catch (Throwable t) {
            throw new ImportException(Messages.getString("ImportService.pipeline.schedule.deserialization.fail"), t);
        }

        for (Schedule schedule : schedules) {
            // bind
            schedule.setPipeline(pipeline);
            schedule.setOwner(user);
            schedule.setActor(user.getUserActor());
            // save into database
            scheduleFacade.save(schedule);
        }
    }

    public ImportedFileInformation getImportedInformation(File zipFile)
            throws ImportException, MissingResourceException, IOException {
        LOG.debug(">>> Entering getImportedInformation(zipFile={})", zipFile);

        boolean isUserData = false;
        boolean isScheduleFile = false;

        File tempDirectory = resourceManager.getNewImportTempDir();
        try {
            ZipCommons.unpack(zipFile, tempDirectory);
            Pipeline pipeline = loadPipeline(tempDirectory);
            
            if (pipelineFacade.hasPipelineWithName(pipeline.getName(), null)) {
                throw new ImportException(Messages.getString("ImportService.pipeline.exists", pipeline.getName()));
            }
            
            final List<DpuItem> usedDpus = loadUsedDpus(tempDirectory);
            final Map<String, DpuItem> missingDpus = new TreeMap<>();
            final Map<String, VersionConflictInformation> oldDpus = new TreeMap<>();
            final Set<String> toDecideDpus = new HashSet<>();
            
            if (pipeline != null) {
                PipelineGraph graph = pipeline.getGraph();
                if (graph != null) {
                    Set<Node> nodes = graph.getNodes();
                    if (nodes != null) {
                        for (Node node : nodes) {
                            DPUInstanceRecord dpu = node.getDpuInstance();
                            if (dpu == null) {
                                continue;
                            }
                            
                            DPUTemplateRecord template = dpu.getTemplate();
                            
                            if (template == null) {
                                continue;
                            }
                            
                            String version = "unknown";
                            String jarDir = null;
                            
                            try {
                                jarDir = DPUJarUtils.parseNameFromJarName(template.getJarName());
                                version = DPUJarUtils.parseVersionStringFromJarName(template.getJarName());
                            } catch (DPUJarNameFormatException e) {
                                throw new ImportException(e.getMessage(), e);
                            }
                            
                            final DpuItem dpuItem = new DpuItem(template.getName(), template.getJarName(), version);
                            final DPUTemplateRecord parentDpuTemplate = dpuFacade.getByDirectory(jarDir);
                            final DPUTemplateRecord matchingDpuTemplate = dpuFacade.getByDirectoryAndName(jarDir, template.getName());
                            
                            try {
                                if (matchingDpuTemplate == null) {
                                    missingDpus.put(template.getName(), dpuItem);
                                } else if (moduleManipulator.compareVersions(parentDpuTemplate.getJarName(), template.getJarName()) < 0) {
                                    oldDpus.put(template.getName(), new VersionConflictInformation(dpuItem, parentDpuTemplate, template));
                                } else if (dpu.isUseTemplateConfig() && !haveTheSameConfig(matchingDpuTemplate, template)) {
                                    toDecideDpus.add(dpu.getName());
                                }
                            } catch (DPUJarNameFormatException e) {
                                // this DPU will fail to import ...
                                throw new ImportException(e.getMessage(), e);
                            }
                            
                            final File userDataFile = new File(tempDirectory,
                                    ArchiveStructure.DPU_DATA_USER.getValue() + File.separator
                                    + template.getJarDirectory());
                            
                            if (userDataFile.exists()) {
                                isUserData = true;
                                
                            }
                            LOG.debug("userDataFile: " + userDataFile.toString());
                        }
                        
                        final File scheduleFile = new File(tempDirectory,
                                ArchiveStructure.SCHEDULE.getValue());
                        if (scheduleFile.exists()) {
                            isScheduleFile = true;
                        }
                    }
                }
            }
            
            ImportedFileInformation result = new ImportedFileInformation(usedDpus,
                    missingDpus, isUserData, isScheduleFile, oldDpus, toDecideDpus);
            
            LOG.debug("<<< Leaving getImportedInformation: {}", result);
            return result;
        } finally {
            ResourceManager.cleanupQuietly(tempDirectory);
        }
    }

    public boolean hasUserPermission(String permission) {
        if (this.permissionUtils != null) {
            return this.permissionUtils.hasUserAuthority(permission);
        }
        return true;
    }

}
