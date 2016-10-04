package cz.cuni.mff.xrg.odcs.frontend.navigation;

/**
 * The navigator can not be created outside UI. For this reason we use this
 * object as a proxy. It will provide access to the navigator for the
 * application.
 * The navigator class must be set before first use, bet in the UI.init method.
 * 
 * @author Petyr
 */
public class ClassNavigatorHolder implements ClassNavigator {

    private ClassNavigator navigator;

    @Override
    public void navigateTo(String url) {
        navigator.navigateTo(url);
    }

    @Override
    public void navigateTo(Class<?> target) {
        navigator.navigateTo(target);
    }

    @Override
    public void navigateTo(Class<?> target, String parameters) {
        navigator.navigateTo(target, parameters);
    }

    /**
     * Sets navigator.
     * 
     * @param navigator
     *            Navigator to use.
     */
    public void setNavigator(ClassNavigator navigator) {
        this.navigator = navigator;
    }

    /**
     * Gets current navigator.
     * 
     * @return Navigator.
     */
    public ClassNavigator getNavigator() {
        return this.navigator;
    }

}
