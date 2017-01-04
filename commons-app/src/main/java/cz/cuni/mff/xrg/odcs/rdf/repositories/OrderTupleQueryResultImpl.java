package cz.cuni.mff.xrg.odcs.rdf.repositories;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.rdf.help.OrderTupleQueryResult;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;

/**
 * Class based on lazy evaluation to get data there are used then in methods
 * from interface TupleQueryResult.
 * This approach requires using of SPARQL select query with ORDER BY (sorted
 * n-tuples) and without LIMIT, OFFSET keywords.
 * Important note:
 * For no problem behaviour of {@link OrderTupleQueryResult} is important to
 * increate setting of variable "MaxSortedTopRows" you find in Server parameters
 * [Parameters] in Virtuoso.ini file (default used value is 10 000). This
 * parameter describe maximum data size (count of rows) that can be sorted.
 * You probably dont have "MaxSortedRows" in your ini.file - you have to add it
 * manually. Be careful - "MaxSortedRows" param must be add only to Server
 * parameters [Parameters] parts, elsewhere has no effect.
 * Basic idea of implementation:
 * Data need for {@link TupleQueryResult} - {@link BindingSet} are getting in
 * the parts. Size of one of this part is defined in {@link #LIMIT} variable.
 * All elements from part are added to collection defined in {@link #bindings}.
 * If collection contains some elements, one element is remove and it´s return.
 * If collection is empty, process tries to fill collection with data from next
 * part. The process ends if collection is empty and there is no reason (no data
 * parts) how to fill it.
 * 
 * @author Jiri Tomes
 */
public class OrderTupleQueryResultImpl implements OrderTupleQueryResult {

    private static Logger logger = LoggerFactory.getLogger(
            OrderTupleQueryResultImpl.class);

    private static long SLEEP_TIME = 1000;

    /**
     * Size of one data part.
     */
    private int LIMIT = 20000;

    private int OFFSET = 0;

    private List<BindingSet> bindings;

    private String orderSelectQuery;

    private RDFDataUnit repository;

    /**
     * Create new instance of {@link OrderTupleQueryResult} based on ordered
     * select query (with ORDER BY and without LIMIT,OFFSET) and {@link RDFDataUnit} as repository instance, where query will be executed.
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using. For more info
     * 
     * @see OrderTupleQueryResult class description.
     * @see OrderTupleQueryResult class description.
     * @param orderSelectQuery
     *            SPARQL ordered select query(containt ORDER BY).
     * @param repository
     *            instance of {@link RDFDataUnit} as repository
     *            where query will be executed.
     */
    public OrderTupleQueryResultImpl(String orderSelectQuery,
            RDFDataUnit repository) {
        this.orderSelectQuery = orderSelectQuery;
        this.repository = repository;
        this.bindings = new LinkedList<>();
    }

    private TupleQueryResult executeSelectQuery(String selectQuery) throws QueryEvaluationException {
        while (true) {
            RepositoryConnection connection = null;
            try {
                connection = repository.getConnection();

                TupleQuery tupleQuery = connection.prepareTupleQuery(
                        QueryLanguage.SPARQL, selectQuery);
                tupleQuery.setDataset(RDFHelper.getDatasetWithDefaultGraphs(repository));

                TupleQueryResult result = tupleQuery.evaluate();
                return result;

            } catch (QueryEvaluationException e) {
                logger.error(
                        "Connection to RDF repositored failed during evaluation. " + e
                                .getMessage());
            } catch (MalformedQueryException ex) {

                throw new QueryEvaluationException(
                        "This query is not valid. " + ex.getMessage(), ex);

            } catch (RepositoryException ex) {
                logger.error("Connection to RDF repository failed. {}",
                        ex.getMessage(), ex);
            } catch (DataUnitException ex) {
                logger.error("DataUnit exception. {}",
                        ex.getMessage(), ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (RepositoryException ex) {
                        logger.warn("Error when closing connection", ex);
                    }
                }
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                logger.debug("Thread interupt during sleeping. {}",
                        ex.getMessage(), ex);
            }
        }

    }

    /**
     * Create ordered select query as restriction of {@link #orderSelectQuery} with defined LIMIT and OFFSET.
     * 
     * @param limitValue
     *            value of LIMIT
     * @param offsetValue
     *            value of OFFSET
     * @return New ordered select query as restriction (LIMIT + OFFSET) of
     *         original ordered select query.
     */
    private String getLimitedQuery(int limitValue, int offsetValue) {
        StringBuilder result = new StringBuilder(orderSelectQuery);

        result.append(" LIMIT ");
        result.append(String.valueOf(limitValue));
        result.append(" OFFSET ");
        result.append(String.valueOf(offsetValue));

        return result.toString();
    }

    /**
     * Fill collection {@link #bindings} with data.
     */
    private void fillBindings() throws QueryEvaluationException {

        int atempts = 0;

        while (true) {

            String limitedQuery = getLimitedQuery(LIMIT, OFFSET);
            TupleQueryResult tupleResult = executeSelectQuery(limitedQuery);

            try {

                while (tupleResult != null && tupleResult.hasNext()) {
                    BindingSet nextBindingSet = tupleResult.next();
                    bindings.add(nextBindingSet);
                }

                if (tupleResult != null) {
                    OFFSET += LIMIT;
                    break;
                }

            } catch (Exception e) {
                atempts++;
                logger.debug("{}. atempt to fill bindins", atempts);
            } finally {
                if (tupleResult != null) {
                    tupleResult.close();
                }
            }

            bindings.clear();

        }

    }

    /**
     * Returns Collection of BindingNames.
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using. For more info
     * 
     * @see OrderTupleQueryResult class description.
     * @return Collection of BindingNames.
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public List<String> getBindingNames() throws QueryEvaluationException {
        TupleQueryResult result = executeSelectQuery(orderSelectQuery);
        return result.getBindingNames();
    }

    /**
     * Close this instance.
     * 
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public void close() throws QueryEvaluationException {
    }

    /**
     * Returns true if next element is available, false otherwise.
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using. For more info
     * 
     * @see OrderTupleQueryResultImpl class description.
     * @return true if next element is available, false otherwise.
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public boolean hasNext() throws QueryEvaluationException {
        if (bindings.isEmpty()) {
            fillBindings();
        }
        return !bindings.isEmpty();
    }

    /**
     * Returns next {@link BindingSet} element
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using. For more info
     * 
     * @see OrderTupleQueryResultImpl class description.
     * @return next {@link BindingSet} element
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public BindingSet next() throws QueryEvaluationException {
        BindingSet nextBinding = bindings.remove(0);
        return nextBinding;
    }

    /**
     * Removes actualy element pointed by iterator.
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using. For more info
     * 
     * @see OrderTupleQueryResult class description.
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public void remove() throws QueryEvaluationException {
        bindings.remove(0);
    }
}
