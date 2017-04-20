package com.oas.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractSignupRelatedTest;
import com.oas.command.model.SignupCommand;
import com.oas.model.AccountOwner;

public class SignupControllerTest extends AbstractSignupRelatedTest {

	@Autowired
	private SignupController controller;

	// ======================================================================

	/**
	 * No existing security context, shows form.
	 */
	@Test
	public void testDoForm_Success() {
		// no security context

		ModelAndView mav = controller.doForm(new MockHttpServletRequest());
		assertNotNull("no mav returned", mav);
		assertNotNull("no view name", mav.getViewName());
		assertEquals("wrong view name", "/signup/signupForm", mav.getViewName());
	}

	/**
	 * Has existing security context (user logged in), redirects to default home
	 * page.
	 */
	@Test
	public void testDoForm_Success_RedirectIfLoggedIn() {
		createAndSetSecureUserWithRoleUser();

		ModelAndView mav = controller.doForm(new MockHttpServletRequest());
		assertNotNull("no mav returned", mav);
		assertNull("expected no view name", mav.getViewName());
		assertIsRedirect("should be redirect", mav);
	}

	// ======================================================================

	@Test
	public void testSubmit_Success() {
		SignupCommand command = newValidCommand();
		command.setUsername("uniqueUsername" + getMBUN());

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull("no mav", mav);
		assertIsRedirect("should be redirect", mav);
	}

	// ======================================================================

	@Test
	public void testSubmit_Fail_EmailTaken() {

		AccountOwner otherUser = (AccountOwner) createTestUser();

		SignupCommand command = newValidCommand();
		command.setEmail(new String[] { otherUser.getEmail(), otherUser.getEmail() });

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull(mav);
		assertEquals("should be form view", "/signup/signupForm", mav.getViewName());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have 1 or more error", errors.getErrorCount() > 0);
	}

	@Test
	public void testSubmit_Fail_UsernameTaken() {

		UserAccount otherUser = createTestUser();

		SignupCommand command = newValidCommand();
		command.setUsername(otherUser.getUsername());

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull(mav);
		assertEquals("should be form view", "/signup/signupForm", mav.getViewName());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have 1 or more error", errors.getErrorCount() > 0);
	}

	@Test
	public void testSubmit_Fail_UsernameEmpty() {
		SignupCommand command = newValidCommand();
		command.setUsername("");

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull(mav);
		assertEquals("should be form view", "/signup/signupForm", mav.getViewName());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have 1 or more error", errors.getErrorCount() > 0);
	}

	@Test
	public void testSubmit_Fail_AnyPasswordEmpty() {
		SignupCommand command = newValidCommand();
		command.setPassword(new String[0]);

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull(mav);
		assertEquals("should be form view", "/signup/signupForm", mav.getViewName());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have 1 or more error", errors.getErrorCount() > 0);
	}

	@Test
	public void testSubmit_Fail_PasswordTooShort() {
		SignupCommand command = newValidCommand();
		command.setPassword(new String[] { "a", "a" });

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull(mav);
		assertEquals("should be form view", "/signup/signupForm", mav.getViewName());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have 1 or more error", errors.getErrorCount() > 0);
	}

	@Test
	public void testSubmit_Fail_PasswordsDiffer() {
		SignupCommand command = newValidCommand();
		command.setPassword(new String[] { "oneThing", "someOther" });

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest());
		assertNotNull(mav);
		assertEquals("should be form view", "/signup/signupForm", mav.getViewName());
		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("should have errors", errors);
		assertTrue("should have 1 or more error", errors.getErrorCount() > 0);
	}

	// ======================================================================

}
