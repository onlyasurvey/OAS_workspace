package com.oas.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractPreferencesRelatedTest;
import com.oas.command.model.PreferencesCommand;
import com.oas.model.AccountOwner;
import com.oas.util.Constants;

public class PreferencesControllerTest extends AbstractPreferencesRelatedTest {

	private static final String REDIRECT_URL = "/someOther.html";

	private static final String FORM_VIEW = "/preferences/preferences";

	@Autowired
	private PreferencesController controller;

	@Autowired
	private LocaleResolver localeResolver;

	// ======================================================================

	/**
	 * No existing security context, shows form.
	 */
	@Test
	public void testDoForm_Success() throws Exception {
		createAndSetSecureUserWithRoleUser();
		ModelAndView mav = controller.doForm(new MockHttpServletRequest());
		assertNotNull("no mav returned", mav);
		assertNotNull("no view name", mav.getViewName());
		assertEquals("wrong view name", FORM_VIEW, mav.getViewName());
	}

	@Test
	public void testDoForm_Security_FailNoUser() throws Exception {

		try {
			controller.doForm(new MockHttpServletRequest());
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	private void doSubmitAndExpectRedirect(PreferencesCommand command, String expectedRedirectUrl) throws Exception {

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		assertNotNull("no mav", mav);
		assertIsRedirect("should be redirect", mav, expectedRedirectUrl);
	}

	@Test
	public void testSubmit_Success_DefaultUrl() throws Exception {
		createAndSetSecureUserWithRoleUser();
		PreferencesCommand command = newValidCommand();
		doSubmitAndExpectRedirect(command, Constants.DEFAULT_HOME);
	}

	@Test
	public void testSubmit_Success_Cancel() throws Exception {
		createAndSetSecureUserWithRoleUser();
		PreferencesCommand command = newValidCommand();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter(CANCEL, "");
		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), request,
				new MockHttpServletResponse());
		assertNotNull("no mav", mav);
		assertIsRedirect("expected redirect", mav, Constants.DEFAULT_HOME);
	}

	@Test
	public void testSubmit_Success_ErrorsShowsForm() throws Exception {
		createAndSetSecureUserWithRoleUser();
		PreferencesCommand command = newInvalidCommand();

		ModelAndView mav = controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest(),
				new MockHttpServletResponse());
		assertNotNull("no mav", mav);
		assertNotRedirect(mav);

		Map<String, Object> model = getModel(mav);
		assertNotNull("no model", model);
		assertNotNull("no errors", model.get("errors"));
	}

	@Test
	public void testSubmit_Success_SpecifiedUrl() throws Exception {
		createAndSetSecureUserWithRoleUser();
		PreferencesCommand command = newValidCommand();
		command.setRedirectUrl(REDIRECT_URL);
		doSubmitAndExpectRedirect(command, REDIRECT_URL);
	}

	@Test
	public void testSubmit_Security_FailNoUser() throws Exception {

		PreferencesCommand command = newValidCommand();
		try {
			controller.doSubmit(command, new BindException(command, "command"), new MockHttpServletRequest(),
					new MockHttpServletResponse());
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	private void assertCurrentLanguage(MockHttpServletRequest request, String expectedCode) {
		// String userCode = LocaleContextHolder.getLocale().getISO3Language();
		String userCode = localeResolver.resolveLocale(request).getISO3Language();
		assertEquals("current language is incorrect", expectedCode, userCode);
	}

	@Test
	public void testDoChangeLanguage_Success_NoUser() {

		setEnglish();

		// no user

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fra.html");

		ModelAndView mav = controller.doChangeLanguage(request, new MockHttpServletResponse(), null, null);
		assertIsRedirect("expected redirect to default home", mav, Constants.DEFAULT_HOME);
		assertCurrentLanguage(request, "fra");
	}

	@Test
	public void testDoChangeLanguage_Success_Authenticated() {

		setEnglish();

		// some user
		createAndSetSecureUserWithRoleUser();
		assertEquals("unexpected language set for user", "eng", ((AccountOwner) getCurrentUser()).getLanguage().getIso3Lang());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fra.html");

		ModelAndView mav = controller.doChangeLanguage(request, new MockHttpServletResponse(), null, null);
		assertIsRedirect("expected redirect to default home", mav, Constants.DEFAULT_HOME);
		assertCurrentLanguage(request, "fra");
		assertEquals("unexpected language set for user", "fra", ((AccountOwner) getCurrentUser()).getLanguage().getIso3Lang());
	}

	@Test
	public void testDoChangeLanguage_Success_DifferentReturnUrls() {

		setEnglish();

		// no user

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fra.html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertIsRedirect("unexpected target URL", controller.doChangeLanguage(request, response, null, null),
				Constants.DEFAULT_HOME);
		assertIsRedirect("unexpected target URL", controller.doChangeLanguage(request, response, "/url", null), "/url");
		assertIsRedirect("unexpected target URL", controller.doChangeLanguage(request, response, null, "/rTo"), "/rTo");

	}

	@Test
	public void testDoChangeLanguage_Fail_InvalidLanguage() {

		final String invalidLanguage = "kli"; // ! kaplah

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/" + invalidLanguage + ".html");
		try {
			controller.doChangeLanguage(request, new MockHttpServletResponse(), null, null);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

}
