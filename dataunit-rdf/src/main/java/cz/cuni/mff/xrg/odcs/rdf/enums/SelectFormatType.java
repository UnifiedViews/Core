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
 * Format types could be used for output for SPARQL select query.
 * 
 * @author Jiri Tomes
 */
@Deprecated
public enum SelectFormatType {

    /**
     * The result of SPARQL select is saved in RDF/XML format.
     */
    XML,
    /**
     * The result of SPARQL select is saved in CSV format.
     */
    CSV,
    /**
     * The result of SPARQL select is saved in JSON format.
     */
    JSON,
    /**
     * The result of SPARQL select is saved in TSV format.
     */
    TSV;
}
