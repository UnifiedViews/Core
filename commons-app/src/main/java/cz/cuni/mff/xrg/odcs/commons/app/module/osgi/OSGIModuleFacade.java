package cz.cuni.mff.xrg.odcs.commons.app.module.osgi;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DbDPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OSGI based implementation of {@link ModuleFacade}.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
class OSGIModuleFacade implements ModuleFacade {

    /**
     * Logger class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OSGIModuleFacade.class);

    private static final String DPU_NAME = "Bundle-Name";

    public static final String DPU_NAME_KEY = "dpu.name";

    public static final String DPU_MENU_NAME_KEY = "dpu.name.menu";

    /**
     * OSGi framework class.
     */
    private Framework framework;

    /**
     * OSGi context.
     */
    private BundleContext context;

    /**
     * Store loaded bundles. DPU's bundles are stored under their directory
     * name. Libraries under their uri.
     */
    private final ConcurrentHashMap<String, BundleContainer> bundles = new ConcurrentHashMap<>();

    /**
     * Store bundles that have been loaded as libraries.
     */
    private final HashMap<String, BundleContainer> libs = new HashMap<>();

    @Autowired
    private OSGIModuleFacadeConfig configuration;

    /**
     * DPU DAO to get all DPU's templates.
     */
    @Autowired
    private DbDPUTemplateRecord dpuTemplateDao;

    @Autowired
    private AppConfig appConfig;

    /**
     * Store directories for bundle which are currently being updated.
     * Also for each directory store Thread, so Thread which has lock can
     * access all methods.
     * The structure must be accessed only inside synchronized block.
     */
    private final Map<String, Thread> updatingBundles = new HashMap<>();

    /**
     * Start the OSGI framework.
     */
    public void start() throws FrameworkStartFailedException {
        FrameworkFactory frameworkFactory = null; // org.eclipse.osgi.launch.EquinoxFactory

        try {
            frameworkFactory = java.util.ServiceLoader
                    .load(FrameworkFactory.class).iterator().next();
        } catch (Exception ex) {
            LOG.error("Failed to load osgi framework class.", ex);
            // failed to load FrameworkFactory class
            throw new FrameworkStartFailedException(
                    Messages.getString("OSGIModuleFacade.frameworkFactory.load.problem"), ex);
        }

        framework = frameworkFactory.newFramework(prepareSettings());
        context = null;
        try {
            // start OSGi container ..
            framework.start();
        } catch (org.osgi.framework.BundleException ex) {
            LOG.error("Failed to start OSGI framework.", ex);
            // failed to start/initiate framework
            throw new FrameworkStartFailedException(
                    Messages.getString("OSGIModuleFacade.osgi.load.problem"), ex);
        }

        try {
            context = framework.getBundleContext();
        } catch (SecurityException ex) {
            LOG.error("Failed to get osgi context.", ex);
            // we have to stop framework ..
            stop();
            throw new FrameworkStartFailedException(
                    Messages.getString("OSGIModuleFacade.context.get.fail"), ex);
        }

        // load resources
        startUpLoad();
    }

    /**
     * Stop OSGI and uninstall all bundles.
     */
    public void stop() {
        for (BundleContainer bundle : bundles.values()) {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                LOG.error("Failed to uninstall bundle {}", bundle.getUri(), e);
            }
        }
        bundles.clear();
        try {
            if (framework != null) {
                // stop equinox
                framework.stop();
            }
        } catch (Throwable e) {
            // we can't throw here ..
        } finally {
            framework = null;
            context = null;
        }
    }

    @Override
    public Object getInstance(DPUTemplateRecord dpu) throws ModuleException {
        // get installed bundle
        BundleContainer container = install(dpu);
        String fullMainClassName = container.getMainClassName();
        // load and return
        return container.loadClass(fullMainClassName);
    }

    @Override
    public eu.unifiedviews.helpers.dpu.localization.Messages getMessageFromDPUInstance(Object DPUInstance) {
        Class<?> objectClass = DPUInstance.getClass();
        ClassLoader loader = objectClass.getClassLoader();
        eu.unifiedviews.helpers.dpu.localization.Messages messages = new eu.unifiedviews.helpers.dpu.localization.Messages(LocaleHolder.getLocale(), loader);
        return messages;
    }

    @Override
    public void unLoad(DPUTemplateRecord dpu) {
        final String directory = dpu.getJarDirectory();
        unLoad(directory);
    }

    @Override
    public void unLoad(String directory) {
        // we need DPUs lock as 'uninstall' do not care about locking 
        lockUpdate(directory);
        // uninstall
        uninstall(directory);

        releaseUpdate(directory);
    }

    @Override
    public void beginUpdate(DPUTemplateRecord dpu) {
        final String directory = dpu.getJarDirectory();
        // first we need lock on this directory
        lockUpdate(directory);
        // now we are the only one running .. we can work

        BundleContainer container = bundles.get(directory);
        if (container == null) {
            // bundle is not loaded .. create one, so we can lock it
            container = new BundleContainer(context, directory);
            bundles.put(directory, container);
        }

        // we secure that there exist record for DPU we wan't to load
    }

    @Override
    public Object update(String directory, String newName)
            throws ModuleException {
        BundleContainer container = bundles.get(directory);
        if (container == null) {
            LOG.error("Missing bundle durign update.");
            // bundle is not loaded yet .. this should not happen
            throw new ModuleException(Messages.getString("OSGIModuleFacade.no.record"));
        }

        try {
            container.update(context, createUri(directory, newName));
        } catch (BundleException e) {
            throw new ModuleException(Messages.getString("OSGIModuleFacade.update.fail"), e);
        }

        // try to load main class
        final String mainClassName = container.getMainClassName();
        try {
            return container.loadClass(mainClassName);
        } catch (ModuleException e) {
            // unload DPU
            try {
                container.uninstall();
            } catch (BundleException uninstallEx) {
                LOG.error("Failed to uninstall bundle in: {}", directory,
                        uninstallEx);
            }
            // re-throw
            throw e;
        }
    }

    /**
     * This method should be called between {@link #beginUpdate(DPUTemplateRecord)} and {@link #endUpdate(DPUTemplateRecord, boolean)}. It will
     * just unload old DPU (if loaded) and load new one. <b>There is no check.</b>
     * Should be used only if we can take that risk that new DPU's jar
     * file is working.
     * In case of error throw.
     * 
     * @param directory
     * @param newName
     * @throws ModuleException
     */
    public void nonCheckUpdate(String directory, String fileName) throws ModuleException {
        // uninstall
        uninstall(directory);
        // install new directory
        install(directory, fileName);
    }

    @Override
    public void endUpdate(DPUTemplateRecord dpu, boolean updateFailed) {
        final String directory = dpu.getJarDirectory();
        BundleContainer container = bundles.get(directory);
        if (container == null) {
            // bundle does not exist			
        } else {
            // bundle exist
            if (updateFailed) {
                // we have to remove and uninstall the bundle
                try {
                    container.uninstall();
                } catch (BundleException e) {
                    LOG.error("Can't unistall bundle after update failure.", e);
                }
                // in every case remove from bundles
                bundles.remove(directory);
            }
        }

        // release lock for given directory
        releaseUpdate(directory);
    }

    @Override
    public void delete(DPUTemplateRecord dpu) {
        final String directory = dpu.getJarDirectory();
        lockUpdate(directory);
        // delete directory
        final File directoryFile = new File(getDPUDirectory(), directory);
        LOG.debug("Deleting {}", directory.toString());
        try {
            FileUtils.deleteDirectory(directoryFile);
        } catch (IOException e) {
            LOG.error("Failed to delete directory.", e);
        }
        // uninstall
        uninstall(directory);

        releaseUpdate(directory);
    }

    @Override
    public Dictionary<String, String> getManifestHeaders(DPUTemplateRecord dpu) {
        try {
            BundleContainer container = install(dpu);
            return container.getHeaders();
        } catch (ModuleException e) {
            LOG.error("Failed to install bundle: {}", dpu.getJarDirectory(), e);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public void preLoadAllDPUs() {
        preLoadDPUs(dpuTemplateDao.getAll());
    }

    @Override
    public void preLoadDPUs(List<DPUTemplateRecord> dpus) {
        boolean useLocalizedDPUnames = false;
        try {
            useLocalizedDPUnames = appConfig.getBoolean(ConfigProperty.USE_LOCALIZED_DPU_NAME);
        } catch (MissingConfigPropertyException e) {
            // property is missing, default value: false remains
        }

        for (DPUTemplateRecord dpu : dpus) {
            try {
                BundleContainer container = install(dpu);
                if (dpu.getParent() == null) { // we only rewrite first level templates
                    Dictionary<String, String> headers = container.getHeaders();
                    String dpuManifestName = headers.get(DPU_NAME);
                    dpuManifestName = StringUtils.abbreviate(dpuManifestName, LenghtLimits.DPU_NAME); // limit length
                    if (useLocalizedDPUnames) {
                        Object dpuInstance = container.loadClass(container.getMainClassName());
                        eu.unifiedviews.helpers.dpu.localization.Messages messages = getMessageFromDPUInstance(dpuInstance);
                        // try to get dpu name from messages bundle
                        if (messages.getString(DPU_NAME_KEY).equals(DPU_NAME_KEY)) {
                            // in this case getString() returns the key, because there is no value for the given key
                            LOG.warn("Missing key {} in the resource bundle", DPU_NAME_KEY);
                            LOG.warn("Localized name for dpu {} was not set", dpu.getJarPath());
                            // fallback to manifest name
                            dpu.setName(dpuManifestName);
                        } else {
                            dpu.setName(messages.getString(DPU_NAME_KEY));
                        }
                        // try to get dpu menu name from messages bundle
                        if (messages.getString(DPU_MENU_NAME_KEY).equals(DPU_MENU_NAME_KEY)) {
                            // in this case getString() returns the key, because there is no value for the given key
                            LOG.warn("Missing key {} in the resource bundle", DPU_MENU_NAME_KEY);
                            LOG.warn("Localized menu name for dpu {} was not set", dpu.getJarPath());
                            // fallback to manifest name
                            dpu.setMenuName(dpuManifestName);
                        } else {
                            dpu.setMenuName(messages.getString(DPU_MENU_NAME_KEY));
                        }
                    } else { // localized names feature turned-off or missing
                        dpu.setName(dpuManifestName);
                        dpu.setMenuName(dpuManifestName);
                    }
                    // save changes to DB
                    dpuTemplateDao.save(dpu);
                }
            } catch (ModuleException e) {
                LOG.warn("Failed to pre-load dpu: {}", dpu.getJarPath(), e);
            }
        }
    }

    @Override
    public void loadLibs(List<String> directoryPaths) {
        for (String directory : directoryPaths) {
            loadLibs(directory);
        }

        List<String> loadFailedList = new LinkedList<>();
        // all libraries have been loaded now try to start them
        // so we figer out if they are resolved as well
        for (String key : libs.keySet()) {
            try {
                libs.get(key).start();
            } catch (ModuleException ex) {
                LOG.error("Failed to resolve the library: {}", key, ex);
                loadFailedList.add(key);
            }
        }

        for (String libUri : loadFailedList) {
            LOG.error("Failed to resolve the library: {}", libUri);
        }
    }

    @Override
    public String getDPUDirectory() {
        return configuration.getDPUDirectory();
    }

    /**
     * Uninstall the DPU from given directory. <b>This function does not lock
     * the DPU, so the caller must have the lock for the DPUs.</b> After
     * end of this function the {@link #bundles} does not contains record
     * for given DPU, and the bundle is uninstalled.
     * 
     * @param directory
     */
    private void uninstall(String directory) {
        BundleContainer container = bundles.get(directory);
        if (container == null) {
            // nothing to uninstall
            return;
        }
        try {
            container.uninstall();
        } catch (BundleException e) {
            LOG.error("Failed to uninstall bundle in: {}", directory, e);
        }
        bundles.remove(directory);
    }

    /**
     * Load jar files from given directory as libraries.
     * 
     * @param directoryPath
     */
    private void loadLibs(String directoryPath) {
        LOG.info("Loading libraries from: {}", directoryPath);

        File directory = new File(directoryPath);
        File[] fList = directory.listFiles();
        if (fList == null) {
            LOG.error("Wrong directory path: {}", directoryPath);
            return;
        }
        for (File file : fList) {
            if (file.isFile()) {
                if (file.getName().contains("jar")) {
                    LOG.debug("Loading library: {}", file.toString());
                    // now we need uri
                    String uri = "file:///"
                            + file.getAbsolutePath().replace('\\', '/');
                    installLib(uri);
                }
            }
        }
    }

    /**
     * Return configuration used to start up OSGi implementation.
     * 
     * @return
     */
    private java.util.Map<String, String> prepareSettings() {
        java.util.Map<String, String> config = new java.util.HashMap<>();
        config.put("osgi.console", "");
        config.put("osgi.clean", "true");
        config.put("osgi.noShutdown", "true");
        // export packages
        config.put(org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                configuration.getPackagesToExpose());
        return config;
    }

    /**
     * If container is not installed yet then install it. In every case return
     * the installed instance of {@link BundleContainer} for given directory.
     * The container is installed as DPU container.
     * 
     * @param dpu
     * @return
     * @throw ModuleException
     */
    private BundleContainer install(DPUTemplateRecord dpu)
            throws ModuleException {
        return install(dpu.getJarDirectory(), dpu.getJarName());
    }

    /**
     * Create uri to given file in given directory in DPU's directory.
     * 
     * @param directory
     * @param fileName
     * @return
     */
    private String createUri(String directory, String fileName) {
        StringBuilder uri = new StringBuilder();
        uri.append("file:///");
        uri.append(configuration.getDPUDirectory());
        uri.append(File.separator);
        uri.append(directory);
        uri.append(File.separator);
        uri.append(fileName);
        return uri.toString();
    }

    /**
     * If container is not installed yet then install it. In every case return
     * the installed instance of {@link BundleContainer} for given directory.
     * The container is installed as DPU container.
     * 
     * @param directory
     *            DPU's directory.
     * @param fileName
     * @return
     * @throw ModuleException
     */
    private BundleContainer install(String directory, String fileName)
            throws ModuleException {
        LOG.debug("Installing bundle from '{}'/'{}'", directory, fileName);

        if (directory == null || fileName == null) {
            LOG.error("Can't install bundle bundle from '{}'/'{}'", directory, fileName);
            throw new ModuleException(Messages.getString("OSGIModuleFacade.missing.path"));
        }

        // prepare uri
        final String uri = createUri(directory, fileName);
        // we lock the directory for updates
        lockUpdate(directory);

        // prepare bundle
        BundleContainer bundleContainer = bundles.get(directory);

        if (bundleContainer != null) {
            if (bundleContainer.isInstalled()) {
                if (bundleContainer.getUri().equals(uri)) {
                    releaseUpdate(directory);
                    return bundleContainer;
                } else {
                    // new uri -> reload
                }
            } else {
                // not loaded yet -> reload
            }
        } else {
            // create instance
            bundleContainer = new BundleContainer(context, directory);
            bundles.put(directory, bundleContainer);
        }
        // try to load bundle from given uri			
        try {
            bundleContainer.install(uri);
        } catch (org.osgi.framework.BundleException e) {
            releaseUpdate(directory);
            throw new ModuleException(e);
        }
        releaseUpdate(directory);
        return bundleContainer;
    }

    /**
     * Try to load library. In case of error log but do not throw. If succeed
     * the newly loaded library is stored in {@link #bundles} and
     * returned.
     * 
     * @param uri
     *            Bundles uri.
     * @return Null in case of error.
     */
    private BundleContainer installLib(String uri) {
        synchronized (libs) {
            if (libs.containsKey(uri)) {
                return libs.get(uri);
            }
        }

        Bundle bundle;
        try {
            bundle = context.installBundle(uri);
        } catch (org.osgi.framework.BundleException e) {
            LOG.error("Failed to load libary from: {}", uri, e);
            return null;
        }

        BundleContainer bundleContainer;
        bundleContainer = new BundleContainer(context, uri.toString(), bundle);
        synchronized (libs) {
            libs.put(uri, bundleContainer);
        }
        return bundleContainer;
    }

    /**
     * Lock given bundle for update. After this only {@link #update(String, String)} and {@link #endUpdate(DPUTemplateRecord, boolean)} can be called.
     * 
     * @param directory
     */
    private void lockUpdate(String directory) {
        // wait till the required directory is not in directory 
        while (true) {
            synchronized (updatingBundles) {
                if (updatingBundles.containsKey(directory)) {
                    // already contains .. check thread
                    if (updatingBundles.get(directory) == Thread.currentThread()) {
                        // we are owners of the lock, we shall pass
                        return;
                    }
                } else {
                    // not present, add -> lock 
                    updatingBundles.put(directory, Thread.currentThread());
                    return;
                }
                // wait .. this will release lock from synchronised block
                try {
                    updatingBundles.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Release lock for given directory.
     * 
     * @param directory
     */
    private void releaseUpdate(String directory) {
        synchronized (updatingBundles) {
            // check that we are the owner
            if (updatingBundles.containsKey(directory)) {
                if (updatingBundles.get(directory) == Thread.currentThread()) {
                    // we have the rights to do this					
                } else {
                    LOG.error("Unlocking DPU from no-owner thread!");
                }
                // in every case .. unlock as we rely on programmers
                // that they use the lock properly
                updatingBundles.remove(directory);
            } else {
                // no record at all
            }// in every case notify everyone
            updatingBundles.notifyAll();
        }
    }

    /**
     * Load libraries and DPU's jar files.
     */
    private void startUpLoad() {
        // first load libraries
        loadLibs(configuration.getDpuLibFolder());
    }

}
