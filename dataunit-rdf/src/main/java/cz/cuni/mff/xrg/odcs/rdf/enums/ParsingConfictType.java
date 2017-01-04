package cz.cuni.mff.xrg.odcs.rdf.enums;

/**
 * Type of problems we want to find out in case of parsing RDF triples using RDF
 * handler.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum ParsingConfictType {

    /**
     * Define the warning type in case of parsing RDF triples.
     */
    WARNING,
    /**
     * Define the error type in case of parsing RDF triples.
     */
    ERROR;

    /**
     * Returns name of {@link ParsingConfictType}. The name starts with capital
     * first letter (Other letters are small).
     * 
     * @return Name of {@link ParsingConfictType} The name starts with capital
     *         first letter (Other letters are small).
     */
    @Override
    public String toString() {
        String typeName = this.name().toLowerCase();

        String firstLetter = typeName.substring(0, 1).toUpperCase();
        String restWord = typeName.substring(1);

        return firstLetter + restWord;
    }
}
