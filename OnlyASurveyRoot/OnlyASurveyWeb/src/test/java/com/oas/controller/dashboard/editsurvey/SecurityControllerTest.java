package com.oas.controller.dashboard.editsurvey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.model.Survey;
import com.oas.service.SupportedLanguageService;
import com.oas.service.SurveyService;

public class SecurityControllerTest extends AbstractOASBaseTest {

	@Autowired
	private SurveyService surveyService;

	@Autowired
	private SecurityController controller;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	// ======================================================================

	@Test
	public void determineSecurityRadio() {
		Survey survey = new Survey();

		// currently default
		{
			survey.setGlobalPassword(null);
			IdListCommand command = controller.determineSecurityRadio(survey);
			assertEquals("expected option 0 selected", Long.valueOf(0), command.getIds().get(0));
		}

		// currently one password per respondent
		{
			survey.setGlobalPassword(TEST_GLOBAL_PASSWORD);
			IdListCommand command = controller.determineSecurityRadio(survey);
			assertEquals("expected option 1 selected", Long.valueOf(1), command.getIds().get(0));
		}
	}

	@Test
	public void determineSecurityLabel() {
		Survey survey = new Survey();

		// currently default
		{
			survey.setGlobalPassword(null);
			String label = controller.determineSecurityLabel(survey);
			assertEquals("unexpected label", "securityTab.level.default", label);
		}

		// currently one password per respondent
		{
			survey.setGlobalPassword(TEST_GLOBAL_PASSWORD);
			String label = controller.determineSecurityLabel(survey);
			assertEquals("unexpected label", "securityTab.level.passwordPerSurvey", label);
		}
	}

	// ======================================================================

	@Test
	public void securityTabChangeSubmit_FromPWToDefault() {
		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		// currently has PW, set to default
		survey.setGlobalPassword(TEST_GLOBAL_PASSWORD);
		persist(survey);

		controller.securityTabChangeSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"),
				new IdListCommand(new Long[] { 0L }));
		flushAndClear();

		//
		assertIsNull("did not clear global password", get(Survey.class, survey.getId()).getGlobalPassword());
	}

	@Test
	public void securityTabChangeSubmit_FromDefaultToPW() {
		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		// currently default (no PW), set a password
		assertIsNull("test data defect", survey.getGlobalPassword());

		ModelAndView mav = controller.securityTabChangeSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId()
				+ ".html"), new IdListCommand(new Long[] { 1L }));
		flushAndClear();
		assertIsRedirect(mav);

		// should still be clear because with this option the user is
		// redirected to a form
		assertIsNull("global password should still be clear", get(Survey.class, survey.getId()).getGlobalPassword());
	}

	// ======================================================================

	@Test
	public void showTab_Success() {
		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());
		ModelAndView mav = controller.securityTab(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertHasViewNamePart(mav, "securityTab");
	}

	@Test
	public void showTab_Security_FailWrongUser() {
		assertFailsSecurityCheck(new GenericControllerCallback() {
			@Override
			public ModelAndView doCallback() {
				// owner
				createAndSetSecureUserWithRoleUser();
				// another user
				Survey survey = scenarioDataUtil.createScenario2(createTestUser());
				return controller.securityTab(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
			}
		});
	}

	// ======================================================================

	@Test
	public void passwordPrompt_Success() {
		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());
		ModelAndView mav = controller.passwordPrompt(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertHasViewNamePart(mav, "surveyPasswordForm");
	}

	@Test
	public void passwordPrompt_Security_FailWrongUser() {
		assertFailsSecurityCheck(new GenericControllerCallback() {
			@Override
			public ModelAndView doCallback() {
				// owner
				createAndSetSecureUserWithRoleUser();
				// another user
				Survey survey = scenarioDataUtil.createScenario2(createTestUser());
				return controller.passwordPrompt(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
			}
		});
	}

	// ======================================================================

	@Test
	public void passwordPromptSubmit_Success() {

		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		assertIsNull("test data defect", survey.getGlobalPassword());

		String password1 = TEST_GLOBAL_PASSWORD;
		String password2 = password1;

		ModelAndView mav = controller.passwordPromptSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId()
				+ ".html"), password1, password2);
		flushAndClear();

		assertIsRedirect(mav);

		assertEquals("did not set password", TEST_GLOBAL_PASSWORD, get(Survey.class, survey.getId()).getGlobalPassword());
	}

	@Test
	public void passwordPromptSubmit_Cancel() {

		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		assertIsNull("test data defect", survey.getGlobalPassword());

		String password1 = TEST_GLOBAL_PASSWORD;
		String password2 = password1;

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.setParameter("_lecnac", "");
		ModelAndView mav = controller.passwordPromptSubmit(request, password1, password2);
		flushAndClear();

		assertIsRedirect(mav);

		assertIsNull("should not have set the password", get(Survey.class, survey.getId()).getGlobalPassword());
	}

	@Test
	public void passwordPromptSubmit_Validation_Mismatch() {

		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		assertIsNull("test data defect", survey.getGlobalPassword());

		String password1 = TEST_GLOBAL_PASSWORD;
		String password2 = password1 + "other";

		ModelAndView mav = controller.passwordPromptSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId()
				+ ".html"), password1, password2);
		flushAndClear();

		assertNotRedirect(mav);

		assertHasViewNamePart(mav, "Form");
		assertModelHasAttribute(mav, "errors");
		Errors errors = (Errors) mav.getModel().get("errors");
		errors.getAllErrors().contains("securityTab.error.mismatch");

		assertIsNull("should not have set the password", get(Survey.class, survey.getId()).getGlobalPassword());
	}

	@Test
	public void passwordPromptSubmit_Validation_Empty() {

		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		assertIsNull("test data defect", survey.getGlobalPassword());

		String password1 = TEST_GLOBAL_PASSWORD;
		String password2 = "";

		ModelAndView mav = controller.passwordPromptSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId()
				+ ".html"), password1, password2);
		flushAndClear();

		assertNotRedirect(mav);

		assertHasViewNamePart(mav, "Form");
		assertModelHasAttribute(mav, "errors");
		Errors errors = (Errors) mav.getModel().get("errors");
		errors.getAllErrors().contains("securityTab.error.empty");

		assertIsNull("should not have set the password", get(Survey.class, survey.getId()).getGlobalPassword());
	}

	@Test
	public void passwordPromptSubmit_Validation_TooLong() {

		Survey survey = scenarioDataUtil.createScenario2(createAndSetSecureUserWithRoleUser());

		assertIsNull("test data defect", survey.getGlobalPassword());

		String password1 = String.format("%200s", ".");
		String password2 = password1;

		ModelAndView mav = controller.passwordPromptSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId()
				+ ".html"), password1, password2);
		flushAndClear();

		assertNotRedirect(mav);

		assertHasViewNamePart(mav, "Form");
		assertModelHasAttribute(mav, "errors");
		Errors errors = (Errors) mav.getModel().get("errors");
		errors.getAllErrors().contains("securityTab.error.passwordTooLong");

		assertIsNull("should not have set the password", get(Survey.class, survey.getId()).getGlobalPassword());
	}
}