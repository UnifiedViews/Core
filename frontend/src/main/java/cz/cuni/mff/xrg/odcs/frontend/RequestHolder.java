package cz.cuni.mff.xrg.odcs.frontend;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores HTTPServletRequest object, which is needed by Spring authentication.
 * 
 * @author Jan Vojt
 */
public class RequestHolder {

    private static final ThreadLocal<HttpServletRequest> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Get request.
     * 
     * @return Request.
     */
    public static HttpServletRequest getRequest() {
        return THREAD_LOCAL.get();
    }

    static void setRequest(HttpServletRequest request) {
        THREAD_LOCAL.set(request);
    }

    static void clean() {
        THREAD_LOCAL.remove();
    }
}
