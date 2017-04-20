package com.oas.controller.dashboard;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.SupportedLanguage;
import com.oas.service.DashboardService;
import com.oas.service.SupportedLanguageService;

public class CreateSurveyControllerTest extends AbstractOASBaseTest {

	@Autowired
	private CreateSurveyController controller;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Test
	public void testReferenceData_DoMain_Success() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "na");

		createAndSetSecureUserWithRoleUser();
		ModelAndView mav = controller.doMain(request);
		// ModelAndView mav = controller.handleRequest(request, response);
		assertNotNull(mav);
		Collection<SupportedLanguage> languages = (Collection<SupportedLanguage>) mav.getModel().get("supportedLanguages");
		assertNotNull(languages);
		assertTrue("must have at least two languages", languages.size() >= 2);
	}

	@Test
	public void testSubmit_Success() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "na");

		// MUST always have some in system data
		Long languageId = supportedLanguageService.getSupportedLanguageIds().iterator().next();

		request.addParameter("ids", languageId.toString());

		createAndSetSecureUserWithRoleUser();

		ModelAndView mav = controller.doSubmit(request);
		assertNotNull(mav);

		// we should always get a redirect out with valid data
		assertTrue("should be a RedirectView", mav.getView().getClass().isAssignableFrom(RedirectView.class));
	}

	@Test
	public void testSubmit_Fail_NoLanguages() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "na");

		createAndSetSecureUserWithRoleUser();

		ModelAndView mav = controller.doSubmit(request);
		assertNotNull(mav);

		// view name should be the same as the form name
		assertEquals(mav.getViewName(), controller.getFormView());
	}

	@Test
	public void testDoMain_FailWithoutSecurityContext() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "na");

		try {
			controller.doMain(request);
			// controller.handleRequest(request, response);
			fail("should have thrown security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

}
