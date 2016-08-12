package eu.unifiedviews.master.converter;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Authentication context for Master API.
 *
 * Authentication is hard-wired to admin user(user with id 1).
 */
@Component
public class MasterAuthenticationContext extends AuthenticationContext {

    @Autowired
    private UserFacade userFacade;

    @Override
    public User getUser() {
        return userFacade.getUser(1);
    }
}
