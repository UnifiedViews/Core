package eu.unifiedviews.dataunit.rdf.impl;

import org.eclipse.rdf4j.model.IRI;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

/**
 * Holds basic informations about a single file.
 *
 * @author Michal Klempa
 */
public class RDFDataUnitEntryImpl implements RDFDataUnit.Entry {
    
    private final String symbolicName;

    private final IRI dataGraphURI;

    public RDFDataUnitEntryImpl(String symbolicName, IRI dataGraphURI) {
        this.symbolicName = symbolicName;
        this.dataGraphURI = dataGraphURI;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public IRI getDataGraphURI() throws DataUnitException {
        return dataGraphURI;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[symbolicName=" + symbolicName + ",dataGraphURI=" + String.valueOf(dataGraphURI) + "]";
    }

}
