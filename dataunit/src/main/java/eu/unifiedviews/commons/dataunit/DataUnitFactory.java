package eu.unifiedviews.commons.dataunit;

import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;

/**
 * Interface for {@link ManagableDataUnit}.
 *
 * @author Škoda Petr
 */
public interface DataUnitFactory {

    /**
     *
     * @param name
     * @param uri Uri of data unit.
     * @param directoryUri Working directory for data unit.
     * @param coreServices
     * @return Newly create data unit. (not loaded).
     */
    ManagableDataUnit create(String name, String uri, String directoryUri, CoreServiceBus coreServices);

}
