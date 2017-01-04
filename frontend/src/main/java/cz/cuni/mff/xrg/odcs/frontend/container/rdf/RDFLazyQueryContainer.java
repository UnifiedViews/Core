package cz.cuni.mff.xrg.odcs.frontend.container.rdf;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;
import org.vaadin.addons.lazyquerycontainer.QueryView;

/**
 * Container for lazy querying of RDF data. Works with SELECT or CONSTRUCT queries.
 * 
 * @author Bogo
 *         TODO (petyr) we should use DbAccess and ReadOnly container instead to unify the data access
 */
public class RDFLazyQueryContainer extends LazyQueryContainer {

    /**
     * Constructor.
     * 
     * @param queryView
     */
    public RDFLazyQueryContainer(QueryView queryView) {
        super(queryView);
    }

    /**
     * Constructor.
     * 
     * @param queryDefinition
     * @param queryFactory
     */
    public RDFLazyQueryContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
        super(queryDefinition, queryFactory);
    }

    /**
     * Constructor.
     * 
     * @param queryFactory
     * @param idPropertyId
     * @param batchSize
     * @param compositeItems
     */
    public RDFLazyQueryContainer(QueryFactory queryFactory, Object idPropertyId, int batchSize, boolean compositeItems) {
        super(queryFactory, idPropertyId, batchSize, compositeItems);
    }

}
