package cz.cuni.mff.xrg.odcs.frontend.monitor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import eu.unifiedviews.commons.util.DbPropertiesTableUtils;

/**
 * Simple probe for monitoring purposes
 * When servlet called, database connection is checked and if successful (SELECT, INSERT, DELETE)
 * HTTP OK status is sent together with text message
 */
public class ProbeServlet extends HttpServlet {

    private static final long serialVersionUID = 3380633496546339831L;

    private static final Logger LOG = LoggerFactory.getLogger(ProbeServlet.class);

    @Autowired
    private DbPropertiesTableUtils dbUtils;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, servletConfig.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean isRunning = true;

        try {
            isRunning = this.dbUtils.trySelectInsertDeleteInDb();
        } catch (Exception e) {
            LOG.error("Connection to database could not be obtained", e);
            isRunning = false;
        }

        if (isRunning) {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println(Messages.getString("ProbeServlet.function.ok"));
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

}
