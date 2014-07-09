package cz.cuni.mff.xrg.odcs.files.copyhelper;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;


public interface CopyHelper extends AutoCloseable {
    void copyMetadataAndContents(String symbolicName) throws DataUnitException;
    void copyMetadata(String symbolicName) throws DataUnitException;
    @Override
    public void close() throws DataUnitException;
}
