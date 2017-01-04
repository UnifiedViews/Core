package cz.cuni.mff.xrg.odcs.frontend.navigation;

import com.vaadin.navigator.View;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Presenter;

/**
 * Interface for application navigator.
 * 
 * @author Petyr
 */
public interface ClassNavigator {

    /**
     * Navigate to given view.
     * 
     * @param url
     */
    void navigateTo(String url);

    /**
     * Navigate to given target ie. {@link Presenter} or {@link View}. The
     * object we are navigating to must have the @link Address} annotation.
     * 
     * @param target
     */
    void navigateTo(Class<?> target);

    /**
     * Navigate to given target ie. {@link Presenter} or {@link View}. The
     * object we are navigating to must have the @link Address} annotation.
     * 
     * @param target
     * @param parameters
     */
    void navigateTo(Class<?> target, String parameters);

}
