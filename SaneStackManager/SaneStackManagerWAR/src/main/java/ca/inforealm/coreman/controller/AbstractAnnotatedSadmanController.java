package ca.inforealm.coreman.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import ca.inforealm.core.controller.AbstractAnnotatedSaneController;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.service.ApplicationManagementService;
import ca.inforealm.coreman.service.ConfigurationManagementService;
import ca.inforealm.coreman.service.RoleManagementService;

abstract public class AbstractAnnotatedSadmanController extends AbstractAnnotatedSaneController {

	/**
	 * (Optional) Allows more refined access to the role data to ensure that
	 * application actions do not allow elevation of privileges.
	 */
	@Autowired
	protected RoleManagementService roleManagementService;

	/**
	 * (Optional) Provides access to the application management service.
	 */
	@Autowired
	protected ApplicationManagementService applicationManagementService;

	/**
	 * (Optional) Provides access to the configuration management service.
	 */
	@Autowired
	protected ConfigurationManagementService configurationManagementService;

	// ======================================================================

	protected void assertHasApplicationRole(Long applicationId, String roleIdentifier) {
		Assert.notNull(roleManagementService, "roleManagementService missing");
		roleManagementService.assertHasApplicationRole(applicationId, roleIdentifier);
	}

	/**
	 * Loads an application based on a request parameter specifying it's ID,
	 * ensuring the user has the ROLE_APPLICATION_ADMIN role for it.
	 * 
	 * @param request
	 * @return
	 */
	protected Application getRequestedApplication(HttpServletRequest request) {

		// require valid user and security context
		requireSecureContext();

		// ?id= must always be set
		requireIdParameter(request);

		// get the value
		Long id = getId(request);

		// ensure user is an app admin for the specified application
		assertHasApplicationRole(id, GlobalRoles.ROLE_APPLICATION_ADMIN);

		// load subject data into model and go
		Application subject = applicationManagementService.load(id);

		//
		return subject;
	}

	// ======================================================================

	/**
	 * @param roleManagementService
	 *            the roleManagementService to set
	 */
	@Autowired
	public void setRoleManagementService(RoleManagementService roleManagementService) {
		this.roleManagementService = roleManagementService;
	}

	/**
	 * @param applicationManagementService
	 *            the applicationManagementService to set
	 */
	@Autowired
	public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {
		this.applicationManagementService = applicationManagementService;
	}

}
