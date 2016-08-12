package eu.unifiedviews.dataunit.files.impl;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
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
import eu.unifiedviews.dataunit.files.FilesDataUnit;

/**
 * Implementation of {@link ManageableWritableFilesDataUnit} on local repository with utilisation of
 * RDF storage for metadata.
 *
 * @author Škoda Petr
 */
class LocalFSFilesDataUnit extends AbstractWritableMetadataDataUnit implements ManageableWritableFilesDataUnit {

    /**
     * How many characters from a symbolic name will be used in newly added file name.
     */
    private static final int MAX_USER_FILENAME_LENGTH = 10;

    private static final Logger LOG = LoggerFactory.getLogger(LocalFSFilesDataUnit.class);

    private final File workingDirectory;

    private final String workingDirectoryURI;

    private static final String FILE_URI_BINDING = "fileUri";

    private static final String UPDATE_EXISTING_FILE = ""
                + "DELETE "
                + "{ "
                + "?s <" + FilesDataUnit.PREDICATE_FILE_URI + "> ?o "
                + "} "
                + "INSERT "
                + "{ "
                + "?s <" + FilesDataUnit.PREDICATE_FILE_URI + "> ?" + FILE_URI_BINDING + " "
                + "} "
                + "WHERE "
                + "{"
                + "?s <" + MetadataDataUnit.PREDICATE_SYMBOLIC_NAME + "> ?" + SYMBOLIC_NAME_BINDING + " . "
                + "?s <" + FilesDataUnit.PREDICATE_FILE_URI + "> ?o "
                + "}";

    public LocalFSFilesDataUnit(String dataUnitName, String workingDirectoryURI,
            String writeContextString, CoreServiceBus coreServices) {
        super(dataUnitName, writeContextString, coreServices);

        this.workingDirectory = new File(java.net.URI.create(workingDirectoryURI));
        workingDirectory.mkdirs();
        this.workingDirectoryURI = workingDirectoryURI;
    }

    //DataUnit interface
    @Override
    public ManagableDataUnit.Type getType() {
        return ManagableDataUnit.Type.FILES;
    }

    //DataUnit interface
    @Override
    public boolean isType(ManagableDataUnit.Type dataUnitType) {
        return this.getType().equals(dataUnitType);
    }

    //FilesDataUnit interface
    @Override
    public FilesDataUnit.Iteration getIteration() throws DataUnitException {
        checkForMultithreadAccess();
        if (connectionSource.isRetryOnFailure()) {
            // Is not safe.
            return new WritableFileIterationEager(this, connectionSource, faultTolerant);
        } else {
            return new WritableFileIterationLazy(this);
        }
    }

    //WritableFilesDataUnit interface
    @Override
    public String getBaseFileURIString() {
        return workingDirectoryURI;
    }

    //WritableFilesDataUnit interface
    @Override
    public void addExistingFile(final String symbolicName, final String existingFileURI) throws DataUnitException {
        checkForMultithreadAccess();
        final File existingFile = new File(java.net.URI.create(existingFileURI));
        if (!existingFile.exists()) {
            throw new DataUnitException("File does not exist: " + existingFileURI + ". File must exists prior being added.");
        }
        if (!existingFile.isFile()) {
            throw new DataUnitException("Only files are permitted to be added. File " + existingFileURI + " is not a proper file.");
        }
        // Create subject and insert data.
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
                            valueFactory.createURI(FilesDataUnit.PREDICATE_FILE_URI),
                            valueFactory.createLiteral(existingFile.toURI().toASCIIString()),
                            getMetadataWriteGraphname()
                    );
                }
            });
        } catch (RepositoryException ex) {
            throw new DataUnitException("Problem with repositry.", ex);
        }
    }

    @Override
    public String addNewFile(String symbolicName) throws DataUnitException {
        // Sure that parent directory exists, parent directory could be deleted by clear method.
        workingDirectory.mkdirs();
        // Create a new file, limit used size of the symbolicName.
        final String encodedFileName = URLEncoder.encode(symbolicName).substring(0, symbolicName.length() > MAX_USER_FILENAME_LENGTH ? MAX_USER_FILENAME_LENGTH : symbolicName.length());
        final Path newFile;
        try {
            newFile = Files.createTempFile(workingDirectory.toPath(), encodedFileName, "");
        } catch (IOException ex) {
            throw new DataUnitException("Error when generating filename.", ex);
        }
        // And now add it as existing file.
        final String newFileURIString = newFile.toUri().toASCIIString();
        addExistingFile(symbolicName, newFileURIString);
        return newFileURIString;
    }

    //ManageableDataUnit interface
    @Override
    public void clear() throws DataUnitException {
        // Delte underlying RDF data first..
        super.clear();
        // Then delete storage directory.
        final File storageFile = new File(java.net.URI.create(this.workingDirectoryURI));
        if (storageFile.exists()) {
            if (!FileUtils.deleteQuietly(storageFile)) {
                LOG.warn("Can't delete storage directory: {}", this.workingDirectoryURI);
            }
        }
    }

    @Override
    public void updateExistingFileURI(String symbolicName, String newFileURIString) throws DataUnitException {
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
                Update update = connection.prepareUpdate(QueryLanguage.SPARQL, UPDATE_EXISTING_FILE);
                update.setBinding(SYMBOLIC_NAME_BINDING, symbolicNameLiteral);
                update.setBinding(FILE_URI_BINDING, valueFactory.createLiteral(newFileURIString));

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
            throw new DataUnitException("Error when adding data graph.", ex);
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
