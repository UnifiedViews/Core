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
package cz.cuni.mff.xrg.odcs.rdf.enums;

/**
 * Type of problems we want to find out in case of parsing RDF triples using RDF
 * handler.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum ParsingConfictType {

    /**
     * Define the warning type in case of parsing RDF triples.
     */
    WARNING,
    /**
     * Define the error type in case of parsing RDF triples.
     */
    ERROR;

    /**
     * Returns name of {@link ParsingConfictType}. The name starts with capital
     * first letter (Other letters are small).
     * 
     * @return Name of {@link ParsingConfictType} The name starts with capital
     *         first letter (Other letters are small).
     */
    @Override
    public String toString() {
        String typeName = this.name().toLowerCase();

        String firstLetter = typeName.substring(0, 1).toUpperCase();
        String restWord = typeName.substring(1);

        return firstLetter + restWord;
    }
}
