package cz.cuni.mff.xrg.odcs.frontend.auth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class UVWebAuthenticationDetailsSource implements
        AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

    private boolean behindProxy = false;

    /**
     * @param context
     *            the {@code HttpServletRequest} object.
     * @return the {@code WebAuthenticationDetails} containing information about the
     *         current request
     */
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        if (behindProxy)
            return new UVAuthenticationDetails(context);
        else
            return new WebAuthenticationDetails(context);
    }

    public void setBehindProxy(boolean behindProxy) {
        this.behindProxy = behindProxy;
    }

}
