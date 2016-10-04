package cz.cuni.mff.xrg.odcs.frontend.gui.components;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

public class RelationalQueryView extends QueryView {

    private static final long serialVersionUID = 8249279688953599096L;

    public RelationalQueryView() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setHeight("240px");

        final Label label = new Label(Messages.getString("RelationalQueryView.browser.not.available"), ContentMode.HTML);
        mainLayout.addComponent(label);
        mainLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

        setCompositionRoot(mainLayout);
    }

    @Override
    public void browseDataUnit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setQueryingEnabled(boolean enabled) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
