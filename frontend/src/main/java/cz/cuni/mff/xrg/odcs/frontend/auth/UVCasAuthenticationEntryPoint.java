package cz.cuni.mff.xrg.odcs.frontend.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;

/**
 * Used by the <code>ExceptionTranslationFilter</code> to commence authentication via the JA-SIG Central
 * Authentication Service (CAS).
 * <p>
 * The user's browser will be redirected to the JA-SIG CAS enterprise-wide login page. This page is specified by the <code>loginUrl</code> property. Once login
 * is complete, the CAS login page will redirect to the page indicated by the <code>service</code> property. The <code>service</code> is a HTTP URL belonging to
 * the current application. The <code>service</code> URL is monitored by the {@link CasAuthenticationFilter}, which will validate the CAS login was successful.
 *
 * @author Ben Alex
 * @author Scott Battaglia
 */
public class UVCasAuthenticationEntryPoint extends CasAuthenticationEntryPoint {

    private static final String HTTP_HEADER_FORWARDED_HOST = "X-Forwarded-Host";

    private static final String HTTP_HEADER_HOST = "Host";

    private static final String HTTP_HEADER_SCHEME = "Scheme";

    private boolean behindProxy = false;

    /**
     * Constructs a new Service Url. The default implementation relies on the CAS client to do the bulk of the work.
     * 
     * @param request
     *            the HttpServletRequest
     * @param response
     *            the HttpServlet Response
     * @return the constructed service url. CANNOT be NULL.
     */

    protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {

        String serviceUrl = null;

        if (behindProxy) {

            String forwardedHost = request.getHeader(HTTP_HEADER_FORWARDED_HOST);
            String host = request.getHeader(HTTP_HEADER_HOST);
            String scheme = request.getHeader(HTTP_HEADER_SCHEME) != null ? request.getHeader(HTTP_HEADER_SCHEME) : "http";

            String resultingHost = null;

            if (forwardedHost != null)
                resultingHost = forwardedHost;
            else if (host != null)
                resultingHost = host;

            if(resultingHost == null){
                throw new IllegalStateException("if behindProxy=true please ensure that either header " + HTTP_HEADER_FORWARDED_HOST + " or " + HTTP_HEADER_HOST + " is sent!");
            }
            
            serviceUrl = scheme + "://" + resultingHost + this.getServiceProperties().getService();
        }
        else
            serviceUrl = this.getServiceProperties().getService();

        return CommonUtils.constructServiceUrl(null, response, serviceUrl, null, this.getServiceProperties().getArtifactParameter(), this.getEncodeServiceUrlWithSessionId());
    }

    public void setBehindProxy(boolean behindProxy) {
        this.behindProxy = behindProxy;
    }
    
    
}
