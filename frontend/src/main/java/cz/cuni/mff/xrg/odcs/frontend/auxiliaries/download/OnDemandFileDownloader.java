package cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download;

import java.io.IOException;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;

/**
 * This specializes {@link FileDownloader} in a way, such that both the file
 * name and content can be determined on-demand, i.e. when the user has clicked
 * the component.
 */
public class OnDemandFileDownloader extends FileDownloader {

    private static final long serialVersionUID = 1L;

    private final OnDemandStreamResource onDemandStreamResource;

    /**
     * Constructor.
     * 
     * @param onDemandStreamResource
     *            Resource to use for download.
     */
    public OnDemandFileDownloader(
            OnDemandStreamResource onDemandStreamResource) {
        super(new StreamResource(onDemandStreamResource, ""));
        this.onDemandStreamResource = onDemandStreamResource;
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request,
            VaadinResponse response, String path)
            throws IOException {
        getResource().setFilename(onDemandStreamResource.getFilename());
        return super.handleConnectorRequest(request, response, path);
    }

    private StreamResource getResource() {
        return (StreamResource) this.getResource("dl");
    }
}
