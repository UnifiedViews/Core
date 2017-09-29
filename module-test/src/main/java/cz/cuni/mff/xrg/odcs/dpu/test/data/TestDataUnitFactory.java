package cz.cuni.mff.xrg.odcs.dpu.test.data;

import cz.cuni.mff.xrg.odcs.rdf.repositories.GraphUrl;
import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.commons.rdf.repository.ManagableRepository;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.impl.FilesDataUnitFactory;
import eu.unifiedviews.dataunit.files.impl.ManageableWritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.ManageableWritableRDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.RDFDataUnitFactory;
import eu.unifiedviews.dataunit.relational.db.DataUnitDatabaseConnectionProvider;
import eu.unifiedviews.dataunit.relational.impl.ManageableWritableRelationalDataUnit;
import eu.unifiedviews.dataunit.relational.impl.RelationalDataUnitFactory;
import eu.unifiedviews.dataunit.relational.repository.FilesRelationalDatabase;
import eu.unifiedviews.dataunit.relational.repository.InMemoryRelationalDatabase;
import eu.unifiedviews.dataunit.relational.repository.ManagableRelationalRepository;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Create DataUnits that can be used
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
     *            for RdfDataUnit that use local storage as RDF repository.
     */
    public TestDataUnitFactory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Create RDF data unit.
     *
     * @param name
     *            Name of the DataUnit.
     * @return New {@link ManageableWritableRDFDataUnit}.
     * @throws RepositoryException
     * @throws java.io.IOException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public ManageableWritableRDFDataUnit createRDFDataUnit(String name) throws RepositoryException, IOException, DataUnitException {
        synchronized (counterLock) {
            final String id = "dpu-test_" + Integer.toString(dataUnitIdCounter++) + "_" + name;
            final String namedGraph = GraphUrl.translateDataUnitId(id);
            String pipelineId = "test_env_" + String.valueOf(this.hashCode());
            String repositoryId = pipelineId + "_" + name;

            File dataUnitWorkingDirectory = Files.createTempDirectory(FileSystems.getDefault().getPath(workingDirectory.getCanonicalPath()), repositoryId).toFile();  //+ File.separator + name

            Repository repository = initializedRepositories.get(repositoryId);
            if (repository == null) {
                repository = new SailRepository(new NativeStore(new File(workingDirectory, repositoryId)));
                repository.initialize();
                initializedRepositories.put(repositoryId, repository);
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
            String repositoryId = pipelineId + "_" + name;

            File dataUnitWorkingDirectory = Files.createTempDirectory(FileSystems.getDefault().getPath(workingDirectory.getCanonicalPath()), repositoryId).toFile();
    
            Repository repository = initializedRepositories.get(repositoryId);
            if (repository == null) {
                repository = new SailRepository(new NativeStore(new File(workingDirectory, repositoryId)));
                repository.initialize();
                initializedRepositories.put(repositoryId, repository);
            }
            return (ManageableWritableFilesDataUnit)filesFactory.create(name, namedGraph,
                    dataUnitWorkingDirectory.toURI().toString(),  createCoreServiceBus(repository));
        }
    }

    public ManageableWritableRelationalDataUnit createRelationalDataUnit(String name) throws IOException, RepositoryException, DataUnitException {
        return createRelationalDataUnit(name, ManagableRelationalRepository.Type.IN_MEMORY);
    }


    public ManageableWritableRelationalDataUnit createRelationalDataUnit(String name, ManagableRelationalRepository.Type type) throws IOException, RepositoryException, DataUnitException {
    synchronized (this.counterLock) {
        int dataUnitId = this.dataUnitIdCounter++;
        final String id = "dpu-test_" + Integer.toString(dataUnitId) + "_" + name;
        final String namedGraph = GraphUrl.translateDataUnitId(id);
        String pipelineId = "test_env_" + String.valueOf(this.hashCode());
        String repositoryId = pipelineId + "_" + name;
        String h2dbrepositoryId = pipelineId + "_" + name + "_db";

        File dataUnitWorkingDirectory = Files.createTempDirectory(FileSystems.getDefault().getPath(this.workingDirectory.getCanonicalPath()), repositoryId).toFile();

        Repository repository = this.initializedRepositories.get(repositoryId);
        if (repository == null) {
            repository = new SailRepository(new NativeStore(new File(this.workingDirectory, repositoryId)));
            repository.initialize();
            this.initializedRepositories.put(repositoryId, repository);
        }

        ManagableRelationalRepository relationalRepo = this.initializedRelationalRepositories.get(repositoryId);
        if (relationalRepo == null) {
            if (type.equals(ManagableRelationalRepository.Type.IN_MEMORY)) {
                relationalRepo = new InMemoryRelationalDatabase(null, null, dataUnitId);
            }
            else {
                relationalRepo = new FilesRelationalDatabase(null, null, dataUnitId, new File(this.workingDirectory, h2dbrepositoryId));
            }
            this.initializedRelationalRepositories.put(repositoryId, relationalRepo);
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

            @Override
            public ManagableRepository.Type getRepositoryType() {
                return ManagableRepository.Type.LOCAL_RDF;
            }

        };
    }
}
