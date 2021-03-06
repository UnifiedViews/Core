package cz.cuni.mff.xrg.odcs.frontend.gui.views;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.frontend.gui.ViewComponent;
import cz.cuni.mff.xrg.odcs.frontend.navigation.Address;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Initial view with short description of the tool.
 * 
 * @author Bogo
 */
@Component
@Scope("prototype")
@Address(url = "")
public class Initial extends ViewComponent {

    private static final Logger LOG = LoggerFactory.getLogger(Initial.class);

    private static final String INITIAL_LOGO_RESOURCE = "img/unifiedviews_logo.svg";

    private static final String INITIAL_TEXT_RESOURCE_PREFIX = "initial_";

    private static final String INITIAL_TEXT_RESOURCE_POSTFIX = ".html";

    private static final String INITIAL_DEFAULT_TEXT_RESOURCE = "initial.html";

    private AbsoluteLayout mainLayout;

    private Embedded logo;

    private Label pageText;

    @Autowired
    private AppConfig appConfig;

    /**
     * Constructor.
     */
    public Initial() {
    }

    @Override
    public boolean isModified() {
        //There are no editable fields.
        return false;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }

    private AbsoluteLayout buildMainLayout() {
        setSizeFull();
        // common part: create layout
        mainLayout = new AbsoluteLayout();
        mainLayout.setSizeFull();

        this.pageText = new Label();
        this.pageText.setValue(loadInitialText());
        this.pageText.setContentMode(ContentMode.HTML);
        this.mainLayout.addComponent(this.pageText, "top:125.0px;left:30px;");

        logo = new Embedded(null, new ThemeResource(INITIAL_LOGO_RESOURCE));
        mainLayout.addComponent(logo, "top:30.0px; left:100px;");

        return mainLayout;
    }

    private String loadInitialText() {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final Locale locale = LocaleHolder.getLocale();

        String customInitialFile = "";
        try {
            customInitialFile = this.appConfig.getString(ConfigProperty.FRONTEND_INITIAL_PAGE);
            LOG.debug("Using custom initial HTML file from {}", customInitialFile);
            if (customInitialFile != null) {
                final String result = loadStringFromResource(classLoader, customInitialFile);
                if (result != null && !result.isEmpty() ) {
                    LOG.debug("Custom file found, text loaded");
                    return result;
                } else {
                    LOG.warn("Custom initial page {} not found, using default one from resources", customInitialFile);
                }
//                File initialFile = new File(customInitialFile);
//                if (initialFile != null &&  initialFile.exists()) {
//                    LOG.debug("Custom file found, loading text");
//                    return loadStringFromFile(initialFile);
//                } else {
//                    LOG.warn("Custom initial page {} not found, using default one from resources", customInitialFile);
//                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to load initial custom page {}, using default one from resources", customInitialFile);
        }

        String resourceFileName = INITIAL_TEXT_RESOURCE_PREFIX + locale.toLanguageTag() + INITIAL_TEXT_RESOURCE_POSTFIX;
        final String result = loadStringFromResource(classLoader, resourceFileName);
        if (result != null) {
            return result;
        } else {
            LOG.debug("Localized initial page resource {} not found, using default", resourceFileName);
            resourceFileName = INITIAL_DEFAULT_TEXT_RESOURCE;
            return loadStringFromResource(classLoader, resourceFileName);
        }
    }

    private static String loadStringFromFile(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toString(inputStream, Charset.forName("UTF-8"));
        }
    }

    private static String loadStringFromResource(ClassLoader classLoader, String resourceName) {
        try (InputStream inStream = classLoader.getResourceAsStream(resourceName)) {
            if (inStream == null) {
                return null;
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, Charset.forName("UTF-8")));
            final StringBuilder builder = new StringBuilder(256);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException ex) {
            LOG.error("Failed to load about.html.", ex);
            return null;
        }
    }

}
