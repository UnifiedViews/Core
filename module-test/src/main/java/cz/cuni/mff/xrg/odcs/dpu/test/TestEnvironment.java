package cz.cuni.mff.xrg.odcs.dpu.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationContainer;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.annotation.AnnotationGetter;
import cz.cuni.mff.xrg.odcs.dpu.test.context.TestContext;
import cz.cuni.mff.xrg.odcs.dpu.test.data.TestDataUnitFactory;
import eu.unifiedviews.commons.dataunit.ManagableDataUnit;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.files.impl.ManageableWritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.ManageableWritableRDFDataUnit;
import eu.unifiedviews.dataunit.relational.WritableRelationalDataUnit;
import eu.unifiedviews.dataunit.relational.impl.ManageableWritableRelationalDataUnit;
import eu.unifiedviews.dpu.DPU;

/**
 * Hold environment used to test DPU.
 *
 * @author Petyr
 */
public class TestEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(
            TestEnvironment.class);

    /**
     * Context used for testing.
     */
    private final TestContext context;

    /**
     * Working directory.
     */
    private final File workingDirectory;

    /**
     * Directories for input {@link ManagableDataUnit}s.
     */
    private final HashMap<String, ManagableDataUnit> inputDataUnits = new HashMap<>();

    /**
     * Directories for output {@link ManagableDataUnit}s.
     */
    private final HashMap<String, ManagableDataUnit> outputDataUnits = new HashMap<>();

    private final HashMap<String, ManagableDataUnit> customDataUnits = new HashMap<>();

    /**
     * Factory for {@link DataUnit}s classes.
     */
    private final TestDataUnitFactory testDataUnitFactory;

    /**
     * Create test environment. As working directory is used temp file.
     */
    public TestEnvironment() {
        try {
            workingDirectory = Files.createTempDirectory(null).toFile();
            LOG.info("Creating {} with workingDirectory {}", this.getClass().getName(), workingDirectory.toString());
            File contextRootDirectory = new File(workingDirectory, "context");
            contextRootDirectory.mkdirs();
            this.context = new TestContext(contextRootDirectory);

            File dataUnitFactoryWorkingDirectory = new File(workingDirectory, "dataUnits");
            dataUnitFactoryWorkingDirectory.mkdirs();
            this.testDataUnitFactory = new TestDataUnitFactory(dataUnitFactoryWorkingDirectory);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // - - - - - - - - - methods for environment setup - - - - - - - - - //
    /**
     * Set path that is used like jar-path during execution. This value will
     * be used during test execution if DPU asks for it.
     *
     * @param jarPath
     *            path to the jar file.
     */
    public void setJarPath(String jarPath) {
        context.setJarPath(jarPath);
    }

    /**
     * Set time for last execution. This value will be used during test
     * execution if DPU asks for it.
     *
     * @param lastExecution
     *            Time of last execution.
     */
    public void setLastExecution(Date lastExecution) {
        context.setLastExecution(lastExecution);
    }

    /**
     * Set given {@link ManagableDataUnit} as an input. If there already is
     * another value for given name it is overridden. The old {@link ManagableDataUnit} is not released.
     *
     * @param name
     *            Name of dataUnit.
     * @param dataUnit
     *            DataUnit to add as input.
     */
    public void addInput(String name, ManagableDataUnit dataUnit) {
        inputDataUnits.put(name, dataUnit);
    }

    /**
     * Set {@link ManagableDataUnit} where should the data, from output
     * DataUnit, be stored. If there is other setting for given name then it is
     * overwritten.
     * The data in given {@link ManagableDataUnit} may not be accessible after
     * call of {@link #release()}.
     * If there already is another value for given name it is overridden. The
     * old {@link ManagableDataUnit} is not released.
     *
     * @param name
     *            Name of dataUnit.
     * @param dataUnit
     *            DataUnit to add as output.
     */
    public void addOutput(String name, ManagableDataUnit dataUnit) {
        outputDataUnits.put(name, dataUnit);
    }

    /**
     * Create {@link RDFDataUnit} which is just returned to test developer for use.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created {@link RDFDataUnit}.
     * @throws RepositoryException
     * @throws java.io.IOException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public WritableRDFDataUnit createRdfFDataUnit(String name) throws RepositoryException, IOException, DataUnitException {
        ManageableWritableRDFDataUnit rdf = testDataUnitFactory.createRDFDataUnit(name);
        customDataUnits.put(name, rdf);
        return rdf;
    }

    /**
     * Create input {@link RDFDataUnit} that is used in test environment.
     *
     * @param name
     *            Name of DataUnit.
     * @param useVirtuoso
     *            If true then Virtuoso is used as a storage.
     * @return Created input {@link RDFDataUnit}.
     * @throws RepositoryException
     * @throws java.io.IOException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public WritableRDFDataUnit createRdfInput(String name, boolean useVirtuoso) throws RepositoryException, IOException, DataUnitException {
        ManageableWritableRDFDataUnit rdf = testDataUnitFactory.createRDFDataUnit(name);
        addInput(name, rdf);
        return rdf;
    }

    /**
     * Create output {@link RDFDataUnit}, add it to the test environment and
     * return it.
     *
     * @param name
     *            Name of DataUnit.
     * @param useVirtuoso
     *            If true then Virtuoso is used as a storage.
     * @return Created output {@link RDFDataUnit}.
     * @throws RepositoryException
     * @throws java.io.IOException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public WritableRDFDataUnit createRdfOutput(String name, boolean useVirtuoso)
            throws RepositoryException, IOException, DataUnitException {
        ManageableWritableRDFDataUnit rdf = testDataUnitFactory.createRDFDataUnit(name);
        addOutput(name, rdf);
        return rdf;
    }

    /**
     * Create {@link WritableFilesDataUnit} which is just returned to test developer for use.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created {@link WritableFilesDataUnit}.
     * @throws RepositoryException
     * @throws DataUnitException
     * @throws IOException
     */
    public WritableFilesDataUnit createFilesFDataUnit(String name) throws RepositoryException, IOException, DataUnitException {
        ManageableWritableFilesDataUnit rdf = testDataUnitFactory.createFilesDataUnit(name);
        customDataUnits.put(name, rdf);
        return rdf;
    }

    /**
     * Create input {@link WritableFilesDataUnit} that is used in test environment.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created input {@link WritableFilesDataUnit}.
     * @throws RepositoryException
     * @throws DataUnitException
     * @throws IOException
     */
    public WritableFilesDataUnit createFilesInput(String name) throws RepositoryException, IOException, DataUnitException {
        ManageableWritableFilesDataUnit rdf = testDataUnitFactory.createFilesDataUnit(name);
        addInput(name, rdf);
        return rdf;
    }

    /**
     * Create output {@link WritableFilesDataUnit}, add it to the test environment and
     * return it.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created output {@link WritableFilesDataUnit}.
     * @throws RepositoryException
     * @throws DataUnitException
     * @throws IOException
     */
    public WritableFilesDataUnit createFilesOutput(String name)
            throws RepositoryException, IOException, DataUnitException {
        ManageableWritableFilesDataUnit rdf = testDataUnitFactory.createFilesDataUnit(name);
        addOutput(name, rdf);
        return rdf;
    }

    /**
     * Create files data unit, add it as an input and return reference to it. The
     * files data unit is created in temp directory and data from given resource
     * path are added to the root.
     *
     * @param name
     *            Name of DataUnit.
     * @param resourceName
     *            Path to the resources.
     * @return Created input {@link FilesDataUnit}.
     * @throws eu.unifiedviews.dataunit.DataUnitException
     * @throws IOException
     * @throws RepositoryException
     */
    public FilesDataUnit createFilesInputFromResource(String name,
            String resourceName)
            throws DataUnitException, RepositoryException, IOException {
        File dir = new File(FileUtils.getTempDirectory(),
                "odcs-file-test-" + Long.toString(System.nanoTime()));
        dir.mkdirs();

        ManageableWritableFilesDataUnit filesDataUnit = testDataUnitFactory.createFilesDataUnit(name);
        // add from resources
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(resourceName);

        // check ..
        if (url == null) {
            throw new DataUnitException("Missing input file in resource for: "
                    + resourceName);
        }

        File resourceRoot = new File(url.getPath());

        //if the resource is a directory:
        if (resourceRoot.isDirectory()) {
            for (File toAdd : FileUtils.listFiles(resourceRoot, FileFileFilter.FILE, TrueFileFilter.INSTANCE)) {
                filesDataUnit.addExistingFile(toAdd.getAbsolutePath(), toAdd.toURI().toASCIIString());
            }
        } else {
            filesDataUnit.addExistingFile(resourceRoot.getAbsolutePath(), resourceRoot.toURI().toASCIIString());
        }

        addInput(name, filesDataUnit);
        return filesDataUnit;
    }

    /**
     * Create {@link WritableRelationalDataUnit} which is just returned to test developer for use.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created {@link WritableRelationalDataUnit}.
     * @throws RepositoryException
     * @throws DataUnitException
     * @throws IOException
     */
    public WritableRelationalDataUnit createRelationalDataUnit(String name) throws RepositoryException, IOException, DataUnitException {
        ManageableWritableRelationalDataUnit relational = this.testDataUnitFactory.createRelationalDataUnit(name);
        this.customDataUnits.put(name, relational);

        return relational;
    }

    /**
     * Create input {@link WritableRelationalDataUnit} that is used in test environment.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created input {@link WritableRelationalDataUnit}.
     * @throws RepositoryException
     * @throws DataUnitException
     * @throws IOException
     */
    public WritableRelationalDataUnit createRelationalInput(String name) throws RepositoryException, IOException, DataUnitException {
        ManageableWritableRelationalDataUnit relational = this.testDataUnitFactory.createRelationalDataUnit(name);
        addInput(name, relational);

        return relational;
    }

    /**
     * Create output {@link WritableRelationalDataUnit} that is used in test environment.
     *
     * @param name
     *            Name of DataUnit.
     * @return Created input {@link WritableRelationalDataUnit}.
     * @throws RepositoryException
     * @throws DataUnitException
     * @throws IOException
     */
    public WritableRelationalDataUnit createRelationalOutput(String name)
            throws RepositoryException, IOException, DataUnitException {
        ManageableWritableRelationalDataUnit relational = this.testDataUnitFactory.createRelationalDataUnit(name);
        addOutput(name, relational);
        return relational;
    }

    // - - - - - - - - - method for test execution - - - - - - - - - //
    /**
     * Run given DPU in the test environment. The test environment is not reset
     * before or after the test. If the test working directory should be deleted
     * then is deleted at the end of this method same as all the {@link DataUnit}s
     * Any thrown exception is passed. In every case the {@link #release()} method must be called in order to release test data.
     *
     * @param dpuInstance
     *            Instance of DPU to run.
     * @return False if the execution failed by sending error message
     * @throws java.lang.Exception
     */
    public boolean run(DPU dpuInstance) throws Exception {
        // prepare dpu instance - set annotations
        connectDataUnits(dpuInstance);

        // execute
        dpuInstance.execute(context);

        return context.isPublishedError();
    }

    /**
     * Delete testing data and release {@link ManagableDataUnit}s. Unused {@link ManagableDataUnit} are not deleted.
     */
    public void release() {
        // release all DataUnits ..
        try {
            for (ManagableDataUnit item : inputDataUnits.values()) {
                if (item != null) {
                    item.clear();
                    item.release();
                }
            }
            for (ManagableDataUnit item : outputDataUnits.values()) {
                if (item != null) {
                    item.clear();
                    item.release();
                }
            }
            for (ManagableDataUnit item : customDataUnits.values()) {
                if (item != null) {
                    item.clear();
                    item.release();
                }
            }
        } catch (DataUnitException ex) {
            throw new RuntimeException(ex);
        }

        // delete working directory ..
        org.apache.commons.io.FileUtils.deleteQuietly(workingDirectory);
    }

    // - - - - - - - - - methods for examining the results - - - - - - - - - //
    /**
     * Return context used during tests. Return null before call of {@link #run(cz.cuni.mff.xrg.odcs.commons.dpu.DPU)} method.
     *
     * @return Context used during testing.
     */
    public TestContext getContext() {
        return context;
    }

    // - - - - - - - - - - - - methods for dpu setup - - - - - - - - - - - - //
    private ManagableDataUnit getInputDataUnit(Field field, String name) {
        if (inputDataUnits.containsKey(name)) {
            // check type
            Class<?> fc = field.getType();
            Class<?> rc = inputDataUnits.get(name).getClass();

            if (fc.isAssignableFrom(rc)) {
                // class match
                return inputDataUnits.get(name);
            } else {
                // miss match ..
                return null;
            }
        }
        return null;
    }

    private ManagableDataUnit getOutputDataUnit(Field field, String name) {
        if (outputDataUnits.containsKey(name)) {
            // check type
            Class<?> fc = field.getType();
            Class<?> rc = outputDataUnits.get(name).getClass();

            if (fc.isAssignableFrom(rc)) {
                // class match
                return outputDataUnits.get(name);
            } else {
                // miss match ..
                return null;
            }
        }
        return null;
    }

    /**
     * Connect data units from {@link #inputDataUnits} and {@link #outputDataUnits} to the given DPU instance.
     *
     * @param dpuInstance
     *            DPU instance object.
     * @throws Exception
     */
    private void connectDataUnits(DPU dpuInstance) throws Exception {
        // add inputs
        List<AnnotationContainer<DataUnit.AsInput>> inputAnnotations = AnnotationGetter
                .getAnnotations(dpuInstance, DataUnit.AsInput.class);
        for (AnnotationContainer<DataUnit.AsInput> item : inputAnnotations) {
            ManagableDataUnit dataUnit = getInputDataUnit(item.getField(),
                    item.annotation.name());
            if (dataUnit == null && !item.getAnnotation().optional()) {
                // missing non option dataUnit
                throw new Exception(
                        "Test failure missing import mandatory DataUnit: "
                                + item.getAnnotation().name());
            }

            item.getField().set(dpuInstance, dataUnit);
            // ...
        }

        // add outputs
        List<AnnotationContainer<DataUnit.AsOutput>> outputAnnotations = AnnotationGetter
                .getAnnotations(dpuInstance, DataUnit.AsOutput.class);
        for (AnnotationContainer<DataUnit.AsOutput> item : outputAnnotations) {

            ManagableDataUnit dataUnit = getOutputDataUnit(item.getField(),
                    item.annotation.name());
            item.getField().set(dpuInstance, dataUnit);
            if (dataUnit == null && !item.getAnnotation().optional()) {
                throw new Exception("Can not bind 'null' to output DataUnit: "
                        + item.getAnnotation().name());
            }
            // ...
        }
    }

}
