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
package cz.cuni.mff.xrg.odcs.rdf.help;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * Define behavior of result ordered SPARQL SELECT queries as iterator over {@link TupleQueryResult}.
 * 
 * @author tomasknap
 */
@Deprecated
public interface OrderTupleQueryResult extends TupleQueryResult {

    /**
     * Returns true if next element is available, false otherwise.
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using.
     * 
     * @return true if next element is available, false otherwise.
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public boolean hasNext() throws QueryEvaluationException;

    /**
     * Returns next {@link BindingSet} element
     * For no problem behavior check you setting "MaxSortedRows" param in your
     * virtuoso.ini file before using.
     * 
     * @return next {@link BindingSet} element
     * @throws QueryEvaluationException
     *             An exception indicating that the
     *             evaluation of a query failed.
     */
    @Override
    public BindingSet next() throws QueryEvaluationException;
}
