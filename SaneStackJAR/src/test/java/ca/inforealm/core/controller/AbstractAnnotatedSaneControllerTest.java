package ca.inforealm.core.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.UserAccount;

public class AbstractAnnotatedSaneControllerTest extends AbstractTestDataCreatingBaseTest {

	@Controller
	private class SampleController extends AbstractAnnotatedSaneController {
		@Override
		@RequestMapping("/index.html")
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

	// ======================================================================

	@Test
	public void testGetId_Pass() {
		createAndSetSecureUserWithRoleUser();
		SampleController c = new SampleController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("id", "44");
		Long id = c.getId(request);
		assertEquals("should have correct ID", new Long(44L), id);
	}

	@Test
	public void testGetId_FailsIfNotValidNumber() {
		createAndSetSecureUserWithRoleUser();
		SampleController c = new SampleController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("id", "smoo44");
		try {
			c.getId(request);
			fail("should have thrown");
		} catch (NumberFormatException nfe) {
			// expected
		}
	}

	@Test
	public void testGetId_NullIfNone() {
		createAndSetSecureUserWithRoleUser();
		SampleController c = new SampleController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		Long id = c.getId(request);
		assertNull("id should be null", id);
	}
}
