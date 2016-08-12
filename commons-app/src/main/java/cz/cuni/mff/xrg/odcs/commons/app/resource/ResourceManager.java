package cz.cuni.mff.xrg.odcs.commons.app.resource;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPURecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * Provide access to resources.
 * TODO Petr: use test to define the folder structure
 *
 * @author Škoda Petr
 */
public class ResourceManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);

    /**
     * Name of sub-directory for shared DPU's data.
     */
    private static final String DPU_JAR_DIR = "dpu";

    /**
     * Name of sub-directory for per-user DPU's data.
     */
    private static final String DPU_DATE_USER_DIR = "user";

    /**
     * Name of sub-directory for DPU's global data.
     */
    private static final String DPU_DATA_GLOBAL_DIR = "dpu";

    /**
     * Name of sub-directory for import.
     */
    private static final String IMPORT_DIR = "temp" + File.separator + "import";

    /**
     * Name of sub-directory for export.
     */
    private static final String EXPORT_DIR = "temp" + File.separator + "export";

    /**
     * Name of sub-directory where RDF repositories could store their data.
     */
    private static final String REPOSITORY_DIRECTORY = "repositories";

    /**
     * Prefix of sub-directory where data related to a single execution should be stored.
     */
    private static final String EXEC_DIR_PREFIX = "exec_";

    /**
     * Located under {@link #EXEC_DIR_PREFIX}.
     */
    private static final String EXEC_WORKING_DIR = "working";

    /**
     * Located under {@link #EXEC_DIR_PREFIX}.
     */
    private static final String EXEC_STORAGE_DIR = "storage";

    private static final String DPU_PREFIX = "dpu_";

    /**
     * No prefix used for backward compatibility.
     */
    private static final String DATA_UNIT_PREFIX = "";

    private static final String REPOSITORY_DIR = "repositories";

    @Autowired
    protected AppConfig appConfig;

    /**
     * @param execution
     * @return Root directory for pipeline execution. Directory does not have to exists.
     * @throws MissingResourceException
     */
    public File getExecutionDir(PipelineExecution execution) throws MissingResourceException {
        return new File(getRootWorkingDir(), EXEC_DIR_PREFIX + execution.getId().toString());
    }

    public File getExecutionRepositoryDir(Long executionId) throws MissingResourceException {
        return new File(getRootWorkingDir(), EXEC_DIR_PREFIX + executionId.toString() +
                File.separator + REPOSITORY_DIR);
    }

    public File getExecutionWorkingDir(PipelineExecution execution) throws MissingResourceException {
        return new File(getExecutionDir(execution), EXEC_WORKING_DIR);
    }

    public File getExecutionStorageDir(PipelineExecution execution) throws MissingResourceException {
        return new File(getExecutionDir(execution), EXEC_STORAGE_DIR);
    }

    public File getDataUnitStorageDir(PipelineExecution execution, DPUInstanceRecord dpu, Integer index)
            throws MissingResourceException {
        return new File(getDataUnitStorageDir(execution, dpu), DATA_UNIT_PREFIX + index.toString());
    }

    public File getDataUnitWorkingDir(PipelineExecution execution, DPUInstanceRecord dpu, Integer index)
            throws MissingResourceException {
        return new File(getDataUnitStorageDir(execution, dpu), DATA_UNIT_PREFIX + index.toString());
    }

    public File getDataUnitStorageDir(PipelineExecution execution, DPUInstanceRecord dpu)
            throws MissingResourceException {
        return new File(getExecutionDir(execution), EXEC_STORAGE_DIR + File.separatorChar +
                getDpuDirectoryName(dpu));
    }

    public File getDataUnitWorkingDir(PipelineExecution execution, DPUInstanceRecord dpu)
            throws MissingResourceException {
        return new File(getExecutionDir(execution), EXEC_WORKING_DIR + File.separatorChar +
                getDpuDirectoryName(dpu));
    }

    /**
     * @param dpuInstance
     * @return Name of directory for given dpu.
     */
    private String getDpuDirectoryName(DPUInstanceRecord dpuInstance) {
        return DPU_PREFIX + dpuInstance.getId().toString();
    }

    /**
     * Return jar file of given DPU.
     * 
     * @param dpu
     * @return
     * @throws MissingResourceException
     */
    public File getDPUJarFile(DPURecord dpu) throws MissingResourceException {
        final String modulePath;
        try {
            modulePath = appConfig.getString(ConfigProperty.MODULE_PATH);
        } catch (MissingConfigPropertyException ex) {
            throw new MissingResourceException(
                    Messages.getString("ResourceManager.module_path.not.set"));
        }
        // get DPU template
        final DPUTemplateRecord template = getDPUTemplate(dpu);

        return new File(modulePath + File.separator + DPU_JAR_DIR,
                template.getJarPath());
    }

    /**
     * @param execution
     * @param dpu
     * @return Path to the DPU working directory.
     * @throws MissingResourceException
     */
    public File getDPUWorkingDir(PipelineExecution execution, DPUInstanceRecord dpu)
            throws MissingResourceException {
        // TODO Petr: we may utilize getDataUnitWorkingDir in some form
        return new File(getExecutionWorkingDir(execution),
                getDpuDirectoryName(dpu) + File.separatorChar + "dpu");
    }

    /**
     * @param execution
     * @param dpu
     * @return Path to the DPU working directory.
     * @throws MissingResourceException
     */
    public File getDPUStorageDir(PipelineExecution execution, DPUInstanceRecord dpu)
            throws MissingResourceException {
        // TODO Petr: we may utilize getDataUnitStorageDir in some form
        return new File(getExecutionStorageDir(execution),
                getDpuDirectoryName(dpu) + File.separatorChar + "dpu");
    }

    /**
     * @param dpu
     * @param user
     * @return Path to per-user DPU data storage.
     * @throws MissingResourceException
     */
    public File getDPUDataUserDir(DPURecord dpu, User user) throws MissingResourceException {
        if (user == null) {
            throw new MissingResourceException(Messages.getString("ResourceManager.unknown.user"));
        }

        final String workingPath = getRootWorkingDir();
        final DPUTemplateRecord template = getDPUTemplate(dpu);

        // prepare relative part of the path
        final String relativePath = DPU_DATE_USER_DIR + File.separator + user.getUsername() +
                File.separator + template.getJarDirectory();

        return new File(workingPath, relativePath);
    }

    /**
     * @param dpu
     * @return Path to global shared DPU data directory.
     * @throws MissingResourceException
     */
    public File getDPUDataGlobalDir(DPURecord dpu) throws MissingResourceException {
        final String workingPath = getRootWorkingDir();
        final DPUTemplateRecord template = getDPUTemplate(dpu);

        // prepare relative part of the path
        final String relativePath = DPU_DATA_GLOBAL_DIR + File.separator
                + template.getJarDirectory();

        return new File(workingPath, relativePath);

    }

    /**
     * @return Path to temp directory for import operations.
     * @throws MissingResourceException
     */
    public File getImportTempDir() throws MissingResourceException {
        final String workingPath = getRootWorkingDir();
        return new File(workingPath, IMPORT_DIR);
    }

    /**
     * @return New unique import directory as subdirectory of {@link #getImportTempDir()}
     * @throws MissingResourceException
     */
    public File getNewImportTempDir() throws MissingResourceException {
        final File root = getImportTempDir();
        return getNewUniqueDir(root);
    }

    /**
     * @return Path to temp directory for export operations.
     * @throws MissingResourceException
     */
    public File getExportTempDir() throws MissingResourceException {
        final String workingPath = getRootWorkingDir();
        return new File(workingPath, EXPORT_DIR);
    }

    /**
     * TODO: Remove, export should use single deterministic file for a pipeline.
     * 
     * @return New unique import directory. It's subdirectory for {@link #getExportTempDir()}.
     * @throws MissingResourceException
     */
    public File getNewExportTempDir() throws MissingResourceException {
        final File root = getExportTempDir();
        return getNewUniqueDir(root);
    }

    /**
     * @return Directory where RDF repositories should be stored.
     * @throws MissingResourceException
     */
    public File getRootRepositoriesDir() throws MissingResourceException {
        return new File(getRootWorkingDir(), REPOSITORY_DIRECTORY);
    }

    /**
     * @param dpu
     * @return Template for given DPU. Return given DPU if it's template.
     * @throws MissingResourceException
     */
    private DPUTemplateRecord getDPUTemplate(DPURecord dpu) throws MissingResourceException {
        final DPUTemplateRecord template;
        if (dpu instanceof DPUInstanceRecord) {
            template = ((DPUInstanceRecord) dpu).getTemplate();
        } else if (dpu instanceof DPUTemplateRecord) {
            template = (DPUTemplateRecord) dpu;
        } else {
            throw new MissingResourceException(Messages.getString("ResourceManager.unknown.dpu.type"));
        }

        if (template == null) {
            throw new MissingResourceException(Messages.getString("ResourceManager.dpuTemplate.not.set"));
        }
        return template;
    }

    /**
     * @return Working directory.
     * @throws MissingResourceException
     */
    private String getRootWorkingDir() throws MissingResourceException {
        try {
            return appConfig.getString(ConfigProperty.GENERAL_WORKINGDIR);
        } catch (MissingConfigPropertyException ex) {
            throw new MissingResourceException(
                    Messages.getString("ResourceManager.module_path.not.set"));
        }
    }

    /**
     * TODO: Not a good way how to get a temporary file, should be removed
     * as the import/export will be done. And pipelines will be saved
     * using their ids.
     * 
     * @param root
     * @return Newly created subdirectory in given directory.
     * @throws MissingResourceException
     */
    private File getNewUniqueDir(File root) throws MissingResourceException {
        final String datePrefix = DateFormatUtils.format(new Date(),
                "MM-dd-HH-mm");
        Integer i = 0;
        // get new file
        File newFile = new File(root, datePrefix + "-" + i.toString());
        while (!newFile.mkdirs()) {
            i++;
            newFile = new File(root, datePrefix + "-" + i.toString());
            if (i > 1000) {
                throw new MissingResourceException(
                        Messages.getString("ResourceManager.temp.dir.create.fail") + root.toString());
            }
        }
        return newFile;
    }

    public static void cleanupQuietly(File... filesToCleanup) {
        for (File file : filesToCleanup) {
            if (file == null || !file.exists()) {
                continue;
            }
            
            LOG.debug("Cleaning up file / dir: " + file);
            if (!FileUtils.deleteQuietly(file)) {
                LOG.warn("Failed to delete temp directory.");
            }
        }
    }
}
