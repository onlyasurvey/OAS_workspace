package com.oas.controller.survey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.service.BranchingService;
import com.oas.service.ResponseService;
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
	private ResponseService responseService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	// ======================================================================

	private MockHttpServletRequest createSimpleScenario() {
		return createSimpleScenario(true);
	}

	private MockHttpServletRequest createSimpleScenario(boolean published) {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		// publish it
		survey.setPublished(published);
		persist(survey);
		scenarioDataUtil.addDefaultScenario1Response(survey);
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

		// Question question = getSurveyService().findFirstQuestion(survey);
		// assertNotNull("could not load first question", question);
		// assertNotNull("could not load first question", question.getId());
		//
		// request.setParameter("qId", question.getId().toString());
		// responseService.addQuestionToHistory(response, question);
		return request;
	}

	// ======================================================================

	private void clearQuestionHistory(Response response) {
		getHibernateTemplate().deleteAll(find("from ResponseQuestionHistory where id.response = ?", response));
	}

	// ======================================================================

	@Test
	public void formBackingObject_InitializesChoiceIdList() {
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
	public void startResponse_Success() {
		MockHttpServletRequest request = createSimpleScenario();
		Survey realSurvey = (Survey) request.getAttribute(TEST_SURVEY);
		assertTrue("unpublished survey", realSurvey.isPublished());
		assertNotNull("failed to configure test", realSurvey);
		Question realQuestion = getFirstQuestion(realSurvey);
		assertNotNull("failed to load first question", realQuestion);
		flushAndClear();

		ModelAndView mav = responseController.startResponse(request, new MockHttpServletResponse());

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
	public void startResponse_PushedWhenNotPublished() {
		MockHttpServletRequest request = createSimpleScenario(false);
		flushAndClear();

		ModelAndView mav = responseController.startResponse(request, new MockHttpServletResponse());

		assertHasViewName(mav, "/response/isPaused");
	}

	@Test
	public void defaultForm_Success() throws Exception {
		MockHttpServletRequest request = createSimpleScenario();
		request.setParameter("n", "1");
		ModelAndView mav = responseController.showQuestion(request, new MockHttpServletResponse());

		assertNotNull("should have a model", mav.getModel());
		assertNotNull("should have question", mav.getModel().get("question"));
		assertNotNull("should have response", mav.getModel().get("response"));
	}

	// DELETE ME: ?qId unused now
	// @Test
	// public void defaultForm_InvalidQuestion_QuestionIsForWrongSurvey() throws
	// Exception {
	//
	// // some other survey
	// Survey otherSurvey =
	// scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(),
	// true);
	// flushAndClear();
	// Question otherQuestion = otherSurvey.getQuestions().iterator().next();
	//
	// // the one we are going to expect
	// MockHttpServletRequest request = createSimpleScenario();
	// request.setParameter("qId", otherQuestion.getId().toString());
	//
	// try {
	// responseController.showQuestion(request);
	// fail("should have thrown an exception due to bad parameter");
	// } catch (IllegalArgumentException e) {
	// // this isn't a validation error of user-supplied data, it's a
	// // failure of the caller to pass a question that belongs to the
	// // given survey
	//
	// // expected
	// }
	// }

	// ======================================================================

	@Test
	public void updateQuestionHistory_Back_HasPreviousQuestion() {

		// no responses nor rules when persist=false
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		persist(survey);

		flushAndClear();

		// this makes assumptions about the test scenario
		Question questionBefore = survey.getQuestions().get(3);
		Question question = survey.getQuestions().get(4);
		assertNotNull(question);

		// as per SDU.addDefaultResponse, no history
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		assertNotNull(response);
		persist(response);
		responseService.addQuestionToHistory(response, questionBefore);
		responseService.addQuestionToHistory(response, question);

		// as per SDU.addDefaultResponse
		Question shouldNotBeNullQuestion = surveyService.findQuestionBefore(question);
		assertNotNull("should be a question before the first", shouldNotBeNullQuestion);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");

		// 2 and 4 as per SDU.addDefaultResponse
		request.setParameter("n", "2");
		request.setParameter(Keys.BACK, "trueEh");

		// what the controller thinks is next
		Question subject = responseController.updateQuestionHistory(request, question, response, responseService
				.getQuestionHistory(response), 2);

		assertEquals("should be same question", questionBefore, subject);
	}

	@Test
	public void updateQuestionHistory_Back_DoesNotHavePreviousQuestion() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		flushAndClear();

		// this makes assumptions about the test scenario
		Question question = survey.getQuestions().get(0);
		// as per SDU.addDefaultResponse
		Response response = firstResponse(survey);
		assertNotNull(response);
		persist(response);
		// no history
		clearQuestionHistory(response);

		responseService.addQuestionToHistory(response, question);

		// as per SDU.addDefaultResponse
		Question shouldBeNullQuestion = surveyService.findQuestionBefore(question);
		assertNull("should be no questions before the first", shouldBeNullQuestion);

		//
		// request.setParameter("qId", firstQuestion.getId().toString());
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");
		request.setParameter(Keys.BACK, "trueEh");

		// what the controller thinks is next
		Question subject = responseController.updateQuestionHistory(request, question, response, responseService
				.getQuestionHistory(response), 1);
		assertNull("should be NO next question", subject);
	}

	@Test
	public void updateQuestionHistory_Forward_HasNextQuestion() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		flushAndClear();

		// this makes assumptions about the test scenario
		Question question = survey.getQuestions().get(0);
		Question thirdQuestion = survey.getQuestions().get(2);
		assertNotNull(question);
		assertNotNull(thirdQuestion);

		// as per SDU.addDefaultResponse
		Response response = firstResponse(survey);
		assertNotNull(response);
		// persist(response);

		// no history
		clearQuestionHistory(response);

		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");

		request.setParameter("n", "1");
		request.setParameter(Keys.FORWARD, "trueEh");

		getHibernateTemplate().deleteAll(branchingService.findEntryRules(thirdQuestion));
		getHibernateTemplate().deleteAll(branchingService.findExitRules(thirdQuestion));
		getHibernateTemplate().deleteAll(branchingService.findEntryRules(question));
		getHibernateTemplate().deleteAll(branchingService.findExitRules(question));
		// branchingService.clearRules(question);
		// branchingService.clearRules(thirdQuestion);
		flushAndClear();

		// what the controller thinks is next
		Question subject = responseController.updateQuestionHistory(request, load(Question.class, question.getId()), load(
				Response.class, response.getId()), responseService.getQuestionHistory(response), 1);
		assertEquals("should be same question", thirdQuestion, subject);
		flushAndClear();

	}

	@Test
	public void updateQuestionHistory_Forward_DoesNotHaveNextQuestion() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question lastQuestion = survey.getQuestions().get(survey.getQuestions().size() - 1);

		assertNull("expected findQuestionAfter to succeed", surveyService.findQuestionAfter(lastQuestion));

		//
		Response response = firstResponse(survey);
		Integer number = responseService.getQuestionHistory(response).size() + 1;
		assertTrue("expected non-empty question history", number - 1 > 0);
		request.setParameter("n", number.toString());
		request.setParameter(Keys.FORWARD, "trueEh");

		// what the controller thinks is next
		Question subject = responseController.updateQuestionHistory(request, lastQuestion, (Response) request
				.getAttribute(TEST_RESPONSE), responseService.getQuestionHistory(response), -1);
		assertNull("should be NO next question", subject);
	}

	@Test
	public void updateQuestionHistory_Success_Default_NeitherClicked() {
		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question lastQuestion = survey.getQuestions().get(survey.getQuestions().size() - 1);

		assertNull("expected findQuestionAfter to succeed", surveyService.findQuestionAfter(lastQuestion));

		//
		Response response = firstResponse(survey);
		Integer number = responseService.getQuestionHistory(response).size() + 1;
		assertTrue("expected non-empty question history", number - 1 > 0);
		request.setParameter("n", number.toString());
		// no forward nor back
		// request.setParameter(Keys.FORWARD, "trueEh");

		// what the controller thinks is next
		Question subject = responseController.updateQuestionHistory(request, lastQuestion, (Response) request
				.getAttribute(TEST_RESPONSE), responseService.getQuestionHistory(response), -1);
		assertNull("should be NO next question", subject);

		// with no parameters, default should be forward, therefore no errors
	}

	// ======================================================================

	@Test
	public void userClickedBack_True() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// any not-null value
		request.setParameter(Keys.BACK, "");
		boolean result = responseController.userClickedBack(request);
		assertTrue("should have received BACK submit", result);
	}

	@Test
	public void userClickedBack_False() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// NULL - not set
		// request.setParameter(Keys.BACK, "");
		boolean result = responseController.userClickedBack(request);
		assertFalse("should NOT have received BACK submit", result);
	}

	@Test
	public void userClickedForward_True() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// any not-null value
		request.setParameter(Keys.FORWARD, "");
		boolean result = responseController.userClickedForward(request);
		assertTrue("should have received FORWARD submit", result);
	}

	@Test
	public void userClickedForward_False() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		// NULL - not set
		// request.setParameter(Keys.FORWARD, "");
		boolean result = responseController.userClickedForward(request);
		assertFalse("should NOT have received FORWARD submit", result);
	}

	// ======================================================================

	@Test
	public void getRedirectUrl_Back_HasPreviousQuestion() {

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
		String subject = responseController.getRedirectUrl(responseController.updateQuestionHistory(request, question, response,
				responseService.getQuestionHistory(response), 4), request, response, 4);

		assertTrue("should be same question", subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void getRedirectUrl_Back_DoesNotHavePreviousQuestion() {

		MockHttpServletRequest request = createSimpleScenario();

		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question firstQuestion = getFirstQuestion(survey);
		assertNotNull(firstQuestion);
		Question shouldBeNullQuestion = surveyService.findQuestionBefore(firstQuestion);
		assertNull("should be no questions before the first", shouldBeNullQuestion);

		//
		request.setParameter("n", "1");
		request.setParameter(Keys.BACK, "trueEh");

		Response response = firstResponse(survey);
		persist(response);
		clearQuestionHistory(response);
		responseService.addQuestionToHistory(response, firstQuestion);
		flushAndClear();

		// what the controller thinks is next
		String subject = responseController.getRedirectUrl(responseController.updateQuestionHistory(request, firstQuestion,
				response, responseService.getQuestionHistory(response), 1), request, response, 1);
		assertTrue("should be same question", subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void getRedirectUrl_Forward_HasNextQuestion() {

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
		assertNotNull("failed to configure test", response);
		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		// what the controller thinks is next
		String subject = responseController.getRedirectUrl(responseController.updateQuestionHistory(request, question, response,
				responseService.getQuestionHistory(response), 2), request, response, 2);

		assertTrue("should be same question", subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void getRedirectUrl_Forward_DoesNotHaveNextQuestion() {

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
		String subject = responseController.getRedirectUrl(responseController.updateQuestionHistory(request, lastQuestion,
				response, responseService.getQuestionHistory(response), -1), request, response, -1);
		assertTrue("should be same question", subject.contains("tks") && subject.contains("/" + response.getId() + ".html"));
	}

	@Test
	public void getRedirectUrl_Success_Default_NeitherForwardNorBackClicked() {

		MockHttpServletRequest request = createSimpleScenario();
		Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
		assertNotNull("failed to configure test", survey);

		Question lastQuestion = survey.getQuestions().get(survey.getQuestions().size() - 1);
		assertNull("expected findQuestionAfter to succeed", surveyService.findQuestionAfter(lastQuestion));

		Response response = firstResponse(survey);

		//
		request.setParameter("qId", lastQuestion.getId().toString());
		// no next/forward parameter

		// what the controller thinks is next
		responseController.getRedirectUrl(responseController.updateQuestionHistory(request, lastQuestion, response,
				responseService.getQuestionHistory(response), -1), request, response, -1);

		// with no parameters, default should be forward, therefore no errors
	}

	// ======================================================================

	@Test
	public void saveAnswer_Choice_PausedWhenNotPublished() throws Exception {
		// survey owned by some user (no auth here)
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, false);
		persist(survey);

		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("could not load first question", question);
		assertNotNull("could not load first question", question.getId());

		// when not persisted, default response + rules are not created
		// create response but not rules
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		responseService.addQuestionToHistory(response, question);
		persist(response);

		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("get", "/oas/res/html/q/" + response.getId() + ".html");
		request.setPathInfo(request.getRequestURI());

		request.setParameter("_choiceIdList", "on");
		// on Question Number 1
		request.addParameter("n", "1");

		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: select the first choice
		String id1 = question.getChoices().get(0).getId().toString();
		request.setParameter("choiceIdList", id1);

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		assertHasViewName(mav, "/response/isPaused");
	}

	@Test
	public void saveAnswer_Choice_Success() throws Exception {

		// survey owned by some user (no auth here)
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		// persist separately to avoid SDU's response data
		persist(survey);

		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("could not load first question", question);
		assertNotNull("could not load first question", question.getId());

		// when not persisted, default response + rules are not created
		// create response but not rules
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		persist(response);

		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("get", "/oas/res/html/q/" + response.getId() + ".html");
		request.setPathInfo(request.getRequestURI());

		request.setParameter("_choiceIdList", "on");
		request.setParameter("n", "1");

		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: select the first choice
		String id1 = question.getChoices().get(0).getId().toString();
		request.setParameter("choiceIdList", id1);

		List<Answer> existingAnswers = find("from Answer where question = ?", question);
		for (Answer answer : existingAnswers) {
			delete(answer);
		}
		flushAndClear();

		long initialCount = countBaseObjects();

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		flushAndClear();
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
		long newCount = countBaseObjects();
		assertEquals("expected 1 new base object", initialCount + 1, newCount);
	}

	@Test
	public void saveAnswer_Choice_Success_NoneOfTheAbove() throws Exception {

		// survey owned by some user (no auth here)
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		Question questionToChange = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		questionToChange.setRequired(false);
		persist(survey);

		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);
		clearQuestionHistory(response);
		responseService.addQuestionToHistory(response, questionToChange);
		flushAndClear();

		// need the ID post-save
		Question question = getFirstQuestionOfType(get(Survey.class, survey.getId()), ChoiceQuestion.class);
		assertNotNull("could not load first question (no ID)", question.getId());

		MockHttpServletRequest request = new MockHttpServletRequest("get", "/oas/res/html/q/" + response.getId() + ".html");
		request.setPathInfo(request.getRequestURI());

		request.setParameter("n", "1");
		request.setParameter("_choiceIdList", "on");

		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: select the first choice
		request.setParameter("choiceIdList", Long.valueOf(Constants.NONE_OF_THE_ABOVE_ID).toString());

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	// ======================================================================

	@Test
	public void saveAnswer_Text_Success() throws Exception {

		// when persist=false, no responses or rules get set - easy to test
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		persist(survey);

		// no history please
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		// this makes assumptions about the test scenario
		Question question = survey.getQuestions().get(1);
		// as per SDU.addDefaultResponse
		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		assertTrue("should be a text question", question.isTextQuestion());
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");

		//
		request.setParameter("n", "1");
		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data
		request.setParameter("a", "someTextAnswer");

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	// http://redmine.itsonlyasurvey.com/issues/show/140
	// @Test(expected = IllegalArgumentException.class)
	// public void saveAnswer_FailsWithoutBackOrForward() throws Exception {
	//
	// MockHttpServletRequest request = createSimpleScenario();
	// Survey survey = (Survey) request.getAttribute(TEST_SURVEY);
	// assertNotNull("failed to configure test", survey);
	//
	// // this makes assumptions about the test scenario
	// Question question = getSecondQuestion(survey);
	// assertTrue("should be a text question", question.isTextQuestion());
	// assertTrue("should be required", question.isRequired());
	//
	// //
	// request.setParameter("n", "1");
	//
	// // do NOT set next/forward parameters
	//
	// // answer data: invalid
	// request.setParameter("a", "");
	//
	// // method under test
	// responseController.saveAnswer(request, new MockHttpServletResponse());
	// }

	@Test
	public void saveAnswer_Text_FailsOnRequired() throws Exception {

		// when persist=false, no responses or rules get set - easy to test
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		persist(survey);

		// no history please
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		// this makes assumptions about the test scenario
		Question question = survey.getQuestions().get(1);
		// as per SDU.addDefaultResponse
		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		assertTrue("should be a text question", question.isTextQuestion());
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");

		//
		// next/forward parameter
		// request.setParameter("qId", question.getId().toString());
		request.setParameter("n", "1");
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data
		request.setParameter("a", "");

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		assertNotNull("no mav", mav);
		assertNull("expected no view", mav.getView());
		assertNotNull("expected model", mav.getModel());

		assertEquals("should have form view", "question/textQuestion", mav.getViewName());
	}

	@Test
	public void saveAnswer_Text_FailsValidation() throws Exception {

		// when persist=false, no responses or rules get set - easy to test
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		persist(survey);

		// no history please
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		// this makes assumptions about the test scenario
		Question question = survey.getQuestions().get(1);
		// as per SDU.addDefaultResponse
		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		assertTrue("should be a text question", question.isTextQuestion());
		assertTrue("should be required", question.isRequired());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");

		//
		request.setParameter("n", "1");
		// next/forward parameter
		request.setParameter(Keys.FORWARD, "trueEh");

		// answer data: huge text data
		request.setParameter("a", String.format("%5000s", ""));

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		assertNotNull("no mav", mav);
		assertNull("expected no view", mav.getView());
		assertNotNull("expected model", mav.getModel());

		assertEquals("should have form view", "question/textQuestion", mav.getViewName());
	}

	@Test
	public void saveAnswer_Text_Success_IgnoresValidationOnBack() throws Exception {

		// when persist=false, no responses or rules get set - easy to test
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false, true);
		persist(survey);

		// no history please
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey, false);
		// this makes assumptions about the test scenario
		Question question = survey.getQuestions().get(1);
		assertTrue("should be a text question", question.isTextQuestion());
		// as per SDU.addDefaultResponse
		responseService.addQuestionToHistory(response, question);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");

		//
		request.setParameter("n", "1");
		// next/forward parameter
		request.setParameter(Keys.BACK, "trueEh");

		// this is invalid data: since Back is pressed, it needs to be ignored
		request.setParameter("a", "");

		ModelAndView mav = responseController.saveAnswer(request, new MockHttpServletResponse());
		assertNotNull("expected mav", mav);
		assertNotNull("expected view", mav.getView());

		assertTrue("should be same question", ((RedirectView) mav.getView()).getUrl().contains("/" + response.getId() + ".html"));
	}

	// ======================================================================

	@Test
	public void doThanks_Success() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");
		ModelAndView mav = responseController.doThanks(request, new MockHttpServletResponse());

		assertNotNull(mav);
		assertNotNull(mav.getViewName());
		assertEquals("wrong view name", "/response/thanksMessage", mav.getViewName());
	}

	@Test
	public void doThanks_PausedWhenNotPublished() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, false);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + response.getId() + ".html");
		ModelAndView mav = responseController.doThanks(request, new MockHttpServletResponse());

		assertNotNull(mav);
		assertNotNull(mav.getViewName());
		assertEquals("wrong view name", "/response/isPaused", mav.getViewName());
	}

}
