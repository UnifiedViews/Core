package cz.cuni.xrg.intlib.frontend.gui;

import com.vaadin.ui.Button;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Wrapper for {@link Button.ClickListener} keeping reference to authentication
 * details, which were available when this wrapper was constructed.
 * 
 * <p>
 * It seems like {@link Button.ClickListener} is sometimes executed in
 * a dedicated thread, where authentication is lost. This wrapper should resolve
 * this situation by saving a local reference to {@link Authentication} while
 * it is constructed in authentication-aware thread. Then when event is
 * triggered, security context is filled with this authentication object.
 * For details about when such situation occurs see GH-415.
 * 
 * @author Jan Vojt
 */
public class AuthAwareButtonClickWrapper implements Button.ClickListener {
	
	/**
	 * Wrapped listener.
	 */
	private Button.ClickListener clickListener;
	
	/**
	 * Reference to authentication available during the construction of wrapper.
	 */
	private Authentication authentication;

	public AuthAwareButtonClickWrapper(Button.ClickListener clickListener) {
		this.clickListener = clickListener;
		authentication = SecurityContextHolder.getContext().getAuthentication();
	}
	
	@Override
	public void buttonClick(Button.ClickEvent event) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		clickListener.buttonClick(event);
	}

}