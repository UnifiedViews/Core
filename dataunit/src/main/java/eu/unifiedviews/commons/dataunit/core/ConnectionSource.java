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
package eu.unifiedviews.commons.dataunit.core;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Provides access to connections into working rdf repository.
 * 
 * @author Škoda Petr
 */
public interface ConnectionSource {

    /**
     * @return Connection into working repository.
     * @throws RepositoryException
     */
    RepositoryConnection getConnection() throws RepositoryException;

    /**
     * This is set to false for LocalRDF, RemoteRDF, InMemoryRDF and set to true for Virtuoso. Specified e.g. in
     * {@link eu.unifiedviews.commons.rdf.repository.LocalRDF}
     * 
     * @return True if operation should retry on RDF failure.
     */
    boolean isRetryOnFailure();

    /**
     * @return Value factory for working repository.
     */
    ValueFactory getValueFactory();

}
