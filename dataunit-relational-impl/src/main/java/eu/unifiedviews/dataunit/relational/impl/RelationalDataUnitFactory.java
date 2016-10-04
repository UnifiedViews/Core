package eu.unifiedviews.dataunit.relational.impl;

import eu.unifiedviews.commons.dataunit.DataUnitFactory;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;

/**
 * Factory for creating relational data units
 */
public class RelationalDataUnitFactory implements DataUnitFactory {

    @Override
    public ManagableDataUnit create(String name, String uri, String directoryUri, CoreServiceBus coreServices) {
        return new RelationalDataUnitImpl(name, uri, directoryUri, coreServices);
    }

}
