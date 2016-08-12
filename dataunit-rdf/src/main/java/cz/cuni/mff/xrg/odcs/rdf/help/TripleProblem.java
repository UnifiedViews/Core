package cz.cuni.mff.xrg.odcs.rdf.help;

import org.openrdf.model.Statement;

import cz.cuni.mff.xrg.odcs.rdf.enums.ParsingConfictType;

/**
 * Responsible for describing invalid RDF triples during data parsing- type of
 * found problem, place where invalidation found out, described message about
 * that.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class TripleProblem {

    private String message;

    private int line;

    private int column;

    private ParsingConfictType type;

    private Statement statement;

    /**
     * Create new instance of {@link TripleProblem} from describe message,
     * number of line and column, where problem was found out and it´s type.
     * 
     * @param message
     *            String desription of problem
     * @param line
     *            number of line where problem was found
     * @param column
     *            number of column where problem was found
     * @param type
     *            type of found problem
     */
    public TripleProblem(String message, int line, int column,
            ParsingConfictType type) {
        this.message = message;
        this.line = line;
        this.column = column;
        this.type = type;

    }

    /**
     * Set RDF problem statement that describes found problem.
     * 
     * @param statement
     *            RDF statement that describes found problem.
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    /**
     * Returns string description message of found problem.
     * 
     * @return string description message of found problem.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns number of line, where problem was found out.
     * 
     * @return number of line, where problem was found out.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns number of column, where problem was found out.
     * 
     * @return number of column, where problem was found out.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns type of finded problem.
     * 
     * @return type of finded problem.
     */
    public ParsingConfictType getConflictType() {
        return type;
    }

    /**
     * Returns RDF problem statement that describes found problem.
     * 
     * @return RDF problem statement that describes found problem.
     */
    public Statement getStatement() {
        return statement;
    }
}
