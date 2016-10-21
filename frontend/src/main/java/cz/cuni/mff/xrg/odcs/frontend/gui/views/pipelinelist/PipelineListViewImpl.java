package cz.cuni.mff.xrg.odcs.frontend.gui.views.pipelinelist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.paged.PagedFilterTable;
import org.tepi.filtertable.paged.PagedTableChangeEvent;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cz.cuni.mff.xrg.odcs.commons.app.auth.EntityPermissions;
import cz.cuni.mff.xrg.odcs.commons.app.auth.PermissionUtils;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.DecorationHelper;
import cz.cuni.mff.xrg.odcs.frontend.container.ValueItem;
import cz.cuni.mff.xrg.odcs.frontend.container.accessor.PipelineViewAccessor;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.ActionColumnGenerator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibFilterDecorator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.PipelineEdit;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Utils;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Vaadin implementation for PipelineListView.
 * 
 * @author Bogo
 */
@Component
@Scope("prototype")
public class PipelineListViewImpl extends CustomComponent implements PipelineListPresenter.PipelineListView {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineListViewImpl.class);

    /**
     * Column widths for pipeline table.
     */
    private static final int COLUMN_ACTIONS_WIDTH = 250;

    private static final int COLUMN_STATUS_WIDTH = 100;

    private static final int COLUMN_DURATION_WIDTH = 220;

    private static final int COLUMN_CREATEDBY_WIDTH = 250;

    private static final int COLUMN_TIME_WIDTH = 220;

    private VerticalLayout mainLayout;

    private IntlibPagedTable tablePipelines;

    private Button btnCreatePipeline;

    private Button btnImportPipeline;

    @Autowired
    private PermissionUtils permissionUtils;

    @Autowired
    private Utils utils;

    private void buildPage(final PipelineListPresenter presenter) {
        // top-level component properties
        setWidth("100%");
        // we do not set heigth, so it enables scroll bars

        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(true);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        HorizontalLayout topLine = new HorizontalLayout();
        topLine.setSpacing(true);

        btnCreatePipeline = new Button();
        btnCreatePipeline.setCaption(Messages.getString("PipelineListViewImpl.create.pipeline"));
        btnCreatePipeline.setHeight("25px");
        btnCreatePipeline.addStyleName("v-button-primary");
        btnCreatePipeline.setVisible(this.permissionUtils.hasUserAuthority(EntityPermissions.PIPELINE_CREATE));
        btnCreatePipeline.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                presenter.navigateToEventHandler(PipelineEdit.class, "New");
            }
        });
        topLine.addComponent(btnCreatePipeline);

        btnImportPipeline = new Button();
        btnImportPipeline.setCaption(Messages.getString("PipelineListViewImpl.import.pipeline"));
        btnImportPipeline.setHeight("25px");
        btnImportPipeline.addStyleName("v-button-primary");
        btnImportPipeline.setVisible(this.permissionUtils.hasUserAuthority(EntityPermissions.PIPELINE_IMPORT));
        btnImportPipeline.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                presenter.importPipeline();
            }
        });
        topLine.addComponent(btnImportPipeline);

        Button buttonDeleteFilters = new Button();
        buttonDeleteFilters.setCaption(Messages.getString("PipelineListViewImpl.clear.filters"));
        buttonDeleteFilters.setHeight("25px");
        buttonDeleteFilters.addStyleName("v-button-primary");
        buttonDeleteFilters.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                tablePipelines.resetFilters();
            }
        });
        topLine.addComponent(buttonDeleteFilters);

        Button btnClearSort = new Button(Messages.getString("PipelineListViewImpl.clear.sort"));
        btnClearSort.setHeight("25px");
        btnClearSort.addStyleName("v-button-primary");
        btnClearSort.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                tablePipelines.setSortContainerPropertyId(null);; // deselect column
                tablePipelines.sort(new Object[] { "id" }, new boolean[] { false });
            }
        });
        topLine.addComponent(btnClearSort);

        mainLayout.addComponent(topLine);

        tablePipelines = new IntlibPagedTable();
        tablePipelines.setWidth("100%");

        mainLayout.addComponent(tablePipelines);
        mainLayout.addComponent(tablePipelines.createControls());
        tablePipelines.setPageLength(this.utils.getPageLength());
        tablePipelines.setColumnCollapsingAllowed(true);

        // add column
        tablePipelines.setImmediate(true);
        tablePipelines.addGeneratedColumn("actions", 0, createColumnGenerator(presenter));
        tablePipelines.addGeneratedColumn(PipelineViewAccessor.COLUMN_STATUS, new CustomTable.ColumnGenerator() {
            @Override
            public Object generateCell(CustomTable source, Object itemId,
                    Object columnId) {
                PipelineExecutionStatus type = (PipelineExecutionStatus) source.getItem(itemId)
                        .getItemProperty(columnId).getValue();
                if (type != null) {
                    ThemeResource img = DecorationHelper.getIconForExecutionStatus(type);
                    Embedded emb = new Embedded(type.name(), img);
                    emb.setDescription(type.name());
                    return emb;
                } else {
                    return null;
                }
            }
        });

        tablePipelines.setColumnHeader("actions", Messages.getString("PipelineListViewImpl.actions"));
        tablePipelines.setColumnWidth("actions", COLUMN_ACTIONS_WIDTH);
        tablePipelines.setColumnWidth(PipelineViewAccessor.COLUMN_DURATION, COLUMN_DURATION_WIDTH);
        tablePipelines.setColumnWidth(PipelineViewAccessor.COLUMN_STATUS, COLUMN_STATUS_WIDTH);
        tablePipelines.setColumnWidth(PipelineViewAccessor.COLUMN_START, COLUMN_TIME_WIDTH);
        tablePipelines.setColumnWidth(PipelineViewAccessor.COLUMN_CREATED_BY, COLUMN_CREATEDBY_WIDTH);

        tablePipelines.setColumnAlignment(PipelineViewAccessor.COLUMN_STATUS, CustomTable.Align.CENTER);
        tablePipelines.setColumnAlignment(PipelineViewAccessor.COLUMN_DURATION, CustomTable.Align.RIGHT);
        tablePipelines.setVisibleColumns();

        tablePipelines.setFilterBarVisible(true);
        tablePipelines.setFilterGenerator(createFilterGenerator());
        tablePipelines.setFilterDecorator(new FilterDecorator());
        tablePipelines.setFilterLayout();
        tablePipelines.setSelectable(true);
        tablePipelines.addItemClickListener(
                new ItemClickEvent.ItemClickListener() {
                    @Override
                    public void itemClick(ItemClickEvent event) {
                        if (!tablePipelines.isSelected(event.getItemId())) {
                            ValueItem item = (ValueItem) event.getItem();
                            final long pipelineId = item.getId();

                            presenter.navigateToEventHandler(PipelineEdit.class, pipelineId);
                        }
                    }
                });
        tablePipelines.addListener(new PagedFilterTable.PageChangeListener() {

            @Override
            public void pageChanged(PagedTableChangeEvent event) {
                int newPageNumber = event.getCurrentPage();
                presenter.pageChangedHandler(newPageNumber);
            }
        });
        tablePipelines.addItemSetChangeListener(new Container.ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                for (Object id : event.getContainer().getContainerPropertyIds()) {
                    Object filterValue = tablePipelines.getFilterFieldValue(id);
                    presenter.filterParameterEventHander((String) id, filterValue);
                }
            }
        });

        setCompositionRoot(mainLayout);
    }

    private ActionColumnGenerator createColumnGenerator(final PipelineListPresenter presenter) {

        ActionColumnGenerator generator = new ActionColumnGenerator();
        // add action buttons

        generator.addButton(Messages.getString("PipelineListViewImpl.run"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.runEventHandler(id, false);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.canRunPipeline(id);
            }
        }, new ThemeResource("icons/running.png"));

        generator.addButton(Messages.getString("PipelineListViewImpl.debug"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.runEventHandler(id, true);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.canDebugPipeline(id);
            }
        }, new ThemeResource("icons/debug.png"));

        generator.addButton(Messages.getString("PipelineListViewImpl.schedule"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.scheduleEventHandler(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.canSchedulePipeline(id);
            }
        }, new ThemeResource("icons/scheduled.png"));

        generator.addButton(Messages.getString("PipelineListViewImpl.copy"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.copyEventHandler(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.canCopyPipeline(id);
            }
        }, new ThemeResource("icons/copy.png"));

        generator.addButton(Messages.getString("PipelineListViewImpl.edit"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(long id) {
                presenter.navigateToEventHandler(PipelineEdit.class, id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.canEditPipeline(id);
            }
        }, new ThemeResource("icons/gear.png"));

        generator.addButton(Messages.getString("PipelineListViewImpl.delete"), null, new ActionColumnGenerator.Action() {
            @Override
            protected void action(final long id) {
                presenter.deleteEventHandler(id);
            }
        }, new ActionColumnGenerator.ButtonShowCondition() {
            @Override
            public boolean show(CustomTable source, long id) {
                return presenter.canDeletePipeline(id);
            }
        }, new ThemeResource("icons/trash.png"));

        return generator;
    }

    @Override
    public Object enter(PipelineListPresenter presenter) {
        if (!presenter.isLayoutInitialized()) {
            buildPage(presenter);
        }
        this.tablePipelines.select(this.tablePipelines.getNullSelectionItemId());

        return this;
    }

    @Override
    public void setDisplay(PipelineListPresenter.PipelineListData dataObject) {
        tablePipelines.setContainerDataSource(dataObject.getContainer());
    }

    @Override
    public void setPage(int pageNumber) {
        tablePipelines.setCurrentPage(pageNumber);
    }

    @Override
    public void setFilter(String key, Object value) {
        tablePipelines.setFilterFieldValue(key, value);
    }

    @Override
    public void refreshTableControls() {
        tablePipelines.setCurrentPage(tablePipelines.getCurrentPage());
    }

    private static FilterGenerator createFilterGenerator() {
        return new FilterGenerator() {
            private static final long serialVersionUID = 4526398226598396388L;

            @Override
            public Container.Filter generateFilter(Object propertyId, Object value) {
                if (PipelineViewAccessor.COLUMN_CREATED_BY.equals(propertyId)) {
                    String val = (String) value;
                    SimpleStringFilter fullNameFilter = new SimpleStringFilter(PipelineViewAccessor.COLUMN_CREATED_BY, val, true, false);
                    SimpleStringFilter actorNameFilter = new SimpleStringFilter(PipelineViewAccessor.COLUMN_ACTOR_NAME, val, true, false);
                    return new Or(fullNameFilter, actorNameFilter);
                }
                return null;
            }

            @Override
            public Container.Filter generateFilter(Object propertyId, Field<?> originatingField) {
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
            public void filterAdded(Object propertyId, Class<? extends Container.Filter> filterType, Object value) {
            }

            @Override
            public Container.Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
                return null;
            }
        };
    }

    /**
     * Settings icons to the table filters "status" and "debug"
     */
    private class FilterDecorator extends IntlibFilterDecorator {

        private static final long serialVersionUID = -918475487163877932L;

        @Override
        public String getEnumFilterDisplayName(Object propertyId, Object value) {
            if (propertyId.equals(PipelineViewAccessor.COLUMN_STATUS)) {
                return ((PipelineExecutionStatus) value).name();
            }
            return super.getEnumFilterDisplayName(propertyId, value);
        }

        @Override
        public Resource getEnumFilterIcon(Object propertyId, Object value) {
            if (propertyId.equals(PipelineViewAccessor.COLUMN_STATUS)) {
                PipelineExecutionStatus type = (PipelineExecutionStatus) value;
                return DecorationHelper.getIconForExecutionStatus(type);
            }
            return super.getEnumFilterIcon(propertyId, value);
        }

    }
}
