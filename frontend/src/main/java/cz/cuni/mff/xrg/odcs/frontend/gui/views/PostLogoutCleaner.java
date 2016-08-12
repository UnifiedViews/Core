package cz.cuni.mff.xrg.odcs.frontend.gui.views;

/**
 * Base interface for classes that need to do something after logout. For example clear
 * cached user data.
 * <br><br>
 * NOTE: !!! components implementing this need to have <b>session scope</b> !!!
 * 
 * @author mvi
 *
 */
public interface PostLogoutCleaner {

	/**
	 * this method is called automatically for any class implementing this interface 
	 */
	public void doAfterLogout();
}
