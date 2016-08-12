package eu.unifiedviews.master.application;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import eu.unifiedviews.master.authentication.BasicAuthenticationFeature;
import eu.unifiedviews.master.model.MasterExceptionMapper;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import java.util.logging.Logger;

/**
 * Resource configuration of Jersey JAX-RS web service.
 */
public class MasterApplication extends ResourceConfig {

    public MasterApplication() {
        // retrieve Spring context
        ApplicationContext context = ContextLoader.getCurrentWebApplicationContext();

        packages("eu.unifiedviews.master.api");

        // register JSON feature
        register(JacksonJaxbJsonProvider.class);

        // register exception mapper
        register(MasterExceptionMapper.class);

        // register logging feature
        register(new LoggingFilter(Logger.getLogger(MasterApplication.class.getName()), true));
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // register feature for supporting Multipart files
        register(MultiPartFeature.class);

        // retrieve authentication feature from spring context
        BasicAuthenticationFeature basicAuthenticationFeature = context.getBean(BasicAuthenticationFeature.class);
        // register authentication feature
        register(basicAuthenticationFeature);
    }
}
