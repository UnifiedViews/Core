package cz.cuni.mff.xrg.odcs.rdf.help;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.i18n.Messages;

/**
 * Responsible for test/controll parameters needed for method RDF.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class ParamController {

    private static final Logger logger = LoggerFactory.getLogger(ParamController.class);

    /**
     * Test if given param is null or not.
     * 
     * @param param
     *            Object you can test
     * @param message
     *            String message that could be logged if param is null.
     * @throws RDFException
     *             if given param is null.
     */
    public static void testNullParameter(Object param, String message) throws RDFException {
        if (param == null) {
            logger.debug(message);
            throw new RDFException(message);
        }
    }

    /**
     * Test if given param is empty or not.
     * 
     * @param param
     *            Object you can test
     * @param message
     *            String message that could be logged if param is empty.
     * @throws RDFException
     *             if given param is empty.
     */
    public static void testEmptyParameter(Object param, String message) throws RDFException {

        if (param != null) {
            boolean isEmpty = param.toString().isEmpty();

            if (param instanceof List) {
                isEmpty = ((List) param).isEmpty();
            }

            if (isEmpty) {
                logger.debug(message);
                throw new RDFException(message);
            }
        }

    }

    /**
     * Test if given param is positive long number or not.
     * 
     * @param param
     *            Value you can test
     * @param message
     *            String message that could be logged if param is not
     *            positive number.
     * @throws RDFException
     *             if given param is not positive number.
     */
    public static void testPositiveParameter(long param, String message) throws RDFException {
        if (param <= 0) {
            logger.debug(message);
            throw new RDFException(message);
        }
    }

    /**
     * Test if given param is valid URL or not.
     * 
     * @param endpointURL
     *            Object you can test
     * @throws RDFException
     *             if given URL is null, started with "http://" or
     *             contains white spaces.
     */
    public static void testEndpointSyntax(URL endpointURL) throws RDFException {

        String message = null;

        if (endpointURL != null) {
            final String endpointName = endpointURL.toString().toLowerCase();

            if (!endpointName.startsWith("http://")) {
                message = Messages.getString("ParamController.endpoint.prefix") + " \"http://\".";
            } else if (endpointName.contains(" ")) {
                message = Messages.getString("ParamController.endpoint.contains.whitespaces");
            }
            if (message != null) {
                logger.debug(message);
                throw new RDFException(message);
            }
        } else {
            message = Messages.getString("ParamController.url.is.null");
            logger.debug(message);
            throw new RDFException(message);

        }
    }
}
