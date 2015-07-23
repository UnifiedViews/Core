/*******************************************************************************
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
 *******************************************************************************/
/*******************************************************************************
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
 *******************************************************************************/
package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.List;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.facade.NamespacePrefixFacade;
import cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace.NamespacePrefix;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.MaxLengthValidator;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Dialog for Namespace Prefix creation. Called from the {@link NamespacePrefixes}.
 * 
 * @author Maria Kukhar
 */
public class PrefixCreate extends Window {

    private static final long serialVersionUID = 1L;

    @AutoGenerated
    private GridLayout prefixDetailsLayout;

    private VerticalLayout mainLayout;

    private TextField prefixName;

    private TextField prefixUri;

    private NamespacePrefix prefix = null;

    private NamespacePrefix selectPrefix = null;

    private InvalidValueException ex;

    private List<NamespacePrefix> prefixes;

    private NamespacePrefixFacade prefixFacade;

    /**
     * The constructor should first build the main layout, set the composition
     * root and then do any custom initialization.
     * The constructor will not be automatically regenerated by the visual
     * editor.
     * 
     * @param newPrefix
     * @param namespacePrefixFacade
     */
    public PrefixCreate(boolean newPrefix, NamespacePrefixFacade namespacePrefixFacade) {
        this.prefixFacade = namespacePrefixFacade;
        this.setResizable(false);
        this.setModal(true);
        this.setCaption(Messages.getString("PrefixCreate.prefix.details"));

        buildMainLayout(newPrefix);
        this.setContent(mainLayout);
        setSizeUndefined();

    }

    /**
     * The method calls from {@link NamespacePrefixes} and sets the
     * corresponding values of Namespace Prefix to the dialog.
     * 
     * @param selectedPrefix
     *            NamespacePrefix that locate in the row of Namespace
     *            Prefixes table in which has been pressed the button Edit.
     */
    public void setSelectedPrefix(NamespacePrefix selectedPrefix) {
        prefixName.setValue(selectedPrefix.getName());
        prefixUri.setValue(selectedPrefix.getPrefixURI());

        selectPrefix = selectedPrefix;

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
        mainLayout.setWidth("370px");

        prefixes = prefixFacade.getAllPrefixes();

        prefixDetailsLayout = new GridLayout(2, 2);
        prefixDetailsLayout.setImmediate(false);
        prefixDetailsLayout.setSpacing(true);

        prefixName = new TextField();
        prefixName.setImmediate(true);
        prefixName.setWidth("250px");
        prefixName.addValidator(new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value.getClass() == String.class
                        && !((String) value).isEmpty()) {

                    String inputName = (String) value;
                    for (NamespacePrefix prefix : prefixes) {
                        if (prefix.getName().equals(inputName)) {
                            if (selectPrefix == null || (selectPrefix.getId() != prefix.getId())) {
                                ex = new InvalidValueException(
                                        Messages.getString("PrefixCreate.prefix.exists"));
                                throw ex;
                            }
                        }
                    }

                    return;
                }
                ex = new InvalidValueException(
                        Messages.getString("PrefixCreate.prefix.empty"));
                throw ex;
            }
        });
        prefixName.addValidator(new MaxLengthValidator(LenghtLimits.NAMESPACE_PREFIX_NAME));

        prefixDetailsLayout.addComponent(new Label(Messages.getString("PrefixCreate.prefix.name")), 0, 0);
        prefixDetailsLayout.addComponent(prefixName, 1, 0);

        prefixUri = new TextField();
        prefixUri.setImmediate(true);
        prefixUri.setWidth("250px");
        prefixUri.setValue("http://");
        prefixUri.addValidator(new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value.getClass() == String.class
                        && !((String) value).isEmpty()) {
                    String inputURI = (String) value;
                    if (!inputURI
                            .startsWith("http://")) {
                        ex = new InvalidValueException(Messages.getString("PrefixCreate.uri.wrong"));
                        throw ex;
                    }
                    return;
                }
                ex = new InvalidValueException(
                        Messages.getString("PrefixCreate.uri.empty"));
                throw ex;
            }
        });
        prefixUri.addValidator(new MaxLengthValidator(LenghtLimits.NAMESPACE_PREFIX_URI));

        prefixDetailsLayout.addComponent(new Label(Messages.getString("PrefixCreate.uri.prefix")), 0, 1);
        prefixDetailsLayout.addComponent(prefixUri, 1, 1);

        prefixDetailsLayout.setColumnExpandRatio(0, 0.3f);
        prefixDetailsLayout.setColumnExpandRatio(1, 0.7f);

        // Layout with buttons Save and Cancel
        HorizontalLayout buttonBar = new HorizontalLayout();

        // Save button
        Button createUser = new Button();
        createUser.setCaption(Messages.getString("PrefixCreate.save"));
        createUser.setWidth("90px");
        createUser.setImmediate(true);
        createUser.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                String errorText = "";
                // validation
                // prefix name, URI should be filled
                // URI should be in correct format
                try {
                    prefixName.validate();

                } catch (Validator.InvalidValueException e) {
                    errorText = errorText + e.getMessage();
                }

                try {
                    prefixUri.validate();

                } catch (Validator.InvalidValueException e) {
                    if (!errorText.equals("")) {
                        errorText = errorText + ", " + e.getMessage();
                    } else {
                        errorText = errorText + e.getMessage();
                    }
                }

                if (!errorText.equals("")) {
                    errorText = errorText + ".";
                    Notification.show(Messages.getString("PrefixCreate.save.failed"),
                            errorText, Notification.Type.ERROR_MESSAGE);
                    return;
                }

                // checking if the dialog was open from the Prefixes table
                // if no, create new prefix record

                if (newUser) {

                    prefix = prefixFacade.createPrefix(prefixName.getValue().trim(), prefixUri.getValue().trim());
                } else {
                    prefix = selectPrefix;
                    prefix.setName(prefixName.getValue().trim());
                    prefix.setPrefixURI(prefixUri.getValue().trim());
                }

                // store prefix record to DB
                prefixFacade.save(prefix);

                close();

            }
        });

        buttonBar.addComponent(createUser);

        Button cancelButton = new Button(Messages.getString("PrefixCreate.cancel"), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();

            }
        });
        cancelButton.setWidth("90px");
        buttonBar.addComponent(cancelButton);

        mainLayout.addComponent(prefixDetailsLayout);
        mainLayout.addComponent(buttonBar);

        return mainLayout;
    }
}
