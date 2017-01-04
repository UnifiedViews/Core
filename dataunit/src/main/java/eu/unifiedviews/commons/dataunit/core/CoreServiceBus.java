package eu.unifiedviews.commons.dataunit.core;

/**
 * Interface for services provided by core to dataunit module.
 *
 * @author Škoda Petr
 */
public interface CoreServiceBus {

    /**
     * 
     * @param <T>
     * @param serviceClass
     * @return Instance of requested service.
     * @throws IllegalArgumentException If service is not available.
     */
    <T> T getService(Class<T> serviceClass) throws IllegalArgumentException;

}
