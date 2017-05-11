package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.model.Choice;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.service.SupportedLanguageService;
import com.oas.service.SurveyService;
import com.oas.util.Constants;

public class PublishingControllerTest extends AbstractOASBaseTest {

	@Autowired
	private SurveyService surveyService;

	@Autowired
	private PublishingController controller;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Test
	public void testShowTab_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.showTab(request);

		assertModelHasSurvey(mav, survey);
		assertHasViewName(mav, "/dashboard/manage/publishTab/publishTab");
	}

	@Test
	public void testSecurity_FailWrongUser() {
		// current user
		createAndSetSecureUserWithRoleUser();

		// some other user owns it
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		try {
			controller.showTab(request);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		try {
			controller.doPublishSubmit(request);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		try {
			controller.doDraftSubmit(request);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testPaymentCheck_IsPaidFor() {
		// valid data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		// must be paid for
		survey.setPaidFor(true);
		assertFalse("invalid test data", survey.isPublished());

		ModelAndView mav = controller.doPublishSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertNull("has view name (validation errors?)", mav.getViewName());
		assertNotNull(mav.getView());
		assertIsRedirect("should redirect to success view (errors exist from validation?)", mav, "/html/db/mgt/pb/sc/"
				+ survey.getId() + ".html");

		// 
		flushAndClear();

		// reload
		survey = surveyService.findNonDeletedSurvey(survey.getId());
		assertTrue("survey should now be published", survey.isPublished());
	}

	// TODO the controller current auto-pays as part of publishing
	// @Test public void testPaymentCheck_NotPaidFor() {
	// // valid data
	// Survey survey =
	// scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(),
	// true);
	//
	// // must be paid for but isn't
	// survey.setPaidFor(false);
	// assertFalse("invalid test data", survey.isPublished());
	//
	// ModelAndView mav = controller.doPublishSubmit(new
	// MockHttpServletRequest("GET",
	// "/prefix/" + survey.getId() + ".html"));
	// assertNotNull(mav);
	// assertNotNull(mav.getViewName());
	// assertEquals("should have Fail view (errors exist from validation?)",
	// "/dashboard/manage/publishTab/paymentNeeded", mav
	// .getViewName());
	// assertNotNull("paymentNeeded page must have notifyUrl attribute",
	// mav.getModel().get("notifyUrl"));
	//
	// //
	// flushAndClear();
	//
	// // reload
	// survey = surveyService.findRunningSurvey(survey.getId());
	// assertFalse("survey should NOT be published", survey.isPublished());
	// }

	@Test
	public void testSurveyTitleNotInAllLanguages() {
		// valid test data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser());
		survey.setPaidFor(true);

		assertEquals("expected 2 supported languages (scenario data)", 2, survey.getSupportedLanguages().size());
		assertEquals("expected titles in 2 languages (scenario data)", 2, survey.getObjectNames().size());
		// remove one
		survey.getObjectNames().clear();
		persist(survey);

		ModelAndView mav = controller.doPublishSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertNotNull("no view", mav.getViewName());
		assertEquals("should have Fail view", "/dashboard/manage/publishTab/errors", mav.getViewName());

		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("no errors object", errors);
		assertTrue("expected errors", errors.hasErrors());
		assertHasError(errors, "publish.error.surveyTitlesMissingForLanguage");

		// ensure clear session, fresh reload
		flushAndClear();

		// reload
		survey = surveyService.findNonDeletedSurvey(survey.getId());
		assertFalse("survey should NOT be published", survey.isPublished());
	}

	@Test
	public void testMessagesMissing() {

		final String[] keys = new String[] { "welcomeMessage", "thanksMessage", "pausedMessage", "closedMessage" };

		for (String key : keys) {

			// valid data
			Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
			assertFalse("invalid test data", survey.isPublished());
			survey.setPaidFor(true);

			// remove the key
			survey.removeObjectResource(key);

			persist(survey);
			flushAndClear();

			ModelAndView mav = controller
					.doPublishSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
			assertNotNull(mav);
			assertNotNull("no view", mav.getViewName());
			assertEquals("should have Fail view", "/dashboard/manage/publishTab/errors", mav.getViewName());

			Errors errors = (Errors) mav.getModel().get("errors");
			assertNotNull("no errors object", errors);
			assertTrue("expected errors", errors.hasErrors());

			// ensure clear session, fresh reload
			flushAndClear();

			// reload
			survey = surveyService.findNonDeletedSurvey(survey.getId());
			assertFalse("survey should NOT be published", survey.isPublished());

			// clear session
			flushAndClear();
		}
	}

	@Test
	public void testQuestionHasNoChoices() {
		// valid data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		surveyService.setSurveyLanguages(survey, new IdListCommand(new Long[] {
				supportedLanguageService.findByCode("eng").getId(), supportedLanguageService.findByCode("fra").getId() }));
		survey.addObjectName(supportedLanguageService.findByCode("fra"), NAME_FR);
		survey.setPaidFor(true);
		// add a new choice question with no choices
		survey.addQuestion(new ChoiceQuestion(survey, 55L));
		assertFalse("invalid test data", survey.isPublished());

		persist(survey);

		ModelAndView mav = controller.doPublishSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertNotNull("no view", mav.getViewName());
		assertEquals("should have Fail view", "/dashboard/manage/publishTab/errors", mav.getViewName());

		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("no errors object", errors);
		assertTrue("expected errors", errors.hasErrors());
		assertHasError(errors, "publish.error.noChoices");

		// ensure clear session, fresh reload
		flushAndClear();

		// reload
		survey = surveyService.findNonDeletedSurvey(survey.getId());
		assertFalse("survey should NOT be published", survey.isPublished());
	}

	@Test
	public void testChoiceMissingLanguageText() {
		// valid data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		surveyService.setSurveyLanguages(survey, new IdListCommand(new Long[] {
				supportedLanguageService.findByCode("eng").getId(), supportedLanguageService.findByCode("fra").getId() }));
		// add a new choice question with a choice that is unilingual
		{
			ChoiceQuestion choiceQuestion = new ChoiceQuestion(survey, 55L);
			Choice choice = new Choice(choiceQuestion, 1L);
			// choice.addObjectName(supportedLanguageService.findByCode("fra"),
			// NAME_FR);
			choiceQuestion.addChoice(choice);
			survey.addQuestion(choiceQuestion);
		}

		assertFalse("invalid test data", survey.isPublished());

		persist(survey);

		ModelAndView mav = controller.doPublishSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertNotNull("no view", mav.getViewName());
		assertEquals("should have Fail view", "/dashboard/manage/publishTab/errors", mav.getViewName());

		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("no errors object", errors);
		assertTrue("expected errors", errors.hasErrors());
		assertHasError(errors, "publish.error.choiceTitlesMissingForLanguage");

		// ensure clear session, fresh reload
		flushAndClear();

		// reload
		survey = surveyService.findNonDeletedSurvey(survey.getId());
		assertFalse("survey should NOT be published", survey.isPublished());
	}

	/**
	 * A basic check to ensure survey validation takes place within the
	 * controller.
	 */

	@Test
	public void testPublish_BasicValidatorCheck() {
		// valid data
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		surveyService.setSurveyLanguages(survey, new IdListCommand(new Long[] { supportedLanguageService.findByCode("fra")
				.getId() }));

		survey.setPublished(false);
		// must be paid for
		survey.setPaidFor(true);
		assertFalse("invalid test data", survey.isPublished());

		// no questions, etc.

		getHibernateTemplate().persist(survey);

		flushAndClear();

		ModelAndView mav = controller.doPublishSubmit(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertNotNull("no view", mav.getViewName());
		assertEquals("should have Fail view", "/dashboard/manage/publishTab/errors", mav.getViewName());

		Errors errors = (Errors) mav.getModel().get("errors");
		assertNotNull("no errors object", errors);
		assertTrue("expected errors", errors.hasErrors());
		assertHasError(errors, "publish.error.surveyTitlesMissingForLanguage");
		assertHasError(errors, "publish.error.hasNoQuestions");

		// ensure clear session, fresh reload
		flushAndClear();

		// reload
		survey = surveyService.findNonDeletedSurvey(survey.getId());
		assertFalse("survey should NOT be published", survey.isPublished());
	}

	@Test
	public void testDoDraftSubmit_Success_NoResponseData() {
		// when createTypicalScenario1 is called with persist=false, no
		// responses are created; otherwise some are
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.setPublished(true);
		persist(survey);
		flushAndClear();

		assertEquals("should have zero response count", Integer.valueOf(0), surveyService.countResponses(survey));

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + survey.getId() + ".html");

		assertTrue("should be published", survey.isPublished());
		ModelAndView mav = controller.doDraftSubmit(request);
		flushAndClear();

		assertIsRedirect(mav);
		Survey loaded = get(Survey.class, survey.getId());
		assertFalse("should now be unpublished", loaded.isPublished());
	}

	@Test
	public void testDoDraftSubmit_Success_WithResponseData_AndConfirmationFlag() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.setPublished(true);
		// scenarioDataUtil.createResponseData(survey, 1000);
		persist(survey);
		scenarioDataUtil.addDefaultResponse(survey);
		flushAndClear();

		assertTrue("should have non-zero response count", surveyService.countResponses(survey) > 0);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "");

		assertTrue("should be published", survey.isPublished());
		ModelAndView mav = controller.doDraftSubmit(request);
		flushAndClear();

		assertIsRedirect(mav);
		Survey loaded = get(Survey.class, survey.getId());
		assertFalse("should now be unpublished", loaded.isPublished());
	}

	@Test
	public void testDoDraftSubmit_Success_WithResponseData_AndCancelFlag() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.setPublished(true);
		persist(survey);
		scenarioDataUtil.addDefaultResponse(survey);
		flushAndClear();

		assertTrue("should have non-zero response count", surveyService.countResponses(survey) > 0);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_lecnac", "");

		assertTrue("should be published", survey.isPublished());
		ModelAndView mav = controller.doDraftSubmit(request);
		flushAndClear();

		assertIsRedirect(mav);
		Survey loaded = get(Survey.class, survey.getId());
		assertTrue("should still be unpublished", loaded.isPublished());
	}

	@Test
	public void testDoDraftSubmit_Success_ShowConfirmationWhenResponseDataExists() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		survey.setPublished(true);
		persist(survey);
		scenarioDataUtil.addDefaultResponse(survey);
		flushAndClear();

		assertTrue("should have non-zero response count", surveyService.countResponses(survey) > 0);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + survey.getId() + ".html");

		assertTrue("should be published", survey.isPublished());
		ModelAndView mav = controller.doDraftSubmit(request);
		flushAndClear();

		assertHasViewName(mav, "/dashboard/manage/publishTab/confirmDraftStatus");
		assertModelHasSurvey(mav, survey);
	}

	@Test
	public void testDoSuccessView() {
		// valid data
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		// no parameters, no URL inspection
		ModelAndView mav = controller.doSuccessView(new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html"));
		assertNotNull(mav);
		assertEquals("should show success message", "/dashboard/manage/publishTab/publishSuccess", mav.getViewName());
	}

	@Test
	public void testDetermineSurveyUrl_Success() {
		// persist to get an ID, since there is no way to set an ID in the model
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("the.com");
		request.setContextPath("/oas");

		String url = controller.determineSurveyUrl(request, survey);
		assertEquals("wrong URL generated", "http://the.com/oas/html/srvy/resp/" + survey.getId() + ".html", url);
	}

	@Test
	public void testDetermineSurveyUrl_Success_CustomPort() {
		// persist to get an ID, since there is no way to set an ID in the model
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("the.com");
		request.setContextPath("/oas");
		request.setServerPort(81);

		String url = controller.determineSurveyUrl(request, survey);
		assertEquals("wrong URL generated", "http://the.com:81/oas/html/srvy/resp/" + survey.getId() + ".html", url);
	}

	@Test
	public void testSurveyUrl_Success_UpstreamReverseProxy() {
		// persist to get an ID, since there is no way to set an ID in the model
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(Constants.X_FORWARDED_HOST, "www.the.com");
		request.setServerName("localhost");
		request.setServerPort(8080);
		request.setContextPath("/oas");

		String url = controller.determineSurveyUrl(request, survey);
		assertEquals("wrong URL generated", "http://www.the.com/oas/html/srvy/resp/" + survey.getId() + ".html", url);
	}

}
