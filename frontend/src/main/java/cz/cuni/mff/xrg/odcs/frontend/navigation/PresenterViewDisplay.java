package cz.cuni.mff.xrg.odcs.frontend.navigation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SingleComponentContainer;

import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Can display {@lnk View} as well as {@link PresenterWrap} in {@link ComponentContainer}.
 * 
 * @author Petyr
 */
class PresenterViewDisplay implements ViewDisplay {

    private static final Logger LOG =
            LoggerFactory.getLogger(PresenterViewDisplay.class);

    private SingleComponentContainer container = null;

    public PresenterViewDisplay(SingleComponentContainer container) {
        this.container = container;
    }

    @Override
    public void showView(View view) {
        if (container == null) {
            // nothing happen
            LOG.error("container is null, showView ignored");
            return;
        }
        if (container instanceof Panel) {
            ((Panel) container).setScrollLeft(0);
        }
        if (view instanceof Component) {
            container.setContent((Component) view);
        } else if (view instanceof PresenterWrap) {
            PresenterWrap wrap = (PresenterWrap) view;
            // enter presenter
            Object presenterView = wrap.enterPresenter();
            if (presenterView instanceof Component) {
                container.setContent((Component) presenterView);
            } else {
                // unknown class
                throw new IllegalArgumentException(Messages.getString("PresenterViewDisplay_no.component")
                        + wrap.getPresenter());
            }
        } else {
            // unknown class
            throw new IllegalArgumentException(Messages.getString("PresenterViewDisplay_view.not.component")
                    + view);
        }
    }

}
