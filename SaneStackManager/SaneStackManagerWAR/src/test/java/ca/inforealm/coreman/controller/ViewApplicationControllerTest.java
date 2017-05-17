package ca.inforealm.coreman.controller;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.ViewApplicationController;

public class ViewApplicationControllerTest extends AbstractSaneManBaseTest {

	protected final static String TEST_APP_NAME = "TESTCASEAPPINDEED";

	public void testViewApp_Pass() {

		// test user
		createAndSetSecureUserWithRoleUser();

		// the object to test
		ViewApplicationController controller = (ViewApplicationController) getApplicationContext().getBean("viewApplicationController");

		// create app
		Application application = createTestApplication(TEST_APP_NAME + getMBUN());

		// create app admin role for app
		createRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN, application);

		// assign to current user
		assignRole(getCurrentUser(), GlobalRoles.ROLE_APPLICATION_ADMIN, application);

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// this is the application to load
		request.setParameter("id", application.getId().toString());

		// run it
		ModelAndView mav = controller.doViewApplication(request, response);
		assertNotNull(mav);
		assertNotNull(mav.getModel());

		// inspect application returned
		Application subject = (Application) mav.getModel().get("subject");
		assertNotNull("no application in model", subject);
	}

	public void testViewApp_RequiresSecurityContext() {

		// the object to test
		ViewApplicationController controller = (ViewApplicationController) getApplicationContext().getBean("viewApplicationController");

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// this is the application to load
		request.setParameter("id", "55");

		// run it
		try {
			controller.doViewApplication(request, response);
			fail("should not have allowed request with no user in context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testViewApp_RequiresAppAdminInTargetApplication() {

		// test user
		createAndSetSecureUserWithRoleUser();

		// the object to test
		ViewApplicationController controller = (ViewApplicationController) getApplicationContext().getBean("viewApplicationController");

		// create app
		Application application = createTestApplication(TEST_APP_NAME + getMBUN());

		// create app admin role for app but DO NOT assign it
		createRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN, application);

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// this is the application to load
		request.setParameter("id", application.getId().toString());

		// run it
		try {
			ModelAndView mav = controller.doViewApplication(request, response);
			fail("should never be allowed to view app unless user has ROLE_APPLICATION_ADMIN on *that* application");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testViewApp_FailsWithoutAppAdminRoleDefinition() {

		// test user
		createAndSetSecureUserWithRoleUser();

		// the object to test
		ViewApplicationController controller = (ViewApplicationController) getApplicationContext().getBean("viewApplicationController");

		// create app
		Application application = createTestApplication(TEST_APP_NAME + getMBUN());

		// DO NOT create app admin role for app

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// this is the application to load
		request.setParameter("id", application.getId().toString());

		// run it
		try {
			ModelAndView mav = controller.doViewApplication(request, response);
			fail("should never be allowed to view app because ROLE_APPLICATION_ADMIN is undefined on *that* application");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testViewApp_MissingIdFails() {

		// the object to test
		ViewApplicationController controller = (ViewApplicationController) getApplicationContext().getBean("viewApplicationController");

		// initialize security context
		createAndSetSecureUserWithRoleUser();

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// run it
		try {
			ModelAndView mav = controller.doViewApplication(request, response);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
