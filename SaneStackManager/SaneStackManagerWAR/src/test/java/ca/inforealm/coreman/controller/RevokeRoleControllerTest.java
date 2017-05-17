package ca.inforealm.coreman.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.RevokeRoleController;
import ca.inforealm.coreman.service.RoleManagementService;

public class RevokeRoleControllerTest extends AbstractSaneManBaseTest {

	@Autowired
	private RoleManagementService roleManagementService;

	@Autowired
	private RevokeRoleController revokeRoleController;

	protected RoleManagementService getRoleManagementService() {
		return roleManagementService;
	}

	public void testFailsWithoutSecureContext() {

		// the controller should fail without a security context
		clearSecurityContext();

		//

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// set the query to the test user's username
		request.setParameter("roleId", getSaneContext().getRoleDefinition(ROLE_USER).getId().toString());
		request.setParameter("q", "blah");

		// ensure one user was returned
		try {
			ModelAndView mav = revokeRoleController.doRevokeRole(request, response);
			fail("should have failed due to lack of security context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	/**
	 * Controller needs to fail if user is not an APP_ADMIN in the app owning
	 * the role
	 */
	public void testFailsWithoutAppAdmin() {

		// the controller should fail without a security context and the
		// APP_ADMIN role
		UserAccount user = createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER }, null);

		//

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// whatever data
		request.setParameter("roleId", getSaneContext().getRoleDefinition(ROLE_USER).getId().toString());
		request.setParameter("actorId", user.getId().toString());

		//
		try {
			ModelAndView mav = revokeRoleController.doRevokeRole(request, response);
			fail("should have failed due to lack of APP ADMIN role in the target app");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testRevokeRole_Pass() {
		// the controller should fail without a security context and the
		// APP_ADMIN role
		UserAccount user = createAndSetSecureUser(new String[] { GlobalRoles.ROLE_APPLICATION_ADMIN }, null);

		//

		// assign the role to the user
		getRoleManagementService().assignRole(user, getSaneContext().getRoleDefinition(ROLE_USER));

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// set the query to the test user's username
		Long roleId = getSaneContext().getRoleDefinition(ROLE_USER).getId();
		request.setParameter("roleId", roleId.toString());
		request.setParameter("actorId", user.getId().toString());

		assertTrue("should have ROLE_USER", getRoleService().hasRole(ROLE_USER));

		// ensure one user was returned
		ModelAndView mav = revokeRoleController.doRevokeRole(request, response);

		// role should have been revoked
		assertFalse("should have revoked ROLE_USER", getRoleService().hasRole(ROLE_USER));

		assertNotNull("no mav", mav);
		assertNull("should have no view", mav.getViewName());
		assertNotNull("should have a View object", mav.getView());
		assertTrue("should have a RedirectView", mav.getView() instanceof RedirectView);

		RedirectView rv = (RedirectView) mav.getView();
		assertNotNull("should redirect back to viewRole", "/viewRole.html?id=" + roleId.toString());
	}
}
