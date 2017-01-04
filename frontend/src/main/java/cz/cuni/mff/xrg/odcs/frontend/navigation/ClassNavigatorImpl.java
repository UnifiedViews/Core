package cz.cuni.mff.xrg.odcs.frontend.navigation;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;

import cz.cuni.mff.xrg.odcs.frontend.gui.views.Presenter;
import cz.cuni.mff.xrg.odcs.frontend.i18n.Messages;

/**
 * Extends Vaadin's {@link ClassNavigator} for possibility to navigate over {@link Presenter}s.
 * The navigator also cooperate with spring and do autodiscovery for beans that
 * implements {@link View} or {@link Presenter} and add them as views. The
 * views, that are obtained from spring, must have {@link Address} annotation.
 * 
 * @author Petyr
 */
public class ClassNavigatorImpl extends Navigator implements ClassNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(ClassNavigatorImpl.class);

    /**
     * Implementation of {@link ClassNavigator}.
     * 
     * @param ui
     *            UI to navigate.
     * @param container
     *            Container which holds the views.
     * @param context
     *            Application context.
     */
    public ClassNavigatorImpl(UI ui, SingleComponentContainer container, ApplicationContext context) {
        super(ui, new UriFragmentManager(ui.getPage()),
                new PresenterViewDisplay(container));
        // ..
        discoverViews(context);
        discoverPresenter(context);
    }

    @Override
    public void navigateTo(String url) {
        super.navigateTo(url);
    }

    @Override
    public void navigateTo(Class<?> target) {
        Address address = target.getAnnotation(Address.class);
        if (address == null) {
            throw new RuntimeException(Messages.getString("ClassNavigatorImpl.no.address")
                    + target);
        }
        // we have address, so we let others do the work instead of us 
        super.navigateTo(address.url());
    }

    @Override
    public void navigateTo(Class<?> target, String parameters) {
        Address address = target.getAnnotation(Address.class);
        if (address == null) {
            throw new RuntimeException(Messages.getString("ClassNavigatorImpl.no.address")
                    + target);
        }
        // we have address, so we let others do the work instead of us 
        super.navigateTo(address.url() + '/' + parameters);
    }

    private void discoverViews(ApplicationContext context) {
        Collection<View> views = context.getBeansOfType(View.class).values();
        if (views == null) {
            LOG.warn("No views has been found.");
        } else {
            for (View view : views) {
                Address address = view.getClass().getAnnotation(Address.class);
                if (address == null) {
                    LOG.error("Autowired '{}' does not have Address annotation, it will be ignored!", view);
                } else {
                    // add to navigator
                    LOG.debug("Adding view: {} for url: {}",
                            view.getClass().getSimpleName(), address.url());
                    addView(address.url(), view);
                }
            }
        }
    }

    private void discoverPresenter(ApplicationContext context) {
        Collection<Presenter> presenters = context.getBeansOfType(Presenter.class).values();
        if (presenters == null) {
            LOG.warn("No presenters has been found.");
        } else {
            for (Presenter presenter : presenters) {
                Address address = presenter.getClass().getAnnotation(Address.class);
                if (address == null) {
                    LOG.error("Autowired '{}' does not have Address annotation, it will be ignored!", presenter);
                } else {
                    // create wrap class
                    PresenterWrap wrap = new PresenterWrap(presenter);
                    // add to navigator
                    LOG.debug("Adding presenter: {} for url: {}",
                            wrap.getPresenter().getClass().getSimpleName(), address.url());
                    addView(address.url(), wrap);
                }
            }
        }
    }

}
