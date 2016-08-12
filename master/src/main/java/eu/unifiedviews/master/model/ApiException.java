package eu.unifiedviews.master.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Exception of Master API.
 */
public class ApiException extends WebApplicationException {
    private static final long serialVersionUID = -5793273224388941908L;

    private static final Logger LOG = LoggerFactory.getLogger(ApiException.class);

    /**
     * When thrown, this exception send response to client with error details.
     *
     * @param status HTTP status code to give to error.
     * @param errorMessage Localized error message. Can be shown in GUI.
     * @param technicalMessage Technical error message. Served for dev purposes.
     */
    public ApiException(Status status, String errorMessage, String technicalMessage) {
        super(Response.status(status)
                .entity(new ErrorResponse(errorMessage, technicalMessage)).type(MediaType.APPLICATION_JSON_TYPE).build());
        LOG.error("API exception raised: status: {}, errorMessage: {}, technicalMessage: {}", status, errorMessage, technicalMessage, this);
    }
}
