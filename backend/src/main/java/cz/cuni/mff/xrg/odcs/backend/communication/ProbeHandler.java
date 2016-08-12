package cz.cuni.mff.xrg.odcs.backend.communication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.cuni.mff.xrg.odcs.backend.i18n.Messages;
import eu.unifiedviews.commons.util.DbPropertiesTableUtils;

/**
 * ProbeHandler for {@link EmbeddedHttpServer}
 * Checks database access (SELECT, INSERT, DELETE) and returns HTTP OK and predefined string
 */
public class ProbeHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProbeHandler.class);

    @Autowired(required = true)
    private DbPropertiesTableUtils dbUtils;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        boolean isDbRunning = true;
        try {
            isDbRunning = this.dbUtils.trySelectInsertDeleteInDb();
        } catch (Exception e) {
            LOG.error("Exception occured during testing connection to database", e);
            isDbRunning = false;
        }

        if (isDbRunning) {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println(Messages.getString("ProbeHandler.function.ok"));
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        baseRequest.setHandled(true);

    }

}
