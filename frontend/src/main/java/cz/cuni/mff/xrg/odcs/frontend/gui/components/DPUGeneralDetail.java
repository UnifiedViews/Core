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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.commons.app.conf.AppConfig;
import cz.cuni.mff.xrg.odcs.commons.app.conf.ConfigProperty;
import cz.cuni.mff.xrg.odcs.commons.app.conf.MissingConfigPropertyException;
import cz.cuni.mff.xrg.odcs.commons.app.constants.LenghtLimits;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPURecord;
import cz.cuni.mff.xrg.odcs.frontend.auxiliaries.MaxLengthValidator;
import cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPURecordWrap;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Component for setting general information about DPU like name and
 * description.
 * 
 * @author Škoda Petr
 */
public class DPUGeneralDetail extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(
            DPUGeneralDetail.class);

    private final TextField dpuName;

    private final TextField dpuTemplateName;

    private final TextArea dpuDescription;

    private final CheckBox useUserDescription;

    private final CheckBox useTemplateConfiguration;

    /**
     * True if the dialog content is read only.
     */
    private boolean isReadOnly = false;

    private AppConfig appConfig;

    private boolean dpuTemplateConfigEnabled = true;

    /**
     * Used to report change of insight property.
     */
    private ValueChangeListener valueChangeListener = null;

    public DPUGeneralDetail(final DPUConfigHolder instanceConfig, AppConfig appConfig) {
        setWidth("100%");
        setHeight("-1px");

        this.appConfig = appConfig;
        try {
            this.dpuTemplateConfigEnabled = this.appConfig.getBoolean(ConfigProperty.DPU_INSTANCE_USE_TEMPLATE_CONFIG);
        } catch (MissingConfigPropertyException e) {
            // ignore, optional configuration
        }

        // create subcomponents
        final GridLayout textInputLayout = new GridLayout(2, 3);
        textInputLayout.setWidth("100%");
        textInputLayout.setSpacing(true);
        {
            // we have to set the width, so the expansion works corectely
            Label lbl = new Label(Messages.getString("DPUGeneralDetail.name"));
            lbl.setWidth("80px");
            textInputLayout.addComponent(lbl, 0, 0);
        }

        dpuName = new TextField();
        //dpuName.setImmediate(false);
        dpuName.setWidth("100%");
        dpuName.setHeight(null);
        dpuName.setRequired(true);
        dpuName.setRequiredError(Messages.getString("DPUGeneralDetail.dpu.filled"));
        dpuName.addValidator(new MaxLengthValidator(LenghtLimits.DPU_NAME));
        textInputLayout.addComponent(dpuName, 1, 0);

        textInputLayout.addComponent(new Label(Messages.getString("DPUGeneralDetail.parent")), 0, 1);
        dpuTemplateName = new TextField();
        dpuTemplateName.setWidth("100%");
        dpuTemplateName.setHeight(null);
        dpuTemplateName.setEnabled(false);
        textInputLayout.addComponent(dpuTemplateName, 1, 1);

        textInputLayout.addComponent(new Label(Messages.getString("DPUGeneralDetail.description")), 0, 2);
        dpuDescription = new TextArea();
        dpuDescription.setWidth("100%");
        dpuDescription.setHeight("45px");
        textInputLayout.addComponent(dpuDescription, 1, 2);

        // expand column with the text boxes
        textInputLayout.setColumnExpandRatio(1, 1.0f);

        final HorizontalLayout optionsLayout = new HorizontalLayout();
        optionsLayout.setWidth("100%");
        optionsLayout.setHeight("-1px");

        useUserDescription = new CheckBox(Messages.getString("DPUGeneralDetail.custom.description"));
        useUserDescription.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                final boolean useUserDesc = useUserDescription.getValue();
                dpuDescription.setEnabled(useUserDesc);
            }
        });
        optionsLayout.addComponent(useUserDescription);

        useTemplateConfiguration = new CheckBox(Messages.getString("DPUGeneralDetail.template.configuration"));
        useTemplateConfiguration.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                boolean isUseTemplConf = (boolean) event.getProperty().getValue();
                instanceConfig.setEnabled(!isUseTemplConf);
            }
        });
        optionsLayout.addComponent(useTemplateConfiguration);
        this.useTemplateConfiguration.setVisible(this.dpuTemplateConfigEnabled);

        final VerticalLayout mainlayout = new VerticalLayout();
        mainlayout.setSizeFull();
        mainlayout.addComponent(textInputLayout);
        mainlayout.addComponent(optionsLayout);

        // set root
        setCompositionRoot(mainlayout);

        // set on change listener
        ValueChangeListener changeListener = new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                // just recall if set
                if (valueChangeListener != null && !isReadOnly) {
                    valueChangeListener.valueChange(event);
                }
            }
        };
        dpuName.addValueChangeListener(changeListener);
        dpuDescription.addValueChangeListener(changeListener);
        useUserDescription.addValueChangeListener(changeListener);
    }

    /**
     * Set listener that is called in case of change of any property.
     * 
     * @param valueChangeListener
     */
    public void setValueChangeListener(ValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    /**
     * Set values in component from the given {@link DPURecord}.
     * 
     * @param dpu
     * @param readOnly
     *            True if the component should be read only.
     */
    public void loadFromDPU(DPURecord dpu, boolean readOnly) {
        this.isReadOnly = readOnly;

        dpuName.setValue(dpu.getName());

        if (dpu instanceof DPUInstanceRecord) {
            DPUInstanceRecord instance = (DPUInstanceRecord) dpu;
            // use template name
            if (instance.getTemplate() == null) {
                dpuTemplateName.setValue(Messages.getString("DPUGeneralDetail.parent.not.set"));
                LOG.error("No parent for dpu instance id: {}", dpu.getId());
            } else {
                dpuTemplateName.setValue(instance.getTemplate().getName());
            }

            dpuTemplateName.setVisible(true);
            useTemplateConfiguration.setValue(instance.isUseTemplateConfig());
            useTemplateConfiguration.setVisible(this.dpuTemplateConfigEnabled);

        } else {
            dpuTemplateName.setVisible(false);
            useTemplateConfiguration.setVisible(false);
        }

        if (dpu.isUseDPUDescription()) {
            // generated description used
            dpuDescription.setValue("");
            dpuDescription.setEnabled(false);
        } else {
            dpuDescription.setValue(dpu.getDescription().trim());
            // we should be enabled, but we also have to respec readOnly
            dpuDescription.setEnabled(!readOnly);
        }
        useUserDescription.setValue(!dpu.isUseDPUDescription());

        dpuName.setEnabled(!readOnly);
        useUserDescription.setEnabled(!readOnly);
    }

    /**
     * Save the values from component into the given {@link DPURecord}.
     * 
     * @param dpu
     * @param wrap
     */
    public void saveToDPU(DPURecord dpu, DPURecordWrap wrap) {
        if (useUserDescription.getValue()) {
            // use user description
            final String userDescription = dpuDescription.getValue().trim();
            dpu.setDescription(userDescription);
            dpu.setUseDPUDescription(false);
        } else {
            final String generateDescription = wrap.getDescription();
            // if null then use empty, else use the value
            if (generateDescription == null) {
                dpu.setDescription("");
            } else {
                dpu.setDescription(generateDescription);
            }
            dpu.setUseDPUDescription(true);
        }

        if (dpu instanceof DPUInstanceRecord) {
            DPUInstanceRecord instance = (DPUInstanceRecord) dpu;
            instance.setUseTemplateConfig(useTemplateConfiguration.getValue());
        }

//		if (userDescription.isEmpty()) {
//			// get configuration from dialog
//			String dialogDescription = wrap.getDescription();
//			if (dialogDescription == null) {
//				// dialog description is not supported .. we have no 
//				// description at all
//				// so we use uset empty
//				dpu.setDescription("");
//				dpu.setUseDPUDescription(false);
//			} else {
//				// use dialogDescription - generate by configuration
//				// dialog
//				dpu.setDescription(dialogDescription);
//				dpu.setUseDPUDescription(true);
//			}
//		} else {
//			// use user provided description
//			dpu.setDescription(dpuDescription.getValue().trim());
//			dpu.setUseDPUDescription(true);
//		}

        dpu.setName(dpuName.getValue().trim());
        dpu.setMenuName(dpuName.getValue().trim());
    }

    /**
     * @param dpu
     * @return True if the data in component differ from those in given {@link DPURecord}.
     */
    public boolean isChanged(DPURecord dpu) {
        return !dpuName.getValue().equals(dpu.getName())
                || !dpuDescription.getValue().equals(dpu.getDescription());
    }

    /**
     * Validate the data in the component.
     * 
     * @return False is the data are invalid.
     */
    public boolean validate() {
        try {
            dpuName.validate();
            dpuDescription.validate();
        } catch (Validator.InvalidValueException e) {
            Notification.show(Messages.getString("DPUGeneralDetail.dpu.saving.error"), e
                    .getMessage(), Notification.Type.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

}
