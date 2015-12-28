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
package cz.cuni.mff.xrg.odcs.rdf.interfaces;

import java.util.List;

import cz.cuni.mff.xrg.odcs.rdf.help.TripleProblem;

/**
 * Validator is responsible for right data validation.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public interface DataValidator {

    /**
     * Method for detection right data syntax.
     * 
     * @return true, if data are valid, false otherwise.
     */
    public boolean areDataValid();

    /**
     * String message describes syntax problem of data validation.
     * 
     * @return empty string, when all data are valid.
     */
    public String getErrorMessage();

    /**
     * Returns list of {@link TripleProblem} describes invalid triples and its
     * cause. If all data are valid return empty list.
     * 
     * @return List of {@link TripleProblem} describes invalid triples and its
     *         cause. If all data are valid return empty list.
     */
    public List<TripleProblem> getFindedProblems();
}
