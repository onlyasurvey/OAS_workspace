package ca.inforealm.coreman.controller;

import java.util.Collection;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.ManageConfigurationController;

public class ManageConfigurationControllerTest extends AbstractSaneManBaseTest {

	protected final static String TEST_APP_NAME = "TESTCASEAPPINDEED";

	private Application createScenario1() {
		// create app
		Application application = createTestApplication(TEST_APP_NAME + getMBUN());

		createConfigItem(application, "someTestConfigItem" + getMBUN(), "someString");

		// create app admin role for app
		createRoleDefinition(GlobalRoles.ROLE_APPLICATION_ADMIN, application);

		// assign to current user
		assignRole(getCurrentUser(), GlobalRoles.ROLE_APPLICATION_ADMIN, application);

		return application;
	}

	public void testMain_Pass() {

		// test user
		createAndSetSecureUserWithRoleUser();

		// the object to test
		ManageConfigurationController controller = (ManageConfigurationController) getApplicationContext().getBean("manageConfigurationController");

		Application application = createScenario1();

		// initial size
		int initialSize = getConfigurationManagementService().getConfigurationItems(application.getId()).size();

		// add new
		getConfigurationManagementService().save(application.getId(), "someConfigItem" + getMBUN(), "fancyAjax");

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// this is the application to load
		request.setParameter("id", application.getId().toString());

		// run it
		ModelAndView mav = controller.doManageConfiguration(request, response);
		assertNotNull(mav);
		assertNotNull(mav.getModel());

		// inspect application returned
		Collection<ConfigurationItem> subject = (Collection<ConfigurationItem>) mav.getModel().get("subject");
		assertNotNull("no model", subject);
		assertEquals("should have two new items", initialSize + 1, subject.size());
	}

	public void testController_RequiresSecurityContext() {

		// the object to test
		ManageConfigurationController controller = (ManageConfigurationController) getApplicationContext().getBean("manageConfigurationController");

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// this is the application to load
		request.setParameter("id", "55");

		// run it
		try {
			controller.doManageConfiguration(request, response);
			fail("should not have allowed request with no user in context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testController_RequiresAppAdminInTargetApplication() {

		// test user
		createAndSetSecureUserWithRoleUser();

		// the object to test
		ManageConfigurationController controller = (ManageConfigurationController) getApplicationContext().getBean("manageConfigurationController");

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
			ModelAndView mav = controller.doManageConfiguration(request, response);
			fail("should never be allowed to view app unless user has ROLE_APPLICATION_ADMIN on *that* application");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testController_FailsWithoutAppAdminRoleDefinition() {

		// test user
		createAndSetSecureUserWithRoleUser();

		// the object to test
		ManageConfigurationController controller = (ManageConfigurationController) getApplicationContext().getBean("manageConfigurationController");

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
			ModelAndView mav = controller.doManageConfiguration(request, response);
			fail("should never be allowed to view app because ROLE_APPLICATION_ADMIN is undefined on *that* application");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	public void testController_MissingIdFails() {

		// the object to test
		ManageConfigurationController controller = (ManageConfigurationController) getApplicationContext().getBean("manageConfigurationController");

		// initialize security context
		createAndSetSecureUserWithRoleUser();

		// 
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// run it
		try {
			ModelAndView mav = controller.doManageConfiguration(request, response);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
