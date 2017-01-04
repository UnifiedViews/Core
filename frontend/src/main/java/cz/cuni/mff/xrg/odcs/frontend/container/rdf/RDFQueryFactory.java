package cz.cuni.mff.xrg.odcs.frontend.container.rdf;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Simple {@link QueryFactory} for constructing RDF queries.
 * 
 * @author Bogo
 */
public class RDFQueryFactory implements QueryFactory {

    /**
     * Construct query from definition.
     * 
     * @param queryDefinition
     *            Query definition.
     * @return Query.
     */
    @Override
    public Query constructQuery(QueryDefinition queryDefinition) {
        if (queryDefinition.getClass() != RDFQueryDefinition.class) {
            throw new UnsupportedOperationException(Messages.getString("RDFQueryFactory.exception"));
        }
        return new RDFQuery((RDFQueryDefinition) queryDefinition);
    }

}
