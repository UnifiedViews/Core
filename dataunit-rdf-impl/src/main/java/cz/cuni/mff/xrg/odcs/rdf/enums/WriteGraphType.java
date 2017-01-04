package cz.cuni.mff.xrg.odcs.rdf.enums;

import cz.cuni.mff.xrg.odcs.rdf.exceptions.GraphNotEmptyException;

/**
 * One of chosed way, how to load RDF data to named graph to SPARQL endpoint.
 * 
 * @author Jiri Tomes
 */
public enum WriteGraphType {

    /**
     * Old data are overriden by new added data
     */
    OVERRIDE,
    /**
     * Disjuction of sets new and old data
     */
    MERGE,
    /**
     * If target graph is not empty - throw {@link GraphNotEmptyException}.
     */
    FAIL
}
