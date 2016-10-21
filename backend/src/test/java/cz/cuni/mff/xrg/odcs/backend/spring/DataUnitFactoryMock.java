package cz.cuni.mff.xrg.odcs.backend.spring;

import static org.mockito.Mockito.mock;

import java.io.File;

import cz.cuni.mff.xrg.odcs.commons.app.dataunit.DataUnitFactory;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;

/**
 * Dummy {@link DataUnitFactory}. Does not create any real data unit.
 * 
 * @author Petyr
 */
public class DataUnitFactoryMock implements DataUnitFactory {

    @Override
    public ManagableDataUnit create(ManagableDataUnit.Type type,
            Long executionId,
            String dataUnitUri,
            String dataUnitName,
            File dataUnitDirectory) {
        // just return mocked object
        return mock(ManagableDataUnit.class);
    }

}
