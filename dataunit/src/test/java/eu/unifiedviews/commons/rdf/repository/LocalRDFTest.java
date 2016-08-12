package eu.unifiedviews.commons.rdf.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import eu.unifiedviews.dataunit.DataUnitException;
import junit.framework.Assert;

/**
 *
 * @author Škoda Petr
 */
public class LocalRDFTest {

    @Test
    public void createAndDestroy() throws IOException, DataUnitException, RDFException {
        final RepositoryFactory factory = new RepositoryFactory();
        final Path rootDir = Files.createTempDirectory(FileUtils.getTempDirectory().toPath(), "uv-dataUnit-");
        // Make sure that the directory is empty.
        Files.deleteIfExists(rootDir);
        rootDir.toFile().mkdirs();
        // Create.
        final String directory = rootDir.toAbsolutePath().toString() + File.separator + "1";
        ManagableRepository repository = factory.create(1l, ManagableRepository.Type.LOCAL_RDF, directory);
        // Check that tere is a directory with data.
        Assert.assertTrue(rootDir.toFile().list().length > 0);
        // Delete.
        repository.delete();
        // Check that the directory is empty.
        Assert.assertTrue(rootDir.toFile().list().length == 0);
        // And clean up.
        Files.deleteIfExists(rootDir);
    }

}
