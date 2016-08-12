package cz.cuni.mff.xrg.odcs.commons.app.dpu.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ArchiveStructure;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ExportException;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.xstream.JPAXStream;
import cz.cuni.mff.xrg.odcs.commons.app.resource.MissingResourceException;
import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Exporting DPU info file
 *
 * @author mvi
 */
public class ExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private ResourceManager resourceManager;

    @Autowired(required = false)
    private AuthenticationContext authCtx;

    @PreAuthorize("hasRole('dpuTemplate.export')")
    public File exportDPUs(List<DPUTemplateRecord> dpusToExport) throws ExportException {

        checkAuth(getAuthCtx());

        final File tempDir = createTempDir();
        File targetFile = new File(tempDir, "dpu_export.zip");

        ZipOutputStream zipStream = null;
        try {
            zipStream = new ZipOutputStream(new FileOutputStream(targetFile));

            Set<String> savedTemplateDir = new HashSet<String>();

            // export dpu jars
            for (DPUTemplateRecord dpuTemplateRecord : dpusToExport) {
                if (savedTemplateDir.contains(dpuTemplateRecord.getJarDirectory())) {
                    continue; // already saved jar
                } else {
                    savedTemplateDir.add(dpuTemplateRecord.getJarDirectory());
                }
                exportDPUJar(dpuTemplateRecord, zipStream);
            }

            // create .lst file with description
            exportTemplates(dpusToExport, zipStream);
        } catch (IOException ex) {
            LOG.error("Failed to prepare file with exported pipeline", ex);
            targetFile.delete();
            throw new ExportException(
                    Messages.getString("ExportService.prepare.file.fail"), ex);
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                    targetFile.delete();
                    throw new ExportException(
                            Messages.getString("ExportService.close.zip.fail"), e);
                }
            }
        }

        return targetFile;
    }

    /**
     * Serialise dpu list into zip stream.
     *
     * @param dpusToExport
     * @param zipStream
     * @throws ExportException
     */
    private void exportTemplates(List<DPUTemplateRecord> dpusToExport,
            ZipOutputStream zipStream) throws ExportException {
        final XStream xStream = JPAXStream.createForDPUTemplate(new DomDriver("UTF-8"));

        try {
            final ZipEntry ze = new ZipEntry(ArchiveStructure.DPU_TEMPLATE.getValue());
            zipStream.putNextEntry(ze);
            // write into entry
            xStream.toXML(dpusToExport, zipStream);
        } catch (IOException ex) {
            LOG.error("Failed to serialize dpu list.", ex);
            throw new ExportException(Messages.getString("ExportService.dpu.list.fail"), ex);
        }
    }

    /**
     * Save jar file for given DPU into given directory.
     *
     * @param template
     * @param zipStream
     * @throws ExportException
     */
    private void exportDPUJar(DPUTemplateRecord dpuTemplateRecord,
            ZipOutputStream zipStream) throws ExportException {
        // we copy the structure in dpu directory
        final File source;
        try {
            source = resourceManager.getDPUJarFile(dpuTemplateRecord);
        } catch (MissingResourceException ex) {
            throw new ExportException(Messages.getString("ExportService.jarFile.path.fail"));
        }
        byte[] buffer = new byte[4096];
        try {
            final ZipEntry ze = new ZipEntry(dpuTemplateRecord.getJarPath());
            zipStream.putNextEntry(ze);
            // move jar file into the zip file
            try (FileInputStream in = new FileInputStream(source)) {
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zipStream.write(buffer, 0, len);
                }
            }
        } catch (IOException ex) {
            LOG.error("Failed to copy jar for dpu " + dpuTemplateRecord.getName(), ex);
            throw new ExportException(Messages.getString("ExportService.jarFile.copy.fail"), ex);
        }
    }

    private File createTempDir() throws ExportException {
        try {
            return resourceManager.getNewExportTempDir();
        } catch (MissingResourceException ex) {
            throw new ExportException(Messages.getString("ExportService.temp.dir.fail"), ex);
        }
    }

    private void checkAuth(AuthenticationContext authCtx) throws ExportException {
        if (authCtx == null) {
            throw new ExportException(Messages.getString("ExportService.authenticationContext.fail"));
        }
        final User user = authCtx.getUser();
        if (user == null) {
            throw new ExportException(Messages.getString("ExportService.unknown.user"));
        }
    }

    public AuthenticationContext getAuthCtx() {
        return authCtx;
    }

}
