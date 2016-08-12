package cz.cuni.mff.xrg.odcs.rdf.validators;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParserUtil;

import cz.cuni.mff.xrg.odcs.rdf.enums.SPARQLQueryType;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.QueryValidator;
import cz.cuni.mff.xrg.odcs.rdf.query.utils.QueryPart;
import eu.unifiedviews.dataunit.rdf.impl.i18n.Messages;

/**
 * Class responsible to find out, if sparql queries are valid or not.
 * 
 * @author Jiri Tomes
 */
public class SPARQLQueryValidator implements QueryValidator {

    private String query;

    private String message;

    private SPARQLQueryType requiredType;

    boolean requireSPARQLType;

    /**
     * Create new instance of {@link SPARQLQueryValidator} with given SPARQL
     * query you can validate.
     * For SPARQL update query use {@link SPARQLUpdateValidator}.
     * 
     * @param query
     *            SPARQL query you can validate
     */
    public SPARQLQueryValidator(String query) {
        this.query = query;
        this.message = "";
        this.requireSPARQLType = false;
        this.requiredType = SPARQLQueryType.UNKNOWN;

    }

    /**
     * Create new instance of {@link SPARQLQueryValidator} with given SPARQL
     * query and it´s required {@link SPARQLQueryType} you can validate.
     * For SPARQL update query use {@link SPARQLUpdateValidator}.
     * 
     * @param query
     *            SPARQL query you can validate
     * @param requiredType
     *            Type of SPARQL query that is required to by same as
     *            in given query
     */
    public SPARQLQueryValidator(String query, SPARQLQueryType requiredType) {
        this.query = query;
        this.message = "";
        this.requireSPARQLType = true;
        this.requiredType = requiredType;
    }

    private boolean isSameType(SPARQLQueryType type1, SPARQLQueryType type2) {
        return type1.equals(type2);
    }

    /**
     * If query has required type returns true if type of the given query and
     * required type are the same, false otherwise. If no required query type is
     * set returns true.
     * 
     * @return true if query has required type and type of the given query and
     *         required type are the same, false otherwise. If no required query
     *         type is set returns true.
     */
    public boolean hasSameType() {
        if (requireSPARQLType) {
            QueryPart queryPart = new QueryPart(query);
            SPARQLQueryType queryType = queryPart.getSPARQLQueryType();
            if (isSameType(queryType, requiredType)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Method for detection right syntax of sparql query.
     * 
     * @return true, if query is valid, false otherwise.
     */
    @Override
    public boolean isQueryValid() {

        if (requireSPARQLType) {
            QueryPart queryPart = new QueryPart(query);

            SPARQLQueryType myType = queryPart.getSPARQLQueryType();
            if (!isSameType(myType, requiredType)) {
                message = requiredType.toString() + Messages.getString("SPARQLQueryValidator.unsupported.query");
                return false;
            }
        }

        boolean isValid = true;

        try {
            QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
        } catch (MalformedQueryException e) {
            message = e.getCause().getMessage();
            isValid = false;
        }

        return isValid;
    }

    /**
     * String message describes syntax problem of SPARQL query.
     * 
     * @return empty string, when SPARQL query is valid
     */
    @Override
    public String getErrorMessage() {
        return message;
    }
}
