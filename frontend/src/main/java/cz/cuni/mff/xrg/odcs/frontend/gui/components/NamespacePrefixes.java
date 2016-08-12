package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cz.cuni.mff.xrg.odcs.commons.app.facade.NamespacePrefixFacade;
import cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace.NamespacePrefix;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibFilterDecorator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * GUI for Namespace Prefixes which opens from the Administrator menu. Contains
 * table with namespace prefixes and button for prefixes creation.
 * 
 * @author Maria Kukhar
 */
@Component
@Scope("prototype")
public class NamespacePrefixes {

    private IntlibPagedTable prefixesTable;

    private VerticalLayout prefixesListLayout;

    private static String[] visibleCols = new String[] { "id", "name", "uri", "actions" };

    private static String[] headers = new String[] { Messages.getString("NamespacePrefixes.id"), Messages.getString("NamespacePrefixes.prefix.name"), Messages.getString("NamespacePrefixes.prefix.URI"), Messages.getString("NamespacePrefixes.actions") };

    private IndexedContainer tableData;

    private Long prefixId;

    private NamespacePrefix prefixDel;

    @Autowired
    private NamespacePrefixFacade namespacePrefixFacade;

    /**
     * Build layout for namespace prefixes.
     * 
     * @return Built layout for namespace prefixes
     */
    public VerticalLayout buildNamespacePrefixesLayout() {

        prefixesListLayout = new VerticalLayout();
        prefixesListLayout.setMargin(true);
        prefixesListLayout.setSpacing(true);
        prefixesListLayout.setWidth("100%");

        prefixesListLayout.setImmediate(true);

        //Layout for buttons Create new prefix and Clear Filters on the top.
        HorizontalLayout topLine = new HorizontalLayout();
        topLine.setSpacing(true);

        Button addUserButton = new Button();
        addUserButton.setCaption(Messages.getString("NamespacePrefixes.prefix.new"));
        addUserButton.setWidth("130px");
        addUserButton
                .addClickListener(new com.vaadin.ui.Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(ClickEvent event) {

                        boolean newPrefix = true;
                        // open usercreation dialog
                        PrefixCreate prefix = new PrefixCreate(newPrefix, namespacePrefixFacade);
                        UI.getCurrent().addWindow(prefix);
                        prefix.addCloseListener(new CloseListener() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void windowClose(CloseEvent e) {
                                refreshData();
                            }
                        });
                    }
                });
        topLine.addComponent(addUserButton);

        Button buttonDeleteFilters = new Button();
        buttonDeleteFilters.setCaption(Messages.getString("NamespacePrefixes.filters.clear"));
        buttonDeleteFilters.setHeight("25px");
        buttonDeleteFilters.setWidth("130px");
        buttonDeleteFilters
                .addClickListener(new com.vaadin.ui.Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(ClickEvent event) {
                        prefixesTable.resetFilters();
                        prefixesTable.setFilterFieldVisible("actions", false);
                    }
                });
        topLine.addComponent(buttonDeleteFilters);
        prefixesListLayout.addComponent(topLine);

        tableData = getTableData(namespacePrefixFacade.getAllPrefixes());

        //table with pipeline execution records
        prefixesTable = new IntlibPagedTable();
        prefixesTable.setSelectable(true);
        prefixesTable.setContainerDataSource(tableData);
        prefixesTable.setWidth("100%");
        prefixesTable.setHeight("100%");
        prefixesTable.setImmediate(true);
        prefixesTable.setVisibleColumns((Object[]) visibleCols); // Set visible columns
        prefixesTable.setColumnHeaders(headers);

        //Actions column. Contains actions buttons: Debug data, Show log, Stop.
        prefixesTable.addGeneratedColumn("actions",
                new actionColumnGenerator());

        prefixesListLayout.addComponent(prefixesTable);
        prefixesListLayout.addComponent(prefixesTable.createControls());
        prefixesTable.setPageLength(10);
        prefixesTable.setFilterDecorator(new filterDecorator());
        prefixesTable.setFilterBarVisible(true);
        prefixesTable.setFilterFieldVisible("actions", false);
        prefixesTable.addItemClickListener(
                new ItemClickEvent.ItemClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void itemClick(ItemClickEvent event) {

                        if (!prefixesTable.isSelected(event.getItemId())) {
                            prefixId = (Long) event.getItem().getItemProperty("id").getValue();
                            showPrefixSettings(prefixId);
                        }
                    }
                });

        return prefixesListLayout;
    }

    /**
     * Container with data for {@link #prefixesTable}
     * 
     * @param data
     * @return result. IndexedContainer with data for users table
     */
    @SuppressWarnings("unchecked")
    public static IndexedContainer getTableData(List<NamespacePrefix> data) {

        IndexedContainer result = new IndexedContainer();

        for (String p : visibleCols) {
            // setting type of columns
            switch (p) {
                case "id":
                    result.addContainerProperty(p, Long.class, null);
                    break;
                default:
                    result.addContainerProperty(p, String.class, "");
                    break;
            }
        }
//		result.addContainerProperty("id", Long.class, "");

        for (NamespacePrefix item : data) {
            Object num = result.addItem();

            result.getContainerProperty(num, "id").setValue(item.getId());
            result.getContainerProperty(num, "name").setValue(item.getName());
            result.getContainerProperty(num, "uri").setValue(item.getPrefixURI());
        }

        return result;

    }

    /**
     * Calls for refresh table {@link #schedulerTable}.
     */
    private void refreshData() {
        int page = prefixesTable.getCurrentPage();
        tableData = getTableData(namespacePrefixFacade.getAllPrefixes());
        prefixesTable.setContainerDataSource(tableData);
        prefixesTable.setCurrentPage(page);
        prefixesTable.setVisibleColumns((Object[]) visibleCols);
        prefixesTable.setFilterFieldVisible("actions", false);

    }

    /**
     * Shows dialog with Namespace Prefix settings for given prefix.
     * 
     * @param id
     *            Id of user to show.
     */
    private void showPrefixSettings(Long id) {

        boolean newPrefix = false;
        // open usercreation dialog
        PrefixCreate prefixEdit = new PrefixCreate(newPrefix, namespacePrefixFacade);
        NamespacePrefix prefix = namespacePrefixFacade.getPrefix(id);
        prefixEdit.setSelectedPrefix(prefix);

        UI.getCurrent().addWindow(prefixEdit);

        prefixEdit.addCloseListener(new CloseListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void windowClose(CloseEvent e) {
                refreshData();
            }
        });

    }

    /**
     * Generate column "actions" in the table {@link #prefixesTable}.
     * 
     * @author Maria Kukhar
     */
    class actionColumnGenerator implements CustomTable.ColumnGenerator {

        private static final long serialVersionUID = 1L;

        @Override
        public Object generateCell(final CustomTable source, final Object itemId,
                Object columnId) {

            HorizontalLayout layout = new HorizontalLayout();

            //Edit button. Open dialog for edit user's details.
            Button changeButton = new Button();
            changeButton.setCaption(Messages.getString("NamespacePrefixes.edit"));
            changeButton.setWidth("80px");
            changeButton.addClickListener(new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {

                    prefixId = (Long) tableData.getContainerProperty(itemId, "id")
                            .getValue();
                    showPrefixSettings(prefixId);

                }
            });

            layout.addComponent(changeButton);

            //Delete button. Delete user's record from Database.
            Button deleteButton = new Button();
            deleteButton.setCaption(Messages.getString("NamespacePrefixes.delete"));
            deleteButton.setWidth("80px");
            deleteButton.addClickListener(new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    prefixId = (Long) tableData.getContainerProperty(itemId, "id")
                            .getValue();
                    prefixDel = namespacePrefixFacade.getPrefix(prefixId);
                    //open confirmation dialog
                    ConfirmDialog.show(UI.getCurrent(), Messages.getString("NamespacePrefixes.dialog.delete.prefix"),
                            Messages.getString("NamespacePrefixes.dialog.description", prefixDel.getName()), Messages.getString("NamespacePrefixes.dialog.delete"), Messages.getString("NamespacePrefixes.dialog.cancel"),
                            new ConfirmDialog.Listener() {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onClose(ConfirmDialog cd) {
                                    if (cd.isConfirmed()) {

                                        namespacePrefixFacade.delete(prefixDel);
                                        refreshData();

                                    }
                                }
                            });

                }
            });
            layout.addComponent(deleteButton);

            return layout;
        }
    }

    private class filterDecorator extends IntlibFilterDecorator {

        private static final long serialVersionUID = 1L;

        @Override
        public String getEnumFilterDisplayName(Object propertyId, Object value) {

            return super.getEnumFilterDisplayName(propertyId, value);
        }
    };
}
