/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.rdf.handlers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.rdf.enums.ParsingConfictType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFCancelException;
import cz.cuni.mff.xrg.odcs.rdf.help.TripleProblem;
import cz.cuni.mff.xrg.odcs.rdf.i18n.Messages;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.TripleCounter;
import eu.unifiedviews.dpu.DPUContext;

/**
 * Class for counting of extracted triples from SPARQL endpoint or given file.
 * It is used very often in case throwing the exception, when no triples were
 * extracted from SPARQL endpoint.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class TripleCountHandler extends RDFInserter implements TripleCounter {

    /**
     * Responsible for log event in class {@link TripleCountHandler} and it´s
     * subclasses.
     */
    private static final Logger logger = LoggerFactory.getLogger(TripleCountHandler.class);

    private List<TripleProblem> warnings = new ArrayList<>();

    private List<TripleProblem> errors = new ArrayList<>();

    private TripleProblem nextProblem = null;

    private boolean hasProblem = false;

    private boolean isStatementAdded = false;

    private DPUContext context;

    /**
     * The variable is responsible for creating message used by {@link #logger}.
     * True value is set, if handler is used by data validator for checking RDF
     * data. False value is set, if handler is used for RDF data parsing and
     * adding data to repository.
     */
    protected boolean checkData;

    /**
     * Default handler constructor for parsing and adding data to repository.
     * 
     * @param connection
     *            connection to repository where RDF data are added.
     */
    public TripleCountHandler(RepositoryConnection connection) {
        super(connection);
        this.context = null;
        this.checkData = false;
    }

    /**
     * Default handler constructor for parsing and adding data from SPARQL
     * endpoint to repository.
     * 
     * @param connection
     *            connection to repository where RDF data are added.
     * @param context
     *            DPU context for checking if parsing was cancelled or
     *            not.
     */
    public TripleCountHandler(RepositoryConnection connection,
            DPUContext context) {
        super(connection);
        this.context = context;
        this.checkData = false;

    }

    /**
     * Handler constructor used for checking data validation in repository.
     * 
     * @param connection
     *            connection to repository where RDF data are added.
     * @param checkData
     *            true value for logging the validation, false value for
     *            logging the parsing and adding process - same as using
     *            constructor null null null null null null null null
     *            null null null null null null null null null null null
     *            null null null null null null null null null null null
     *            {@link TripleCountHandler#TripleCountHandler(org.openrdf.repository.RepositoryConnection)
	 *                   }.
     */
    public TripleCountHandler(RepositoryConnection connection, boolean checkData) {
        super(connection);
        this.checkData = checkData;
    }

    private long tripleCount = 0;

    /**
     * If given statement contains valid RDF data then the method calls {@link RDFInserter#handleStatement(org.openrdf.model.Statement)} on
     * parent class, otherwise this problem statement is added to the specific
     * problem collection of {@link TripleProblem}.
     * 
     * @param st
     *            Statement that will be added to repostory.
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        try {
            if (isParsingCanceled()) {
                throw new RDFCancelException(Messages.getString("TripleCountHandler.excraction.calcelled")); //$NON-NLS-1$

            } else if (!hasProblem) {
                super.handleStatement(st);
                tripleCount++;
                isStatementAdded = true;
            } else {
                isStatementAdded = false;
                hasProblem = false;

                nextProblem.setStatement(st);
                switch (nextProblem.getConflictType()) {
                    case ERROR:
                        errors.add(nextProblem);
                        break;
                    case WARNING:
                        warnings.add(nextProblem);
                        break;
                }

            }
        } catch (RDFCancelException e) {
            throw new RDFCancelException(e.getMessage(), e);

        } catch (RDFHandlerException e) {
            logger.debug(
                    "\n" + "Triple contains problems:" //$NON-NLS-1$ //$NON-NLS-2$
                            + "\n Subject:" + st.getSubject().toString() //$NON-NLS-1$
                            + "\n Predicate:" + st.getPredicate().toString() //$NON-NLS-1$
                            + "\n Object:" + st.getObject().toString()); //$NON-NLS-1$

        }
    }

    /**
     * Method calls {@link RDFInserter#startRDF()} on parent class and write log
     * message about it.
     * 
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        try {
            super.startRDF();
            if (checkData) {
                logger.info("Data validation started"); //$NON-NLS-1$
            } else {
                logger.info("Parsing started"); //$NON-NLS-1$
            }
        } catch (RDFHandlerException e) {
            if (checkData) {
                logger.info("Starting data validation FAILED"); //$NON-NLS-1$
            } else {
                logger.info("Starting parsing FAILED"); //$NON-NLS-1$
            }
            throw new RDFHandlerException(e.getMessage(), e);
        }
    }

    /**
     * Method calls {@link RDFInserter#endRDF()} on parent class and write log
     * message about it.
     * 
     * @throws RDFHandlerException
     *             if handler find out problem during execution
     *             this method.
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        try {
            super.endRDF();
            if (checkData) {
                logger.info("Data validation successfully"); //$NON-NLS-1$
                logger.info("TOTAL VALIDATED:" + getTripleCount() + " triples"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                logger.info("Parsing ended successfully"); //$NON-NLS-1$
                logger.info("TOTAL ADDED:" + getTripleCount() + " triples"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (RDFHandlerException e) {
            logger.error(e.getMessage());
            if (checkData) {
                logger.info("Ending data validating FAILED"); //$NON-NLS-1$
                logger.info("TOTAL VALIDATED:" + getTripleCount() + " triples"); //$NON-NLS-1$ //$NON-NLS-2$

            } else {
                logger.info("Ending parsing FAILED"); //$NON-NLS-1$
                logger.info("TOTAL ADDED:" + getTripleCount() + " triples"); //$NON-NLS-1$ //$NON-NLS-2$

            }
        }
    }

    /**
     * Add next found error during data parsing.
     * 
     * @param message
     *            description of found error
     * @param line
     *            number of line where error was find out
     * @param column
     *            number of column where error was find out
     */
    public void addError(String message, int line, int column) {
        nextProblem = new TripleProblem(message, line, column,
                ParsingConfictType.ERROR);
        hasProblem = true;
    }

    /**
     * Add next found warning during data parsing.
     * 
     * @param message
     *            describe of found warning
     * @param line
     *            number of line where warning was find out
     * @param column
     *            number of column where warning was find out
     */
    public void addWarning(String message, int line, int column) {
        nextProblem = new TripleProblem(message, line, column,
                ParsingConfictType.WARNING);
        hasProblem = true;
    }

    /**
     * Returns true if last parsed statement was added to repository, false
     * otherwise.
     * 
     * @return true if last parsed statement was added to repository,false
     *         otherwise.
     */
    protected boolean isStatementAdded() {
        return isStatementAdded;
    }

    /**
     * Returns true, if parsing proccess was canceled by user, false otherwise.
     * 
     * @return true, if parsing proccess was canceled by user, false otherwise.
     */
    private boolean isParsingCanceled() {
        if (context != null && context.canceled()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns string warning message contains the description of all problems
     * from given collection.
     * 
     * @param warningsList
     *            Collection of {@link TripleProblem} to create string
     *            message
     * @return String warning message contains the description of all problems
     *         from given collection.
     */
    protected static String getWarningsAsString(List<TripleProblem> warningsList) {
        StringBuilder result = new StringBuilder();

        int warningCount = 0;

        for (TripleProblem next : warningsList) {
            warningCount++;
            result.append(getDescribedProblem(next, warningCount));
        }

        return result.toString();
    }

    /**
     * Returns string warning message contains the description of all problems
     * from collection {@link #warnings}.
     * 
     * @return String warning message contains the description of all problems
     *         from collection {@link #warnings}.
     */
    protected String getWarningsAsString() {
        return getWarningsAsString(warnings);
    }

    /**
     * Returns string error message contains the description of all problems
     * from given collection.
     * 
     * @param errorsList
     *            Collection of {@link TripleProblem} to create string
     *            message
     * @return String error message contains the description of all problems
     *         from given collection.
     */
    protected static String getErorrsAsString(List<TripleProblem> errorsList) {

        StringBuilder result = new StringBuilder();

        int errorCount = 0;

        for (TripleProblem next : errorsList) {
            errorCount++;
            result.append(getDescribedProblem(next, errorCount));
        }

        return result.toString();

    }

    /**
     * Returns string warning message contains the description of all problems
     * from collection {@link #errors}.
     * 
     * @return String warning message contains the description of all problems
     *         from collection {@link #errors}.
     */
    protected String getErorrsAsString() {
        return getErorrsAsString(errors);
    }

    /**
     * Create string description from given {@link TripleProblem}.
     * 
     * @param next
     *            Instance of {@link TripleProblem } used for creating
     *            description from that.
     * @param errorCount
     *            number of found problems used in description.
     * @return string description from given {@link TripleProblem}.
     */
    private static String getDescribedProblem(TripleProblem next, int errorCount) {

        Statement statement = next.getStatement();
        String problemType = next.getConflictType().name();

        String problem = "\n" + errorCount + "] " + problemType + Messages.getString("TripleCountHandler.in.triple") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + Messages.getString("TripleCountHandler.subject") + statement.getSubject().toString() //$NON-NLS-1$
                + Messages.getString("TripleCountHandler.predicate") + statement.getPredicate().toString() //$NON-NLS-1$
                + Messages.getString("TripleCountHandler.object") + statement.getObject().toString() //$NON-NLS-1$
                + Messages.getString("TripleCountHandler.problem.message") + next.getMessage() //$NON-NLS-1$
                + Messages.getString("TripleCountHandler.source.line") + next.getLine(); //$NON-NLS-1$

        return problem;
    }

    /**
     * Returns true if handler find out some errors, false otherwise.
     * 
     * @return true if handler find out some errors, false otherwise.
     */
    protected boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns true if handler find out some warnings, false otherwise.
     * 
     * @return true if handler find out some warnings, false otherwise.
     */
    protected boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Returns list as collection of {@link TripleProblem} contains all found
     * problems - warning and errors.
     * 
     * @return List as collection of {@link TripleProblem} contains all found
     *         problems - warning and errors.
     */
    protected List<TripleProblem> getTripleProblems() {

        List<TripleProblem> problems = new ArrayList<>();
        problems.addAll(warnings);
        problems.addAll(errors);

        return problems;
    }

    /**
     * Returns count of extracted triples.
     * 
     * @return count of extracted triples.
     */
    @Override
    public long getTripleCount() {
        return tripleCount;
    }

    /**
     * Reset counting triples, found errors and warnings.
     */
    @Override
    public void reset() {
        tripleCount = 0;
        hasProblem = false;
        warnings.clear();
        errors.clear();

    }

    /**
     * Returns true if there is no triples, false otherwise.
     * 
     * @return true if there is no triples, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return tripleCount == 0;
    }

    /**
     * Set graphs where data are inserted using this handler.
     * 
     * @param graphs
     *            Collection of graphs for inserting data using this handler.
     */
    public void setGraphContext(Resource... graphs) {
        if (graphs != null) {
            super.enforceContext(graphs);

        }
    }
}
