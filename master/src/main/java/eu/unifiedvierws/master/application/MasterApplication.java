package eu.unifiedvierws.master.application;

import eu.unifiedviews.master.authentication.BasicAuthenticationFeature;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 *  Resource configuration of Jersey JAX-RS web service.
 */
public class MasterApplication extends ResourceConfig {

    public MasterApplication() {
        // retrieve Spring context
        ApplicationContext context = ContextLoader.getCurrentWebApplicationContext();

        packages("eu.unifiedviews.master.api");

        // register logging feature
        register(LoggingFilter.class);

        // register feature for supporting Multipart files
        register(MultiPartFeature.class);

        // retrieve authentication feature from spring context
        BasicAuthenticationFeature basicAuthenticationFeature = context.getBean(BasicAuthenticationFeature.class);
        // register authentication feature
        register(basicAuthenticationFeature);
    }
}
