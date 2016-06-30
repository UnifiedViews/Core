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
