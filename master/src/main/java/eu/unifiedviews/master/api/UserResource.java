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
package eu.unifiedviews.master.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import eu.unifiedviews.master.authentication.AuthenticationRequired;
import eu.unifiedviews.master.i18n.Messages;
import eu.unifiedviews.master.model.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Component
@Path("/users")
@AuthenticationRequired
public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private ObjectMapper mapper;

    @Value("${cas.attributeName.role}")
    private String roleAttributeName;

    @Value("${cas.attributeName.actorId}")
    private String actorIdAttributeName;

    @Value("${cas.attributeName.actorName}")
    private String actorNameAttributeName;

    @Value("${cas.attributeName.fullName}")
    private String fullNameAttributeName;

    @Value("${cas.attributeName.userName}")
    private String userNameAttributeName;

    private String[] requiredAttributes;

    @PostConstruct
    public void setup() {
        // setup array of required attributes
        this.requiredAttributes = new String[] { roleAttributeName, actorIdAttributeName, actorNameAttributeName, fullNameAttributeName, userNameAttributeName };
    }

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public String createUser(String casResponseAsJSON) {
        LOG.info("Received user create request: " + casResponseAsJSON);
        Multimap<String, String> multimap = parseJSONtoMultimap(casResponseAsJSON);

        // validate if request contains all required attributes
        List<String> missingAttributes = new ArrayList<>();
        for (String requiredAttribute : requiredAttributes) {
            if (!multimap.containsKey(requiredAttribute)) {
                missingAttributes.add(requiredAttribute);
            }
        }
        if (missingAttributes.size() > 0) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("user.request.parameter.missing", Joiner.on(", ").join(missingAttributes)), "Missing attribute/s: '" + Joiner.on(", ").join(missingAttributes) + "'.");
        }

        String userName = getFirstValue(multimap.get(userNameAttributeName));
        String userFullName = getFirstValue(multimap.get(fullNameAttributeName));
        String actorId = getFirstValue(multimap.get(actorIdAttributeName));
        String actorName = getFirstValue(multimap.get(actorNameAttributeName));
        List<String> roles = (List) multimap.get(roleAttributeName);

        userFacade.createOrUpdateUser(userName, userFullName, actorId, actorName, roles);
        return "{\"success\":true}";
    }

    private Multimap<String, String> parseJSONtoMultimap(String casResponseAsJSON) {
        // parse request attributes to multimap
        Multimap<String, String> multimap = ArrayListMultimap.create();
        try {
            JsonNode rootNode = mapper.readTree(casResponseAsJSON);
            Iterator<Map.Entry<String, JsonNode>> nodeIterator = rootNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = nodeIterator.next();
                if (entry.getValue().isArray()) {
                    Iterator<JsonNode> elementsIterator = entry.getValue().elements();
                    while (elementsIterator.hasNext()) {
                        JsonNode element = elementsIterator.next();
                        multimap.put(entry.getKey(), element.asText());
                    }
                } else {
                    multimap.put(entry.getKey(), entry.getValue().asText());
                }
            }
        } catch (IOException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, Messages.getString("user.request.not.parsed"), e.getMessage());
        }
        return multimap;
    }

    private <T> T getFirstValue(Collection<T> col) {
        Iterator<T> iterator = col.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}
