package ca.inforealm.core.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.security.AccessDeniedException;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.RoleDefinition;

public class RoleServiceTest extends AbstractBaseTest {

	// ======================================================================

	@Test
	public void testHasRole_NullFails() {
		try {
			getRoleService().hasRole(null);
			fail("should have thrown an AccessDeniedException");
		} catch (AccessDeniedException iae) {
			// expected
		}
	}

	@Test
	public void testHasRole_EmptyFails() {
		try {
			getRoleService().hasRole("");
			fail("should have thrown an AccessDeniedException");
		} catch (AccessDeniedException iae) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testAssertHasRole_Fail() throws Exception {
		try {
			Application app1 = createTestApplication("application1");
			createRoleDefinition(ROLE_USER, app1);

			getSaneContext().setApplicationIdentifier(app1.getIdentifier());
			getSaneContext().reloadAllModels();

			// set a user
			createAndSetSecureUser();

			getRoleService().assertHasRole(ROLE_USER);

			fail("should have failed");
		} catch (AccessDeniedException ise) {
			// expected
		}
	}

	@Test
	public void testAssertHasRole_Pass() throws Exception {
		try {
			Application app1 = createTestApplication("application1");
			RoleDefinition app1role = createRoleDefinition(ROLE_USER, app1);

			getSaneContext().setApplicationIdentifier(app1.getIdentifier());
			getSaneContext().reloadAllModels();

			// set a user
			Collection<RoleDefinition> roles = new ArrayList<RoleDefinition>();
			roles.add(app1role);

			// create a test user with our test role, and set in security
			// context
			createAndSetSecureUser(roles, null);

			// assert!
			getRoleService().assertHasRole(ROLE_USER);

		} catch (AccessDeniedException ise) {
			// expected
			fail("should not have failed assertion");
		}
	}

	// ======================================================================

	/**
	 * Test that a user with a role in one application doesn't accidentally have
	 * that role leaked into another application.
	 */
	@Test
	public void testMultipleApplicationsDontLeak() throws Exception {

		final String SOME_ROLE = "ROLE_SOME_RANDOM_THING";
		final String SOME_OTHER_ROLE = "ROLE_OTHER_SOME_RANDOM_THING";

		Application app1 = createTestApplication("application1");
		createRoleDefinition(SOME_ROLE, app1);

		//
		// set SANE to app1
		//
		getSaneContext().setApplicationIdentifier(app1.getIdentifier());
		getSaneContext().reloadAllModels();

		// set a user and add a role
		createAndSetSecureUser(new String[] { SOME_ROLE }, null);

		assertTrue("should have been assigned " + SOME_ROLE, getRoleService().hasRole(SOME_ROLE));

		//
		// set SANE to app2
		//

		Application app2 = createTestApplication("application2");
		createRoleDefinition(SOME_ROLE, app2);
		createRoleDefinition(SOME_OTHER_ROLE, app2);

		getSaneContext().setApplicationIdentifier(app2.getIdentifier());
		getSaneContext().reloadAllModels();

		createAndSetSecureUser();

		// we added an ActorRole mapping to app1, not app2, therefore this
		// should return false
		assertFalse("should not yet have the role", getRoleService().hasRole(SOME_ROLE));

		// now assign some OTHER role, and ensure the user doesn't have the role
		// we originally assigned
		getRoleService().assignRole(SOME_OTHER_ROLE);
		assertFalse("should not yet have the roles", getRoleService().hasRole(SOME_ROLE));
		// sanity
		assertTrue("should have been assgned the 'OTHER' role", getRoleService().hasRole(SOME_OTHER_ROLE));

	}

	// ======================================================================

	@Test
	public void testGetRoles_Pass() throws Exception {

		Application app1 = createTestApplication("application1");
		RoleDefinition app1role = createRoleDefinition(ROLE_USER, app1);

		getSaneContext().setApplicationIdentifier(app1.getIdentifier());
		getSaneContext().reloadAllModels();

		// set a user
		Collection<RoleDefinition> roles = new ArrayList<RoleDefinition>();
		roles.add(app1role);

		// create a test user with our test role, and set in security
		// context
		createAndSetSecureUser(roles, null);

		Collection<String> roleList = getRoleService().getRoles();
		assertEquals("should have one role", 1, roleList.size());

		String expectedRole = roleList.iterator().next();
		assertEquals("should have appropriate role", ROLE_USER, expectedRole);

	}

	@Test
	public void testAllGetRoles_Pass() throws Exception {

		Application app1 = createTestApplication("application1");
		RoleDefinition app1role = createRoleDefinition(ROLE_USER, app1);

		getSaneContext().setApplicationIdentifier(app1.getIdentifier());
		getSaneContext().reloadAllModels();

		// set a user
		Collection<RoleDefinition> roles = new ArrayList<RoleDefinition>();
		roles.add(app1role);

		// create a test user with our test role, and set in security
		// context
		createAndSetSecureUser(roles, null);

		Collection<RoleDefinition> roleList = getRoleService().getAllRoleDefinitions();
		assertEquals("should have one role", 1, roleList.size());

		RoleDefinition expectedRole = roleList.iterator().next();
		assertEquals("should have appropriate role", ROLE_USER, expectedRole.getIdentifier());

	}

	// ======================================================================
}
