package cz.cuni.mff.xrg.odcs.backend.execution.pipeline.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupUtils {

    private static Logger LOG = LoggerFactory.getLogger(CleanupUtils.class);

    /**
     * Try to delete directory in execution directory. If error occur then is
     * logged but otherwise ignored.
     * 
     * @param toDelete
     */
    public static void deleteDirectory(File toDelete) {
        LOG.debug("Deleting: {}", toDelete.toString());

        try {
            FileUtils.deleteDirectory(toDelete);
        } catch (IOException e) {
            LOG.warn("Can't delete directory after execution", e);
        }
    }

    /**
     * Delete directory if it's empty.
     * 
     * @param toDelete
     */
    public static void deleteDirectoryIfEmpty(File toDelete) {
        if (!toDelete.exists()) {
            // file does not exist
            return;
        }

        LOG.debug("Deleting: {}", toDelete.toString());

        if (!toDelete.isDirectory()) {
            LOG.warn("Directory to delete is file: {}", toDelete.toString());
            return;
        }

        // check if empty
        if (toDelete.list().length == 0) {
            // empty
            try {
                FileUtils.deleteDirectory(toDelete);
            } catch (IOException e) {
                LOG.warn("Can't delete directory after execution", e);
            }
        }

    }

}
