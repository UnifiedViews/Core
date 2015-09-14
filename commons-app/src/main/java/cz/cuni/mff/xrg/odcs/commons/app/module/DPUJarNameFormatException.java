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
package cz.cuni.mff.xrg.odcs.commons.app.module;

/**
 * Exception indicating wrong DPU's jar name format
 * 
 * @author mvi
 *
 */
public class DPUJarNameFormatException extends Exception {

    private static final long serialVersionUID = -1114519630027656944L;

    /**
     * 
     * @param cause
     *          Cause of the {@link DPUJarNameFormatException}
     */
    public DPUJarNameFormatException(String cause) {
        super(cause);
    }
    
    /**
     * 
     * @param message
     *          Description of the error
     * @param cause
     *          Cause of the {@link DPUJarNameFormatException}
     */
    public DPUJarNameFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
