package cz.cuni.mff.xrg.odcs.rdf.query.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renposible for transforming given SELECT/CONTRUCT valid query using
 * restriction LIMIT and OFFSET.
 * 
 * @author Jiri Tomes
 */
public class QueryRestriction {

    private String query;

    private int limit;

    private int offset;

    private static final int UNDEFINED_VALUE = -1;

    /**
     * Create new instance of {@link QueryRestriction} based on SPARQL query.
     * 
     * @param query
     *            SPARQL query for transforming using restriction LIMIT and
     *            OFFSET.
     */
    public QueryRestriction(String query) {
        this.query = query;
        this.limit = UNDEFINED_VALUE;
        this.offset = UNDEFINED_VALUE;
    }

    /**
     * Returns original SPARQL query used for restriction.
     * 
     * @return original SPARQL query used for restriction.
     */
    public String getOriginalQuery() {
        return query;
    }

    /**
     * Returns true, if limit restriction was set, false otherwise.
     * 
     * @return true, if limit restriction was set, false otherwise.
     */
    private boolean hasLimit() {
        return limit > UNDEFINED_VALUE;
    }

    /**
     * Returns true, if offset restriction was set, false otherwise.
     * 
     * @return true, if offset restriction was set, false otherwise.
     */
    private boolean hasOffset() {
        return offset > UNDEFINED_VALUE;
    }

    /**
     * Set limit and offset value as restriction.
     * 
     * @param limit
     *            new limit value to set
     * @param offset
     *            new offset value to set
     */
    public void setRestriction(int limit, int offset) {
        setLimit(limit);
        setOffset(offset);
    }

    /**
     * Set new limit value as restriction.
     * 
     * @param limit
     *            new value of limit to set as restriction.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Set new offset value as restriction.
     * 
     * @param offset
     *            new offset value to set as restriction.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Clean up set restriction.
     */
    public void removeRestriction() {
        setRestriction(UNDEFINED_VALUE, UNDEFINED_VALUE);
    }

    /**
     * Return string representation of transformed query by set restriction.
     * 
     * @return query transformed by given restriction
     */
    public String getRestrictedQuery() {

        String regex = "((limit|offset)(\\s)*[0-9]+(\\s)*)+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query.toLowerCase());

        String resultQuery = query;

        boolean hasResult = matcher.find();

        if (!hasResult) {
            resultQuery += getRestrictedPart();
        }

        int startIndex = 0;

        StringBuilder queryParts = new StringBuilder();

        while (hasResult) {

            int endIndex = matcher.end();

            String before = query.substring(startIndex, matcher.start());
            String restrictionPart = getRestrictedPart();

            queryParts.append(before);
            queryParts.append(restrictionPart);

            hasResult = matcher.find();

            if (!hasResult) {
                String after = query.substring(endIndex, query.length());
                queryParts.append(after);

                resultQuery = queryParts.toString();
            } else {
                startIndex = endIndex;
            }
        }

        return resultQuery;
    }

    /**
     * Returns string value of set restriction - Keyword (LIMIT|OFFSET) + set
     * value restriction.
     * 
     * @return string value of set restriction.
     */
    private String getRestrictedPart() {
        StringBuilder bulder = new StringBuilder();

        if (hasLimit()) {
            String limitPart = String.format("LIMIT %s ", String.valueOf(limit));
            bulder.append(limitPart);
        }

        if (hasOffset()) {
            String offsetPart = String.format("OFFSET %s ", String.valueOf(
                    offset));
            bulder.append(offsetPart);
        }

        return bulder.toString();
    }
}
