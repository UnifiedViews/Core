package cz.cuni.mff.xrg.odcs.rdf.interfaces;

/**
 * It is responsible for right validation of queries.
 * 
 * @author Jiri Tomes
 */
public interface QueryValidator {

    /**
     * Method for detection right syntax of query.
     * 
     * @return true, if query is valid, false otherwise.
     */
    public boolean isQueryValid();

    /**
     * String message describes syntax problem of validation query.
     * 
     * @return empty string, when query is valid.
     */
    public String getErrorMessage();
}
