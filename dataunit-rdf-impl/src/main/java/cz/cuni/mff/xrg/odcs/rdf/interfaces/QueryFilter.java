package cz.cuni.mff.xrg.odcs.rdf.interfaces;

import cz.cuni.mff.xrg.odcs.rdf.query.utils.QueryFilterManager;

/**
 * Interface responsible for filtering queries. Methods are using to managing
 * filters in class {@link QueryFilterManager}.
 * 
 * @author Jiri Tomes
 */
public interface QueryFilter {

    /**
     * Return string representation for name of filter.
     * 
     * @return name of filter.
     */
    public String getFilterName();

    /**
     * Return string representation of query transformed by filter.
     * 
     * @param originalQuery
     *            query as input to filter
     * @return transformed query using filter.
     */
    public String applyFilterToQuery(String originalQuery);
}
