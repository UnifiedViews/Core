package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecutionStatus;
import cz.cuni.mff.xrg.odcs.commons.app.user.RoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibFilterDecorator;
import cz.cuni.mff.xrg.odcs.frontend.gui.tables.IntlibPagedTable;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * GUI for User List which opens from the Administrator menu. Contains table
 * with users and button for user creation.
 * 
 * @author Maria Kukhar
 */
@Component
@Scope("prototype")
public class UsersList {

    private IntlibPagedTable usersTable;

    private VerticalLayout usersListLayout;

    private static String[] visibleCols = new String[] { "id", "fullname",
            "user", "role", "actions" };

    private static String[] headers = new String[] { Messages.getString("UsersList.id"), Messages.getString("UsersList.full.username"), Messages.getString("UsersList.username"), Messages.getString("UsersList.roles"),
            Messages.getString("UsersList.actions") };

    private IndexedContainer tableData;

    private Long userId;

    private User userDel;

    private UserCreate userEdit;

    @Autowired
    private UserFacade userFacade;

    /**
     * Builds users list layout.
     * 
     * @return built users list layout
     */
    public VerticalLayout buildUsersListLayout() {

        usersListLayout = new VerticalLayout();
        usersListLayout.setMargin(true);
        usersListLayout.setSpacing(true);
        usersListLayout.setWidth("100%");

        usersListLayout.setImmediate(true);

        // Layout for buttons Add new user and Clear Filters on the top.
        HorizontalLayout topLine = new HorizontalLayout();
        topLine.setSpacing(true);

        Button addUserButton = new Button();
        addUserButton.setCaption(Messages.getString("UsersList.user.create"));
        addUserButton.addStyleName("v-button-primary");
        addUserButton
                .addClickListener(new com.vaadin.ui.Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(ClickEvent event) {

                        boolean newUser = true;
                        // open usercreation dialog
                        UserCreate user = new UserCreate(newUser, userFacade);
                        UI.getCurrent().addWindow(user);
                        user.addCloseListener(new CloseListener() {
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
        buttonDeleteFilters.setCaption(Messages.getString("UsersList.filter.clear"));
        buttonDeleteFilters.setHeight("25px");
        buttonDeleteFilters.addStyleName("v-button-primary");
        buttonDeleteFilters
                .addClickListener(new com.vaadin.ui.Button.ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(ClickEvent event) {
                        usersTable.resetFilters();
                        usersTable.setFilterFieldVisible("actions", false);
                    }
                });
        topLine.addComponent(buttonDeleteFilters);
        usersListLayout.addComponent(topLine);

        tableData = getTableData(userFacade.getAllUsers());

        // table with pipeline execution records
        usersTable = new IntlibPagedTable();
        usersTable.setSelectable(true);
        usersTable.setContainerDataSource(tableData);
        usersTable.setWidth("100%");
        usersTable.setHeight("100%");
        usersTable.setImmediate(true);
        usersTable.setVisibleColumns((Object[]) visibleCols); // Set visible
                                                              // columns
        usersTable.setColumnHeaders(headers);
        usersTable.setColumnCollapsingAllowed(true);

        // Actions column. Contains actions buttons: Debug data, Show log, Stop.
        usersTable.addGeneratedColumn("actions", new actionColumnGenerator());

        usersListLayout.addComponent(usersTable);
        usersListLayout.addComponent(usersTable.createControls());
        usersTable.setPageLength(10);
        usersTable.setFilterDecorator(new filterDecorator());
        usersTable.setFilterBarVisible(true);
        usersTable.setFilterFieldVisible("actions", false);
        usersTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void itemClick(ItemClickEvent event) {

                if (!usersTable.isSelected(event.getItemId())) {
                    userId = (Long) event.getItem().getItemProperty("id")
                            .getValue();
                    showUserSettings(userId);
                }
            }
        });

        return usersListLayout;
    }

    /**
     * Container with data for {@link #usersTable}
     * 
     * @param data
     *            . List of users
     * @return result. IndexedContainer with data for users table
     */
    private IndexedContainer getTableData(List<User> data) {

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

        for (User item : data) {
            Object num = result.addItem();

            Set<RoleEntity> roles = item.getRoles();
            String roleStr = new String();
            int i = 0;
            for (RoleEntity role : roles) {
                i++;
                if (i < roles.size()) {
                    roleStr = roleStr + role.toString() + ", ";
                } else {
                    roleStr = roleStr + role.toString();
                }
            }

            result.getContainerProperty(num, "id").setValue(item.getId());
            result.getContainerProperty(num, "fullname").setValue(
                    item.getFullName());
            result.getContainerProperty(num, "user").setValue(
                    item.getUsername());
            result.getContainerProperty(num, "role").setValue(roleStr);

        }

        return result;

    }

    /**
     * Calls for refresh table {@link #schedulerTable}.
     */
    private void refreshData() {
        int page = usersTable.getCurrentPage();
        tableData = getTableData(userFacade.getAllUsers());
        usersTable.setContainerDataSource(tableData);
        usersTable.setCurrentPage(page);
        usersTable.setVisibleColumns((Object[]) visibleCols);
        usersTable.setFilterFieldVisible("actions", false);

    }

    /**
     * Shows dialog with user settings for given user.
     * 
     * @param id
     *            Id of user to show.
     */
    private void showUserSettings(Long id) {

        boolean newUser = false;
        // open usercreation dialog
        if (userEdit == null) {
            userEdit = new UserCreate(newUser, userFacade);
            userEdit.addCloseListener(new CloseListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void windowClose(CloseEvent e) {
                    refreshData();
                }
            });
        }
        User user = userFacade.getUser(id);
        userEdit.setSelectedUser(user);

        if (!UI.getCurrent().getWindows().contains(userEdit)) {
            UI.getCurrent().addWindow(userEdit);
        }
    }

    /**
     * Generate column "actions" in the table {@link #usersTable}.
     * 
     * @author Maria Kukhar
     */
    class actionColumnGenerator implements CustomTable.ColumnGenerator {

        private static final long serialVersionUID = 1L;

        @Override
        public Object generateCell(final CustomTable source,
                final Object itemId, Object columnId) {

            HorizontalLayout layout = new HorizontalLayout();

            // Edit button. Open dialog for edit user's details.
            Button changeButton = new Button();
            changeButton.setDescription(Messages.getString("UsersList.edit"));
            changeButton.addStyleName("small_button");
            changeButton.setIcon(new ThemeResource("icons/gear.svg"));
            // changeButton.setWidth("80px");
            changeButton.addClickListener(new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {

                    userId = (Long) tableData
                            .getContainerProperty(itemId, "id").getValue();
                    showUserSettings(userId);

                }
            });

            layout.addComponent(changeButton);

            // Delete button. Delete user's record from Database.
            Button deleteButton = new Button();
            deleteButton.setDescription(Messages.getString("UsersList.delete"));
            deleteButton.addStyleName("small_button");
            deleteButton.setIcon(new ThemeResource("icons/trash.svg"));
            // deleteButton.setWidth("80px");
            deleteButton.addClickListener(new ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(ClickEvent event) {
                    userId = (Long) tableData
                            .getContainerProperty(itemId, "id").getValue();
                    userDel = userFacade.getUser(userId);
                    //open confirmation dialog
                    ConfirmDialog.show(UI.getCurrent(), Messages.getString("UsersList.delete.confirmation"),
                            Messages.getString("UsersList.detele.message", userDel.getUsername()), Messages.getString("UsersList.delete.option"), Messages.getString("UsersList.cancel.option"),
                            new ConfirmDialog.Listener() {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void onClose(ConfirmDialog cd) {
                                    if (cd.isConfirmed()) {

                                        userFacade.delete(userDel);
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
            if (propertyId == "role") {
                return ((PipelineExecutionStatus) value).name();
            }
            return super.getEnumFilterDisplayName(propertyId, value);
        }
    };
}
