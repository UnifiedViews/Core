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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ScheduleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUJarNameFormatException;
import cz.cuni.mff.xrg.odcs.commons.app.module.DPUJarUtils;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.Pipeline;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Node;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream.JPAXStream;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.commons.app.scheduling.Schedule;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Export given pipeline into file.
 *
 * @author Škoda Petr
 */
public class ExportService {

    private static final Logger LOG = LoggerFactory.getLogger(
            ExportService.class);

    private static final String XML_ENCODING = "UTF-8";

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    @Autowired
    private PermissionUtils permissionUtils;

    /**
     * Create a temp file and exportPipeline pipeline into it.
     *
     * @param pipeline
     * @param setting
     * @return File with exportPipelineed pipeline.
     * @throws ExportException
     */
    @PreAuthorize("hasPermission(#pipeline,'pipeline.export') AND hasRole('pipeline.export')")
    public File exportPipeline(Pipeline pipeline, ExportSetting setting) throws ExportException {
        final File tempDir;
        try {
            tempDir = resourceManager.getNewExportTempDir();
        } catch (MissingResourceException ex) {
            throw new ExportException(Messages.getString("ExportService.temp.dir.fail"), ex);
        }
        final StringBuilder fileName = new StringBuilder();
        fileName.append("pipeline-");
        fileName.append(pipeline.getId().toString());
        fileName.append(".zip");

        File targetFile = new File(tempDir, fileName.toString());
        exportPipeline(pipeline, targetFile, setting, getAuthCtx());
        return targetFile;
    }

    public AuthenticationContext getAuthCtx() {
        return authCtx;
    }

    public void setAuthCtx(AuthenticationContext authCtx) {
        this.authCtx = authCtx;
    }

    /**
     * Export given pipeline and all it's dependencies into given file.
     *
     * @param pipeline
     * @param targetFile
     * @param setting
     * @param authCtx
     * @throws ExportException
     */
    @PreAuthorize("hasPermission(#pipeline,'pipeline.export') AND hasRole('pipeline.export')")
    public void exportPipeline(Pipeline pipeline, File targetFile, ExportSetting setting, AuthenticationContext authCtx)
            throws ExportException {

        if (authCtx == null) {
            throw new ExportException(Messages.getString("ExportService.authenticationContext.fail"));
        }
        final User user = authCtx.getUser();
        if (user == null) {
            throw new ExportException(Messages.getString("ExportService.unknown.user"));
        }

        try (FileOutputStream fos = new FileOutputStream(targetFile);
                ZipOutputStream zipStream = new ZipOutputStream(fos)) {
            // save information about pipeline and schedule
            savePipeline(pipeline, zipStream);

            if (setting.isChbExportSchedule()) {
                saveSchedule(pipeline, zipStream);
            }
            // save jar and dpu files
            Map<Long, DPUTemplateRecord> savedTemplates = new HashMap<Long, DPUTemplateRecord>();
            Set<String> savedTemplateDir = new HashSet<String>();

            TreeSet<DpuItem> dpusInformation = new TreeSet<>();

            for (Node node : pipeline.getGraph().getNodes()) {
                final DPUInstanceRecord dpu = node.getDpuInstance();
                final DPUTemplateRecord template = dpu.getTemplate();
                if (savedTemplates.containsKey(template.getId())) {
                    // template already saved
                } else {
                    savedTemplates.put(template.getId(), template);
                    // export jar file
                    final String jarDirectory = template.getJarDirectory();
                    if (savedTemplateDir.contains(jarDirectory)) {
                        // jar already exported
                    } else {
                        savedTemplateDir.add(jarDirectory);

                        if (setting.isExportJars()) {
                            saveDPUJar(template, zipStream);
                        }
                    }
                    // copy data
                    if (setting.isExportDPUUserData()) {
                        saveDPUDataUser(template, user, zipStream);
                        saveDPUDataGlobal(template, zipStream);
                    }
                }

                String version = "unknown";
                
                try {
                    version = DPUJarUtils.parseVersionStringFromJarName(template.getJarName());
                } catch (DPUJarNameFormatException e) {
                    LOG.warn(e.getMessage());
                }
                
                DpuItem dpuItem = new DpuItem(dpu.getName(), template.getJarName(), version);
                if (!dpusInformation.contains(dpuItem)) {
                    dpusInformation.add(dpuItem);
                }

            }
            saveDpusInfo(dpusInformation, zipStream);
            saveTemplateInfo(new ArrayList<DPUTemplateRecord>(savedTemplates.values()), zipStream);

        } catch (IOException ex) {
            targetFile.delete();
            throw new ExportException(
                    Messages.getString("ExportService.pipeline.prepare.fail"), ex);
        } catch (ExportException ex) {
            targetFile.delete();
            throw ex;
        }
    }

    private void saveTemplateInfo(List<DPUTemplateRecord> savedTemplates, ZipOutputStream zipStream) throws ExportException {
        final XStream xStream = JPAXStream.createForDPUTemplate(new DomDriver(XML_ENCODING));
        try {
            final ZipEntry ze = new ZipEntry(ArchiveStructure.DPU_TEMPLATE.getValue());
            zipStream.putNextEntry(ze);
            xStream.toXML(savedTemplates, zipStream);
        } catch (IOException e) {
            throw new ExportException(Messages.getString("ExportService.dpu.serialization.fail"), e);
        }
    }

    /**
     * Serialise pipeline into zip stream.
     *
     * @param pipeline
     * @param zipStream
     * @throws ExportException
     */
    private void savePipeline(Pipeline pipeline, ZipOutputStream zipStream)
            throws ExportException {
        final XStream xStream = JPAXStream.createForPipeline(new DomDriver(XML_ENCODING));
        try {
            final ZipEntry ze = new ZipEntry(ArchiveStructure.PIPELINE.getValue());
            zipStream.putNextEntry(ze);
            // write into entry
            xStream.toXML(pipeline, zipStream);
        } catch (IOException ex) {
            throw new ExportException(Messages.getString("ExportService.pipeline.serialization.fail"), ex);
        }
    }

    /**
     * Serialise all schedule that are visible to current used into given zip
     * stream.
     *
     * @param pipeline
     * @param zipStream
     * @throws ExportException
     */
    @PreAuthorize("hasRole('pipeline.exportScheduleRules')")
    private void saveSchedule(Pipeline pipeline, ZipOutputStream zipStream)
            throws ExportException {
        final XStream xStream = JPAXStream.createForSchedule(new DomDriver(XML_ENCODING));
        final List<Schedule> schedules = scheduleFacade
                .getSchedulesFor(pipeline);
        try {
            final ZipEntry ze = new ZipEntry(ArchiveStructure.SCHEDULE.getValue());
            zipStream.putNextEntry(ze);
            // write into entry
            xStream.toXML(schedules, zipStream);
        } catch (IOException ex) {
            throw new ExportException(Messages.getString("ExportService.schedule.serialization.fail"), ex);
        }
    }

    /**
     * Save jar file for given DPU into subdirectory in given directory.
     *
     * @param template
     * @param zipStream
     * @throws ExportException
     */
    @PreAuthorize("hasRole('pipeline.exportDpuJars')")
    private void saveDPUJar(DPUTemplateRecord template,
            ZipOutputStream zipStream)
            throws ExportException {
        // we copy the structure in dpu directory
        final File source;
        try {
            source = resourceManager.getDPUJarFile(template);
        } catch (MissingResourceException ex) {
            throw new ExportException(Messages.getString("ExportService.jarFile.path.fail"));
        }
        byte[] buffer = new byte[4096];
        try {
            final ZipEntry ze = new ZipEntry(ArchiveStructure.DPU_JAR.getValue() +
                    ZipCommons.uniteSeparator + template.getJarPath());
            zipStream.putNextEntry(ze);
            // move jar file into the zip file
            try (FileInputStream in = new FileInputStream(source)) {
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zipStream.write(buffer, 0, len);
                }
            }
        } catch (IOException ex) {
            throw new ExportException(Messages.getString("ExportService.jarFile.copy.fail"), ex);
        }
    }

    /**
     * Export DPU's user-based data into given zip stream.
     *
     * @param template
     * @param user
     * @param zipStream
     * @throws ExportException
     */
    @PreAuthorize("hasRole('pipeline.exportDpuData')")
    private void saveDPUDataUser(DPUTemplateRecord template, User user,
            ZipOutputStream zipStream) throws ExportException {
        final File source;
        try {
            source = resourceManager.getDPUDataUserDir(template, user);
        } catch (MissingResourceException ex) {
            throw new ExportException(Messages.getString("ExportService.jarFile.path.fail"));
        }

        final String zipPrefix = ArchiveStructure.DPU_DATA_USER.getValue()
                + ZipCommons.uniteSeparator + template.getJarDirectory();

        saveDirectory(source, zipPrefix, zipStream);
    }

    /**
     * Export DPU's global data into given zip stream.
     *
     * @param template
     * @param zipStream
     * @throws ExportException
     */
    @PreAuthorize("hasRole('pipeline.exportDpuData')")
    private void saveDPUDataGlobal(DPUTemplateRecord template,
            ZipOutputStream zipStream) throws ExportException {
        final File source;
        try {
            source = resourceManager.getDPUDataGlobalDir(template);
        } catch (MissingResourceException ex) {
            throw new ExportException(Messages.getString("ExportService.jarFile.path.fail"));
        }

        final String zipPrefix = ArchiveStructure.DPU_DATA_GLOBAL.getValue()
                + ZipCommons.uniteSeparator + template.getJarDirectory();

        saveDirectory(source, zipPrefix, zipStream);
    }

    /**
     * Add files and directories from given directory into a zip. Relative path
     * from the given directory is used to identify the relative path in zip.
     *
     * @param source
     * @param targetPrefix
     *            Path prefix in output zip, it should not end with
     *            separator.
     * @param zipStream
     * @throws ExportException
     */
    private void saveDirectory(File source, String targetPrefix,
            ZipOutputStream zipStream) throws ExportException {
        if (!source.exists()) {
            // nothing to exportPipeline
            LOG.trace("Skipping '{}' as it does not exist.", source.toString());
            return;
        }
        LOG.trace("Copy '{}' under '{}'.", source.toString(), targetPrefix);

        // no we add files into the
        byte[] buffer = new byte[4096];
        final int sourceLenght;
        try {
            sourceLenght = source.getCanonicalPath().length() + 1;
        } catch (IOException ex) {
            throw new ExportException(Messages.getString("ExportService.canonical.path.fail"), ex);
        }

        final Collection<File> files = FileUtils.listFiles(source,
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        for (File file : files) {
            if (!file.isFile()) {
                // not a file -> skip
                continue;
            }
            try {
                // prepare relative path in archive
                final String relativePath = targetPrefix + ZipCommons.uniteSeparator
                        + file.getCanonicalPath().substring(sourceLenght);
                // ...
                final ZipEntry ze = new ZipEntry(relativePath);
                zipStream.putNextEntry(ze);
            } catch (IOException ex) {
                throw new ExportException(Messages.getString("ExportService.zip.preparation.fail"),
                        ex);
            }
            // transfer data
            try (FileInputStream in = new FileInputStream(file)) {
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zipStream.write(buffer, 0, len);
                }
            } catch (IOException ex) {
                throw new ExportException(Messages.getString("ExportService.zip.archive.fail"),
                        ex);
            }
        }
    }

    public void saveDpusInfo(TreeSet<DpuItem> dpusInformation, ZipOutputStream zipStream) throws ExportException {
        LOG.debug(">>> Entering saveDpusInfo(dpusInformation={}, zipStream={})", dpusInformation, zipStream);

        XStream xStream = new XStream(new DomDriver(XML_ENCODING));
        // treeSet is not possible to aliasing
        List<DpuItem> dpus = new ArrayList<DpuItem>();
        dpus.addAll(dpusInformation);
        xStream.alias("dpus", List.class);
        xStream.alias("dpu", DpuItem.class);

        File serializedTarget = null;
        try {
            try {
                serializedTarget = File.createTempFile("temp", ".tmp");
            } catch (IOException ex2) {
                throw new ExportException(Messages.getString("ExportService.error"), ex2);
            }
            try (FileOutputStream foutStream = new FileOutputStream(serializedTarget)) {
                xStream.toXML(dpus, foutStream);
            } catch (IOException ex1) {
                throw new ExportException(Messages.getString("ExportService.error"), ex1);
            }

            byte[] buffer = new byte[4096];
            try {
                final ZipEntry ze = new ZipEntry(ArchiveStructure.USED_DPUS.getValue());
                zipStream.putNextEntry(ze);

                // move jar file into the zip file
                try (FileInputStream in = new FileInputStream(serializedTarget)) {
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zipStream.write(buffer, 0, len);
                    }
                }
            } catch (IOException ex) {
                throw new ExportException(Messages.getString("ExportService.jarFile.infos.fail"), ex);
            }
            LOG.debug("<<< Leaving saveDpusInfo()");
        } finally {
            ResourceManager.cleanupQuietly(serializedTarget);
        }
    }

    public TreeSet<DpuItem> getDpusInformation(Pipeline pipeline) {
        LOG.debug(">>> Entering getDpusInformation(pipeline={})", pipeline.getId());

        TreeSet<DpuItem> dpusInformation = new TreeSet<>();
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
                        String instanceName = dpu.getName();

                        if (template == null) {
                            continue;
                        }

                        String jarName = template.getJarName();
                        String version = "unknown";
                        
                        try {
                            version = DPUJarUtils.parseVersionStringFromJarName(template.getJarName());
                        } catch (DPUJarNameFormatException e) {
                            LOG.warn(e.getMessage());
                        }
                        
                        DpuItem dpuItem = new DpuItem(instanceName, jarName, version);
                        if (!dpusInformation.contains(dpuItem)) {
                            dpusInformation.add(dpuItem);
                        }
                    }
                }
            }
        }

        LOG.debug("<<< Leaving getDpusInformation: {}", dpusInformation);
        return dpusInformation;
    }

    public boolean hasUserPermission(String permission) {
        return this.permissionUtils.hasUserAuthority(permission);
    }

}
