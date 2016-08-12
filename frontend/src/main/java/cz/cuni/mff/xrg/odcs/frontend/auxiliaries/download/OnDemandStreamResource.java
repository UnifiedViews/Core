package cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download;

import com.vaadin.server.StreamResource.StreamSource;

/**
 * Provide both the {@link StreamSource} and the filename in an on-demand way.
 */
public interface OnDemandStreamResource extends StreamSource {

    /**
     * Get file name.
     * 
     * @return File name.
     */
    String getFilename();
}
