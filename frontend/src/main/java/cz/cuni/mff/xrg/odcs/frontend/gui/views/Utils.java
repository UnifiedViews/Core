package cz.cuni.mff.xrg.odcs.frontend.gui.views;

import org.springframework.beans.factory.annotation.Autowired;
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

    public User getUser() {
        return this.authCtx.getUser();
    }

    /**
     * Get default max length of column.
     * 
     * @return default max length of column
     */
    public static int getColumnMaxLenght() {
        return 100;
    }

}
