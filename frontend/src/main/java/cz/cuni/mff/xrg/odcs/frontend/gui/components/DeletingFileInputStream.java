package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import cz.cuni.mff.xrg.odcs.commons.app.resource.ResourceManager;

/**
 * Deletes File after closing the stream. It is intended to use with temp files.
 * 
 * inspired by: https://vaadin.com/forum/#!/thread/159584/
 * 
 * @author mvi
 *
 */
public class DeletingFileInputStream extends FileInputStream {

    protected File file;

    public DeletingFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        ResourceManager.cleanupQuietly(file);
    }
}
