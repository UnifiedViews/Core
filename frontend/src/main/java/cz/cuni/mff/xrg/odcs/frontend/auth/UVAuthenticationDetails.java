package cz.cuni.mff.xrg.odcs.frontend.auth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class UVAuthenticationDetails extends WebAuthenticationDetails {

    private String forwardedHost;

    private String host;

    private String scheme;

    private static final String HTTP_HEADER_FORWARDED_HOST = "X-Forwarded-Host";

    private static final String HTTP_HEADER_HOST = "Host";

    private static final String HTTP_HEADER_SCHEME = "Scheme";

    public UVAuthenticationDetails(HttpServletRequest request) {
        super(request);

        this.forwardedHost = request.getHeader(HTTP_HEADER_FORWARDED_HOST);
        this.host = request.getHeader(HTTP_HEADER_HOST);
        this.scheme = request.getHeader(HTTP_HEADER_SCHEME);
    }

    public String getForwardedHost() {
        return forwardedHost;
    }

    public String getHost() {
        return host;
    }

    public String getScheme() {
        return scheme;
    }

}
