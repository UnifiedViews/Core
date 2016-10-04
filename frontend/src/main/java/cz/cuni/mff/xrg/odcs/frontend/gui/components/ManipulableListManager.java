package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Manager for manipulating list of components. This
 * creates vaadin component with option to add (+ button)
 * or remove (- button) "rows". The class creating this
 * manager should implement {@link ManipulableListComponentProvider}.
 * This class manages the process of adding and removing components
 * from the list and not the components itself.
 * 
 * @author mvi
 */
public class ManipulableListManager {

    private String buttonWidth = "55px";

    private List<Component> componentList;

    private ManipulableListComponentProvider componentProvider;

    private GridLayout mainLayout;

    private Listener changeListener;

    public ManipulableListManager(ManipulableListComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    /**
     * @param list
     *            with components or null
     * @return
     */
    public GridLayout initList(List<Component> list) {
        this.componentList = list != null ? list : new LinkedList<Component>();

        mainLayout = new GridLayout();
        mainLayout.setSpacing(true);
        mainLayout.setImmediate(false);
        mainLayout.setMargin(false);
        mainLayout.setColumns(2);
        mainLayout.setColumnExpandRatio(0, 1f);

        refreshData();
        return mainLayout;
    }

    /**
     * refreshes the view, if there are no compoments added
     * or components are cleared, adds a empty one
     */
    public void refreshData() {
        mainLayout.removeAllComponents();

        if (componentList.size() < 1) {
            componentList.add(componentProvider.createNewComponent());
        }

        int row = 0;
        mainLayout.setRows(componentList.size() + 1);
        for (Component component : componentList) {

            final Button removeButton = new Button();
            removeButton.setEnabled(componentList.size() > 1);
            removeButton.setWidth(buttonWidth);
            removeButton.setCaption("-");
            removeButton.setData(row);
            removeButton.addClickListener(new Button.ClickListener() {
                private static final long serialVersionUID = 607166912536251164L;

                @Override
                public void buttonClick(ClickEvent event) {
                    Integer row = (Integer) event.getButton().getData();
                    removeComponent(row);
                    fireChangeEvent(removeButton);
                    refreshData();
                }
            });

            mainLayout.addComponent(component, 0, row);
            mainLayout.addComponent(removeButton, 1, row);
            row++;
        }

        // add button
        final Button addButton = new Button();
        addButton.setCaption("+");
        addButton.setImmediate(true);
        addButton.setWidth(buttonWidth);
        addButton.setHeight("-1px");
        addButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 3112570238711166625L;

            @Override
            public void buttonClick(ClickEvent event) {
                componentList.add(componentProvider.createNewComponent());
                fireChangeEvent(addButton);
                refreshData();
            }
        });
        mainLayout.addComponent(addButton, 0, row);
    }

    private void fireChangeEvent(Component component) {
        if (changeListener != null) {
            changeListener.componentEvent(new Event(component));
        }
    }

    /**
     * removes components in selected row
     * 
     * @param row
     */
    private void removeComponent(int row) {
        if (componentList.size() > 1) {
            componentList.remove(row);
        }
    }

    /**
     * retrieving component list
     * 
     * @return
     */
    public List<Component> getComponentList() {
        return componentList;
    }

    /**
     * clears all components
     */
    public void clearComponents() {
        this.componentList.clear();
    }

    /**
     * sets the components and refreshes the view
     * 
     * @param dataList
     */
    public void setComponentList(List<Component> dataList) {
        this.componentList = dataList;
        refreshData();
    }

    /**
     * refreshData() should be called after last added component
     * 
     * @param values
     */
    public void addComponent(String[] values) {
        this.componentList.add(componentProvider.createNewComponent(values));
    }

    public void setButtonWidth(String buttonWidth) {
        this.buttonWidth = buttonWidth;
        refreshData();
    }

    public void setChangeListener(Listener changeListener) {
        this.changeListener = changeListener;
    }
}
