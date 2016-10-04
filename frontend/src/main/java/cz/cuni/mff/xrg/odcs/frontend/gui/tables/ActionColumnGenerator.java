package cz.cuni.mff.xrg.odcs.frontend.gui.tables;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Generate column with action buttons. This class can be used just with one
 * table instance. Using with multiple instances may result in unexpected
 * behavior.
 * 
 * @author Petyr
 */
public class ActionColumnGenerator implements CustomTable.ColumnGenerator {

    /**
     * Abstract class for reaction on action button click.
     */
    public static abstract class Action implements Button.ClickListener {

        /**
         * Source of the event ie. the table.
         */
        protected CustomTable source;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            // in button.data the row id is stored
            action((long) event.getButton().getData());
        }

        /**
         * Action to invoke on click.
         * 
         * @param id
         *            Id of object to use in action.
         */
        protected abstract void action(long id);

    }

    /**
     * Interface for class that determines if the button is visible for given
     * item.
     */
    public static interface ButtonShowCondition {

        /**
         * Whether button should be showed.
         * 
         * @param source
         *            Source table.
         * @param id
         *            Id of object in row with the button.
         * @return Whether button should be showed
         */
        public boolean show(CustomTable source, long id);

    }

    /**
     * Contains information about single action button.
     */
    private class ActionButtonInfo {

        /**
         * Button name.
         */
        String name;

        /**
         * Button width.
         */
        String width;

        /**
         * Action to execute if button is clicked.
         */
        Action action;

        /**
         * Show condition, if null the button is always shown.
         */
        ButtonShowCondition showCondition;

        /**
         * Button icon(overrides name)
         */
        ThemeResource icon;

        public void setIcon(ThemeResource icon) {
            this.icon = icon;
        }

        ActionButtonInfo(String name, String width, Action action, ButtonShowCondition showCondition) {
            this.name = name;
            this.width = width;
            this.action = action;
            this.showCondition = showCondition;
        }
    }

    private final List<ActionButtonInfo> actionButtons = new LinkedList<>();

    @Override
    public Object generateCell(CustomTable source, Object itemId, Object columnId) {
        HorizontalLayout box = new HorizontalLayout();
        box.setSpacing(true);
        // add buttons
        for (ActionButtonInfo template : actionButtons) {
            if (template.showCondition == null
                    || template.showCondition.show(source, (Long) itemId)) {
                // we show button
                Button button = new Button();
                if (template.icon == null) {
                    button.setCaption(template.name);
                } else {
                    button.setDescription(template.name);
                    button.setIcon(template.icon);
                    button.addStyleName("small_button");
                }
                if (template.width != null) {
                    button.setWidth(template.width);
                }
                // set source }table]
                template.action.source = source;
                button.addClickListener(template.action);
                // set button data as id
                button.setData(itemId);
                // add to the component
                box.addComponent(button);
            } else {
                // do not show this button
            }
        }
        return box;
    }

    /**
     * Add template for action button.
     * 
     * @param name
     * @param width
     * @param action
     */
    public void addButton(String name, String width, Action action) {
        actionButtons.add(new ActionButtonInfo(name, width, action, null));
    }

    /**
     * Add template for action button.
     * 
     * @param name
     * @param width
     * @param action
     * @param showCondition
     */
    public void addButton(String name, String width, Action action, ButtonShowCondition showCondition) {
        actionButtons.add(new ActionButtonInfo(name, width, action, showCondition));
    }

    /**
     * Add template for action button with icon.
     * 
     * @param name
     * @param width
     * @param action
     * @param icon
     */
    public void addButton(String name, String width, Action action, ThemeResource icon) {
        ActionButtonInfo abi = new ActionButtonInfo(name, width, action, null);
        abi.setIcon(icon);
        actionButtons.add(abi);
    }

    /**
     * Add template for action button with icon.
     * 
     * @param name
     * @param width
     * @param action
     * @param showCondition
     * @param icon
     */
    public void addButton(String name, String width, Action action, ButtonShowCondition showCondition, ThemeResource icon) {
        ActionButtonInfo abi = new ActionButtonInfo(name, width, action, showCondition);
        abi.setIcon(icon);
        actionButtons.add(abi);
    }

}
