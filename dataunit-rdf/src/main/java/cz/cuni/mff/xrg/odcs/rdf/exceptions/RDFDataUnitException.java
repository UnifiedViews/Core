/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.rdf.exceptions;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Exception is responsible for global problems related with RDF data unit.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class RDFDataUnitException extends DataUnitException {

    /**
     * Create a new instance of {@link RDFDataUnitException} with the empty
     * message.
     */
    public RDFDataUnitException() {
        super("");
    }

    /**
     * Create new instance of {@link RDFDataUnitException} with the specific
     * message.
     * 
     * @param message
     *            String value of described message
     */
    public RDFDataUnitException(String message) {
        super(message);
    }

    /**
     * Create new instance of {@link RDFDataUnitException} with the cause of
     * throwing this exception.
     * 
     * @param cause
     *            The cause of throwing exception
     */
    public RDFDataUnitException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link RDFDataUnitException} with the specific
     * message and the cause of throwing this exception.
     * 
     * @param message
     *            String value of described message
     * @param cause
     *            The cause of throwing exception
     */
    public RDFDataUnitException(String message, Throwable cause) {
        super(message, cause);
    }
}
