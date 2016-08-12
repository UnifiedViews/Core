package eu.unifiedviews.dataunit.files.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eu.unifiedviews.commons.dataunit.core.ConnectionSource;
import eu.unifiedviews.commons.dataunit.core.CoreServiceBus;
import eu.unifiedviews.commons.dataunit.core.FaultTolerant;
import eu.unifiedviews.commons.rdf.repository.ManagableRepository;
import eu.unifiedviews.commons.rdf.repository.RDFException;
import eu.unifiedviews.commons.rdf.repository.RepositoryFactory;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;

/**
 *
 * @author Škoda Petr
 */
public class LocalFSFilesDataUnitTest {

    private Path rootDir;

    private Path rootDirFile;

    private ManagableRepository repository;

    @Before
    public void prepare() throws IOException, DataUnitException, RDFException {
        final RepositoryFactory factory = new RepositoryFactory();
        rootDir = Files.createTempDirectory(FileUtils.getTempDirectory().toPath(), "uv-filesDataUnit-");
        rootDirFile = (new File(rootDir.toFile(), "storage")).toPath();
        final String directory = rootDir.toAbsolutePath().toString() + File.separator + "1";
        repository = factory.create(1l, ManagableRepository.Type.LOCAL_RDF, directory);
    }

    @After
    public void cleanUp() throws IOException, DataUnitException, RDFException {
        repository.delete();
        Files.deleteIfExists(rootDirFile);
        // There should be no data as we called clear.
        Assert.assertTrue("Failed to delete data directory", Files.deleteIfExists(rootDir));
    }

    @Test
    public void addFilesAndIterate() throws DataUnitException {
        final FilesDataUnitFactory factory  = new FilesDataUnitFactory();

        final ManageableWritableFilesDataUnit dataUnit = (ManageableWritableFilesDataUnit)factory.create(
                "test",
                "http://unifiedviews.eu/test/write",
                rootDirFile.toFile().toURI().toString(),
                new CoreServiceBus() {
                    @Override
                    public <T> T getService(Class<T> serviceClass) throws IllegalArgumentException {
                        // Simple test implementation of bus service
                        if (serviceClass.isAssignableFrom(ConnectionSource.class)) {
                            return (T)repository.getConnectionSource();
                        } else if (serviceClass.isAssignableFrom(FaultTolerant.class)) {
                            return (T) new FaultTolerant() {

                                @Override
                                public void execute(FaultTolerant.Code codeToExecute)
                                        throws RepositoryException, DataUnitException {
                                    final RepositoryConnection conn =
                                            repository.getConnectionSource().getConnection();
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
                });

        // Add initial files.
        dataUnit.addNewFile("myFileA");
        dataUnit.addNewFile("myFileB");
        // Just check for size.
        int counter = 0;
        FilesDataUnit.Iteration iter = dataUnit.getIteration();
        while (iter.hasNext()) {
            FilesDataUnit.Entry entry = iter.next();
            ++counter;
        }
        Assert.assertEquals(2, counter);
        // Call clear add new file and again check.
        dataUnit.clear();
        dataUnit.addNewFile("myFileC");
        counter = 0;
        iter = dataUnit.getIteration();
        while (iter.hasNext()) {
            FilesDataUnit.Entry entry = iter.next();
            ++counter;
        }
        Assert.assertEquals(1, counter);
        // Clear and release.
        dataUnit.clear();
        dataUnit.release();
    }


}
