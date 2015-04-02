package cz.cuni.mff.xrg.odcs.frontend;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringVaadinServlet;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServletService;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.frontend.auth.AuthenticationService;

/**
 * Customized servlet implementation to provide access to original {@link HttpServletRequest} across application.
 * 
 * @see RequestHolder
 * @author Jan Vojt
 */
public class ODCSSSOApplicationServlet extends SpringVaadinServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ODCSSSOApplicationServlet.class);


    @Autowired
    private AppConfig appConfig;
    
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, servletConfig.getServletContext());
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

        // Frontend theme for pipeline canvas.
        if (request.getRequestURI().endsWith(ConfigProperty.FRONTEND_THEME.toString())) {
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(appConfig.getString(ConfigProperty.FRONTEND_THEME).getBytes("utf8"));
            outputStream.flush();
            outputStream.close();
        } else {
            super.service(request, response);
        }
    }
}
