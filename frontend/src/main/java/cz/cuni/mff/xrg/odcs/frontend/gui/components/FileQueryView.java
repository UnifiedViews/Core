package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * @author Bogo
 */
public class FileQueryView extends QueryView {

    /**
     * Constructor.
     */
    public FileQueryView() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setHeight("240px");

        final Label label = new Label(Messages.getString("FileQueryView.browser.not.available"), ContentMode.HTML);
        mainLayout.addComponent(label);
        mainLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

        setCompositionRoot(mainLayout);
    }

    @Override
    public void browseDataUnit() {

    }

    @Override
    public void setQueryingEnabled(boolean enabled) {

    }

    @Override
    public void reset() {

    }

}
