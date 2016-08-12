package eu.unifiedviews.master.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * Feature that uses basic authentication to restrict access to annotated resources.
 *
 * Annotation used: {@link AuthenticationRequired}.
 *
 * This feature finds all resources provided by webservice, and looks for AuthenticationRequired annotation.
 * If authentication is required, feature will register {@link BasicAuthenticationFilter} to every request send to this resource.
 */
@Component
public class BasicAuthenticationFeature implements DynamicFeature {

    @Autowired
    private BasicAuthenticationFilter basicAuthenticationFilter;

    /**
     * A callback method called by the JAX-RS runtime during the application
     * deployment to register provider instances or classes in a
     * {@link javax.ws.rs.core.Configuration runtime configuration} scope of a particular {@link javax.ws.rs.HttpMethod
     * resource or sub-resource method}; i.e. the providers that should be dynamically bound
     * to the method.
     * <p>
     * The registered provider instances or classes are expected to be implementing one
     * or more of the following interfaces:
     * </p>
     * <ul>
     * <li>{@link ContainerRequestFilter}</li>
     * <li>{@link ContainerResponseFilter}</li>
     * <li>{@link ReaderInterceptor}</li>
     * <li>{@link WriterInterceptor}</li>
     * <li>{@link javax.ws.rs.core.Feature}</li>
     * </ul>
     * <p>
     * A provider instance or class that does not implement any of the interfaces
     * above may be ignored by the JAX-RS implementation. In such case a
     * {@link java.util.logging.Level#WARNING warning} message must be logged.
     * JAX-RS implementations may support additional provider contracts that
     * can be registered using a dynamic feature concept.
     * </p>
     * <p>
     * Conceptually, this callback method is called during a {@link javax.ws.rs.HttpMethod
     * resource or sub-resource method} discovery phase (typically once per each discovered
     * resource or sub-resource method) to register provider instances or classes in a
     * {@code configuration} scope of each particular method identified by the supplied
     * {@link javax.ws.rs.container.ResourceInfo resource information}.
     * The responsibility of the feature is to properly update the supplied {@code configuration}
     * context.
     * </p>
     *
     * @param resourceInfo resource class and method information.
     * @param context      configurable resource or sub-resource method-level runtime context
     *                     associated with the {@code resourceInfo} in which the feature
     */
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if(isAuthenticationRequired(resourceInfo)) {
            context.register(basicAuthenticationFilter);
        }
    }

    /**
     * Method retrieves annotation value from given resource.
     *
     * Method annotation value overrides class annotation value.
     *
     * @param resourceInfo
     * @return True if resource needs authentication.
     */
    private boolean isAuthenticationRequired(ResourceInfo resourceInfo) {
        // method annotation
        AuthenticationRequired methodAuthenticationRequired = resourceInfo.getResourceMethod().getAnnotation(AuthenticationRequired.class);
        if(methodAuthenticationRequired != null) {
            return methodAuthenticationRequired.value();
        }

        // class annotation
        AuthenticationRequired classAuthenticationRequired = resourceInfo.getResourceClass().getAnnotation(AuthenticationRequired.class);
        return (classAuthenticationRequired != null) && classAuthenticationRequired.value();
    }
}
