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
package cz.cuni.mff.xrg.odcs.frontend.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.RoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.commons.app.user.UserActor;

public class CasAuthenticationUserDetailsService extends
        AbstractCasAssertionUserDetailsService {

    private static final Logger LOG = LoggerFactory
            .getLogger(CasAuthenticationUserDetailsService.class);

    private String roleAttributeName;

    private String actorIdAttributeName;

    private String actorNameAttributeName;

    private String fullNameAttributeName;

    private String userNameAttributeName;

    private UserFacade userFacade;

    /**
     * Constructor
     * 
     * @param userFacade
     *            UserFacade object used for loading of user data
     */
    public CasAuthenticationUserDetailsService(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @Override
    protected UserDetails loadUserDetails(final Assertion assertion) {

        String userName = assertion.getPrincipal().getName();
        Map<String, Object> attributes = assertion.getPrincipal().getAttributes();
        // FIXME: this is temporal solution; In the future, subject Id should be sent by CAS in username
        // Currently Actor ID is sent in username CAS parameter
        String userNameFromAttributes = attributes.get(this.userNameAttributeName) != null ? attributes.get(this.userNameAttributeName).toString() : null;
        if (userNameFromAttributes != null) {
            userName = userNameFromAttributes;
        }
        String userFullName = attributes.get(this.fullNameAttributeName) != null ? attributes.get(this.fullNameAttributeName).toString() : null;

        List<String> roles = new ArrayList<>();
        Object roleAttributes = attributes.get(roleAttributeName);
        if (roleAttributes != null) {
            if (roleAttributes instanceof String)
                roles.add((String) roleAttributes);// = attributes.get(ROLE_ATTRIBUTE).toString();
            else if (roleAttributes instanceof List)
                roles.addAll((List) roleAttributes);
        }
        String actorId = attributes.get(this.actorIdAttributeName) != null ? attributes.get(this.actorIdAttributeName).toString() : null;
        String actorName = attributes.get(this.actorNameAttributeName) != null ? attributes.get(this.actorNameAttributeName).toString() : null;

        User user = userFacade.createOrUpdateUser(userName, userFullName, actorId, actorName, roles);
        return user;
    }

    public void setFullNameAttributeName(String fullNameAttributeName) {
        this.fullNameAttributeName = fullNameAttributeName;
    }

    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }

    public void setActorNameAttributeName(String actorNameAttributeName) {
        this.actorNameAttributeName = actorNameAttributeName;
    }

    public void setUserNameAttributeName(String userNameAttributeName) {
        this.userNameAttributeName = userNameAttributeName;
    }

    public void setActorIdAttributeName(String actorIdAttributeName) {
        this.actorIdAttributeName = actorIdAttributeName;
    }

}
