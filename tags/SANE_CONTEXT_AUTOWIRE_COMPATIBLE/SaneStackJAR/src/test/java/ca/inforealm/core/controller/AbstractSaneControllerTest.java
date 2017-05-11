package ca.inforealm.core.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.UserAccount;

public class AbstractSaneControllerTest extends AbstractTestDataCreatingBaseTest {

	private class SampleController extends AbstractSaneController {
		@Override
		protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Test
	public void testRequireSecureContext_FailsWithNone() {

		SampleController c = new SampleController();
		// no user
		clearSecurityContext();
		try {
			c.requireSecureContext();
			fail("should have failed due to lack of security context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testRequireSecureContext_FailWithNoRoles() {

		SampleController c = new SampleController();
		// user with no roles
		createAndSetSecureUser();
		try {
			c.requireSecureContext();
			fail("should have failed due to lack of security context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testRequireSecureContext_Pass() {

		SampleController c = new SampleController();
		// user with at least one role
		createAndSetSecureUser(new String[] { ROLE_USER }, null);

		// this call should not cause any failure
		c.requireSecureContext();
	}

	@Test
	public void testGetCurrentUser_EmptyWithNoSecurityContext() {
		// UserAccount user = createAndSetSecureUserWithRoleUser();
		clearSecurityContext();
		SampleController c = new SampleController();
		try {
			c.getCurrentUser();
			fail("should have failed due to lack of security context");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testGetCurrentUser_Pass() {
		UserAccount user = createAndSetSecureUserWithRoleUser();
		SampleController c = new SampleController();
		UserAccount actual = c.getCurrentUser();
		assertNotNull("should have received a user", actual);
		assertEquals("should be the same user", user.getId(), actual.getId());
	}
}
