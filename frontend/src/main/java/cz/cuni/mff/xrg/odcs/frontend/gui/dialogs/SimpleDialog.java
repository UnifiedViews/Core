package cz.cuni.mff.xrg.odcs.frontend.gui.dialogs;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class SimpleDialog extends Window {
    private static final long serialVersionUID = -6088542008168348352L;

    public SimpleDialog(Component content) {
        // set dialog properties
        setModal(true);
        setResizable(true);
        // set initial size
        setWidth("640px");
        setHeight("640px");
        setContent(content);
    }

}
