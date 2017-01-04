package cz.cuni.mff.xrg.odcs.rdf.help;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * Define behavior of result ordered SPARQL SELECT queries as iterator over {@link TupleQueryResult}.
 * 
 * @author tomasknap
 */
@Deprecated
public interface OrderTupleQueryResult extends TupleQueryResult {

    /**
     * Returns true if next element is available, false otherwise.
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using.
     * 
     * @return true if next element is available, false otherwise.
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public boolean hasNext() throws QueryEvaluationException;

    /**
     * Returns next {@link BindingSet} element
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using.
     * 
     * @return next {@link BindingSet} element
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public BindingSet next() throws QueryEvaluationException;
}
