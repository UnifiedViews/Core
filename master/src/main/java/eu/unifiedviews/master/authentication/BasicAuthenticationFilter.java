package eu.unifiedviews.master.authentication;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PasswordHash;
import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import eu.unifiedviews.master.i18n.Messages;
import eu.unifiedviews.master.model.ApiException;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AuthenticationManager;

import javax.annotation.PostConstruct;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Authentication filter, that filters incoming request to resource.
 *
 * Filter looks at header of incoming request and retrieves authentication header. If authentication does not match
 * stored credentials, unauthorized exception is thrown and request to resource is denied.
 */
@Component
public class BasicAuthenticationFilter implements ContainerRequestFilter {

    @Autowired
    private AppConfig configuration;

    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authManager;

    @Autowired
    private AuthenticationContext authCtx;

    private String credentials;
    private String username = "";

    private static final Logger log = LoggerFactory.getLogger(BasicAuthenticationFilter.class);

    @PostConstruct
    private void init() {
//        String username = configuration.getString(ConfigProperty.MASTER_API_USER);
//        String password = configuration.getString(ConfigProperty.MASTER_API_PASSWORD);
//
//        this.credentials = Base64.encodeAsString(username + ":" + password);
    }

    /**
     * Filter method called before a request has been dispatched to a resource.
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     * If a request filter produces a response by calling {@link javax.ws.rs.container.ContainerRequestContext#abortWith}
     * method, the execution of the (either pre-match or post-match) request filter
     * chain is stopped and the response is passed to the corresponding response
     * filter chain (either pre-match or post-match). For example, a pre-match
     * caching filter may produce a response in this way, which would effectively
     * skip any post-match request filters as well as post-match response filters.
     * Note however that a responses produced in this manner would still be processed
     * by the pre-match response filter chain.
     * </p>
     *
     * @param requestContext request context.
     * @throws java.io.IOException if an I/O exception occurs.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorization = requestContext.getHeaderString("authorization");
        if(!isAuthorizationValid(authorization)) {
            throw new ApiException(Response.Status.UNAUTHORIZED, Messages.getString("unauthorized.request"), "Basic authentication from request header is missing or incorrect.");
        }
    }

    public String getUserName() {
        return this.username;
    }

    private boolean isAuthorizationValid(String authorization) {
        if(isEmpty(authorization)) {
            return false;
        }
        // remove "Basic " part from header
        authorization = authorization.replaceAll("[Bb]asic ", "");

        //get the user, correctPass, check
        String decodedToken = Base64.decodeAsString(authorization);

        //parse username and password
        int delim = decodedToken.indexOf(":");

        if (delim == -1) {
            log.error("Invalid basic authentication token {}", decodedToken);
            return false;
        }

        this.username = decodedToken.substring(0, delim);
        String pass = decodedToken.substring(delim + 1);

        //authentication
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, pass);
            Authentication authentication = authManager.authenticate(token);
            authCtx.setAuthentication(authentication);
        } catch (AuthenticationException ex) {
            //authentication failed
            log.error("Basic authentication failed: {}", ex.getLocalizedMessage());
            return false;
        }

        return true;

    }
}
