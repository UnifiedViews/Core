package cz.cuni.mff.xrg.odcs.dpu.test.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

import cz.cuni.mff.xrg.odcs.rdf.repositories.GraphUrl;
import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.impl.FilesDataUnitFactory;
import eu.unifiedviews.dataunit.files.impl.ManageableWritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.ManageableWritableRDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.RDFDataUnitFactory;
import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;
import eu.unifiedviews.dataunit.relational.impl.ManageableWritableRelationalDataUnit;
import eu.unifiedviews.dataunit.relational.impl.RelationalDataUnitFactory;
import eu.unifiedviews.dataunit.relational.repository.InMemoryRelationalDatabase;
import eu.unifiedviews.dataunit.relational.repository.ManagableRelationalRepository;

/**
 * Create {@link cz.cuni.mff.xrg.odcs.commons.data.DataUnit}s that can be used
 * in {@link cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment}.
 *
 * @author Petyr
 */
public class TestDataUnitFactory {

    /**
     * Counter for dataUnits id's and directories.
     */
    private int dataUnitIdCounter = 0;

    private final Object counterLock = new Object();

    /**
     * Working directory.
     */
    private final File workingDirectory;

    private final Map<String, Repository> initializedRepositories = new HashMap<>();

    private final Map<String, ManagableRelationalRepository> initializedRelationalRepositories = new HashMap<>();

    private final RDFDataUnitFactory rdfFactory = new RDFDataUnitFactory();

    private final FilesDataUnitFactory filesFactory = new FilesDataUnitFactory();

    private final RelationalDataUnitFactory relationalFactory = new RelationalDataUnitFactory();

    /**
     * Create a {@link TestDataUnitFactory} that use given directory as working
     * directory.
     *
     * @param workingDirectory
     *            Directory where to create working subdirectories
     *            for {@link cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit} that use local storage as RDF repository.
     */
    public TestDataUnitFactory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Create RDF data unit.
     *
     * @param name
     *            Name of the DataUnit.
     * @return New {@link ManagableRdfDataUnit}.
     * @throws RepositoryException
     * @throws java.io.IOException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public ManageableWritableRDFDataUnit createRDFDataUnit(String name) throws RepositoryException, IOException, DataUnitException {
        synchronized (counterLock) {
            final String id = "dpu-test_" + Integer.toString(dataUnitIdCounter++) + "_" + name;
            final String namedGraph = GraphUrl.translateDataUnitId(id);
            String pipelineId = "test_env_" + String.valueOf(this.hashCode());

            File dataUnitWorkingDirectory = Files.createTempDirectory(FileSystems.getDefault().getPath(workingDirectory.getCanonicalPath()), pipelineId).toFile();

            Repository repository = initializedRepositories.get(pipelineId);
            if (repository == null) {
                repository = new SailRepository(new NativeStore(new File(workingDirectory, pipelineId)));
                repository.initialize();
                initializedRepositories.put(pipelineId, repository);
            }
            return (ManageableWritableRDFDataUnit)rdfFactory.create(name, namedGraph,
                    dataUnitWorkingDirectory.toURI().toString(),  createCoreServiceBus(repository));
        }
    }

    public ManageableWritableFilesDataUnit createFilesDataUnit(String name) throws RepositoryException, IOException, DataUnitException {
        synchronized (counterLock) {
            final String id = "dpu-test_" + Integer.toString(dataUnitIdCounter++) + "_" + name;
            final String namedGraph = GraphUrl.translateDataUnitId(id);
            String pipelineId = "test_env_" + String.valueOf(this.hashCode());

            File dataUnitWorkingDirectory = Files.createTempDirectory(FileSystems.getDefault().getPath(workingDirectory.getCanonicalPath()), pipelineId).toFile();
    
            Repository repository = initializedRepositories.get(pipelineId);
            if (repository == null) {
                repository = new SailRepository(new NativeStore(new File(workingDirectory, pipelineId)));
                repository.initialize();
                initializedRepositories.put(pipelineId, repository);
            }
            return (ManageableWritableFilesDataUnit)filesFactory.create(name, namedGraph,
                    dataUnitWorkingDirectory.toURI().toString(),  createCoreServiceBus(repository));
        }
    }

    public ManageableWritableRelationalDataUnit createRelationalDataUnit(String name) throws IOException, RepositoryException, DataUnitException {
        synchronized (this.counterLock) {
            int dataUnitId = this.dataUnitIdCounter++;
            final String id = "dpu-test_" + Integer.toString(dataUnitId) + "_" + name;
            final String namedGraph = GraphUrl.translateDataUnitId(id);
            String pipelineId = "test_env_" + String.valueOf(this.hashCode());

            File dataUnitWorkingDirectory = Files.createTempDirectory(FileSystems.getDefault().getPath(this.workingDirectory.getCanonicalPath()), pipelineId).toFile();

            Repository repository = this.initializedRepositories.get(pipelineId);
            if (repository == null) {
                repository = new SailRepository(new NativeStore(new File(this.workingDirectory, pipelineId)));
                repository.initialize();
                this.initializedRepositories.put(pipelineId, repository);
            }

            ManagableRelationalRepository relationalRepo = this.initializedRelationalRepositories.get(pipelineId);
            if (relationalRepo == null) {
                relationalRepo = new InMemoryRelationalDatabase(null, null, dataUnitId);
                this.initializedRelationalRepositories.put(pipelineId, relationalRepo);
            }

            return (ManageableWritableRelationalDataUnit) this.relationalFactory.create(name, namedGraph,
                    dataUnitWorkingDirectory.toURI().toString(), createCoreServiceBus(repository, relationalRepo));

        }
    }

    private CoreServiceBus createCoreServiceBus(final Repository repository) {
        return new CoreServiceBus() {
            @Override
            public <T> T getService(Class<T> serviceClass) throws IllegalArgumentException {
                // Simple test implementation of bus service
                if (serviceClass.isAssignableFrom(ConnectionSource.class)) {
                    return (T)createConnectionSource(repository);
                } else if (serviceClass.isAssignableFrom(FaultTolerant.class)) {
                    return (T) new FaultTolerant() {

                        @Override
                        public void execute(FaultTolerant.Code codeToExecute)
                                throws RepositoryException, DataUnitException {
                            final RepositoryConnection conn =
                                    createConnectionSource(repository).getConnection();
                            try {
                                codeToExecute.execute(conn);
                            } finally {
                                conn.close();
                            }
                        }

                    };
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
    }

    private CoreServiceBus createCoreServiceBus(final Repository repository, final ManagableRelationalRepository relationalRepo) {
        return new CoreServiceBus() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T getService(Class<T> serviceClass) throws IllegalArgumentException {
                // Simple test implementation of bus service
                if (serviceClass.isAssignableFrom(ConnectionSource.class)) {
                    return (T) createConnectionSource(repository);
                } else if (serviceClass.isAssignableFrom(FaultTolerant.class)) {
                    return (T) new FaultTolerant() {

                        @Override
                        public void execute(FaultTolerant.Code codeToExecute)
                                throws RepositoryException, DataUnitException {
                            final RepositoryConnection conn =
                                    createConnectionSource(repository).getConnection();
                            try {
                                codeToExecute.execute(conn);
                            } finally {
                                conn.close();
                            }
                        }

                    };
                } else if (serviceClass.isAssignableFrom(DataUnitDatabaseConnectionProvider.class)) {
                    return (T) relationalRepo.getDatabaseConnectionProvider();
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
    }

    private ConnectionSource createConnectionSource(final Repository repository) {
        return new ConnectionSource() {

            @Override
            public RepositoryConnection getConnection() throws RepositoryException {
                return repository.getConnection();
            }

            @Override
            public boolean isRetryOnFailure() {
                return false;
            }

            @Override
            public ValueFactory getValueFactory() {
                return repository.getValueFactory();
            }

        };
    }
}
