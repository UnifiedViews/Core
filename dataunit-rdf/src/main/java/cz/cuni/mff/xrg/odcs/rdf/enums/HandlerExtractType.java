package cz.cuni.mff.xrg.odcs.rdf.enums;

import cz.cuni.mff.xrg.odcs.rdf.handlers.StatisticalHandler;

/**
 * Possibilies how to choose handler for data extraction and how to solve found
 * problems with no valid data.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum HandlerExtractType {

    /**
     * Statistical and error handler is used for data extraction - see {@link StatisticalHandler}. Pipeline execution fails, if some data are
     * invalid.
     */
    ERROR_HANDLER_FAIL_WHEN_MISTAKE,
    /**
     * Statistical and error handler is used for data extraction - see {@link StatisticalHandler}. The report message is shown after finishing
     * extraction, if some data are invalid. Problematic triples are not added
     * to repository, pipeline execution continues to execute the next DPU.
     */
    ERROR_HANDLER_CONTINUE_WHEN_MISTAKE,
    /**
     * Standard handler is used for data extraction with the information about
     * count of extracted triples. No valid triples are automatically skipped
     * during extraction and there is no error message about it. Pipeline
     * execution continues to execute the next DPU.
     */
    STANDARD_HANDLER;

    /**
     * @param useStatisticalErrorHandler
     *            If is used statistical and error
     *            handler for data parsing or not.
     * @param failWhenErrors
     *            If pipeline execution will fail, if
     *            some data are invalid, or not.
     * @return Concrete HandlerExtractType as the choise based on the specified
     *         parameters.
     */
    public static HandlerExtractType getHandlerType(
            boolean useStatisticalErrorHandler, boolean failWhenErrors) {

        if (useStatisticalErrorHandler) {
            if (failWhenErrors) {
                return HandlerExtractType.ERROR_HANDLER_FAIL_WHEN_MISTAKE;
            } else {
                return HandlerExtractType.ERROR_HANDLER_CONTINUE_WHEN_MISTAKE;
            }
        } else {
            return HandlerExtractType.STANDARD_HANDLER;
        }
    }
}
