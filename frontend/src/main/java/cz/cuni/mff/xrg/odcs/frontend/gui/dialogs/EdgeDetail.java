package cz.cuni.mff.xrg.odcs.frontend.gui.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.*;

import cz.cuni.mff.xrg.odcs.commons.app.data.DataUnitDescription;
import cz.cuni.mff.xrg.odcs.commons.app.data.EdgeCompiler;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUExplorer;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph.Edge;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Window showing Edge detail. Used to create mappings between output and input
 * DataUnits of connected DPUs.
 * 
 * @author Bogo
 */
public class EdgeDetail extends Window {

    private final Edge edge;

    private List<DataUnitDescription> outputUnits;

    private List<DataUnitDescription> inputUnits;

    private List<MutablePair<Integer, Integer>> mappings;

    private Table outputSelect;

    private Table inputSelect;

    private ListSelect mappingsSelect;

    private HashMap<String, MutablePair<Integer, Integer>> map;

    private DPUExplorer explorer;

    private CheckBox chbRunAfterEdge;

    /**
     * Class for working with edge's script.
     */
    private EdgeCompiler edgeCompiler = new EdgeCompiler();

    /**
     * Basic constructor, takes {@link Edge} which detail should be showed.
     * 
     * @param e
     * @param readOnly
     * @param dpuExplorer
     */
    public EdgeDetail(Edge e, DPUExplorer dpuExplorer, boolean readOnly) {
        this.explorer = dpuExplorer;
        this.map = new HashMap<>();
        this.setResizable(false);
        this.setModal(true);
        this.edge = e;
        this.setCaption(Messages.getString("EdgeDetail.edge.detail"));

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setStyleName("dpuDetailMainLayout");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        final GridLayout edgeSettingsLayout = new GridLayout(3, 10);
        edgeSettingsLayout.setSpacing(true);

        chbRunAfterEdge = new CheckBox();
        chbRunAfterEdge.setCaption(Messages.getString("EdgeDetail.run.after"));
        mainLayout.addComponent(chbRunAfterEdge);
        chbRunAfterEdge.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                boolean enabled = !(Boolean) event.getProperty().getValue();
                edgeSettingsLayout.setEnabled(enabled);
                outputSelect.setEnabled(enabled);
                inputSelect.setEnabled(enabled);
                mappingsSelect.setEnabled(enabled);
            }
        });

        outputSelect = new Table(Messages.getString("EdgeDetail.output.dataUnits"));
        outputSelect.setSelectable(true);
        outputSelect.setMultiSelect(true);
        outputSelect.setNewItemsAllowed(false);
        outputSelect.setWidth(250, Unit.PIXELS);
        outputSelect.setImmediate(true);
        outputSelect.setPageLength(8);
        outputSelect.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        outputUnits = explorer.getOutputs(edge.getFrom().getDpuInstance());
        outputSelect.setContainerDataSource(getTableData(outputUnits));
        outputSelect.setVisibleColumns("name");

        edgeSettingsLayout.addComponent(outputSelect, 0, 0, 0, 4);

        ItemDescriptionGenerator tooltipGenerator = new ItemDescriptionGenerator() {
            @Override
            public String generateDescription(Component source, Object itemId,
                    Object propertyId) {
                if (propertyId == null) {
                    return null;
                } else if (propertyId == "name" && itemId.getClass() == DataUnitDescription.class) {
                    DataUnitDescription dataUnit = (DataUnitDescription) itemId;
                    return dataUnit.getDescription();
                }
                return null;
            }
        };

        outputSelect.setItemDescriptionGenerator(tooltipGenerator);

        inputSelect = new Table(Messages.getString("EdgeDetail.input.dataUnits"));
        inputSelect.setSelectable(true);
        inputSelect.setWidth(250, Unit.PIXELS);
        inputSelect.setNewItemsAllowed(false);
        inputSelect.setNullSelectionAllowed(false);
        inputSelect.setImmediate(true);
        inputSelect.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        inputSelect.setPageLength(8);

        //edgeCompiler.getInputNames(edge.getTo().getDpuInstance());
        inputUnits = explorer.getInputs(edge.getTo().getDpuInstance());

        inputSelect.setContainerDataSource(getTableData(inputUnits));
        inputSelect.setVisibleColumns("name");
        inputSelect.setItemDescriptionGenerator(tooltipGenerator);

        edgeSettingsLayout.addComponent(inputSelect, 1, 0, 1, 4);
        Button mapButton = new Button(Messages.getString("EdgeDetail.map"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                Set<DataUnitDescription> outputs = new HashSet<>(); //Set<String>)outputSelect.getValue();
                Collection<DataUnitDescription> outputItems = (Collection<DataUnitDescription>) outputSelect
                        .getItemIds();
                for (DataUnitDescription outputItem : outputItems) {
                    if (outputSelect.isSelected(outputItem)) {
                        outputs.add(outputItem);
                    }
                }
                DataUnitDescription input = (DataUnitDescription) inputSelect
                        .getValue();
                if (outputs.isEmpty() || input == null) {
                    Notification.show(
                            Messages.getString("EdgeDetail.minimum.selected"),
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }

                for (DataUnitDescription output : outputs) {
                    MutablePair<Integer, Integer> newMapping =
                            new MutablePair<>(outputUnits.indexOf(output), inputUnits.indexOf(input));

                    if (!input.getTypeName().equals(output.getTypeName())) {
                        Notification.show(
                                Messages.getString("EdgeDetail.incompatible.type"),
                                Notification.Type.WARNING_MESSAGE);
                        // and skipp given mapping
                        continue;
                    }

                    if (addMappingToList(newMapping)) {
                        mappings.add(newMapping);
                    } else {
                        Notification.show(Messages.getString("EdgeDetail.mapping.exists"),
                                Notification.Type.WARNING_MESSAGE);
                    }
                }
            }
        });
        mapButton.setWidth(130, Unit.PIXELS);
        mapButton.setEnabled(!readOnly);
        edgeSettingsLayout.addComponent(mapButton, 2, 1);

        Button clearButton = new Button(Messages.getString("EdgeDetail.clear"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        outputSelect.setValue(null);
                        inputSelect.setValue(null);
                    }
                });
        clearButton.setWidth(130, Unit.PIXELS);
        edgeSettingsLayout.addComponent(clearButton, 2, 2);

        mappingsSelect = new ListSelect(Messages.getString("EdgeDetail.available.mappings"));
        mappingsSelect.setStyleName("select-hide-tb");
        mappingsSelect.setWidth(500, Unit.PIXELS);
        mappingsSelect.setMultiSelect(true);
        mappingsSelect.setNewItemsAllowed(false);
        mappingsSelect.setImmediate(true);
        edgeSettingsLayout.addComponent(mappingsSelect, 0, 5, 1, 9);

        Button deleteButton = new Button(Messages.getString("EdgeDetail.detele"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        Set<String> selectedMappings = (Set<String>) mappingsSelect
                                .getValue();
                        for (String strMapping : selectedMappings) {
                            MutablePair<Integer, Integer> mapping = map
                                    .get(strMapping);
                            map.remove(strMapping);
                            mappingsSelect.removeItem(strMapping);
                            mappings.remove(mapping);
                        }
                    }
                });
        deleteButton.setWidth(130, Unit.PIXELS);
        deleteButton.setEnabled(!readOnly);
        edgeSettingsLayout.addComponent(deleteButton, 2, 6);
        mainLayout.addComponent(edgeSettingsLayout);

        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setSpacing(true);
        buttonBar.setWidth(100, Unit.PERCENTAGE);
        buttonBar.setMargin(new MarginInfo(true, false, false, false));

        Button cancelButton = new Button(Messages.getString("EdgeDetail.cancel"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        cancelButton.setWidth(100, Unit.PIXELS);
        buttonBar.addComponent(cancelButton);

        Button saveAndCommitButton = new Button(Messages.getString("EdgeDetail.save"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (save()) {
                            close();
                        }
                    }
                });
        saveAndCommitButton.setEnabled(!readOnly);
        saveAndCommitButton.setWidth(100, Unit.PIXELS);
        buttonBar.addComponent(saveAndCommitButton);

        Label placeFiller = new Label(" ");
        buttonBar.addComponentAsFirst(placeFiller);
        buttonBar.setExpandRatio(placeFiller, 1.0f);

        mainLayout.addComponent(buttonBar);

        setContent(mainLayout);
        setSizeUndefined();

        // * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
        // * * * * * * * * * load mapping  * * * * * * * * * * * * //
        // * * * * * * * * * * * * * * * * * * * * * * * * * * * * //

        // inputUnits and outputUnits are already set !		
        mappings = edgeCompiler.translate(edge.getScript(), outputUnits,
                inputUnits, null);

        for (MutablePair<Integer, Integer> mapping : mappings) {
            addMappingToList(mapping);
        }

        chbRunAfterEdge.setValue(edgeCompiler.isRunAfter(edge.getScript()));
    }

    /**
     * Saves configuration of Edge which was set in detail dialog.
     * 
     * @return True if save was successful, false otherwise.
     */
    protected boolean save() {
        if (!validate()) {
            return false;
        }

        if (chbRunAfterEdge.getValue()) {
            edge.setScript(edgeCompiler.createRunAfterMapping());
        } else {
            String script = edgeCompiler.translate(mappings, outputUnits, inputUnits, null);
            edge.setScript(script);
        }
        return true;
    }

    private boolean validate() {
        return true;
    }

    private Container getTableData(List<DataUnitDescription> data) {
        BeanItemContainer container = new BeanItemContainer(
                DataUnitDescription.class);
        container.addAll(data);
        return container;
    }

    private boolean addMappingToList(MutablePair<Integer, Integer> mapping)
            throws UnsupportedOperationException {
        // create string representation of mapping
        String strMapping = String.format("%s -> %s", outputUnits.get(
                mapping.left).getName(), inputUnits.get(mapping.right).getName());
        // add record to the mapping
        map.put(strMapping, mapping);
        // add to the component
        Item result = mappingsSelect.addItem(strMapping);
        return result != null;
    }

}
