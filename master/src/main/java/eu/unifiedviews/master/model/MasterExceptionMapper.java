package eu.unifiedviews.master.model;

import eu.unifiedviews.master.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class MasterExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(MasterExceptionMapper.class);

    /**
     * Map an exception to a {@link Response}. Returning
     * {@code null} results in a {@link Response.Status#NO_CONTENT}
     * response. Throwing a runtime exception results in a
     * {@link Response.Status#INTERNAL_SERVER_ERROR} response.
     *
     * @param throwable the throwable to map to a response.
     * @return a response mapped from the supplied exception.
     */
    @Override
    public Response toResponse(Throwable throwable) {
        LOG.error("Exception raised:", throwable);

        ErrorResponse response = new ErrorResponse();

        // uncomment this part, if you want to send whole stacktrace
        //StringWriter errorStackTrace = new StringWriter();
        //throwable.printStackTrace(new PrintWriter(errorStackTrace));
        //response.setTechnicalMessage(errorStackTrace.toString());

        // send just original throwable message
        response.setErrorMessage(Messages.getString("general.exception"));
        response.setTechnicalMessage(throwable.getMessage());

        int statusCode = determineHttpStatus(throwable);
        return Response.status(statusCode)
                .entity(response)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private int determineHttpStatus(Throwable ex) {
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse().getStatus();
        } else {
            return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
    }
}
