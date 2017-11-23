package eu.unifiedviews.master.api;

import eu.unifiedviews.master.authentication.BasicAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class UserHelper {

    @Autowired
    private BasicAuthenticationFilter authFilter;

    private static final Logger LOG = LoggerFactory.getLogger(UserHelper.class);

    public String getUser(String userExternalId) {

        if (isEmpty(userExternalId)) {
            String username = this.authFilter.getUserName();
            if (isEmpty(username)) {
                LOG.error("No user defined in the parameter and no user was retrieved from the authentication header");
                return null;
            }
            else {
                return username;
            }
        }
        else {
            //no change, return what was provided in the param
            return userExternalId;
        }

    }

}
