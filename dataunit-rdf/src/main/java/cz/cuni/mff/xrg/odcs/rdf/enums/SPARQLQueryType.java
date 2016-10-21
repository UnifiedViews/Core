package cz.cuni.mff.xrg.odcs.rdf.enums;

/**
 * Possible types of SPARQL queries.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum SPARQLQueryType {

    /**
     * Type used for SELECT queries.
     */
    SELECT,
    /**
     * Type used for CONSTRUCT queries.
     */
    CONSTRUCT,
    /**
     * Type used for DESCRIBE queries.
     */
    DESCRIBE,
    /**
     * Value for syntax error or other types of queries.
     */
    UNKNOWN;
}
