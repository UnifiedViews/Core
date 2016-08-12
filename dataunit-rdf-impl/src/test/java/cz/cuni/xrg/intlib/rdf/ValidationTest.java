package cz.cuni.xrg.intlib.rdf;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cz.cuni.mff.xrg.odcs.rdf.enums.SPARQLQueryType;
import cz.cuni.mff.xrg.odcs.rdf.validators.SPARQLQueryValidator;

/**
 * @author Jiri Tomes
 */
public class ValidationTest {

    /**
     * Test using contruct query in transformer.
     */
    @Test
    public void testContructQueryInTransformer() {
        String query = "prefix lex: <http://purl.org/lex#> \n"
                + "prefix frbr: <http://purl.org/vocab/frbr/core#> \n"
                + "prefix dcterms: <http://purl.org/dc/terms/> \n"
                + "prefix fn: <http://www.w3.org/2005/xpath-functions/#>  \n"
                + "\n"
                + "construct {\n"
                + " ?eoz a frbr:Expression. \n"
                + " ?eoz frbr:realizationOf ?oz . \n"
                + " ?eoz dcterms:title ?oztitle . \n"
                + " ?eoz dcterms:valid ?ucinnost . \n"
                + " ?eoz dcterms:language \"cs\" . \n"
                + " ?y lex:definesChange [ a lex:Update ; lex:changeResult ?eoz ] . \n"
                + "} \n"
                + "WHERE { "
                + "{ SELECT (iri(fn:concat(str(?oz),'/version/cz/',str(?ucinnost))) as ?eoz) "
                + "?oz ?y ?oztitle ?ucinnost where { ?y lex:aktivni-novelizuje ?oz . "
                + "?oz dcterms:title ?oztitle . ?y dcterms:valid ?ucinnost . } } }";

        SPARQLQueryValidator validator = new SPARQLQueryValidator(query,
                SPARQLQueryType.CONSTRUCT);

        boolean isValid = validator.isQueryValid();
        assertTrue(isValid);
    }
}
