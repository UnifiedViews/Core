package eu.unifiedviews.dataunit.files.impl;

import eu.unifiedviews.dataunit.files.FilesDataUnit;

/**
 * Holds basic informations about a single file.
 * 
 * @author Michal Klempa
 */
class FilesDataUnitEntryImpl implements FilesDataUnit.Entry {

    private final String symbolicName;

    private final String fileURIString;

    public FilesDataUnitEntryImpl(String symbolicName, String fileURIString) {
        this.symbolicName = symbolicName;
        this.fileURIString = fileURIString;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public String getFileURIString() {
        return fileURIString;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[symbolicName=" + symbolicName + ",filesystemURI=" + fileURIString + "]";
    }
   
}
