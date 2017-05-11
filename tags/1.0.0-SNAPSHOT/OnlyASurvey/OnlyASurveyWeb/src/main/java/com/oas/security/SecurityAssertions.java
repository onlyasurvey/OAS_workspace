package com.oas.security;

import org.apache.log4j.Logger;
import org.springframework.security.AccessDeniedException;
import org.springframework.util.Assert;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.SecurityUtil;

import com.oas.model.Survey;
import com.oas.util.EnterpriseRoles;

/**
 * Assertions related to the user's roles and ownership of Surveys.
 * 
 * @author xhalliday
 * 
 */
public class SecurityAssertions {

	/** Log. */
	private static Logger log = Logger.getLogger(SecurityAssertions.class);

	/**
	 * Assert that the current user owns the given survey.
	 * 
	 * @param survey
	 * @throws AccessDeniedException
	 *             if the user does not own the survey, or if there is no
	 *             security context
	 */
	public static void assertOwnership(Survey survey) {

		// validate input
		Assert.notNull(survey);
		Assert.notNull(survey.getOwner());
		Assert.notNull(survey.getOwner().getId());

		// must be in a secure context
		SecurityUtil.requireSecureContext();

		// the current user
		UserAccount user = SecurityUtil.getCurrentUser();

		// sanity check
		Assert.notNull(user);

		if (user.getId().equals(survey.getOwner().getId())) {
			// current user owns the object
		} else {
			if (isEnterpriseAdmin()) {
				log.info("Enterprise Admin overriding security check (ownership of survey).");
			} else {
				// not the owner and not an enterprise admin
				log.error("survey ownership assertion failed for user #" + user.getId() + " on survey #" + survey.getId());
				throw new AccessDeniedException("not the owner");
			}
		}
	}

	/**
	 * Determine if the current user ROLE_ENTERPRISE_ADMIN.
	 */
	public static boolean isEnterpriseAdmin() {

		return SecurityUtil.isSecureContext() && SecurityUtil.hasAuthority(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN);
	}

	/**
	 * Require that ther ebe a current user, and that the user have
	 * ROLE_ENTERPRISE_ADMIN.
	 */
	public static void assertEnterpriseAdmin() {
		if (!isEnterpriseAdmin()) {
			String message = "BUG";
			if (SecurityUtil.isSecureContext()) {
				message = "Enterprise Admin role assertion failed for user #" + SecurityUtil.getCurrentUser().getId();
			} else {
				message = "Enterprise Admin role assertion failed for anonymous user";
			}

			log.error(message);

			throw new AccessDeniedException("not an Enterprise Admin");
		}
	}
}
