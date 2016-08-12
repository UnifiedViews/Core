package cz.cuni.mff.xrg.odcs.commons.app.module.impl;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleDeleteEvent;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleNewEvent;
import cz.cuni.mff.xrg.odcs.commons.app.module.event.ModuleUpdateEvent;

/**
 * Should be used with cooperation with {@link FileNotifierClient}. Use
 * separated thread in order to watch directory for notification files.
 * Notifications:
 * <ul>
 * <li><b>DPU update</b> create file in given DPU's directory in format {new_dpu_name}.UPDATE_EXT</li>
 * <li><b>new DPU</b> create file in DPU's root directory in format {new_dpu_directory_name}.NEW_EXT</li>
 * <li><b>delete DPU</> create file in DPU's root directory in format {dpu_directory_name}.DELETE_EXT</li>
 * </ul>
 * 
 * @author Petyr
 */
class FileNotifierServer implements Runnable {

    public static final String NEW_EXT = ".new";

    public static final String UPDATE_EXT = ".update";

    public static final String DELETE_EXT = ".delete";

    private static final Logger LOG = LoggerFactory
            .getLogger(FileNotifierClient.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Used to get information about modules.
     */
    @Autowired
    private ModuleFacade moduleFacade;

    private WatchService watcher = null;

    private final Map<WatchKey, Path> keys = new HashMap<>();

    /**
     * Inverse mapping for {@link #keys}.
     */
    private final Map<Path, WatchKey> keysInverse = new HashMap<>();

    private Thread watcherThread = null;

    private boolean interrupted = false;

    /**
     * Contains names of directories in which first notification will be
     * ignored.
     */
    private final Set<String> toIgnore = new HashSet<>();

    /**
     * Start watching for DPUs changes.
     */
    public void start() {
        if (watcherThread == null) {
            // continue
        } else {
            // already started
            LOG.warn("Duplicit start. Second start ignored.");
            return;
        }

        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            LOG.error("Failed to start DPU change watcher.", e);
            return;
        }
        new File(moduleFacade.getDPUDirectory()).mkdirs();
        // register directories to watch
        register(Paths.get(moduleFacade.getDPUDirectory()));

        // start watching .. the function run asynchronously
        watcherThread = new Thread(this, "File notifier server");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    /**
     * Terminates watcher.
     */
    public void stop() {
        LOG.trace("Stopping FileNotifierServer ...");

        watcherThread.interrupt();

        try {
            watcherThread.join();
            // ..
            LOG.trace("FileNotifierServer has been stopped.");
        } catch (InterruptedException e) {
            LOG.trace("Interrupted when fait for FileNotifierServer to stop.");
        }
        // give up the thread ..
        watcherThread = null;
    }

    /**
     * Ignore first change in given directory.
     * 
     * @param directory
     */
    public void addToIgnore(String directory) {
        toIgnore.add(directory);
    }

    /**
     * Register given directory and all sub-directories for watching.
     * 
     * @param dir
     * @throws IOException
     */
    private void register(Path dir) {
        WatchKey key;
        try {
            key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        } catch (IOException e) {
            LOG.error("Failed to register watcher for directory: {}",
                    dir.toString(), e);
            return;
        }
        LOG.debug("File watcher registered for: {}", dir.toString());
        keys.put(key, dir);
        keysInverse.put(dir, key);
        // now sub directories
        File[] files = (new File(dir.toString())).listFiles();
        if (files == null) {
            // empty directory
        } else {
            // call for sub-directories
            for (File item : files) {
                if (item.isDirectory()) {
                    register(Paths.get(item.toString()));
                }
            }
        }
    }

    /**
     * Return true if given notification should be ignored.
     * 
     * @param directoryName
     * @return
     */
    private boolean ignore(String directoryName) {
        if (toIgnore.contains(directoryName)) {
            // we have orders to ignore this
            toIgnore.remove(directoryName);
            LOG.debug("Ignoring notification: {}", directoryName);
            return true;
        }
        return false;
    }

    /**
     * React on {@link #NEW_EXT} event. Register new listener for given
     * directory and publish {@link ModuleNewEvent}.
     * 
     * @param dir
     * @param eventPath
     */
    private void onExtNew(Path dir, Path eventPath) {
        String directory = FilenameUtils.removeExtension(eventPath.getFileName().toString());
        // new directory, register
        if (eventPath.toFile().isDirectory()) {
            register(dir.toAbsolutePath());
        }

        if (ignore(directory)) {
            return;
        }
        eventPublisher.publishEvent(new ModuleNewEvent(this, directory));
    }

    /**
     * Publish {@link ModuleUpdateEvent} for given directory and jar file. {@link ModuleNewEvent}.
     * 
     * @param dir
     * @param eventPath
     */
    private void onExtUpdate(Path dir, Path eventPath) {
        String directory = dir.getFileName().toString();
        String fileName = FilenameUtils.removeExtension(eventPath.getFileName().toString());

        if (ignore(directory)) {
            return;
        }
        eventPublisher.publishEvent(new ModuleUpdateEvent(this, directory, fileName));
    }

    /**
     * React on {@link #DEL_EXT} event. Unregister listener for given directory
     * and publish {@link ModuleDeleteEvent}.
     * 
     * @param dir
     * @param eventPath
     */
    private void onExtDelete(Path dir, Path eventPath) {
        String directory = FilenameUtils.removeExtension(eventPath
                .getFileName().toString());
        // unregister listener
        Path directoryPath = Paths.get(dir.toString(), directory);
        if (keysInverse.containsKey(directoryPath)) {
            // unregister
            keysInverse.get(directoryPath).cancel();
            // remove from collections
            keys.remove(keysInverse.get(directoryPath));
            keysInverse.remove(directoryPath);
        }

        if (ignore(directory)) {
            return;
        }
        eventPublisher.publishEvent(new ModuleDeleteEvent(this, directory));
    }

    /**
     * React on file notification.
     * 
     * @param dir
     * @param eventPath
     */
    private void onNotification(Path dir, Path eventPath) {
        if (eventPath.getFileName().toString().endsWith(NEW_EXT)) {
            onExtNew(dir, eventPath);
        } else if (eventPath.getFileName().toString().endsWith(UPDATE_EXT)) {
            onExtUpdate(dir, eventPath);
        } else if (eventPath.getFileName().toString().endsWith(DELETE_EXT)) {
            onExtDelete(dir, eventPath);
        } else {
            // unknown notification -> but the new one may be the directory
            if (Files.isDirectory(eventPath, NOFOLLOW_LINKS)) {
                // register listener for this .. 
                register(eventPath);
            }
            // return, we do not wan't to delete this file 
            // as it is not our notification file
            return;
        }

        // in every case delete the notification file at the end

        // delete the notification file - wait for some time
        // so it is not locked by OS
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            interrupted = true;
            LOG.info("DPU's change watcher has been interrupted.");
        }

        try {
            Files.delete(eventPath);
            // we use Exception as it can throw IOException as
            // well
        } catch (IOException e) {
            LOG.debug("Failed to delete notification file '{}', " +
                    "but it is ok as it was probably delete by other " +
                    "notification server.", eventPath.toString());
        }
    }

    /**
     * The main function here we watch for changes.
     */
    @Override
    public void run() {
        LOG.info("DPU's change watcher is running ... ");
        // wait for event
        while (!interrupted) {
            if (Thread.interrupted()) {
                // if yes finish				
                break;
            }

            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                break;
            }
            // get the path for the event
            Path dir = keys.get(key);
            if (dir == null) {
                LOG.warn("WatchKey not recognized!");
                continue;
            }
            // iterate over events
            for (WatchEvent<?> event : key.pollEvents()) {
                final WatchEvent.Kind kind = event.kind();
                // for lost or discarded events
                if (kind == OVERFLOW) {
                    continue;
                }
                // otherwise recast
                @SuppressWarnings("unchecked")
                final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                // here we get path relative to the registered
                // in our case DPU's relative path
                final Path eventPath = dir.resolve(pathEvent.context());
                if (kind == ENTRY_CREATE) {
                    onNotification(dir, eventPath);
                } else if (kind == ENTRY_DELETE) {

                } else if (kind == ENTRY_MODIFY) {

                }
            }
            boolean valid = key.reset();
            if (valid) {
                // everything is all right
            } else {
                // it is broken ... the directory may be deleted
                keys.remove(key);
                keysInverse.remove(dir);
                LOG.debug("Key for: '{}' is no longer valid.", dir.toString());
            }
        }
        // unregister watcher
        try {
            if (watcher != null) {
                watcher.close();
            }
            watcher = null;
        } catch (IOException e) {
        }

        LOG.trace("DPU's change watcher has been interrupted.");
    }

}
