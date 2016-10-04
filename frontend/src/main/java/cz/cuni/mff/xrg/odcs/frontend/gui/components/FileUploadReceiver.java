package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload.Receiver;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Upload selected file to template directory
 * 
 * @author Maria Kukhar
 */
public class FileUploadReceiver implements Receiver {
    private static final long serialVersionUID = -7085312769546042292L;

    private Logger LOG = LoggerFactory.getLogger(FileUploadReceiver.class);

    private File file;

    private File parentDir;
    
    /**
     * 
     * @param filename
     * @param MIMEType
     * @return
     */
    @Override
    public OutputStream receiveUpload(final String filename,
            final String MIMEType) {
        try {
            //create template directory
            parentDir = Files.createTempDirectory("jarDPU").toFile();
        } catch (IOException e) {
            String message = Messages.getString("FileUploadReceiver.temp.dir.fail");
            LOG.error(message);
            Notification.show(message, e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return null;
        }

        try {
            file = new File(parentDir, filename);
            file.createNewFile();
            FileOutputStream fstream = new FileOutputStream(file);
            return fstream;
        } catch (FileNotFoundException e) {
            String msg = Messages.getString("FileUploadReceiver.file.open");
            LOG.error(msg);
            new Notification(msg, e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
            return null;
        } catch (IOException e) {
            String msg = Messages.getString("FileUploadReceiver.file.create.fail");
            LOG.error(msg);
            Notification.show(msg, e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return null;
        }

    }
    
    /**
     * Get uploaded file.
     * 
     * @return uploaded file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get parent dir.
     * 
     * @return parent directory
     */
    public File getParentDir() {
        return parentDir;
    }
}
