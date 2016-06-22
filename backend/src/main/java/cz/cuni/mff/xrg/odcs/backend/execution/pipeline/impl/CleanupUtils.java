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
