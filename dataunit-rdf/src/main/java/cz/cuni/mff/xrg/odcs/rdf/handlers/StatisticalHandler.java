package cz.cuni.mff.xrg.odcs.rdf.handlers;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.rdf.help.TripleProblem;
import cz.cuni.mff.xrg.odcs.rdf.i18n.Messages;
import eu.unifiedviews.dpu.DPUContext;

/**
 * Class allows monitoring about the extraction data process - information about
 * parsed triples and detail error log.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class StatisticalHandler extends TripleCountHandler {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalHandler.class);

    private static final int TRIPLE_LOGGED_SIZE = 100000;

    private static List<TripleProblem> parsingProblems = new LinkedList<>();

    private long addedCount = 0;

    /**
     * Default handler constructor for parsing and adding RDF data to
     * repository.
     * 
     * @param connection
     *            connection to repository where RDF data are added.
     */
    public StatisticalHandler(RepositoryConnection connection) {
        super(connection);
    }

    /**
     * Default handler constructor for parsing and adding data from SPARQL
     * endpoint to repository.
     * 
     * @param connection
     *            connection to repository where RDF data are added.
     * @param context
     *            DPU context for checking if parsing process was
     *            cancelled or not.
     */
    public StatisticalHandler(RepositoryConnection connection,
            DPUContext context) {
        super(connection, context);
    }

    /**
     * Handler constructor used for checking data validation in repository.
     * 
     * @param connection
     *            connection to repository where RDF data are added.
     * @param checkData
     *            true value for logging the validation, false value for
     *            logging the parsing and adding process - same as using
     *            constructor {@link StatisticalHandler#StatisticalHandler(org.openrdf.repository.RepositoryConnection)
	 *                   }
     */
    public StatisticalHandler(RepositoryConnection connection, boolean checkData) {
        super(connection, checkData);
    }

    /**
     * Method calls {@link TripleCountHandler#handleStatement(org.openrdf.model.Statement)} on parent class and after successfully parsing/validating every X
     * triples
     * add the log about that, where X is the number defined by {@link #TRIPLE_LOGGED_SIZE} variable.
     * 
     * @param st
     *            Statement that will be added to repostory.
     * @throws RDFHandlerException
     *             if handler find out the problem during
     *             execution this method.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        super.handleStatement(st);

        if (getTripleCount() % TRIPLE_LOGGED_SIZE == 0 && isStatementAdded()) {

            addedCount += TRIPLE_LOGGED_SIZE;
            if (checkData) {
                logger.debug(String.format(
                        "%s TRIPLES validated.",
                        String.valueOf(addedCount)));
            } else {
                logger.debug(String.format(
                        "%s X triples extracted.",
                        String.valueOf(addedCount)));
            }

        }
    }

    /**
     * Method calls {@link TripleCountHandler#endRDF()} on parent class and add
     * all finded problems during parsing to collection {@link #parsingProblems}.
     * For getting this collection of problems you call method {@link #getFindedProblems()
	 * }.
     * 
     * @throws RDFHandlerException
     *             if handler find out the problem during
     *             execution this method.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        super.endRDF();
        if (!checkData) {
            addToParsingProblems();
        }
    }

    /**
     * Reset counting RDF triples, found errors and warnings.
     */
    @Override
    public void reset() {
        super.reset();
        addedCount = 0;
        parsingProblems.clear();
    }

    /**
     * Returns boolean value if during data parsing the handler found some
     * problems (invalid data) or not.
     * 
     * @return if during data parsing the handler found some problems (invalid
     *         data) or not.
     */
    public boolean hasFindedProblems() {
        if (hasWarnings() || hasErrors()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns string representation of all found data validation problems. It
     * is returned the empty string, if all data are valid.
     * 
     * @return string representation of all found data validation problems. It
     *         is returned the empty string, if all data are valid.
     */
    public String getFindedProblemsAsString() {
        StringBuilder result = new StringBuilder();

        if (hasWarnings()) {
            result.append("\n");
            result.append(Messages.getString("StatisticalHandler.warnings.list"));
            result.append(getWarningsAsString());

        }
        if (hasErrors()) {
            result.append("\n");
            result.append(Messages.getString("StatisticalHandler.errors.list"));
            result.append(getErorrsAsString());
        }

        return result.toString();
    }

    /**
     * Returns list as collection of all found data validation problems. It is
     * returned the empty list, if all data are valid.
     * 
     * @return list as collection of all found data validation problems. It is
     *         returned the empty list, if all data are valid.
     */
    public List<TripleProblem> getFindedProblems() {
        return getTripleProblems();
    }

    /**
     * Add found {@link TripleProblem} to collection of {@link #parsingProblems}.
     */
    private void addToParsingProblems() {
        for (TripleProblem next : getTripleProblems()) {
            if (!parsingProblems.contains(next)) {
                parsingProblems.add(next);
            }
        }
    }

    /**
     * Returns true, if some problems were found out during parsing process,
     * false otherwise.
     * 
     * @return true, if some problems were found out during parsing process,
     *         false otherwise.
     */
    public static boolean hasParsingProblems() {
        return !parsingProblems.isEmpty();
    }

    /**
     * Returns string representation of all found problems fixed by parsing {@link StatisticalHandler}.
     * 
     * @return string representation of all found problems fixed by parsing {@link StatisticalHandler}.
     */
    public static String getFoundGlobalProblemsAsString() {
        StringBuilder result = new StringBuilder();

        List<TripleProblem> warning = new LinkedList<>();
        List<TripleProblem> errors = new LinkedList<>();

        for (TripleProblem next : parsingProblems) {
            switch (next.getConflictType()) {
                case ERROR:
                    errors.add(next);
                    break;
                case WARNING:
                    warning.add(next);
                    break;

            }
        }

        if (!warning.isEmpty()) {
            result.append("\n");
            result.append(Messages.getString("StatisticalHandler.warnings.list"));
            result.append(getWarningsAsString(warning));

        }
        if (!errors.isEmpty()) {
            result.append("\n");
            result.append(Messages.getString("StatisticalHandler.errors.list"));
            result.append(getErorrsAsString(errors));
        }

        return result.toString();
    }

    /**
     * Clear list of problems found by parsing.
     */
    public static void clearParsingProblems() {
        parsingProblems.clear();
    }
}
