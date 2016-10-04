package cz.cuni.mff.xrg.odcs.frontend.navigation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cz.cuni.mff.xrg.odcs.frontend.gui.ViewComponent;
import cz.cuni.mff.xrg.odcs.frontend.gui.views.Presenter;

/**
 * Specify address under which the given object is accessible. Can be used
 * for {@link ViewComponent} and {@link Presenter}.
 * 
 * @author Petyr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Address {

    /**
     * Address under which the view can be accessed.
     * 
     * @return address under which the view can be accessed
     */
    public String url() default "";

}
