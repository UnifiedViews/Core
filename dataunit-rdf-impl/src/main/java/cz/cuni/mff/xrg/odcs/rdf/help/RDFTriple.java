package cz.cuni.mff.xrg.odcs.rdf.help;

/**
 * Class for representing RDF Triple for browsing in frontend.
 * 
 * @author Bogo
 * @author Jiri Tomes
 */
public class RDFTriple {

    private int id;

    private String subject;

    private String predicate;

    private String object;

    /**
     * Constructor with complete information about triple.
     * 
     * @param id
     *            Id of triple for indexing in container.
     * @param subject
     *            {@link String} with subject of triple.
     * @param predicate
     *            {@link String} with predicate of triple.
     * @param object
     *            {@link String} with object of triple.
     */
    public RDFTriple(int id, String subject, String predicate, String object) {
        this.id = id;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Returns ID for indexing in container.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns string value of subject.
     * 
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns string value of predicate.
     * 
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * Returns string value of object.
     * 
     * @return the object
     */
    public String getObject() {
        return object;
    }
}
