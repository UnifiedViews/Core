package cz.cuni.mff.xrg.odcs.commons.app.module.osgi;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.Messages;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;

/**
 * Represent a single bundle. Enable loading class from bundle. Also
 * contains cache for loaded classes.
 * The used {@link OSGIModuleFacade} muse secure that bundle is installed
 * before further use.
 * 
 * @author Petyr
 */
class BundleContainer {

    private static final Logger LOG = LoggerFactory.getLogger(BundleContainer.class);

    /**
     * Bundle context for loading bundles.
     */
    private final BundleContext context;

    /**
     * The OSGI bundle it self. Can be null if bundle is not loaded.
     */
    private Bundle bundle;

    /**
     * Uri from where the bundle has been load.
     */
    private String uri;

    /**
     * If {@link #isLib} == true, then contains DPU's directory.
     */
    private String dpuDirectory;

    /**
     * List of loaded class<?> from this bundle.
     */
    private final Map<String, Class<?>> loadedClassCtors = new HashMap<>();

    /**
     * Used to lock instance during non concurrent operations.
     */
    private final Object lock = new Object();

    /**
     * True if container encapsulate library.
     */
    private final boolean isLib;

    /**
     * Used only if {@link #isLib} == true. Cache main class name for DPUs.
     */
    private String mainClassNameCache = null;

    /**
     * Create container for unloaded DPU's bundle.
     * 
     * @param context
     * @param dpuDirectory
     */
    public BundleContainer(BundleContext context, String dpuDirectory) {
        this.context = context;
        this.bundle = null;
        this.uri = "";
        this.dpuDirectory = dpuDirectory;
        this.isLib = false;
    }

    /**
     * Create container for library.
     * 
     * @param context
     * @param uri
     * @param bundle
     */
    public BundleContainer(BundleContext context, String uri, Bundle bundle) {
        this.context = context;
        this.bundle = bundle;
        this.uri = uri;
        this.isLib = true;
    }

    /**
     * Try to start bundle, throw if import-packages are not satisfied.
     * 
     * @throws ModuleException
     */
    public void start() throws ModuleException {
        try {
            if (bundle.getState() != Bundle.ACTIVE) {
                // just installed

                // try to start bundle ... give us 
                // exception with missing bundles
                bundle.start();
            }
        } catch (BundleException e) {
            throw new ModuleException(e);
        }
    }

    /**
     * Load class with given name from the bundle. Automatically install
     * bundle if needed.
     * 
     * @param className
     *            Class name prefixed with packages.
     * @return Loaded class.
     * @throws ModuleException
     */
    public Object loadClass(String className)
            throws ModuleException {
        try {
            if (bundle.getState() == Bundle.INSTALLED) {
                // just installed

                // try to start bundle ... give us 
                // exception with missing bundles
                bundle.start();
            }
        } catch (BundleException e) {
            throw new ModuleException(e);
        }

        Class<?> loaderClass = null;
        synchronized (lock) {
            if (loadedClassCtors.containsKey(className)) {
                // class already been loaded 
                loaderClass = this.loadedClassCtors.get(className);
            } else {
                // try to load class -> throw: ClassNotFoundException
                try {
                    loaderClass = bundle.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new ModuleException(e);
                }
                // store loaded class
                this.loadedClassCtors.put(className, loaderClass);
            }
        }
        // we have loader, create instance ..		
        Object result = null;
        try {
            result = loaderClass.newInstance();
        } catch (InstantiationException | IllegalAccessException | NoClassDefFoundError | NoSuchMethodError e) {
            throw new ModuleException(e);
        }
        // return instance
        return result;
    }

    /**
     * If bundle is not installed then install it from given
     * given uri.
     * 
     * @throws BundleException
     */
    public void install(String uri) throws BundleException {
        if (bundle == null) {
            // install			
            bundle = context.installBundle(uri);
            // set uri
            this.uri = uri;
        }
    }

    /**
     * Uninstall bundle.
     * 
     * @throws BundleException
     */
    public void uninstall() throws BundleException {
        synchronized (lock) {
            // clear list
            loadedClassCtors.clear();
            //
            if (bundle != null) {
                try {
                    bundle.stop();
                } catch (BundleException e) {
                    LOG.error("Failed to stop bundle. But it will be uninstalled anyway.", e);
                }
                bundle.uninstall();
            }
            bundle = null;
            mainClassNameCache = null;
        }
    }

    /**
     * Update current bundle. Drop cache of loaded classes. The possible
     * exception during bundle uninstallation is silently ignored.
     * For loading the {@link BundleException} can be thrown, in such case
     * the {@link #bundle} is set to null.
     * The {@link uri} is updated only in case of successful update.
     * The {@link #beginUpdate()} method must be called before this method.
     * 
     * @throws BundleException
     */
    public synchronized void update(BundleContext context, String uri) throws BundleException {
        // clear list
        loadedClassCtors.clear();
        // uninstall old bundle
        try {
            if (bundle != null) {
                bundle.uninstall();
            }
        } catch (BundleException e) {
            LOG.error("Failed to unistall bundle", e);
        } finally {
            bundle = null;
            mainClassNameCache = null;
        }
        // load new bundle
        bundle = context.installBundle(uri);
        // update uri
        this.uri = uri;
    }

    /**
     * Return content of manifest.mf file.
     * 
     * @return
     * @throws ModuleException
     */
    public Dictionary<String, String> getHeaders() throws ModuleException {
        return bundle.getHeaders();
    }

    public String getDirectory() {
        return dpuDirectory;
    }

    public boolean isLibrary() {
        return isLib;
    }

    public String getUri() {
        return uri;
    }

    public boolean isInstalled() {
        return bundle == null;
    }

    /**
     * Return name of main class. The value is cached.
     * 
     * @return
     */
    public String getMainClassName() throws ModuleException {
        if (mainClassNameCache == null) {
            final Dictionary<String, String> headers = getHeaders();
            final String packageName = (String) headers.get("DPU-Package");
            if (packageName == null) {
                LOG.error("'DPU-Package' undefined for '{}'", uri);
                throw new ModuleException(
                        Messages.getString("BundleContainer.dpu-package.not.found"));
            }
            final String className = (String) headers.get("DPU-MainClass");
            if (className == null) {
                LOG.error("'DPU-MainClass' undefined for '{}'", uri);
                throw new ModuleException(
                        Messages.getString("BundleContainer.dpu-mainClass.not.found"));
            }
            mainClassNameCache = packageName + "." + className;
        }

        return mainClassNameCache;
    }

}
