package eu.unifiedviews.dataunit.files.impl;

import eu.unifiedviews.commons.dataunit.DataUnitFactory;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;

/**
 * Factory for files data units.
 *
 * @author Škoda Petr
 */
public class FilesDataUnitFactory implements DataUnitFactory {

    @Override
    public ManagableDataUnit create(String name, String uri, String directoryUri, CoreServiceBus coreServices) {
        return new LocalFSFilesDataUnit(name , directoryUri, uri, coreServices);
    }

}
