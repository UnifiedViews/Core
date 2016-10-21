package cz.cuni.mff.xrg.odcs.backend.auxiliaries;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AppLock taken from
 * http://nerdydevel.blogspot.com/2012/07/run-only-single-java-application-instance.html
 * 
 * @author rumatoest
 */
public class AppLock {

    /** The lock_file. */
    private File lock_file = null;

    /** The lock. */
    private FileLock lock = null;

    /** The lock_channel. */
    private FileChannel lock_channel = null;

    /** The lock_stream. */
    private FileOutputStream lock_stream = null;

    private static Logger LOG = LoggerFactory.getLogger(AppLock.class);

    /**
     * Instantiates a new app lock.
     */
    private AppLock() {
    }

    /**
     * Instantiates a new app lock.
     * 
     * @param key
     *            Unique application key
     * @throws Exception
     *             The exception
     */
    private AppLock(String key) throws Exception {
        String tmp_dir = System.getProperty("java.io.tmpdir");
        if (!tmp_dir.endsWith(System.getProperty("file.separator"))) {
            tmp_dir += System.getProperty("file.separator");
        }

        // Acquire MD5
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            md.reset();
            String hash_text = new java.math.BigInteger(1, md.digest(key
                    .getBytes())).toString(16);
            // Hash string has no leading zeros
            // Adding zeros to the beginnig of has string
            while (hash_text.length() < 32) {
                hash_text = "0" + hash_text;
            }
            lock_file = new File(tmp_dir + hash_text + ".app_lock");
        } catch (Exception ex) {
            System.out.println("AppLock.AppLock() file fail");
        }

        // MD5 acquire fail
        if (lock_file == null) {
            lock_file = new File(tmp_dir + key + ".app_lock");
        }

        lock_stream = new FileOutputStream(lock_file);

        String f_content = "Java AppLock Object\r\nLocked by key: " + key
                + "\r\n";
        lock_stream.write(f_content.getBytes());

        lock_channel = lock_stream.getChannel();

        lock = lock_channel.tryLock();

        if (lock == null) {
            throw new Exception("Can't create Lock");
        }
    }

    /**
     * Release Lock. Now another application instance can gain lock.
     * 
     * @throws Throwable
     */
    private void release() throws Throwable {
        if (lock.isValid()) {
            lock.release();
        }
        if (lock_stream != null) {
            lock_stream.close();
        }
        if (lock_channel.isOpen()) {
            lock_channel.close();
        }
        if (lock_file.exists()) {
            lock_file.delete();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.release();
        super.finalize();
    }

    /** The instance. */
    private static AppLock instance;

    /**
     * Set application lock. Method can be run only one time per application.
     * All next calls will be ignored.
     * 
     * @param key
     *            Unique application lock key
     * @return true, if successful
     */
    public static boolean setLock(String key) {
        if (instance != null) {
            return true;
        }

        try {
            instance = new AppLock(key);
        } catch (Exception e) {
            instance = null;
            LOG.debug("Fail to set application lock", e);
            return false;
        }

        Runtime.getRuntime().addShutdownHook(new Thread("AppLock-ShutdownHook") {
            @Override
            public void run() {
                AppLock.releaseLock();
            }
        });
        return true;
    }

    /**
     * Trying to release Lock. After release you can not user AppLock again in
     * this application.
     */
    public static void releaseLock() {
        try {
            if (instance == null) {
                throw new NoSuchFieldException("INSTATCE IS NULL");
            }
            instance.release();
        } catch (Throwable e) {
            LOG.error("Fail to release application lock", e);
        }
    }

}
