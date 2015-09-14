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
package cz.cuni.mff.xrg.odcs.frontend.gui.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

import cz.cuni.mff.xrg.odcs.commons.app.pipeline.transfer.ImportStrategy;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

public class ChooseImportStrategyDialog extends Window {

    private static final long serialVersionUID = 1121006224147759854L;
    private Map<String, OptionGroup> choosenStrategies;
    protected boolean okButtonClicked = false;

    public ChooseImportStrategyDialog(Set<String> toDecideDpus) {
        super(Messages.getString("ChooseImportStrategyDialog.caption"));
        init(toDecideDpus);
    }
    
    /**
     * Initialise user interface.
     */
    private void init(Set<String> toDecideDpus) {
        this.setResizable(false);
        this.setModal(true);
        this.setWidth("500px");
        this.setHeight("520px");
        this.markAsDirtyRecursive();
        
        choosenStrategies = new HashMap<String, OptionGroup>(toDecideDpus.size());
        
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();
        setContent(mainLayout);
        
        final Table table = new Table();
        table.setWidth(100, Unit.PERCENTAGE);
        table.setHeight(350, Unit.PIXELS);
        table.addContainerProperty(Messages.getString("ChooseImportStrategyDialog.column.name"), String.class, null);
        table.addContainerProperty(Messages.getString("ChooseImportStrategyDialog.column.options"), OptionGroup.class, null);

        final Label instructionLabel = new Label(Messages.getString("ChooseImportStrategyDialog.info.label"));
        instructionLabel.setWidth(100, Unit.PERCENTAGE);
        
        for (String string : toDecideDpus) {
            OptionGroup importStrategies = getOptions();
            
            
            choosenStrategies.put(string, importStrategies);
            
            table.addItem(new Object[] { string, importStrategies }, null);
        }
        
        final Button okBtn = new Button(Messages.getString("ChooseImportStrategyDialog.button.ok"), new Button.ClickListener() {
            private static final long serialVersionUID = -7158395217174873817L;

            @Override
            public void buttonClick(ClickEvent event) {
                okButtonClicked  = true;
                close();
            }
        });
        final Button cancelBtn = new Button(Messages.getString("ChooseImportStrategyDialog.button.cancel"), new Button.ClickListener() {
            private static final long serialVersionUID = -7158395217174873817L;

            @Override
            public void buttonClick(ClickEvent event) {
                close();
            }
        });
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth(100, Unit.PERCENTAGE);
        
        buttonLayout.addComponent(okBtn);
        buttonLayout.addComponent(cancelBtn);
        buttonLayout.setComponentAlignment(okBtn, Alignment.MIDDLE_CENTER);
        buttonLayout.setComponentAlignment(cancelBtn, Alignment.MIDDLE_CENTER);
        
        mainLayout.addComponent(instructionLabel);
        mainLayout.setExpandRatio(instructionLabel, 2);
        mainLayout.addComponent(table);
        mainLayout.setExpandRatio(table, 9);
        mainLayout.addComponent(buttonLayout);
        mainLayout.setExpandRatio(buttonLayout, 0);
        
        this.addCloseListener(new CloseListener() {
            private static final long serialVersionUID = 7630360741081498295L;
            
            @Override
            public void windowClose(CloseEvent e) {
                // the same as clicking close button
                if (!okButtonClicked) {
                    choosenStrategies.clear();
                }
                close();
            }
        });
    }

    private OptionGroup getOptions() {
        OptionGroup importStrategies = new OptionGroup();
        importStrategies.setMultiSelect(false);
        importStrategies.addItem(ImportStrategy.CHANGE_TO_EXISTING);
//        importStrategies.addItem(ImportStrategy.CREATE_NEW_CHILD);
//        importStrategies.addItem(ImportStrategy.OVERWRITE);
        importStrategies.addItem(ImportStrategy.REPLACE_INSTANCE_CONFIG);
        
        // setting labels
        importStrategies.setItemCaption(ImportStrategy.CHANGE_TO_EXISTING, Messages.getString("ChooseImportStrategyDialog.choice.CHANGE_TO_EXISTING"));
        importStrategies.setItemCaption(ImportStrategy.REPLACE_INSTANCE_CONFIG, Messages.getString("ChooseImportStrategyDialog.choice.REPLACE_INSTANCE_CONFIG"));

        importStrategies.setDescription(Messages.getString("ChooseImportStrategyDialog.choice.description"));
        
        importStrategies.select(ImportStrategy.REPLACE_INSTANCE_CONFIG);
        
        return importStrategies;
    }

    /**
     * 
     * @return strategies if OK button was clicked, empty map if Cancel button was clicked
     */
    public Map<String, ImportStrategy> getChoices() {
        Map<String, ImportStrategy> choices = new HashMap<>();
        for (Entry<String, OptionGroup> choice : choosenStrategies.entrySet()) {
            choices.put(choice.getKey(), (ImportStrategy) choice.getValue().getValue());
        }
        return choices;
    }
}
