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

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;

/**
 * Exception is thrown when during {@link RDFParser#parse(java.io.InputStream, java.lang.String)} method is the
 * execution cancelled manually by user.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public class RDFCancelException extends RDFHandlerException {

    /**
     * 
     */
    private static final long serialVersionUID = 6289348156771806583L;

    /**
     * Create new instance of {@link RDFCancelException} with the specific
     * message.
     * 
     * @param msg
     *            String value of described message
     */
    public RDFCancelException(String msg) {
        super(msg);
    }

    /**
     * Create new instance of {@link RDFCancelException} with the cause of
     * throwing this exception.
     * 
     * @param cause
     *            The cause of throwing exception
     */
    public RDFCancelException(Throwable cause) {
        super(cause);
    }

    /**
     * Create new instance of {@link RDFCancelException} with the specific
     * message and the cause of throwing this exception.
     * 
     * @param msg
     *            String value of described message
     * @param cause
     *            The cause of throwing exception
     */
    public RDFCancelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
