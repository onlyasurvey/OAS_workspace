package ca.inforealm.coreman.service;

import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.service.ApplicationManagementService;

/**
 * Allows the application to manage application data.
 * 
 * @author Jason Mroz
 * 
 */
public class ApplicationManagementServiceTest extends AbstractSaneManBaseTest {

	@Autowired
	private ApplicationManagementService applicationManagementService;

	public void testGetApplicationsForAdmin_Pass() {

		// no action can take place if the user has zero roles in any
		// application, ie, getApplicationsForAdmin can't run, so the test user
		// must have at least one role.

		// not an admin in the app
		createAndSetSecureUserWithRoleUser();
		assertEquals("should have no existing admin privileges in any app", 0, applicationManagementService.getApplicationsForAdmin().size());

		// set as an app admin, conveniently in the existing sane context
		createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER, GlobalRoles.ROLE_APPLICATION_ADMIN }, null);
		assertEquals("should have app admin in one app", 1, applicationManagementService.getApplicationsForAdmin().size());

	}

	public void testGetApplicationsForAdmin_FailWithoutSecureContext() {

		assertFalse("can't run this test with existing security context", isSecureContext());

		try {
			applicationManagementService.getApplicationsForAdmin();

			fail("expected failure due to lack of security context");

		} catch (Exception ise) {
			// expected
		}
	}
}
