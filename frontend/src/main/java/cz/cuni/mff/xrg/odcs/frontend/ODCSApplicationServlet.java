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
package cz.cuni.mff.xrg.odcs.frontend;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServletService;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.frontend.auth.AuthenticationService;
import cz.cuni.mff.xrg.odcs.frontend.i18n.LocalizedSystemMessagesProvider;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringVaadinServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Customized servlet implementation to provide access to original {@link HttpServletRequest} across application.
 * 
 * @see RequestHolder
 * @author Jan Vojt
 */
public class ODCSApplicationServlet extends SpringVaadinServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ODCSApplicationServlet.class);

    private int serviceCounter = 0;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, servletConfig.getServletContext());
    }

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().setSystemMessagesProvider(new LocalizedSystemMessagesProvider());
    }

    /**
     * Create {@link VaadinServletService} from supplied {@link DeploymentConfiguration}.
     * 
     * @param deploymentConfiguration
     *            Deployment configuration.
     * @return Vaadin servlet service.
     * @throws ServiceException
     */
    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
        VaadinServletService service = super.createServletService(deploymentConfiguration);

        // Preload all DPUs on servlet startup, so openning them is fast.
        ApplicationContext context = SpringApplicationContext.getApplicationContext();
        try {
            context.getBean(ModuleFacade.class).preLoadAllDPUs();
        } catch (TransactionException | DatabaseException ex) {
            LOG.error("Could not preload DPUs.", ex);
        }

        return service;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Store current HTTP request in thread-local, so Spring can access it
        // later during user login.
        RequestHolder.setRequest(request);

        // First clear the security context, as we need to load it from session.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        // Load authentication context from session (if there is any).
        Authentication auth = (Authentication) request.getSession()
                .getAttribute(AuthenticationService.SESSION_KEY);
        if (auth != null) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Do the business.
        Date start = new Date();
        int serviceId = serviceCounter++;
        LOG.info("Request ({}) received", serviceId);

        super.service(request, response);

        Date end = new Date();
        if (end.getTime() - start.getTime() > 1000) {
            LOG.warn("Request ({}) finished processing in: {} ms - LONG RESPONSE", serviceId, end.getTime() - start.getTime());
        } else {
            LOG.info("Request ({}) finished processing in: {} ms", serviceId, end.getTime() - start.getTime());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request ({}) URI", request.getRequestURI());
        }

        // We remove the request from the thread local, there's no reason
        // to keep it once the work is done. Next request might be serviced
        // by different thread, which will need to load security context from
        // the session anyway.
        RequestHolder.clean();
        SecurityContextHolder.clearContext();
    }
}
