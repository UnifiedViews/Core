package cz.cuni.mff.xrg.odcs.rdf.enums;

/**
 * Format types could be used for output for SPARQL select query.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum SelectFormatType {

    /**
     * The result of SPARQL select is saved in RDF/XML format.
     */
    XML,
    /**
     * The result of SPARQL select is saved in CSV format.
     */
    CSV,
    /**
     * The result of SPARQL select is saved in JSON format.
     */
    JSON,
    /**
     * The result of SPARQL select is saved in TSV format.
     */
    TSV;
}
