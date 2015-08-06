/**
 * This file is part of UnifiedViews.
 *
 * UnifiedViews is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UnifiedViews is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.facade.UserFacade;
import cz.cuni.mff.xrg.odcs.commons.app.user.EmailAddress;
import cz.cuni.mff.xrg.odcs.commons.app.user.Role;
import cz.cuni.mff.xrg.odcs.commons.app.user.RoleEntity;
import cz.cuni.mff.xrg.odcs.commons.app.user.User;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.MaxLengthValidator;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Dialog for new user creation. Called from the {@link UsersList}.
 * 
 * @author Maria Kukhar
 */
public class UserCreate extends Window {

    private static final long serialVersionUID = 1L;

    @AutoGenerated
    private GridLayout userDetailsLayout;

    private TwinColSelect roleSelector;

    private TextField userEmail;

    private TextField userFullName;

    private VerticalLayout mainLayout;

    private PasswordField password;

    private PasswordField passwordConfim;

    private TextField userName;

    private User user = null;

    private User selectUser = null;

    private Set<RoleEntity> roles = null;

    private List<User> users;

    private boolean passChanged = false;

    private static final String allowedcharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZXY0123456789";

    private UserFacade userFacade;

    /**
     * The constructor should first build the main layout, set the composition
     * root and then do any custom initialization. The constructor will not be
     * automatically regenerated by the visual editor.
     * 
     * @param newUser
     * @param userFacade
     */
    public UserCreate(boolean newUser, UserFacade userFacade) {
        this.userFacade = userFacade;
        this.setResizable(false);
        this.setModal(true);
        this.setCaption(Messages.getString("UserCreate.user.details"));

        buildMainLayout(newUser);
        this.setContent(mainLayout);
        setSizeUndefined();

    }

    /**
     * Builds main layout
     * 
     * @return mainLayout VerticalLayout with all dialog components
     */
    @AutoGenerated
    private VerticalLayout buildMainLayout(final boolean newUser) {

        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("425px");

        users = userFacade.getAllUsers();

        userDetailsLayout = new GridLayout(2, 5);
        userDetailsLayout.setImmediate(false);
        userDetailsLayout.setSpacing(true);

        userFullName = new TextField();
        userFullName.setImmediate(true);
        userFullName.setWidth("250px");
        userFullName.setNullRepresentation("");
        userFullName.addValidator(new MaxLengthValidator(
                LenghtLimits.USER_FULLNAME));
        userFullName.addValidator(new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value == null || value.getClass() != String.class || ((String) value).isEmpty()) {
                    throw new InvalidValueException(Messages.getString("UserCreate.full.empty"));
                }
            }
        });

        userDetailsLayout.addComponent(new Label(Messages.getString("UserCreate.full.user.name")), 0, 0);
        userDetailsLayout.addComponent(userFullName, 1, 0);

        userName = new TextField();
        userName.setImmediate(true);
        userName.setWidth("250px");
        userName.addValidator(new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value.getClass() == String.class
                        && !((String) value).isEmpty()) {

                    String inputName = (String) value;
                    for (User user : users) {
                        if (user.getUsername().equals(inputName)) {
                            if (selectUser == null
                                    || (selectUser.getId() != user.getId())) {
                                throw new InvalidValueException(
                                        Messages.getString("UserCreate.user.exists"));
                            }
                        }
                    }

                    return;
                }
                throw new InvalidValueException(
                        Messages.getString("UserCreate.user.filled"));
            }
        });
        userName.addValidator(new MaxLengthValidator(LenghtLimits.USER_NAME));

        userDetailsLayout.addComponent(new Label(Messages.getString("UserCreate.user.name")), 0, 1);
        userDetailsLayout.addComponent(userName, 1, 1);

        password = new PasswordField();
        password.setImmediate(true);
        password.setWidth("250px");
        password.addFocusListener(new FocusListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void focus(FocusEvent event) {
                password.setValue("");
                passwordConfim.setValue("");
                passChanged = true;
            }
        });

        Label passLabel = new Label(Messages.getString("UserCreate.password"));

        userDetailsLayout.addComponent(passLabel, 0, 2);
        userDetailsLayout.addComponent(password, 1, 2);

        passwordConfim = new PasswordField();
        passwordConfim.setImmediate(true);
        passwordConfim.setWidth("250px");
        Label confirmLabel = new Label(Messages.getString("UserCreate.password.confirmation"));
        passwordConfim.addFocusListener(new FocusListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void focus(FocusEvent event) {
                passwordConfim.setValue("");
                passChanged = true;
            }
        });

        confirmLabel.setContentMode(ContentMode.HTML);

        userDetailsLayout.addComponent(confirmLabel, 0, 3);
        userDetailsLayout.addComponent(passwordConfim, 1, 3);

        userEmail = new TextField();
        userEmail.setImmediate(true);
        userEmail.setWidth("250px");
        userEmail.addValidator(new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {

                if (value.getClass() == String.class
                        && !((String) value).isEmpty()) {
                    String inputEmail = (String) value;
                    if (!EmailValidator.getInstance().isValid(inputEmail)) {
                        throw new InvalidValueException(Messages.getString("UserCreate.wrong.email"));
                    }
                } else {
                    throw new InvalidValueException(Messages.getString("UserCreate.email.empty"));
                }
            }
        });

        userDetailsLayout.addComponent(new Label(Messages.getString("UserCreate.email")), 0, 4);
        userDetailsLayout.addComponent(userEmail, 1, 4);

        userDetailsLayout.setColumnExpandRatio(0, 0.3f);
        userDetailsLayout.setColumnExpandRatio(1, 0.7f);

        roleSelector = new TwinColSelect();

        for (RoleEntity role : userFacade.getAllRoles()) {
            roleSelector.addItem(role);
        }

        // roleSelector.addItem(Role.ROLE_ADMIN);
        // roleSelector.addItem(Role.ROLE_USER);

        roleSelector.setNullSelectionAllowed(true);
        roleSelector.setMultiSelect(true);
        roleSelector.setImmediate(true);
        roleSelector.setWidth("335px");
        roleSelector.setHeight("200px");
        roleSelector.setLeftColumnCaption(Messages.getString("UserCreate.roles.defined"));
        roleSelector.setRightColumnCaption(Messages.getString("UserCreate.roles.set"));
        roleSelector.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {

                Set<Object> selectedRoles = (Set<Object>) roleSelector
                        .getValue();
                Iterator<Object> it = selectedRoles.iterator();
                Set<Role> role = new HashSet<>();
                while (it.hasNext()) {
                    Object selectRole = it.next();
                    if (selectRole.equals(Role.ROLE_ADMIN)) {
                        role.add(Role.ROLE_ADMIN);
                        role.add(Role.ROLE_USER);
                        roleSelector.setValue(role);
                    }
                }

            }
        });
        // roleSelector is mandatory component
        roleSelector.addValidator(new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {

                if (newUser && "[]".equals(value.toString())) {
                    throw new InvalidValueException(Messages.getString("UserCreate.roles.atLeastOne"));
                }
            }
        });

        // Layout with buttons Save and Cancel
        HorizontalLayout buttonBar = new HorizontalLayout();
        // buttonBar.setMargin(true);

        // Save button
        Button createUser = new Button();
        createUser.setCaption(Messages.getString("UserCreate.save"));
        createUser.setWidth("90px");
        createUser.setImmediate(true);
        createUser.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void buttonClick(ClickEvent event) {
                String errorText = "";
                // validation
                // User name, password,e-mail and roles should be filled
                // email should be in correct format
                try {
                    userFullName.validate();

                } catch (Validator.InvalidValueException e) {
                    errorText = errorText + e.getMessage();
                }

                try {
                    userName.validate();

                } catch (Validator.InvalidValueException e) {
                    if (!errorText.equals("")) {
                        errorText = errorText + ", " + e.getMessage();
                    } else {
                        errorText = errorText + e.getMessage();
                    }
                }

                try {
                    userEmail.validate();

                } catch (Validator.InvalidValueException e) {
                    if (!errorText.equals("")) {
                        errorText = errorText + ", " + e.getMessage();
                    } else {
                        errorText = errorText + e.getMessage();
                    }
                }

                try {
                    roleSelector.validate();

                } catch (Validator.InvalidValueException e) {
                    if (!errorText.equals("")) {
                        errorText = errorText + ", " + e.getMessage();
                    } else {
                        errorText = errorText + e.getMessage();
                    }
                }

                if (!errorText.equals("")) {
                    errorText = errorText + ".";
                    Notification.show(Messages.getString("UserCreate.save.failed"),
                            errorText, Notification.Type.ERROR_MESSAGE);
                    return;
                }

                // checking if the dialog was open from the User table
                // if no, create new user record

                if (newUser) {
                    String userPassword;

                    if (passwordConfim.getValue().equals(password.getValue())) {
                        if (!passwordConfim.getValue().isEmpty()) {
                            userPassword = password.getValue();
                        } else {
                            Notification.show(Messages.getString("UserCreate.wrong.confirmation"), Messages.getString("UserCreate.password.empty"), Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        Notification.show(Messages.getString("UserCreate.wrong.confirmation2"), Messages.getString("UserCreate.password.different"), Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    EmailAddress email = new EmailAddress(userEmail.getValue()
                            .trim());
                    user = userFacade.createUser(userName.getValue().trim(),
                            userPassword, email);
                    if (userFullName.getValue() != null) {
                        user.setFullName(userFullName.getValue().trim());
                    }

                } else {
                    user = selectUser;
                    if (userFullName.getValue() != null) {
                        user.setFullName(userFullName.getValue().trim());
                    }
                    user.setUsername(userName.getValue().trim());
                    if (passChanged) {
                        if (passwordConfim.getValue().equals(
                                password.getValue())) {
                            if (!passwordConfim.getValue().isEmpty()) {
                                user.setPassword(password.getValue());
                            } else {
                                Notification.show(Messages.getString("UserCreate.wrong.confirmation3"), Messages.getString("UserCreate.password.empty2"), Notification.Type.ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            Notification.show(Messages.getString("UserCreate.wrong.confirmation4"), Messages.getString("UserCreate.password.different2"), Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    }

                    EmailAddress email = new EmailAddress(userEmail.getValue()
                            .trim());
                    user.setEmail(email);
                }

                @SuppressWarnings("unchecked")
                Set<RoleEntity> selectedRoles = (Set<RoleEntity>) roleSelector
                        .getValue();
                // Iterator<Object> it = selectedRoles.iterator();
                // roles = new HashSet<>();
                // while (it.hasNext()) {
                // Object selectRole = it.next();
                // if (selectRole.equals(Role.ROLE_ADMIN)) {
                // roles.add(Role.ROLE_ADMIN);
                // } else {
                // roles.add(Role.ROLE_USER);
                // }
                // }

                user.setRoles(new HashSet<RoleEntity>());
                user.getRoles().addAll(selectedRoles);

                // store user record to DB
                userFacade.save(user);

                close();

            }
        });

        buttonBar.addComponent(createUser);

        Button cancelButton = new Button(Messages.getString("UserCreate.cancel"), new Button.ClickListener() {
            /**
             * Closes Scheduling pipeline window
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();

            }
        });
        cancelButton.setWidth("90px");
        buttonBar.addComponent(cancelButton);

        mainLayout.addComponent(userDetailsLayout);
        mainLayout.addComponent(roleSelector);
        mainLayout.addComponent(buttonBar);
        // mainLayout.setComponentAlignment(buttonBar, Alignment.MIDDLE_RIGHT);

        return mainLayout;
    }

    /**
     * Sets the corresponding values of specific user to the dialog.
     * 
     * @param selectedUser
     *            . User that locate in the row of User table in which has been
     *            pressed the button Change settings.
     */
    public void setSelectedUser(User selectedUser) {
        userFullName.setValue(selectedUser.getFullName());
        userName.setValue(selectedUser.getUsername());
        password.setValue("*****");
        passwordConfim.setValue("*****");
        userEmail.setValue(selectedUser.getEmail().toString());
        roleSelector.setValue(selectedUser.getRoles());

        selectUser = selectedUser;

    }

    /**
     * Generate random password of 6 symbols a-zA-z0-9
     * 
     * @return password in string format
     */
    private String createPassword() {
        int passwordSize = 6;
        Random rnd = new Random();

        StringBuilder result = new StringBuilder();
        int randomIndex;
        while (passwordSize > 0) {
            randomIndex = rnd.nextInt(allowedcharacters.length());
            result.append(allowedcharacters.charAt(randomIndex));
            passwordSize--;
        }
        return result.toString();
    }
}
