package ca.inforealm.coreman.service;

import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.service.RoleManagementService;

/**
 * Allows the application to manage application data.
 * 
 * @author Jason Mroz
 * 
 */
public class RoleManagementServiceTest extends AbstractSaneManBaseTest {

	@Autowired
	private RoleManagementService roleManagementService;

	public void testNonExistingIdFails() {

		try {
			roleManagementService.load(993843L);
			fail("should have thrown since input was invalid");
		} catch (IllegalArgumentException iae) {
			// expected
		}
	}

	public void testPass() {
		Long id = getSaneContext().getRoleDefinition(GlobalRoles.ROLE_USER).getId();

		RoleDefinition rd = roleManagementService.load(id);

		assertNotNull("role should be loaded", rd);
		assertEquals("should have same ID", id, rd.getId());
	}

	public void testAssignRole() {

		final String someRole = GlobalRoles.ROLE_USER;

		UserAccount user = createAndSetSecureUser();
		RoleDefinition role = getSaneContext().getRoleDefinition(someRole);

		assertFalse("should not yet have the role", getRoleService().hasRole(someRole));

		roleManagementService.assignRole(user, role);
		assertTrue("now should have the role", getRoleService().hasRole(someRole));
	}

	public void testAssignRole_DupeAssignmentIgnored() {

		final String someRole = GlobalRoles.ROLE_USER;

		UserAccount user = createAndSetSecureUser();
		RoleDefinition role = getSaneContext().getRoleDefinition(someRole);

		assertFalse("should not yet have the role", getRoleService().hasRole(someRole));

		roleManagementService.assignRole(user, role);
		assertTrue("now should have the role", getRoleService().hasRole(someRole));

		// re-assign
		roleManagementService.assignRole(user, role);
		assertTrue("now should have the role", getRoleService().hasRole(someRole));
	}

	public void testRevokeRole() {
		final String someRole = GlobalRoles.ROLE_USER;

		UserAccount user = createAndSetSecureUser();
		RoleDefinition role = getSaneContext().getRoleDefinition(someRole);

		roleManagementService.assignRole(user, role);
		assertTrue("now should have the role", getRoleService().hasRole(someRole));

		roleManagementService.revokeRole(user, role);
		assertFalse("should not yet have the role", getRoleService().hasRole(someRole));
	}

	public void testRevokeRole_NonExistingAssignmentWorks() {
		final String someRole = GlobalRoles.ROLE_USER;

		UserAccount user = createAndSetSecureUser();
		RoleDefinition role = getSaneContext().getRoleDefinition(someRole);

		assertFalse("should not yet have the role", getRoleService().hasRole(someRole));
		// should silently ignore the fact that the user doesn't have the role
		roleManagementService.revokeRole(user, role);
		assertFalse("still should not yet have the role", getRoleService().hasRole(someRole));
	}
}
