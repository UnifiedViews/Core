package eu.unifiedviews.dataunit.rdf.impl;

import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
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
import eu.unifiedviews.commons.rdf.repository.ManagableRepository;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.i18n.Messages;

/**
 * Abstract class provides common parent methods for RDFDataUnitImpl implementation.
 */
class RDFDataUnitImpl extends AbstractWritableMetadataDataUnit implements ManageableWritableRDFDataUnit {

    protected static final String GET_DATA_GRAPHS = "SELECT ?toDelete WHERE { "
            + "GRAPH ?" + WRITE_CONTEXT_BINDING + " { ?x <" + PREDICATE_DATAGRAPH_URI + "> ?toDelete } }" ;  //http://unifiedviews.eu/DataUnit/MetadataDataUnit/RDFDataUnit/dataGraphURI

    private static final Logger LOG = LoggerFactory.getLogger(RDFDataUnitImpl.class);

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

    /**
     * Base URI available to the user.
     */
    private final URI baseDataGraphURI;

    public RDFDataUnitImpl(String dataUnitName, String workingDirectoryURI,
            String directoryUri, CoreServiceBus coreServices) {
        super(dataUnitName, directoryUri, coreServices);

        baseDataGraphURI = new URIImpl(directoryUri + "/user/");
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
        if (connectionSource.isRetryOnFailure()) {
            return new RDFDataUnitIterationEager(this, connectionSource, faultTolerant);
        } else {
            return new RDFDataUnitIterationEager(this, connectionSource, faultTolerant);
        }
    }

    @Override
    public URI getBaseDataGraphURI() throws DataUnitException {
        return baseDataGraphURI;
    }

    @Override
    public void addExistingDataGraph(final String symbolicName, final URI existingDataGraphURI) throws DataUnitException {
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

    @Override
    /**
     * Clears RDF data graphs - the content of the graph. Relevant for GraphDB e.g. to clear the graph content.
     * Uses super() for removing the generated metadata from the RDF store
     * Not relevant for localRDF as in this case it is easier to just throw away the file created.
     */
    public void clear() throws DataUnitException {

        if (!this.connectionSource.getRepositoryType().equals(ManagableRepository.Type.LOCAL_RDF)) {
        //not called for RDF local native store as in this case it is easier just to delete the file (instead of deleting the graphs)
            RepositoryConnection connection = null;
            TupleQueryResult result = null;
            try {
                connection = this.getConnection();
                //get all data graphs from the writeContext graphs
                TupleQuery tupleQuery = null;
                try {
                    tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, GET_DATA_GRAPHS);
                } catch (MalformedQueryException e) {
                    throw new DataUnitException(e);
                }
                tupleQuery.setBinding(WRITE_CONTEXT_BINDING, writeContext);

                try {
                    result = tupleQuery.evaluate();

                    while (result.hasNext()) {

                        BindingSet bindingSet = result.next();
                        Value dataGraphToBeDeleted = bindingSet.getValue("toDelete");
                        Resource dataGraphToBeDeletedResource = (Resource) dataGraphToBeDeleted;

                        // Delete graph with entries.
                        connection.clear(dataGraphToBeDeletedResource);
                        //connection.prepareUpdate(QueryLanguage.SPARQL, "CLEAR SILENT GRAPH <" + dataGraphToBeDeleted.toString() + "> ").execute();
                    }

                } catch (QueryEvaluationException ex) {
                    throw new DataUnitException(ex);
                }
            } catch (RepositoryException ex) {
                throw new DataUnitException("Could not delete RDF data graphs.", ex);
            } finally {
                if (result != null) {
                    try {
                        result.close();
                    } catch (QueryEvaluationException ex) {
                        LOG.warn("Cannot close result set.", ex);
                    }
                }
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (RepositoryException ex) {
                    LOG.warn("Error when closing connection.", ex);
                }
            }

        }

        super.clear();
    }

}
