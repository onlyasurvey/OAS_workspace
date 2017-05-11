package com.oas.controller.survey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.service.BranchingService;
import com.oas.service.SurveyService;
import com.oas.util.Constants;
import com.oas.util.Keys;

public class ResponseControllerTest extends AbstractOASBaseTest {

	private static final String TEST_SURVEY = "key.TEST_SURVEY_DATA";
	private static final String TEST_RESPONSE = "key.TEST_RESPONSE_DATA";

	@Autowired
	private ResponseController responseController;

	@Autowired
	private SurveyService surveyService;

	@Autowired
	private BranchingService branchingService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	// ======================================================================

	private MockHttpServletRequest createSimpleScenario() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		// publish it
		survey.setPublished(true);
		persist(survey);
		scenarioDataUtil.addDefaultResponse(survey);
		flushAndClear();

		assertNotNull(survey.getId());

		int responseCount = surveyService.countResponses(survey);
		assertTrue("SANITY: loading potentially large dataset: refactor", responseCount < 5);
		assertTrue("SANITY: loading potentially large dataset: refactor", responseCount < 20);

		Response response = scenarioDataUtil.firstResponse(survey);
		assertNotNull("no first response", response);

		MockHttpServletRequest request = new MockHttpServletRequest("get", "/oas/html/res/q/" + response.getId() + ".html");
		request.setPathInfo(request.getRequestURI());

		request.setAttribute(TEST_RESPONSE, response);
		request.setAttribute(TEST_SURVEY, survey);

		Question question = getSurveyService().findFirstQuestion(survey);
		assertNotNull("could not load first question", question);
		assertNotNull("could not load first question", question.getId());

		request.setParameter("qId", question.getId().toString());
		return request;
	}

	// ======================================================================

	@Test
	public void testFormBackingObject_InitializesChoiceIdList() {
		MockHttpServletRequest request = createSimpleScenario();
		Survey realSurvey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", realSurvey);
		// dependent on scenario data: 3rd question created must be a choice
		// question
		Question question = getQuestionByOrder(realSurvey, 2);

		assertNotNull("failed to load last question", question);
		assertTrue("question of wrong type: expected last question to be choice question", question.isChoiceQuestion());

		SimpleAnswerCommand command = responseController.formBackingObject(request, question);
		assertNotNull(command);
		assertNotNull("no choiceIdList", command.getChoiceIdList());
		assertFalse("choiceIdList not properly initialized", command.getChoiceIdList().length == 0);

	}

	// ======================================================================

	@Test
	public void testStartResponse_Success() {
		MockHttpServletRequest request = createSimpleScenario();
		Survey realSurvey = (Survey) request.getAttribute(TEST_SURVEY);
		assertTrue("unpublished survey", realSurvey.isPublished());
		assertNotNull("failed to configure test", realSurvey);
		Question realQuestion = getFirstQuestion(realSurvey);
		assertNotNull("failed to load first question", realQuestion);
		flushAndClear();

		ModelAndView mav = responseController.startResponse(request);

		Survey survey = (Survey) mav.getModel().get("survey");
		Response response = (Response) mav.getModel().get("response");
		Question firstQuestion = (Question) mav.getModel().get("firstQuestion");

		assertNotNull("no survey data", survey);
		assertNotNull("no response data", response);
		assertNotNull("no firstQuestion data", firstQuestion);

		assertEquals("not the same survey", realSurvey.getId(), survey.getId());
		assertEquals("not the same question", realQuestion.getId(), firstQuestion.getId());
	}

	@Test
	public void testDefaultForm_Success() throws Exception {
		MockHttpServletRequest request = createSimpleScenario();
		ModelAndView mav = responseController.showQuestion(request);

		assertNotNull("should have a model", mav.getModel());
		assertNotNull("should have question", mav.getModel().get("question"));
		assertNotNull("should have response", mav.getModel().get("response"));
	}

	@Test
	public void testDefaultForm_InvalidQuestion_QuestionIsForWrongSurvey() throws Exception {

		// some other survey
		Survey otherSurvey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();
		Question otherQuestion = otherSurvey.getQuestions().iterator().next();

		// the one we are going to expect
		MockHttpServletRequest request = createSimpleScenario();
		request.setParameter("qId", otherQuestion.getId().toString());

		try {
			responseController.showQuestion(request);
			fail("should have thrown an exception due to bad parameter");
		} catch (IllegalArgumentException e) {
			// this isn't a validation error of user-supplied data, it's a
			// failure of the caller to pass a question that belongs to the
			// given survey

			// expected
		}
	}

	// ======================================================================

	@Test
	public void testGetRedirectQuestion_Back_HasPreviousQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question question = getSecondQuestion(survey);
		assertNotNull(question);
		request.setParameter("qId", question.getId().toString());
		request.setParameter(Keys.BACK, "trueEh");

		Question firstQuestion = surveyService.findQuestionBefore(question);
		assertNotNull(firstQuestion);

		// what the controller thinks is next
		Question subject = responseController.getRedirectQuestion(request, question, (Response) request
				.getAttribute(TEST_RESPONSE));

		assertEquals("should be same question", firstQuestion, subject);
	}

	@Test
	public void testGetRedirectQuestion_Back_DoesNotHavePreviousQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question firstQuestion = getFirstQuestion(survey);
		assertNotNull(firstQuestion);
		Question shouldBeNullQuestion = surveyService.findQuestionBefore(firstQuestion);
		assertNull("should be no questions before the first", shouldBeNullQuestion);

		//
		request.setParameter("qId", firstQuestion.getId().toString());
		request.setParameter(Keys.BACK, "trueEh");

		// what the controller thinks is next
		Question subject = responseController.getRedirectQuestion(request, firstQuestion, (Response) request
				.getAttribute(TEST_RESPONSE));
		assertNull("should be NO next question", subject);
	}

	@Test
	public void testGetRedirectQuestion_Forward_HasNextQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question question = survey.getQuestions().get(1);
		Question thirdQuestion = survey.getQuestions().get(2);
		assertNotNull(thirdQuestion);
		assertNotNull(question);

		request.setParameter("qId", question.getId().toString());
		request.setParameter(Keys.FORWARD, "trueEh");

		branchingService.clearRules(question);
		branchingService.clearRules(thirdQuestion);
		flushAndClear();

		// what the controller thinks is next
		Question subject = responseController.getRedirectQuestion(request, question, (Response) request
				.getAttribute(TEST_RESPONSE));

		assertEquals("should be same question", thirdQuestion, subject);
	}

	@Test
	public void testGetRedirectQuestion_Forward_DoesNotHaveNextQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question lastQuestion = survey.getQuestions().get(survey.getQuestions().size() - 1);

		assertNull("expected findQuestionAfter to succeed", surveyService.findQuestionAfter(lastQuestion));

		//
		request.setParameter("qId", lastQuestion.getId().toString());
		request.setParameter(Keys.FORWARD, "trueEh");

		// what the controller thinks is next
		Question subject = responseController.getRedirectQuestion(request, lastQuestion, (Response) request
				.getAttribute(TEST_RESPONSE));
		assertNull("should be NO next question", subject);
	}

	@Test
	public void testGetRedirectQuestion_NeitherClicked() {
		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question firstQuestion = getSecondQuestion(survey);
		assertNotNull(firstQuestion);

		//
		request.setParameter("qId", firstQuestion.getId().toString());
		// no BACK nor FORWARD parameters

		// what the controller thinks is next
		Question subject = responseController.getRedirectQuestion(request, firstQuestion, (Response) request
				.getAttribute(TEST_RESPONSE));
		assertNull("should NOT get any redirect question, because neither BACK nor FORWARD was clicked", subject);
	}

	@Test
	public void testAssertUserClickedBackOrForward_Forward() {
		MockHttpServletRequest request = createSimpleScenario();
		request.setParameter(Keys.FORWARD, "sureWhyNot");

		// should NOT throw
		responseController.assertUserClickedBackOrForward(request);
	}

	@Test
	public void testAssertUserClickedBackOrForward_Back() {
		MockHttpServletRequest request = createSimpleScenario();
		request.setParameter(Keys.BACK, "sureWhyNot");

		// should NOT throw
		responseController.assertUserClickedBackOrForward(request);
	}

	@Test
	public void testAssertUserClickedBackOrForward_NeitherClicked() {
		MockHttpServletRequest request = createSimpleScenario();
		// no params

		try {
			// should NOT throw
			responseController.assertUserClickedBackOrForward(request);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testUserClickedBack_True() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// any not-null value
		request.setParameter(Keys.BACK, "");
		boolean result = responseController.userClickedBack(request);
		assertTrue("should have received BACK submit", result);
	}

	@Test
	public void testUserClickedBack_False() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// NULL - not set
		// request.setParameter(Keys.BACK, "");
		boolean result = responseController.userClickedBack(request);
		assertFalse("should NOT have received BACK submit", result);
	}

	@Test
	public void testUserClickedForward_True() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// any not-null value
		request.setParameter(Keys.FORWARD, "");
		boolean result = responseController.userClickedForward(request);
		assertTrue("should have received FORWARD submit", result);
	}

	@Test
	public void testUserClickedForward_False() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// NULL - not set
		// request.setParameter(Keys.FORWARD, "");
		boolean result = responseController.userClickedForward(request);
		assertFalse("should NOT have received FORWARD submit", result);
	}

	// ======================================================================

	@Test
	public void testGetRedirectUrl_Back_HasPreviousQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question question = getSecondQuestion(survey);
		assertNotNull(question);
		request.setParameter("qId", question.getId().toString());
		request.setParameter(Keys.BACK, "trueEh");

		Response response = firstResponse(survey);

		Question firstQuestion = surveyService.findQuestionBefore(question);
		assertNotNull(firstQuestion);

		// what the controller thinks is next
		String subject = responseController.getRedirectUrl(responseController.getRedirectQuestion(request, question, response),
				request, response);

		assertTrue("should be same question", subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void testGetRedirectUrl_Back_DoesNotHavePreviousQuestion() {

		MockHttpServletRequest request = createSimpleScenario();

		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question firstQuestion = getFirstQuestion(survey);
		assertNotNull(firstQuestion);
		Question shouldBeNullQuestion = surveyService.findQuestionBefore(firstQuestion);
		assertNull("should be no questions before the first", shouldBeNullQuestion);

		//
		request.setParameter("qId", firstQuestion.getId().toString());
		request.setParameter(Keys.BACK, "trueEh");

		Response response = firstResponse(survey);

		// what the controller thinks is next
		String subject = responseController.getRedirectUrl(responseController.getRedirectQuestion(request, firstQuestion,
				response), request, response);
		assertTrue("should be same question", subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void testGetRedirectUrl_Forward_HasNextQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question question = getSecondQuestion(survey);
		assertNotNull(question);
		request.setParameter("qId", question.getId().toString());
		request.setParameter(Keys.FORWARD, "trueEh");

		Question thirdQuestion = surveyService.findQuestionAfter(question);
		assertNotNull(thirdQuestion);

		Response response = firstResponse(survey);

		// what the controller thinks is next
		String subject = responseController.getRedirectUrl(responseController.getRedirectQuestion(request, question, response),
				request, response);

		assertTrue("should be same question", subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void testGetRedirectUrl_Forward_DoesNotHaveNextQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question lastQuestion = survey.getQuestions().get(survey.getQuestions().size() - 1);

		assertNull("expected findQuestionAfter to succeed", surveyService.findQuestionAfter(lastQuestion));

		//
		request.setParameter("qId", lastQuestion.getId().toString());
		request.setParameter(Keys.FORWARD, "trueEh");

		Response response = firstResponse(survey);

		// what the controller thinks is next
		String subject = responseController.getRedirectUrl(responseController
				.getRedirectQuestion(request, lastQuestion, response), request, response);
		assertTrue("should be same question", subject.contains("tks") && subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void testGetRedirectUrl_Fail_NeitherForwardNorBackClicked() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question secondLast = getSecondQuestion(survey);

		Response response = firstResponse(survey);

		//
		request.setParameter("qId", secondLast.getId().toString());
		// no next/forward parameter

		// what the controller thinks is next
		try {
			responseController.getRedirectUrl(responseController.getRedirectQuestion(request, secondLast, response), request,
					response);
			fail("should have thrown illegal argument");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testSaveAnswer_Choice_Success() throws Exception {

		// survey owned by some user (no auth here)
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		Response response = firstResponse(survey);
		flushAndClear();

		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("could not load first question", question);
		assertNotNull("could not load first question", question.getId());

		MockHttpServletRequest request = new MockHttpServletRequest("get", "/oas/res/html/q/" + response.getId() + ".html");
		request.setPathInfo(request.getRequestURI());

		request.setParameter("_choiceIdList", "on");
		request.setParameter("qId", question.getId().toString());

		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: select the first choice
		String id1 = question.getChoices().get(0).getId().toString();
		request.setParameter("choiceIdList", id1);

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	@Test
	public void testSaveAnswer_Choice_Success_NoneOfTheAbove() throws Exception {

		// survey owned by some user (no auth here)
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		getFirstQuestionOfType(survey, ChoiceQuestion.class).setRequired(false);
		persist(survey);
		Response response = scenarioDataUtil.addDefaultResponse(survey);
		flushAndClear();

		// need the ID post-save
		Question question = getFirstQuestionOfType(get(Survey.class, survey.getId()), ChoiceQuestion.class);
		assertNotNull("could not load first question (no ID)", question.getId());

		MockHttpServletRequest request = new MockHttpServletRequest("get", "/oas/res/html/q/" + response.getId() + ".html");
		request.setPathInfo(request.getRequestURI());

		request.setParameter("_choiceIdList", "on");
		request.setParameter("qId", question.getId().toString());

		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: select the first choice
		request.setParameter("choiceIdList", Long.valueOf(Constants.NONE_OF_THE_ABOVE_ID).toString());

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	// ======================================================================

	@Test
	public void testSaveAnswer_Text_Success() throws Exception {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		// this makes assumptions about the test scenario
		Question question = getSecondQuestion(survey);
		assertTrue("should be a text question", question.isTextQuestion());

		//
		Response response = firstResponse(survey);

		//
		request.setParameter("qId", question.getId().toString());
		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data
		request.setParameter("a", "someTextAnswer");

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	@Test
	public void testSaveAnswer_FailsWithoutBackOrForward() throws Exception {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		// this makes assumptions about the test scenario
		Question question = getSecondQuestion(survey);
		assertTrue("should be a text question", question.isTextQuestion());
		assertTrue("should be required", question.isRequired());

		//
		request.setParameter("qId", question.getId().toString());

		// do NOT set next/forward parameters

		// answer data: invalid
		request.setParameter("a", "");

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("no mav", mav);
		assertNull("expected no view", mav.getView());
		assertNotNull("expected model", mav.getModel());

		assertEquals("should have form view", "question/textQuestion", mav.getViewName());
	}

	@Test
	public void testSaveAnswer_Text_FailsOnRequired() throws Exception {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		// this makes assumptions about the test scenario
		Question question = getSecondQuestion(survey);
		assertTrue("should be a text question", question.isTextQuestion());
		assertTrue("should be required", question.isRequired());

		//
		request.setParameter("qId", question.getId().toString());
		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data
		request.setParameter("a", "");

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("no mav", mav);
		assertNull("expected no view", mav.getView());
		assertNotNull("expected model", mav.getModel());

		assertEquals("should have form view", "question/textQuestion", mav.getViewName());
	}

	@Test
	public void testSaveAnswer_Text_FailsValidation() throws Exception {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		// this makes assumptions about the test scenario
		Question question = getSecondQuestion(survey);
		assertTrue("should be a text question", question.isTextQuestion());
		assertTrue("should be required", question.isRequired());

		//
		request.setParameter("qId", question.getId().toString());
		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: huge text data
		request.setParameter("a", "".format("%5000s", ""));

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("no mav", mav);
		assertNull("expected no view", mav.getView());
		assertNotNull("expected model", mav.getModel());

		assertEquals("should have form view", "question/textQuestion", mav.getViewName());
	}

	@Test
	public void testSaveAnswer_Text_Success_IgnoresValidationOnBack() throws Exception {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		// this makes assumptions about the test scenario
		Question question = getSecondQuestion(survey);
		assertTrue("should be a text question", question.isTextQuestion());

		//
		Response response = firstResponse(survey);

		//
		request.setParameter("qId", question.getId().toString());
		// next/forward parameter
		request.setParameter(Keys.BACK, "trueEh");

		// this is invalid data: since Back is pressed, it needs to be ignored
		request.setParameter("a", "");

		ModelAndView mav = responseController.saveAnswer(request);
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	// ======================================================================

	@Test
	public void testDoThanks_Success() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		Response response = scenarioDataUtil.addDefaultResponse(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");
		ModelAndView mav = responseController.doThanks(request);

		assertNotNull(mav);
		assertNotNull(mav.getViewName());
		assertEquals("wrong view name", "/thanks/view", mav.getViewName());
	}

}
