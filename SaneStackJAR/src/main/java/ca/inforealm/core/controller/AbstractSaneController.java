package ca.inforealm.core.controller;

import org.springframework.web.servlet.mvc.AbstractController;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.SecurityUtil;

/**
 * Convenience parent class for Web controllers.
 * 
 * @author Jason Mroz
 * 
 */
abstract public class AbstractSaneController extends AbstractController {

	/**
	 * Require a secure context, ensuring that calling code has an authenticated
	 * user.
	 */
	public void requireSecureContext() {
		SecurityUtil.requireSecureContext();
	}

	/**
	 * Get the current user from the security context; this returns the actual
	 * UserAccount in use.
	 * 
	 * @return
	 */
	public UserAccount getCurrentUser() {
		return SecurityUtil.getCurrentUser();
	}
}
