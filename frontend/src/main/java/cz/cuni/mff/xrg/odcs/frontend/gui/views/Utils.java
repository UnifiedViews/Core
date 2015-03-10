package cz.cuni.mff.xrg.odcs.frontend.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;

/**
 * @author tomasknap
 */
@Component
public class Utils {

    @Autowired
    private AuthenticationContext authCtx;

    /**
     * Get page length.
     * 
     * @return page length
     */
    public int getPageLength() {
        User user = authCtx.getUser();
        Integer rows = user == null
                ? null : user.getTableRows();

        if (rows == null) {
            rows = 20;
        }
        return rows;
    }

    /**
     * Get username.
     * 
     * @return username
     */
    public String getUserName() {
        User user = authCtx.getUser();
        return user == null ? "" : user.getUsername();
    }

    /**
     * Get default max length of column.
     * 
     * @return default max length of column
     */
    public static int getColumnMaxLenght() {
        return 100;
    }

    public User getUser() {
        return authCtx.getUser();
    }

    public boolean hasUserAuthority(String authString) {
        if(authString == null)
            return false;
        for (GrantedAuthority ga : getUser().getAuthorities()) {
            if(authString.equals(ga.getAuthority())){
                return true;
            }
        }
        return false;
    }
}