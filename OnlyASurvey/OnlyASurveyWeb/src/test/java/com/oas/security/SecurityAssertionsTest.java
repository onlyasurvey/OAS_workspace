package com.oas.security;

import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.AccessDeniedException;

import ca.inforealm.core.security.GlobalRoles;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.util.EnterpriseRoles;

public class SecurityAssertionsTest extends AbstractOASBaseTest {

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void testAssertOwnership_Success() {

		// create with a valid owner
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());

		// this will throw an exception on error
		SecurityAssertions.assertOwnership(survey);
	}

	@Test
	public void testAssertOwnership_Success_EnterpriseAdmin() {

		// create with a valid owner - but NOT the current user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());

		// Enterprise Admin
		createAndSetSecureUser(new String[] { EnterpriseRoles.ROLE_ENTERPRISE_ADMIN });

		// this will throw an exception on error
		SecurityAssertions.assertOwnership(survey);
	}

	@Test
	public void testAssertOwnershipFails_WithoutUser() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());
		try {
			SecurityAssertions.assertOwnership(survey);
			fail("expected access denied because no user in context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testAssertOwnershipFails_WithWrongUser() {
		createAndSetSecureUserWithRoleUser();
		// create with a different user as owner
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser());
		try {
			SecurityAssertions.assertOwnership(survey);
			fail("expected access denied because no user in context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testAssertEnterpriseAdmin_Success() {

		// create with a valid owner
		createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER, EnterpriseRoles.ROLE_ENTERPRISE_ADMIN }, null);

		// this will throw an exception on error
		SecurityAssertions.assertEnterpriseAdmin();
	}

	@Test
	public void testAssertEnterpriseAdmin_Fail_NoUser() {
		// no user in context0

		try {
			SecurityAssertions.assertEnterpriseAdmin();
			fail("expected exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

}
