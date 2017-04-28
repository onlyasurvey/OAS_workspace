package ca.inforealm.core.security;

import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.util.Assert;

import ca.inforealm.core.model.UserAccount;

public class SecurityUtil {

	/**
	 * Determine if the current thread is operating in a secure context, meaning
	 * that calling code has an authenticated user with at least one role in the
	 * current application.
	 */
	public static boolean isSecureContext() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// no user
		if (auth == null || auth.getPrincipal() == null) {
			return false;
		}

		// invalid: should be, at least, empty role data
		Assert.state(auth.getAuthorities() != null, "no role data for user in this application (possible bug)");

		// must always have at least one role
		Assert.state(auth.getAuthorities().length > 0, "no roles for user in this application");

		//
		return true;
	}

	/**
	 * Require a secure context, ensuring that calling code has an authenticated
	 * user with at least one role in the current application.
	 */
	public static void requireSecureContext() {
		requireSecureContext(true);
	}

	/**
	 * Require a secure context, ensuring that calling code has an authenticated
	 * user.
	 */
	public static void requireSecureContext(boolean requireRoles) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Assert.state(auth != null, "no security context exists");
			Assert.state(auth.getPrincipal() != null, "no security principal is in context");

			// this is optional in some contexts, eg., when assigning a role
			if (requireRoles) {
				// must always have at least one role
				Assert.state(auth.getAuthorities() != null, "no role data for user in this application (possible bug)");
				Assert.state(auth.getAuthorities().length > 0, "no roles for user in this application");
			}
		} catch (IllegalStateException ise) {
			throw new AccessDeniedException(ise.getMessage(), ise);
		}
	}

	/**
	 * Get the current user from the security context; this returns the actual
	 * UserAccount in use.
	 * 
	 * @return
	 */
	public static UserAccount getCurrentUser() {
		try {
			UserAccount retval = null;

			// get the auth object
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Assert.state(auth != null, "no security context exists");

			// extract the principal, which is our user type
			retval = ((RealUserWrapper) auth.getPrincipal()).getRealUser();
			Assert.state(retval != null, "no principal or real user in context");

			// done
			return retval;
		} catch (IllegalStateException ise) {
			throw new AccessDeniedException(ise.getMessage(), ise);
		}
	}

	/**
	 * Determine if the current user has the specified role.
	 * 
	 * @param role
	 * @return boolean
	 */
	public static boolean hasAuthority(String role) {

		Assert.notNull(role, "no role specified");

		if (!isSecureContext()) {
			throw new AccessDeniedException("non-secure context");
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Assert.notNull(auth, "not authenticated");
		Assert.state(auth.isAuthenticated(), "auth data exists but not authenticated");

		GrantedAuthority[] roles = auth.getAuthorities();

		// handled by isSecureContext, here for DiD
		Assert.notEmpty(roles, "no roles");

		for (GrantedAuthority grantedAuthority : roles) {
			if (role.equals(grantedAuthority.getAuthority())) {
				// matched: we're done
				return true;
			}
		}

		// not found
		return false;
	}
}
