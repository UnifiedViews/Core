package eu.unifiedviews.dataunit.relational.impl;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;

/**
 * Holds basic information about the single database table
 */
public class RelationalDataUnitEntryImpl implements RelationalDataUnit.Entry {

    private final String symbolicName;
    
    private final String tableName;
    
    public RelationalDataUnitEntryImpl(String symbolicName, String tableName) {
        this.symbolicName = symbolicName;
        this.tableName = tableName;
    }
    
    @Override
    public String getSymbolicName() throws DataUnitException {
        return this.symbolicName;
    }

    @Override
    public String getTableName() throws DataUnitException {
        return this.tableName;
    }

}
