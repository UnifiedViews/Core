package cz.cuni.mff.xrg.odcs.frontend.gui.tables;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.tepi.filtertable.paged.PagedFilterTable;
import org.tepi.filtertable.paged.PagedFilterTableContainer;
import org.tepi.filtertable.paged.PagedTableChangeEvent;

import com.vaadin.data.Container;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.Reindeer;

import cz.cuni.mff.xrg.odcs.commons.app.i18n.LocaleHolder;
import cz.cuni.mff.xrg.odcs.frontend.container.ReadOnlyContainer;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Intlib extension of FilterTable add-on. PagedFilterTable provides paging for
 * custom Vaadin Table. Our extension modifies its paging controls to fit our
 * needs, supplies it with our improved FilterGenerator and fixes filter visual
 * by setting height and width to 100%
 * Uses internally ReadOnlyContainer - custom solution to load data to the table
 * 
 * @author Bogo
 */
public class IntlibPagedTable extends PagedFilterTable {

    private HashMap<Object, Integer> positions;

    private Label totalRecords;

    /**
     * Constructor.
     */
    public IntlibPagedTable() {
        super();
        positions = new HashMap<>();
        setFilterDecorator(new IntlibFilterDecorator());
    }

    /**
     * Creates controls for navigating between pages of table. Hides the page
     * size selector.
     * 
     * @return layout with table controls.
     */
    public HorizontalLayout createControls() {
        this.setLocale(LocaleHolder.getLocale());
        Label pageLabel = new Label(Messages.getString("IntlibPagedTable.page"), ContentMode.HTML);
        final TextField currentPageTextField = new TextField();
        currentPageTextField.setValue(String.valueOf(getCurrentPage()));
        currentPageTextField.setConverter(new StringToIntegerConverter() {
            @Override
            protected NumberFormat getFormat(Locale locale) {
                NumberFormat nf = super.getFormat(UI.getCurrent().getLocale());
                nf.setGroupingUsed(false);
                return nf;
            }
        });
        Label separatorLabel = new Label("&nbsp;/&nbsp;", ContentMode.HTML);
        final Label totalPagesLabel = new Label(
                String.valueOf(getTotalAmountOfPages()), ContentMode.HTML);

        currentPageTextField.setStyleName(Reindeer.TEXTFIELD_SMALL);
        currentPageTextField.setImmediate(true);
        currentPageTextField.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = -2255853716069800092L;

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                if (currentPageTextField.isValid()
                        && currentPageTextField.getValue() != null) {
                    Object value = currentPageTextField.getConvertedValue();
                    int page = (int) value;
                    setCurrentPage(page);
                    totalRecords.setCaption(Messages.getString("IntlibPagedTable.records") + getContainerDataSource().getRealSize());
                }
            }
        });
        pageLabel.setWidth(null);
        currentPageTextField.setColumns(5);
        separatorLabel.setWidth(null);
        totalPagesLabel.setWidth(null);

        HorizontalLayout controlBar = new HorizontalLayout();
        HorizontalLayout pageManagement = new HorizontalLayout();
        HorizontalLayout recordsStats = new HorizontalLayout();
        final Button first = new Button("<<", new Button.ClickListener() {
            private static final long serialVersionUID = -355520120491283992L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setCurrentPage(0);
            }
        });
        final Button previous = new Button("<", new Button.ClickListener() {
            private static final long serialVersionUID = -355520120491283992L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                previousPage();
            }
        });
        final Button next = new Button(">", new Button.ClickListener() {
            private static final long serialVersionUID = -1927138212640638452L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                nextPage();
            }
        });
        final Button last = new Button(">>", new Button.ClickListener() {
            private static final long serialVersionUID = -355520120491283992L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setCurrentPage(getTotalAmountOfPages());
            }
        });
        first.setStyleName(Reindeer.BUTTON_LINK);
        previous.setStyleName(Reindeer.BUTTON_LINK);
        next.setStyleName(Reindeer.BUTTON_LINK);
        last.setStyleName(Reindeer.BUTTON_LINK);

        first.setWidth("15px");
        last.setWidth("15px");
        previous.setWidth("8px");
        next.setWidth("8px");

        pageLabel.addStyleName("pagedtable-pagecaption");
        currentPageTextField.addStyleName("pagedtable-pagefield");
        separatorLabel.addStyleName("pagedtable-separator");
        totalPagesLabel.addStyleName("pagedtable-total");
        first.addStyleName("pagedtable-first");
        previous.addStyleName("pagedtable-previous");
        next.addStyleName("pagedtable-next");
        last.addStyleName("pagedtable-last");

        pageLabel.addStyleName("pagedtable-label");
        currentPageTextField.addStyleName("pagedtable-label");
        separatorLabel.addStyleName("pagedtable-label");
        totalPagesLabel.addStyleName("pagedtable-label");
        first.addStyleName("pagedtable-button");
        previous.addStyleName("pagedtable-button");
        next.addStyleName("pagedtable-button");
        last.addStyleName("pagedtable-button");

        pageManagement.addComponent(first);
        pageManagement.addComponent(previous);
        pageManagement.addComponent(pageLabel);
        pageManagement.addComponent(currentPageTextField);
        pageManagement.addComponent(separatorLabel);
        pageManagement.addComponent(totalPagesLabel);
        pageManagement.addComponent(next);
        pageManagement.addComponent(last);
        pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(currentPageTextField,
                Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(separatorLabel,
                Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(totalPagesLabel,
                Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
        pageManagement.setWidth(null);
        pageManagement.setSpacing(true);

        totalRecords = new Label("", ContentMode.HTML);
        totalRecords.setWidth(null);
        totalRecords.addStyleName("pagedtable-label");
        recordsStats.addComponent(totalRecords);
        recordsStats.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);
        recordsStats.setWidth(null);
        recordsStats.setSpacing(true);

        controlBar.addComponent(new HorizontalLayout());
        controlBar.addComponent(pageManagement);
        controlBar.addComponent(recordsStats);
        controlBar.setComponentAlignment(pageManagement,
                Alignment.MIDDLE_CENTER);
        controlBar.setComponentAlignment(recordsStats, Alignment.BOTTOM_RIGHT);
        controlBar.setWidth(100, Unit.PERCENTAGE);
        addListener(new PageChangeListener() {
            private boolean inMiddleOfValueChange;

            @Override
            public void pageChanged(PagedTableChangeEvent event) {
                if (!inMiddleOfValueChange) {
                    inMiddleOfValueChange = true;
                    PagedFilterTableContainer container = getContainerDataSource();
                    first.setEnabled(container.getStartIndex() > 0);
                    previous.setEnabled(container.getStartIndex() > 0);
                    next.setEnabled(container.getStartIndex() < container
                            .getRealSize() - getPageLength());
                    last.setEnabled(container.getStartIndex() < container
                            .getRealSize() - getPageLength());
                    currentPageTextField.setValue(String
                            .valueOf(getCurrentPage()));
                    totalPagesLabel.setValue(Integer
                            .toString(getTotalAmountOfPages()));
                    inMiddleOfValueChange = false;
                    totalRecords.setCaption(Messages.getString("IntlibPagedTable.records") + getContainerDataSource().getRealSize());
                }
            }
        });
        return controlBar;
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        return getContainerDataSource().getSortableContainerPropertyIds();
    }

    @Override
    public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn) {
        super.addGeneratedColumn(id, generatedColumn);
        setFilterFieldVisible(id, false);
    }

    /**
     * Adds generated columns on specified position.
     * 
     * @param id
     *            Id of column.
     * @param position
     *            Position of column.
     * @param generatedColumn
     *            Column.
     */
    public void addGeneratedColumn(Object id, int position, ColumnGenerator generatedColumn) {
        positions.put(id, position);
        addGeneratedColumn(id, generatedColumn);
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        super.setContainerDataSource(newDataSource);
        if (newDataSource.getClass() == ReadOnlyContainer.class) {
            ReadOnlyContainer container = (ReadOnlyContainer) newDataSource;
            for (Object id : newDataSource.getContainerPropertyIds()) {
                setColumnHeader(id, container.getColumnName((String) id));
            }
            setVisibleColumns();
            totalRecords.setCaption(Messages.getString("IntlibPagedTable.records") + String.valueOf(getContainerDataSource().getContainer().size()));
        }
    }

    /**
     * Set visible columns from accessor, if available. Take actions columns into account.
     */
    public void setVisibleColumns() {
        PagedFilterTableContainer pagedContainer = getContainerDataSource();
        if (pagedContainer != null) {
            Container c = pagedContainer.getContainer();
            if (c != null && c.getClass() == ReadOnlyContainer.class) {
                LinkedList<String> visible = new LinkedList<>(((ReadOnlyContainer) c).getVisibles());
                for (Object o : getVisibleColumns()) {
                    if (o.equals("actions")) {
                        if (positions.containsKey(o)) {
                            visible.add(positions.get(o), "actions");
                        } else {
                            visible.add("actions");
                        }
                        break;
                    }
                }
                this.setVisibleColumns(visible.toArray());
            }
        }

    }

    /**
     * Set filters on columns from accessor, if available.
     */
    public void setFilterLayout() {
        PagedFilterTableContainer pagedContainer = getContainerDataSource();
        if (pagedContainer != null) {
            Container c = pagedContainer.getContainer();
            if (c != null && c.getClass() == ReadOnlyContainer.class) {
                ReadOnlyContainer container = (ReadOnlyContainer) c;
                List<String> filterables = container.getFilterables();
                for (Object columnId : getVisibleColumns()) {
                    setFilterFieldVisible(columnId, filterables.contains((String) columnId));
                }
            }
        }

        for (Object id : getVisibleColumns()) {
            Component filterField = getFilterField(id);
            if (filterField != null) {
                filterField.setWidth(100, Unit.PERCENTAGE);
                filterField.setHeight(100, Unit.PERCENTAGE);
            }
        }
    }

    @Override
    public void setFilterBarVisible(boolean filtersVisible) {
        super.setFilterBarVisible(filtersVisible);
        setFilterLayout();
    }

    @Override
    public void resetFilters() {
        super.resetFilters();
        setFilterLayout();
    }
}
