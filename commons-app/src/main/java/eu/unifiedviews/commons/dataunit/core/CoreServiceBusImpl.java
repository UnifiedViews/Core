package eu.unifiedviews.commons.dataunit.core;

import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;

/**
 * @author Škoda Petr
 */
public class CoreServiceBusImpl implements CoreServiceBus {

    private final ConnectionSource connectionSource;

    private final FaultTolerant faultTolerant;

    private DataUnitDatabaseConnectionProvider dataUnitDatabase;

    public CoreServiceBusImpl(ConnectionSource connectionSource, FaultTolerant faultTolerant) {
        this.connectionSource = connectionSource;
        this.faultTolerant = faultTolerant;
    }

    public CoreServiceBusImpl(ConnectionSource connectionSource, FaultTolerant faultTolerant, DataUnitDatabaseConnectionProvider dataUnitDatabase) {
        this.connectionSource = connectionSource;
        this.faultTolerant = faultTolerant;
        this.dataUnitDatabase = dataUnitDatabase;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) throws IllegalArgumentException {
        if (serviceClass.isAssignableFrom(ConnectionSource.class)) {
            return (T) connectionSource;
        } else if (serviceClass.isAssignableFrom(FaultTolerant.class)) {
            return (T) faultTolerant;
        } else if (serviceClass.isAssignableFrom(DataUnitDatabaseConnectionProvider.class)) {
            return (T) this.dataUnitDatabase;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
