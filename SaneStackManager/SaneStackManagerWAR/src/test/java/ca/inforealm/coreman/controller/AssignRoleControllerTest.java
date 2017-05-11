package ca.inforealm.coreman.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.AssignRoleController;

public class AssignRoleControllerTest extends AbstractSaneManBaseTest {

	@Autowired
	private AssignRoleController assignRoleController;

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
			ModelAndView mav = assignRoleController.doAssignRole(request, response);
			fail("should have failed due to lack of security context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	/**
	 * With only a roleId specified there should be no actors in $list
	 */
	public void testBasicForm() {
		// the controller should fail without a security context and the
		// APP_ADMIN role
		createAndSetSecureUser(new String[] { GlobalRoles.ROLE_APPLICATION_ADMIN }, null);

		//

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// create a test user
		UserAccount user = createTestUser();

		// set the query to the test user's username
		request.setParameter("roleId", getSaneContext().getRoleDefinition(ROLE_USER).getId().toString());

		// ensure one user was returned
		ModelAndView mav = assignRoleController.doAssignRole(request, response);

		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getViewName());
		assertEquals("/assignRole", mav.getViewName());
		assertNotNull("no model", mav.getModel());

		Collection<Actor> list = (Collection<Actor>) mav.getModel().get("list");
		assertNotNull("invalid list", list);
		assertEquals("should have no records", 0, list.size());
	}

	/**
	 * If no actorId is specified then shows a search form, possibly with
	 * results based on ?q
	 */
	public void testQuery_Username() {

		// the controller should fail without a security context and the
		// APP_ADMIN role
		UserAccount user = createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER, GlobalRoles.ROLE_APPLICATION_ADMIN }, null);

		//

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// set the query to the test user's username
		request.setParameter("roleId", getSaneContext().getRoleDefinition(ROLE_USER).getId().toString());
		request.setParameter("q", user.getUsername());

		// ensure one user was returned
		ModelAndView mav = assignRoleController.doAssignRole(request, response);

		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getViewName());
		assertEquals("/assignRole", mav.getViewName());
		assertNotNull("no model", mav.getModel());
		assertNotNull("should have role in model", mav.getModel().get("subject"));

		Collection<Actor> list = (Collection<Actor>) mav.getModel().get("list");
		assertNotNull("invalid list", list);
		assertEquals("should have one record", 1, list.size());
		assertEquals("should have the test user", user.getUsername(), ((UserAccount) list.iterator().next()).getUsername());
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

		// set the query to the test user's username
		request.setParameter("roleId", getSaneContext().getRoleDefinition(ROLE_USER).getId().toString());
		request.setParameter("q", user.getUsername());

		//
		try {
			ModelAndView mav = assignRoleController.doAssignRole(request, response);
			fail("should have failed due to lack of APP ADMIN role in the target app");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testAssignRole_Pass() {
		// the controller should fail without a security context and the
		// APP_ADMIN role
		UserAccount user = createAndSetSecureUser(new String[] { GlobalRoles.ROLE_APPLICATION_ADMIN }, null);

		//

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// set the query to the test user's username
		Long roleId = getSaneContext().getRoleDefinition(ROLE_USER).getId();
		request.setParameter("roleId", roleId.toString());
		request.setParameter("actorId", user.getId().toString());

		assertFalse("should not yet have ROLE_USER", getRoleService().hasRole(ROLE_USER));

		// ensure one user was returned
		ModelAndView mav = assignRoleController.doAssignRole(request, response);

		// should now have role
		assertTrue("now should have ROLE_USER", getRoleService().hasRole(ROLE_USER));

		assertNotNull("no mav", mav);
		assertNull("should have no view", mav.getViewName());
		assertNotNull("should have a View object", mav.getView());
		assertTrue("should have a RedirectView", mav.getView() instanceof RedirectView);

		RedirectView rv = (RedirectView) mav.getView();
		assertNotNull("should redirect back to viewRole", "/viewRole.html?id=" + roleId.toString());
	}
}
