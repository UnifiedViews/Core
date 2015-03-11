package eu.unifiedviews.master.authentication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify if access to resource needs to be authorized.
 *
 * This annotation is picked by {@link eu.unifiedviews.master.authentication.BasicAuthenticationFeature}.
 * Default value is true.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AuthenticationRequired {
    boolean value() default true;
}
