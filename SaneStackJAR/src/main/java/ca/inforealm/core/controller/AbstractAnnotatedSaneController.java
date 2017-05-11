package ca.inforealm.core.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.SimpleFormController;

import ca.inforealm.core.SecureResource;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.SecurityUtil;

/**
 * Convenience parent class for Web controllers.
 * 
 * @author Jason Mroz
 * 
 */
abstract public class AbstractAnnotatedSaneController extends SimpleFormController implements SecureResource {

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

	// ======================================================================

	protected Long getId(HttpServletRequest request) {
		return getId(request, "id");
	}

	protected Long getId(HttpServletRequest request, String paramName) {
		// get the ID, using a simple cast since we're only supposed to be
		// called from generated links.
		String value = request.getParameter(paramName);
		if (value != null && value.length() > 0) {
			Long id = new Long(value);
			return id;
		}

		return null;
	}

	protected void requireIdParameter(HttpServletRequest request) {
		requireIdParameter(request, "id");
	}

	protected void requireIdParameter(HttpServletRequest request, String paramName) {
		String id = request.getParameter(paramName);
		Assert.hasLength(id, "id parameter required");
	}
}
