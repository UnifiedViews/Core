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
package cz.cuni.mff.xrg.odcs.commons.app.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import eu.unifiedviews.commons.i18n.DataunitLocaleHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;

import eu.unifiedviews.commons.util.Cryptography;

/**
 * Class with global application configuration.
 * 
 * @author Petyr
 * @author Jan Vojt
 */
public class AppConfig extends PropertyPlaceholderConfigurer {

    public static final List<ConfigProperty> ENCRYPTED_PROPERTIES = Arrays.asList(
            ConfigProperty.DATABASE_SQL_PASSWORD,
            ConfigProperty.DATABASE_RDF_PASSWORD,
            ConfigProperty.EMAIL_PASSWORD,
            ConfigProperty.DPU_UV_T_FILES_METADATA_POOL_PARTY_PASSWORD,
            ConfigProperty.DPU_UV_L_RELATIONAL_TO_CKAN_SECRET_TOKEN,
            ConfigProperty.DPU_UV_L_RELATIONAL_DIFF_TO_CKAN_SECRET_TOKEN,
            ConfigProperty.DPU_UV_L_RDF_TO_CKAN_SECRET_TOKEN,
            ConfigProperty.DPU_UV_L_FILES_TO_CKAN_SECRET_TOKEN,
            ConfigProperty.DPU_UV_L_RDF_TO_VIRTUOSO_PASSWORD);

    /**
     * Modifiable configuration itself.
     */
    private final Properties prop = new Properties();

    /**
     * Logging gateway.
     */
    private static final Logger LOG = Logger.getLogger(AppConfig.class.getName());

    /**
     * Determines whether cryptography is enabled or not.
     */
    private static Boolean cryptographyEnabled = Boolean.FALSE;

    /**
     * Cryptography instance;
     */
    private static Cryptography cryptography;

    /**
     * Use factory methods for constructing configurations.
     */
    private AppConfig() {
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory,
            Properties props) throws BeansException {
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            prop.put(keyStr, props.getProperty(keyStr));
        }

        postprocess();

        super.processProperties(beanFactory, prop);
    }

    /**
     * Constructor building from Spring resource.
     * 
     * @param resource
     *            configuration
     * @return application configuration
     */
    public static AppConfig loadFrom(Resource resource) {
        // we do not use slf4j as it is not initilized yet
        LOG.log(Level.INFO, "Loading configuration from classpath resource.");
        try {
            return loadFrom(resource.getInputStream());
        } catch (IOException ex) {
            throw new ConfigFileNotFoundException(ex);
        }
    }

    /**
     * Loads configuration from input stream.
     * 
     * @param stream
     * @return application configuration
     */
    public static AppConfig loadFrom(InputStream stream) {
        AppConfig config = new AppConfig();
        try {
            config.prop.load(stream);
        } catch (IOException ex) {
            throw new ConfigFileNotFoundException(ex);
        } catch (IllegalArgumentException ex) {
            throw new MalformedConfigFileException(ex);
        }

        config.postprocess();

        return config;
    }

    /**
     * Gets value of given configuration property.
     * 
     * @param key
     * @return configuration value for given property
     */
    public String getString(ConfigProperty key) {
        String value = prop.getProperty(key.toString());
        if (value == null) {
            throw new MissingConfigPropertyException(key);
        }
        return value;
    }

    /**
     * Gets integer value of given configuration property.
     * 
     * @param key
     * @return integer value of given configuration property.
     */
    public int getInteger(ConfigProperty key) {
        String value = getString(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new InvalidConfigPropertyException(key, value);
        }
    }

    /**
     * Gets boolean value of given configuration property.
     * 
     * @param key
     * @return boolean value of given configuration property.
     */
    public boolean getBoolean(ConfigProperty key) {
        return Boolean.parseBoolean(getString(key));
    }

    /**
     * Creates a new configuration containing only a subset of this
     * configuration properties matching given namespace. The keys of newly
     * created configuration are trimmed off namespace prefix.
     * 
     * @param namespace
     * @return new configuration
     */
    public AppConfig getSubConfiguration(ConfigProperty namespace) {

        AppConfig subConfig = new AppConfig();
        String strNamespace = namespace.toString().concat(".");

        for (Map.Entry<Object, Object> e : prop.entrySet()) {
            if (e.getKey().toString().startsWith(strNamespace)) {
                String newNamespace = e.getKey().toString()
                        .substring(strNamespace.length());
                subConfig.prop.setProperty(newNamespace, e.getValue().toString());
            }
        }

        return subConfig;
    }

    /**
     * @return defensive copy of wrapped properties.
     */
    public Properties getProperties() {
        return (Properties) prop.clone();
    }

    /**
     * Test if given key is present in application properties.
     * @param key possible key
     * @return true if and only if the specified property is present in application properties.
     */
    public boolean contains(ConfigProperty key) {
        return this.prop.containsKey(key.toString());
    }

    private void postprocess() {
        // get cryptography configuration
        try {
            cryptographyEnabled = getBoolean(ConfigProperty.CRYPTOGRAPHY_ENABLED);
        } catch (MissingConfigPropertyException e) {
            // default value is false, we are safe
        }

        if (cryptographyEnabled) {
            if (cryptography == null) {
                try {
                    cryptography = new Cryptography(prop.getProperty(ConfigProperty.CRYPTOGRAPHY_KEY_FILE.toString()));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            for (ConfigProperty configProperty : ENCRYPTED_PROPERTIES) {
                if (prop.containsKey(configProperty.toString())) {
                    prop.put(configProperty.toString(), cryptography.decrypt(prop.getProperty(configProperty.toString())));
                }
            }
        }

        // initialize localization: retrieve property and set it in LocaleHolders
        Locale locale = Locale.forLanguageTag(prop.getProperty(ConfigProperty.LOCALE.toString(), "en_US"));
        LocaleHolder.setLocale(locale);
        DataunitLocaleHolder.setLocale(locale);
        LOG.log(Level.INFO, "Using locale: " + LocaleHolder.getLocale());
    }

    /**
     * @param input
     *            byte array for preprocessing.
     * @return Encrypted byte array if cryptography is enabled, input byte array otherwise.
     */
    public static byte[] preprocess(byte[] input) {
        byte[] result = input;

        if (cryptographyEnabled) {
            result = cryptography.encrypt(input);
        }

        return result;
    }

    /**
     * @param input
     *            byte array for postprocessing.
     * @return Decrypted byte array if cryptography is enabled, input byte array otherwise.
     */
    public static byte[] postprocess(byte[] input) {
        byte[] result = input;

        if (cryptographyEnabled) {
            result = cryptography.decrypt(input);
        }

        return result;
    }

}
