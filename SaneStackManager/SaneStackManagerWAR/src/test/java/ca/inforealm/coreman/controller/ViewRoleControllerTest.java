package ca.inforealm.coreman.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.ActorRole;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.ViewRoleController;

public class ViewRoleControllerTest extends AbstractSaneManBaseTest {

	@Autowired
	private ViewRoleController viewRoleController;

	public void testFailsWithoutLogin() throws Exception {

		// clear security context
		clearSecurityContext();

		try {
			ModelAndView mav = viewRoleController.doViewRole(new MockHttpServletRequest(), new MockHttpServletResponse());
			fail("should have thrown");
		} catch (AccessDeniedException ise) {
			// expected
		}
	}

	public void testPass() throws Exception {

		// user needs to be have some role in this app to do anything..
		UserAccount user = createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER }, null);

		// some test application
		Application app1 = createTestApplication("barboo1" + getMBUN());
		RoleDefinition appUser = createRoleDefinition(GlobalRoles.ROLE_USER, app1);
		RoleDefinition appAdminRole = createRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN, app1);

		app1.getRoleDefinitions().add(appUser);
		app1.getRoleDefinitions().add(appAdminRole);

		// security context with appropriate roles: user needs to be an app
		// admin in the specified role's application to do anything
		getHibernateTemplate().persist(new ActorRole(user, appAdminRole));

		// need to set the test user as the current user for the purposes of the
		// security context
		// createSecureContext(user.getUsername());

		// 
		// check with the APP ADMIN role, returning 1 user
		//
		MockHttpServletRequest request = new MockHttpServletRequest();

		{
			request.setParameter("id", appAdminRole.getId().toString());
			ModelAndView mav = viewRoleController.doViewRole(request, new MockHttpServletResponse());

			// model and view
			assertNotNull("should have mav", mav);

			// model

			// the app
			assertNotNull("should have model", mav.getModel());
			Application appFromModel = (Application) mav.getModel().get("application");
			assertNotNull("should have Application in model", appFromModel);
			assertEquals("should have loaded right app", app1.getId(), appFromModel.getId());

			// the role
			RoleDefinition role = (RoleDefinition) mav.getModel().get("subject");
			assertNotNull("should have role as subject", role);
			assertEquals("should have specified role", appAdminRole.getId(), role.getId());

			// actors in role
			Collection<Actor> actors = (Collection<Actor>) mav.getModel().get("list");
			assertNotNull("should have list of actors", actors);
			assertEquals("should have one actor in role", 1, actors.size());

			// view
			assertEquals("should have right view", "/viewRole", mav.getViewName());
		}

		// 
		// check with the USER role, returning 0 users
		//
		{
			request.setParameter("id", appUser.getId().toString());
			ModelAndView mav = viewRoleController.doViewRole(request, new MockHttpServletResponse());
			Collection<Actor> actors = (Collection<Actor>) mav.getModel().get("list");
			assertEquals("should have zero actors in role", 0, actors.size());
			RoleDefinition role = (RoleDefinition) mav.getModel().get("subject");
			assertNotNull("should have role in model", role);
			assertEquals("should have specified role", appUser.getId(), role.getId());

			// view
			assertEquals("should have right view", "/viewRole", mav.getViewName());
		}
	}

	public void testFailsWithoutRoleInApp() throws Exception {

		// user needs to be have some role in this app to do anything..
		createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER }, null);

		// some test application
		Application app1 = createTestApplication("barboo1" + getMBUN());
		RoleDefinition appUser = createRoleDefinition(GlobalRoles.ROLE_USER, app1);
		RoleDefinition appAdminRole = createRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN, app1);

		app1.getRoleDefinitions().add(appUser);
		app1.getRoleDefinitions().add(appAdminRole);

		// security context with appropriate roles
		UserAccount user = createTestUser();
		getHibernateTemplate().persist(new ActorRole(user, appUser));

		try {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.addParameter("id", appUser.getId().toString());
			ModelAndView mav = viewRoleController.doViewRole(request, new MockHttpServletResponse());
			fail("should have thrown");
		} catch (AccessDeniedException ise) {
			// expected
		}
	}
}
