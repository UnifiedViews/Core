package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.ShareType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.DPUFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.PipelineFacade;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUCreateException;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUModuleManipulator;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream.JPAXStream;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

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

    @Autowired
    private DPUModuleManipulator moduleManipulator;

    public Pipeline importPipeline(File zipFile, boolean importUserDataFile, boolean importScheduleFile) throws ImportException, IOException {
        final File tempDir;
        try {
            tempDir = resourceManager.getNewImportTempDir();
        } catch (MissingResourceException ex) {
            throw new ImportException(Messages.getString("ImportService.pipeline.temp.dir.fail"), ex);
        }
        return importPipeline(zipFile, tempDir, importUserDataFile, importScheduleFile);
    }

    public Pipeline importPipeline(File zipFile, File tempDirectory, boolean importUserDataFile, boolean importScheduleFile)
            throws ImportException, IOException {
        // delete tempDirectory
        FileUtils.deleteQuietly(tempDirectory);

        if (authCtx == null) {
            throw new ImportException(Messages.getString("ImportService.pipeline.authenticationContext.null"));
        }
        final User user = authCtx.getUser();
        if (user == null) {
            throw new ImportException(Messages.getString("ImportService.pipeline.unknown.user"));
        }
        // unpack
        Pipeline pipe;
        try {
            ZipCommons.unpack(zipFile, tempDirectory);

            pipe = loadPipeline(tempDirectory);
            pipe.setUser(user);
            if (user.getOrganization() != null)
                pipe.setOrganization(user.getOrganization());
            pipe.setShareType(ShareType.PRIVATE);

            // TODO skoda: Check for DPU versions here and warn in case of problems

            Map<DPUTemplateRecord, DPUTemplateRecord> importedTemplates = new HashMap<>();
            for (Node node : pipe.getGraph().getNodes()) {
                final DPUInstanceRecord dpu = node.getDpuInstance();
                final DPUTemplateRecord template = dpu.getTemplate();
                final DPUTemplateRecord templateToUse;
                if (importedTemplates.containsKey(template)) {
                    // already imported
                    templateToUse = importedTemplates.get(template);
                } else {
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
                    templateToUse = importDPUTemplate(template, user, jarFile,
                            userDataFile, globalDataFile, importUserDataFile);
                    // add to cache
                    importedTemplates.put(template, templateToUse);
                }
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
            // in every case delte temp directory
            if (!FileUtils.deleteQuietly(tempDirectory)) {
                // failed to delete directory
                LOG.warn("Failed to delete temp directory.");
            }
        }
        return pipe;
    }

    /**
     * @param baseDir
     * @return
     * @throws ImportException
     */
    public Pipeline loadPipeline(File baseDir) throws ImportException {
        final XStream xStream = JPAXStream.createForPipeline(new DomDriver("UTF-8"));
        final File sourceFile = new File(baseDir, ArchiveStructure.PIPELINE
                .getValue());
        try {
            return (Pipeline) xStream.fromXML(sourceFile);
        } catch (Throwable t) {
            String msg = Messages.getString("ImportService.pipeline.pipeline.file.fail");
            LOG.error(msg);
            throw new ImportException(msg, t);
        }
    }

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

    /**
     * Check if given template exist (compare for jar directory and jar name).
     * If not then import new DPU template into system. In both cases the user
     * and global DPU's data are copied into respective directories.
     *
     * @param template
     * @param user
     * @param jarFile
     * @param userDataDir
     * @param globalDataDir
     * @return Template that is stored in database and is equivalent to the
     *         given one.
     * @throws ImportException
     */
    private DPUTemplateRecord importDPUTemplate(DPUTemplateRecord template,
            User user, File jarFile, File userDataDir, File globalDataDir, boolean importUserDataFile)
            throws ImportException {
        // try to detect if there already exist same DPU
        DPUTemplateRecord result = dpuFacade.getByName(template
                .getName());
        if (result == null) {
            try {
                // we have to import new DPU
                result = moduleManipulator.create(jarFile, template.getName());
            } catch (DPUCreateException ex) {
                throw new ImportException(Messages.getString("ImportService.pipeline.dpu.import.fail"), ex);
            }
        } else {
            // check visibility
            if (result.getShareType() == ShareType.PRIVATE && !result
                    .getOwner().equals(user)) {
                // is private and not visible to given user
                result.setShareType(ShareType.PUBLIC_RO);
                dpuFacade.save(result);
            }

            // TODO add version check here
        }
        // copy user data
        if (userDataDir.exists() && importUserDataFile) {
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
        ZipCommons.unpack(zipFile, tempDirectory);
        Pipeline pipeline = loadPipeline(tempDirectory);

        List<DpuItem> usedDpus = loadUsedDpus(tempDirectory);
        TreeMap<String, DpuItem> missingDpus = new TreeMap<>();

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

                        // try to detect if dpus are installed
                        DPUTemplateRecord dpuTemplateRecord = dpuFacade
                                .getByName(template.getName());
                        // TODO jmc add version
                        String version = "unknown";
                        DpuItem dpuItem = new DpuItem(dpu.getName(), template.getJarName(), version);
                        if (dpuTemplateRecord == null) {
                            // these dpus is missing
                            if (!missingDpus.containsKey(dpu.getName())) {
                                missingDpus.put(dpu.getName(), dpuItem);
                            }
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
                missingDpus, isUserData, isScheduleFile);

        LOG.debug("<<< Leaving getImportedInformation: {}", result);
        return result;
    }

}
