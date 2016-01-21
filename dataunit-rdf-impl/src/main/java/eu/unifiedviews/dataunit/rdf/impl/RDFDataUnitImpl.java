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
package eu.unifiedviews.dataunit.rdf.impl;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.commons.dataunit.AbstractWritableMetadataDataUnit;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.i18n.Messages;

/**
 * Abstract class provides common parent methods for RDFDataUnitImpl implementation.
 */
class RDFDataUnitImpl extends AbstractWritableMetadataDataUnit implements ManageableWritableRDFDataUnit {

    private static final Logger LOG = LoggerFactory.getLogger(RDFDataUnitImpl.class);

    /**
     * Base URI available to the user.
     */
    private final URI baseDataGraphURI;

    private static final String DATA_GRAPH_BINDING = "dataGraph";

    private static final String UPDATE_EXISTING_GRAPH = ""
            + "DELETE "
            + "{ "
            + "?s <" + RDFDataUnit.PREDICATE_DATAGRAPH_URI + "> ?o "
            + "} "
            + "INSERT "
            + "{ "
            + "?s <" + RDFDataUnit.PREDICATE_DATAGRAPH_URI + "> ?" + DATA_GRAPH_BINDING + " "
            + "} "
            + "WHERE "
            + "{"
            + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " . "
            + "?s <" + RDFDataUnit.PREDICATE_DATAGRAPH_URI + "> ?o "
            + "}";

    public RDFDataUnitImpl(String dataUnitName, String workingDirectoryURI,
            String writeContextString, CoreServiceBus coreServices) {
        super(dataUnitName, writeContextString, coreServices);

        baseDataGraphURI = new URIImpl(writeContextString + "/user/");
    }

    @Override
    public ManagableDataUnit.Type getType() {
        return ManagableDataUnit.Type.RDF;
    }

    @Override
    public boolean isType(ManagableDataUnit.Type dataUnitType) {
        return getType().equals(dataUnitType);
    }

    @Override
    public RDFDataUnit.Iteration getIteration() throws DataUnitException {
        checkForMultithreadAccess();

        if (connectionSource.isRetryOnFailure()) {
            return new RDFDataUnitIterationEager(this, connectionSource, faultTolerant);
        } else {
            return new RDFDataUnitIterationLazy(this);
        }
    }

    @Override
    public URI getBaseDataGraphURI() throws DataUnitException {
        return baseDataGraphURI;
    }

    @Override
    public void addExistingDataGraph(final String symbolicName, final URI existingDataGraphURI) throws DataUnitException {
        checkForMultithreadAccess();

        final URI entrySubject = this.creatEntitySubject();
        try {
            faultTolerant.execute(new FaultTolerant.Code() {

                @Override
                public void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException {
                    addEntry(entrySubject, symbolicName, connection);
                    final ValueFactory valueFactory = connection.getValueFactory();
                    // Add file uri.
                    connection.add(
                            entrySubject,
                            valueFactory.createURI(RDFDataUnitImpl.PREDICATE_DATAGRAPH_URI),
                            existingDataGraphURI,
                            getMetadataWriteGraphname()
                            );
                }
            });
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitImpl.repository.problem"), ex);
        }
    }

    @Override
    public URI addNewDataGraph(final String symbolicName) throws DataUnitException {
        checkForMultithreadAccess();

        final URI entrySubject = this.creatEntitySubject();
        try {
            faultTolerant.execute(new FaultTolerant.Code() {

                @Override
                public void execute(RepositoryConnection connection) throws RepositoryException, DataUnitException {
                    //adds triple with symbolic name
                    addEntry(entrySubject, symbolicName, connection);
                    final ValueFactory valueFactory = connection.getValueFactory();
                    //adds triple with data graph URI 
                    connection.add(
                            entrySubject,
                            valueFactory.createURI(RDFDataUnitImpl.PREDICATE_DATAGRAPH_URI),
                            entrySubject,
                            getMetadataWriteGraphname()
                            );
                }
            });
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitImpl.repository.problem"), ex);
        }
        return entrySubject;
    }

    @Override
    public void updateExistingDataGraph(String symbolicName, URI newDataGraphURI) throws DataUnitException {
        checkForMultithreadAccess();

        RepositoryConnection connection = null;
        RepositoryResult<Statement> result = null;
        try {
            // TODO michal.klempa think of not connecting everytime
            connection = this.connectionSource.getConnection();
            connection.begin();
            ValueFactory valueFactory = connection.getValueFactory();
            Literal symbolicNameLiteral = valueFactory.createLiteral(symbolicName);
            try {
                Update update = connection.prepareUpdate(QueryLanguage.SPARQL, UPDATE_EXISTING_GRAPH);
                update.setBinding(SYMBOLIC_NAME_BINDING, symbolicNameLiteral);
                update.setBinding(DATA_GRAPH_BINDING, newDataGraphURI);

                DatasetImpl dataset = new DatasetImpl();
                dataset.addDefaultGraph(getMetadataWriteGraphname());
                dataset.setDefaultInsertGraph(getMetadataWriteGraphname());
                dataset.addDefaultRemoveGraph(getMetadataWriteGraphname());

                update.setDataset(dataset);
                update.execute();
            } catch (MalformedQueryException | UpdateExecutionException ex) {
                // Not possible
                throw new DataUnitException(ex);
            }
            connection.commit();
        } catch (RepositoryException ex) {
            throw new DataUnitException(Messages.getString("RDFDataUnitImpl.adding.data.error"), ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error when closing connection", ex);
                    // eat close exception, we cannot do anything clever here
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close", ex);
                    // eat close exception, we cannot do anything clever here
                }
            }
        }
    }

}
