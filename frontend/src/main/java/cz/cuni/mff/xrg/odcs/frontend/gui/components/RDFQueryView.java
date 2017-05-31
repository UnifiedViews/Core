package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tepi.filtertable.FilterGenerator;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.context.DataUnitInfo;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download.OnDemandFileDownloader;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.download.OnDemandStreamResource;
import cz.cuni.mff.xrg.odcs.frontend.container.rdf.RDFLazyQueryContainer;
import cz.cuni.mff.xrg.odcs.frontend.container.rdf.RDFQueryDefinition;
import cz.cuni.mff.xrg.odcs.frontend.container.rdf.RDFQueryFactory;
import cz.cuni.mff.xrg.odcs.frontend.container.rdf.RDFRegexFilter;
import cz.cuni.mff.xrg.odcs.frontend.container.rdf.RepositoryFrontendHelper;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.enums.SPARQLQueryType;
import cz.cuni.mff.xrg.odcs.rdf.enums.SelectFormatType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException;
import cz.cuni.mff.xrg.odcs.rdf.query.utils.QueryPart;
import cz.cuni.mff.xrg.odcs.rdf.validators.SPARQLQueryValidator;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.impl.ManageableWritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;

/**
 * Simple query view for browsing and querying debug data. User selects DPU and
 * then specifies DataUnit. User can simply browse the data, or query them. Both
 * SELECT and CONSTRUCT query can be used to show data in table and also to
 * download them. User can select format of data.
 *
 * @author Bogo
 */
public class RDFQueryView extends QueryView {

    private final String defaultQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 1000";

    //private DataUnitSelector selector;
    private TextArea queryText;

    private IntlibPagedTable resultTable;

    private HorizontalLayout resultTableControls;

    private ComboBox formatSelect;

    private ComboBox downloadFormatSelect;

    private Button tableDownload;

    private final static Logger LOG = LoggerFactory
            .getLogger(RDFQueryView.class);

    private boolean isEnabled = true;

    Button queryDownloadButton;

    Button queryButton;

    private HorizontalLayout resultDownloadControls;

    private String tableQuery = null;

    private DPUInstanceRecord tableDpu = null;

    private DataUnitInfo tableDataUnit = null;

    /**
     * Constructor with parent view.
     */
    public RDFQueryView() {
        VerticalLayout mainLayout = new VerticalLayout();

        mainLayout.addComponent(new Label(Messages.getString("RDFQueryView.query")));

        HorizontalLayout queryLine = new HorizontalLayout();
        queryLine.setSpacing(true);
        queryLine.setWidth(100, Unit.PERCENTAGE);

        queryText = new TextArea("", defaultQuery);
        queryText.setWidth("100%");
        queryText.setHeight("210px");
        queryText.setImmediate(true);
        queryLine.addComponent(queryText);

        VerticalLayout queryControls = new VerticalLayout();

        queryButton = new Button(Messages.getString("RDFQueryView.run"));
        queryButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    runQuery();
                } catch (InvalidQueryException e) {
                    Notification.show(Messages.getString("RDFQueryView.validator"),
                            Messages.getString("RDFQueryView.query.not.valid")
                                    + e.getCause().getMessage(),
                            Notification.Type.ERROR_MESSAGE);
                }
            }
        });
        queryControls.addComponent(queryButton);

        VerticalLayout runDownload = new VerticalLayout();

        //Export options
        formatSelect = new ComboBox();
        for (RDFFormatType type : RDFFormatType.values()) {
            if (type != RDFFormatType.AUTO) {
                formatSelect.addItem(RDFFormatType.getStringValue(type));
            }
        }
        for (SelectFormatType t : SelectFormatType.values()) {
            formatSelect.addItem(t);
        }
        formatSelect.setImmediate(true);
        formatSelect.setNullSelectionAllowed(false);
        formatSelect.select(RDFFormatType.getStringValue(RDFFormatType.TTL));
        runDownload.addComponent(formatSelect);

        queryDownloadButton = new Button(Messages.getString("RDFQueryView.run.and.download"));
        OnDemandFileDownloader fileDownloader = new OnDemandFileDownloader(
                new OnDemandStreamResource() {
                    @Override
                    public String getFilename() {
                        return getFileName(formatSelect.getValue());
                    }

                    @Override
                    public InputStream getStream() {
                        ManageableWritableRDFDataUnit repository = RepositoryFrontendHelper.getRepositoryForDownload(getExecutionInfo(), getSelectedDpu(), getDataUnitInfo());
                        String query = getQuery();
                        if (repository == null || query == null) {
                            return null;
                        }
                        return getDownloadData(repository, query, formatSelect
                                .getValue(), null);
                    }
                });
        fileDownloader.extend(queryDownloadButton);
        runDownload.addComponent(queryDownloadButton);
        queryControls.addComponent(runDownload);
        queryControls.setSpacing(true);
        queryControls.setWidth(170, Unit.PIXELS);
        queryLine.setExpandRatio(queryText, 1.0f);
        queryLine.addComponent(queryControls);

        mainLayout.addComponent(queryLine);

        resultTable = new IntlibPagedTable();
        resultTable.setWidth("100%");
        resultTable.setImmediate(true);
        resultTable.setPageLength(15);
        resultTable.setSortEnabled(false);
        //resultTable.setFilterBarVisible(true);
        resultTable.setFilterGenerator(new FilterGenerator() {
            @Override
            public Container.Filter generateFilter(Object propertyId,
                    Object value) {
                return new RDFRegexFilter((String) propertyId, (String) value);
            }

            @Override
            public Container.Filter generateFilter(Object propertyId,
                    Field<?> originatingField) {
                return null;
            }

            @Override
            public AbstractField<?> getCustomFilterComponent(Object propertyId) {
                return null;
            }

            @Override
            public void filterRemoved(Object propertyId) {
            }

            @Override
            public void filterAdded(Object propertyId,
                    Class<? extends Container.Filter> filterType, Object value) {
            }

            @Override
            public Container.Filter filterGeneratorFailed(Exception reason,
                    Object propertyId, Object value) {
                return null;
            }
        });
        resultTable.setCellStyleGenerator(new CustomTable.CellStyleGenerator() {
            @Override
            public String getStyle(CustomTable source, Object itemId, Object propertyId) {
                if (propertyId == null) {
                    return null;
                }
                Object oValue = source.getItem(itemId).getItemProperty(propertyId).getValue();
                if (oValue.getClass() == URIImpl.class) {
                    return "link";
                } else {
                    String value = oValue.getClass() == String.class ? (String) oValue : oValue.toString();
                    if (value.startsWith("http://")) {
                        return "link";
                    } else {
                        return null;
                    }
                }
            }
        });
        mainLayout.addComponent(resultTable);
        resultTableControls = resultTable.createControls();
        resultTableControls.setImmediate(true);
        mainLayout.addComponent(resultTableControls);

        resultDownloadControls = new HorizontalLayout();

        downloadFormatSelect = new ComboBox();
        downloadFormatSelect.setImmediate(true);
        resultDownloadControls.addComponent(downloadFormatSelect);

        tableDownload = new Button(Messages.getString("RDFQueryView.download"));
        OnDemandFileDownloader tableFileDownloader = new OnDemandFileDownloader(
                new OnDemandStreamResource() {
                    @Override
                    public String getFilename() {
                        return getFileName(downloadFormatSelect.getValue());
                    }

                    @Override
                    public InputStream getStream() {
                        if (tableDpu == null) {
                            return null;
                        }

                        List<Filter> filters = new LinkedList<>();
                        for (Object id : resultTable.getVisibleColumns()) {
                            Object value = resultTable.getFilterFieldValue(id);
                            if (value != null && !"".equals(value)) {
                                filters.add(new RDFRegexFilter((String) id, (String) value));
                            }
                        }

                        ManageableWritableRDFDataUnit tableRepo = RepositoryFrontendHelper.getRepositoryForDownload(getExecutionInfo(), tableDpu, tableDataUnit);
                        return getDownloadData(tableRepo, tableQuery, downloadFormatSelect.getValue(), filters);
                    }
                });
        tableFileDownloader.extend(tableDownload);
        resultDownloadControls.addComponent(tableDownload);
        resultDownloadControls.setVisible(false);
        mainLayout.addComponent(resultDownloadControls);

        mainLayout.setSizeFull();
        setCompositionRoot(mainLayout);
    }

    /**
     * Resets the query view to default state. Restores default query.
     */
    @Override
    public void reset() {
        queryText.setValue(defaultQuery);
        setResultVisible(false);
    }

    private boolean isSelectQuery(String query) throws InvalidQueryException {

        if (query.length() < 9) {
            //Due to expected exception format in catch block
            throw new InvalidQueryException(new InvalidQueryException(
                    Messages.getString("RDFQueryView.query.invalid") + query));
        }
        QueryPart queryPart = new QueryPart(query);
        SPARQLQueryType type = queryPart.getSPARQLQueryType();

        if (type == SPARQLQueryType.SELECT) {
            return true;
        } else {
            return false;
        }

    }

    private String getQuery() {
        String query = queryText.getValue();
        if (query.trim().isEmpty()) {
            Notification.show(Messages.getString("RDFQueryView.query.empty"), Notification.Type.WARNING_MESSAGE);
            return null;
        }
        return query.trim();
    }

    /**
     * Execute query on selected graph.
     *
     * @throws InvalidQueryException
     *             If the query is badly formatted.
     */
    private void runQuery() throws InvalidQueryException {
        String query = getQuery();
        if (query == null) {
            return;
        }

        SPARQLQueryValidator validator = new SPARQLQueryValidator(query);
        if (!validator.isQueryValid()) {
            Notification.show(Messages.getString("RDFQueryView.run.validator"),
                    Messages.getString("RDFQueryView.run.query.not.valid")
                            + validator.getErrorMessage(),
                    Notification.Type.ERROR_MESSAGE);
            return;
        }

        DPUInstanceRecord selectedDpu = getSelectedDpu();
        DataUnitInfo selectedDataUnit = getDataUnitInfo();
        setUpTableDownload(selectedDpu, selectedDataUnit, query);

        //New RDFLazyQueryContainer
        RDFLazyQueryContainer container = new RDFLazyQueryContainer(
                new RDFQueryDefinition(15, "id", query, getExecutionInfo(),
                        selectedDpu, selectedDataUnit), new RDFQueryFactory());
        if (container.size() > 0) {
            if (isSelectQuery(query)) {
                for (Object propertyId : container.getItem(container
                        .getIdByIndex(0)).getItemPropertyIds()) {
                    container.addContainerProperty(propertyId, String.class,
                            null, true, true);
                }
                resultTable.setFilterBarVisible(true);
            } else {
                container.addContainerProperty("subject", String.class, null,
                        true, true);
                container.addContainerProperty("predicate", String.class, null,
                        true, true);
                container.addContainerProperty("object", String.class, null,
                        true, true);
                resultTable.setFilterBarVisible(false);
            }
        }
        setResultVisible(true);
        resultTable.setContainerDataSource(container);
        if (resultTable.getItemIds().isEmpty()) {
            resultTable.setStyleName("empty");
            tableDownload.setEnabled(false);
            downloadFormatSelect.setEnabled(false);

        } else {
            resultTable.removeStyleName("empty");
            tableDownload.setEnabled(true);
            downloadFormatSelect.setEnabled(true);
        }
    }

    @Override
    public void browseDataUnit() {
        queryText.setValue("SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 1000");
        try {
            runQuery();
        } catch (InvalidQueryException ex) {
            LOG.error(ex.getLocalizedMessage(), ex.getStackTrace());
        }
    }

    @Override
    public void setQueryingEnabled(boolean value) {
        if (isEnabled != value) {
            queryText.setEnabled(value);
            formatSelect.setEnabled(value);
            queryButton.setEnabled(value);
            queryDownloadButton.setEnabled(value);
            isEnabled = value;
        }
    }

    /**
     * Prepare data file for download after SELECT or CONSTRUCT query.
     *
     * @param repository
     *            {@link LocalRDFDataUnit} of selected graph.
     * @param query
     *            {@link String} containing query to execute on repository.
     * @throws InvalidQueryException
     *             If the query is badly formatted.
     */
    private InputStream getDownloadData(RDFDataUnit repository, String query,
            Object format, Collection<Filter> filters) {
        RepositoryConnection connection = null;
        try {
            boolean isSelectQuery = isSelectQuery(query);

            File constructData;
            String fn = File.createTempFile("data", "dt")
                    .getAbsolutePath();

            if (isSelectQuery != (format.getClass() == SelectFormatType.class)) {

                if (isSelectQuery) {
                    Notification.show(Messages.getString("RDFQueryView.select.not.supported", format.toString()),
                            Messages.getString("RDFQueryView.format.not.supported.construct"),
                            Notification.Type.ERROR_MESSAGE);
                } else {
                    Notification.show(
                            Messages.getString("RDFQueryView.consctuct.not.supported", format.toString()),
                            Messages.getString("RDFQueryView.consctuct.not.supported.description"),
                            Notification.Type.ERROR_MESSAGE);
                }

                return null;
            }

            connection = repository.getConnection();

            if (isSelectQuery) {
                query = RepositoryFrontendHelper.filterRDFQuery(query, filters);
                SelectFormatType selectType = (SelectFormatType) format;

                constructData = RepositoryFrontendHelper.executeSelectQuery(connection, query, fn,
                        selectType, RDFHelper.getGraphsURISet(repository));
            } else {
                RDFFormatType rdfType = RDFFormatType.getTypeByString(format
                        .toString());

                constructData = RepositoryFrontendHelper.executeConstructQuery(connection, RDFHelper.getGraphsURISet(repository), query, rdfType, fn);
            }

            FileInputStream fis = new FileInputStream(constructData);
            return fis;
        } catch (IOException ex) {
            LOG.error("File with result not found.", ex);
            Notification.show(
                    Messages.getString("RDFQueryView.download.error"),
                    Notification.Type.ERROR_MESSAGE);
        } catch (InvalidQueryException ex) {
            LOG.error("Invalid query!", ex);
            Notification.show(Messages.getString("RDFQueryView.download.validator"),
                    Messages.getString("RDFQueryView.download.query.not.valid")
                            + ex.getCause().getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        } catch (DataUnitException ex) {
            Notification.show(Messages.getString("RDFQueryView.dataunit.problem"),
                    ex.getCause().getMessage(),
                    Notification.Type.ERROR_MESSAGE);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    Notification.show(Messages.getString("RDFQueryView.closing.problem"),
                            ex.getCause().getMessage(),
                            Notification.Type.ERROR_MESSAGE);
                }
            }
        }
        return null;
    }

    private String getFileName(Object oFormat) {
        if (oFormat == null) {
            Notification.show(Messages.getString("RDFQueryView.format.empty"),
                    Messages.getString("RDFQueryView.format.empty.description"),
                    Notification.Type.ERROR_MESSAGE);
            return "";
        }
        String filename = null;
        if (oFormat.getClass() == String.class) {

            RDFFormatType type = RDFFormatType.getTypeByString(oFormat
                    .toString());

            switch (type) {
                case TTL:
                    filename = "data.ttl";
                    break;
                case AUTO:
                case RDFXML:
                    filename = "data.rdf";
                    break;
                case N3:
                    filename = "data.n3";
                    break;
                case TRIG:
                    filename = "data.trig";
                    break;
                case TRIX:
                    filename = "data.trix";
                    break;
                case NT:
                    filename = "data.nt";
                    break;
            }
        } else if (oFormat.getClass() == SelectFormatType.class) {
            switch ((SelectFormatType) oFormat) {
                case CSV:
                    filename = "data.csv";
                    break;
                case XML:
                    filename = "data.xml";
                    break;
                case TSV:
                    filename = "data.tsv";
                    break;
                case JSON:
                    filename = "data.json";
                    break;
            }
        }
        return filename;
    }

    private void setUpTableDownload(DPUInstanceRecord selectedDpu,
            DataUnitInfo selectedDataUnit, String query) {
        resultDownloadControls.setVisible(true);
        tableDpu = selectedDpu;
        tableDataUnit = selectedDataUnit;
        tableQuery = query;
        try {
            Object first = null;
            if (isSelectQuery(query)) {
                downloadFormatSelect.removeAllItems();
                for (SelectFormatType t : SelectFormatType.values()) {
                    if (first == null) {
                        first = t;
                    }
                    downloadFormatSelect.addItem(t);
                }
            } else {
                downloadFormatSelect.removeAllItems();
                for (RDFFormatType type : RDFFormatType.values()) {
                    if (type != RDFFormatType.AUTO) {
                        String value = RDFFormatType.getStringValue(type);
                        if (first == null) {
                            first = value;
                        }
                        downloadFormatSelect.addItem(value);
                    }
                }
            }
            downloadFormatSelect.select(first);
        } catch (InvalidQueryException ex) {
            //Should not happen, only correct queries are shown in table.
        }
    }

    private void setResultVisible(boolean visible) {
        resultTable.setVisible(visible);
        resultTableControls.setVisible(visible);
        resultDownloadControls.setVisible(visible);
    }
}
