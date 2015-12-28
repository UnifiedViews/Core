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

/**
 * Interface provides information about extracted triples.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public interface TripleCounter {

    /**
     * Returns count of extracted triples.
     * 
     * @return count of extracted triples.
     */
    public long getTripleCount();

    /**
     * Returns true if there is no triples, false otherwise.
     * 
     * @return true if there is no triples, false otherwise.
     */
    public boolean isEmpty();

    /**
     * Set count of extracted triples to 0.
     */
    public void reset();
}
