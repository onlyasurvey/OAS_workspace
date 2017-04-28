package com.oas.service.enterprise;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.SpringSecurityException;
import org.springframework.test.annotation.ExpectedException;

import com.oas.model.AccountOwner;
import com.oas.model.ContactUsMessage;
import com.oas.model.Survey;
import com.oas.model.enterprise.QuickStats;
import com.oas.util.EnterpriseRoles;

public class EnterpriseDashboardServiceTest extends AbstractEnterpriseServiceTest {

	@Autowired
	private EnterpriseDashboardService service;

	// ======================================================================

	@Test
	public void countContactMessages_Success() {

		createEnterpriseAdmin();

		persist(new ContactUsMessage(TEST_EMAIL, "message 1"));
		persist(new ContactUsMessage(TEST_EMAIL, "message 2"));
		persist(new ContactUsMessage(TEST_EMAIL, "message 3"));
		flushAndClear();

		assertTrue("expected count to include created messages", service.countContactUsMessages() >= 3);
	}

	// ======================================================================

	@Test
	public void findContactUsMessages_Success() {
		persist(new ContactUsMessage(TEST_EMAIL, "message 1"));
		persist(new ContactUsMessage(TEST_EMAIL, "message 2"));
		persist(new ContactUsMessage(TEST_EMAIL, "message 3"));
		flushAndClear();

		// requires security context with the appropriate role
		createEnterpriseAdmin();

		// invoke: page 1
		{
			Collection<ContactUsMessage> list = service.findContactUsMessages(0, 2);

			assertNotNull("expected list", list);
			assertTrue("expected list to have at exactly 2 items", list.size() == 2);
		}

		// invoke: page 2
		{
			Collection<ContactUsMessage> list = service.findContactUsMessages(1, 2);

			assertNotNull("expected list", list);
			assertTrue("expected list to have at least 1 item", list.size() > 0);
		}

		// invoke: page 1000000
		{
			Collection<ContactUsMessage> list = service.findContactUsMessages(1000000, 2);

			assertNotNull("expected list", list);
			assertTrue("expected list to be empty", list.size() == 0);
		}

	}

	@Test
	public void findContactUsMessages_Security_Fail_NoUser() {

		// no security context

		// invoke
		try {
			service.findContactUsMessages(0, 10);
			fail("Expected SpringSecurityException");
		} catch (SpringSecurityException e) {
			// expected
		}
	}

	@Test
	public void findContactUsMessages_Security_Fail_MissingRole() {

		// create user, but does not have required role
		createAndSetSecureUserWithRoleUser();

		// invoke
		try {
			service.findContactUsMessages(0, 10);
			fail("Expected access denied exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void findAccountOwners_Success() {

		// requires security context with the appropriate role
		createEnterpriseAdmin();

		// invoke: note it depends on another service call, which has it's own
		// test elsewhere
		int ownerCount = service.countAccountOwners();
		assertTrue("test data not sane: huge number of accounts exist: " + ownerCount, ownerCount < 100);
		Collection<AccountOwner> list = service.findAccountOwners(0, ownerCount);

		assertNotNull("expected list", list);
		assertTrue("expected list to have at least 1 item", list.size() >= 1);
		assertTrue("expected list to include current user", list.contains(getCurrentUser()));

	}

	@Test
	public void findAccountOwners_Security_Fail_NoUser() {

		// no security context

		// invoke
		try {
			service.findAccountOwners(0, 10);
			fail("Expected SpringSecurityException");
		} catch (SpringSecurityException e) {
			// expected
		}
	}

	@Test
	public void findAccountOwners_Security_Fail_MissingRole() {

		// create user, but does not have required role
		createAndSetSecureUserWithRoleUser();

		// invoke
		try {
			service.findAccountOwners(0, 10);
			fail("Expected access denied exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	@ExpectedException(AuthenticationCredentialsNotFoundException.class)
	public void findSurveysFor_Security_Fail_NoUser() {
		// no user
		service.findSurveysFor(createTestUser(), true);
	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void findSurveysFor_Security_Fail_NoRole() {
		// no without role
		createAndSetSecureUserWithRoleUser();
		service.findSurveysFor(createTestUser(), true);
	}

	@Test
	public void findSurveysFor_Success() {

		// create test data for other users
		scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		scenarioDataUtil.createTypicalScenario1(createTestUser(), true);

		// create test data to validate
		AccountOwner owner = createTestUser();
		Survey survey = scenarioDataUtil.createTypicalScenario1(owner, true);

		// create test data for other users
		scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		scenarioDataUtil.createTypicalScenario1(createTestUser(), true);

		//
		flushAndClear();

		// enterprise admin role required
		createAndSetSecureUser(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN);
		List<Survey> list = service.findSurveysFor(owner, true);
		assertNotNull("expected list", list);
		assertEquals("expected 1 result", 1, list.size());

		assertTrue(list.contains(survey));
	}

	// ======================================================================

	@Test
	public void getQuickStats_Success() {
		// enterprise admin role required
		createAndSetSecureUser(EnterpriseRoles.ROLE_ENTERPRISE_ADMIN);

		QuickStats quickStats = service.getQuickStats();
		// should ALWAYS return a value...
		assertNotNull("unable to retrieve enterprise-quick-stats", quickStats);
		// ...but the state of the database is not being tested here, so no
		// asserts on content
	}

	@Test(expected = AccessDeniedException.class)
	public void getQuickStats_Security_RoleRequired() {

		// enterprise admin role required but absent
		createAndSetSecureUserWithRoleUser();

		// will fail security check
		service.getQuickStats();
	}

	// ======================================================================

}
