package cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipCommons {
    private static final Logger LOG = LoggerFactory.getLogger(ZipCommons.class);

    public static String uniteSeparator = "/";

    /**
     * Unzip given zip file into given directory.
     * 
     * @param sourceZip
     * @param targetDir
     */
    public static void unpack(File sourceZip, File targetDir) throws IOException {
        LOG.debug(">>> Entering unpack(sourceZip,targetDir={})", sourceZip, targetDir);

        ZipFile zipFile = new ZipFile(sourceZip);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDestination = new File(targetDir, entry.getName());
            entryDestination.getParentFile().mkdirs();
            if (entry.isDirectory())
                entryDestination.mkdirs();
            else {
                InputStream in = zipFile.getInputStream(entry);
                OutputStream out = new FileOutputStream(entryDestination);
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        zipFile.close();

        LOG.debug("<<< Leaving unpack");
    }

}
