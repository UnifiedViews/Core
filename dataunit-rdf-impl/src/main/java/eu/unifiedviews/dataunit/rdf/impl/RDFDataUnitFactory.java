package eu.unifiedviews.dataunit.rdf.impl;

import eu.unifiedviews.commons.dataunit.DataUnitFactory;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;

/**
 * Factory for RDF data units.
 *
 * @author Škoda Petr
 */
public class RDFDataUnitFactory implements DataUnitFactory {

    @Override
    public ManagableDataUnit create(String name, String uri, String directoryUri, CoreServiceBus coreServices) {
        return new RDFDataUnitImpl(name, directoryUri, uri, coreServices);
    }

}
