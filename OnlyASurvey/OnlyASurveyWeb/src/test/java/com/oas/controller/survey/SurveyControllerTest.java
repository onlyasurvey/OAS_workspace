package com.oas.controller.survey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.AccountOwner;
import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.BooleanAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.util.WebKeys;

public class SurveyControllerTest extends AbstractOASBaseTest {

	@Autowired
	private SurveyController controller;

	@Test
	public void displaySurvey_Success() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false);

		// these MUST be true to respond to a survey
		survey.setPaidFor(true);
		survey.setPublished(true);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request, new MockHttpServletResponse());

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertEquals("wrong view", "/survey/view", mav.getViewName());
	}

	@Test
	public void displaySurvey_notAvailable() {

		// create a test user - this ensures that some random number hard coded
		// here will never happen to be a persisted Survey.
		AccountOwner notASurvey = createTestUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + notASurvey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request, new MockHttpServletResponse());
		assertHasViewNamePart(mav, "notAvailableGeneral");
	}

	@Test
	public void displaySurvey_Fail_NotPublished() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request, new MockHttpServletResponse());

		survey.setPaidFor(true);
		survey.setPublished(false);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		// assertNotNull("errors missing", mav.getModel().get("errors"));
		assertEquals("wrong view", "/response/isPaused", mav.getViewName());
	}

	@Test
	public void displaySurvey_Fail_Unpaid() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.displaySurvey(request, new MockHttpServletResponse());

		survey.setPaidFor(false);
		survey.setPublished(true);
		getHibernateTemplate().persist(survey);
		flushAndClear();

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		// assertNotNull("errors missing", mav.getModel().get("errors"));
		assertEquals("wrong view", "/response/isPaused", mav.getViewName());
	}

	// TODO displaySurvey fails if closed, not published, etc

	// ======================================================================

	@Test
	public void createResponse_Success() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.createResponse(request, new MockHttpServletResponse(), null, null);

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertEquals("should have no view name", null, mav.getViewName());
		assertNotNull(mav.getView());
		assertIsRedirect(mav);

		// the action redirects to the first question: ensure that the redirect
		// does contain that ID
		// Question question = surveyService.findFirstQuestion(survey);
		// assertNotNull(question);

		// this assert is for if the action doesn't redirect to a Welcome
		// message, which is how it currently works
		// assertTrue("redirect needs to contain first question's ID",
		// ((RedirectView) mav.getView()).getUrl().contains("=" +
		// question.getId()));

	}

	@Test
	public void createResponse_SkipWelcomeFlag_Success() {
		// test user without a security context
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true, true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.createResponse(request, new MockHttpServletResponse(), "", null);

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertEquals("should have no view name", null, mav.getViewName());
		assertNotNull(mav.getView());
		assertIsRedirect(mav);

		// the action redirects to the first question: ensure that the redirect
		// does contain that ID
		Question question = surveyService.findFirstQuestion(survey);
		assertNotNull(question);

		// this assert is for if the action doesn't redirect to a Welcome
		// message, which is how it currently works
		// assertTrue("redirect needs to contain first question's ID",
		// ((RedirectView) mav.getView()).getUrl().contains(
		// "=" + question.getId()));
		assertTrue("redirect needs to contain first question's number", ((RedirectView) mav.getView()).getUrl().contains("n=1"));

		assertIsRedirect(mav);
	}

	// ======================================================================
	// Password-Protected Surveys
	// ======================================================================

	private void doPasswordProtectionAssertion(Survey survey, String password, boolean expectSuccess) {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		int initialCount = countRowsInTable("oas.response");
		ModelAndView mav = controller.createResponse(request, new MockHttpServletResponse(), null, password);
		flushAndClear();
		assertNotNull(mav);

		int newCount = countRowsInTable("oas.response");
		int expected = -1;

		if (expectSuccess) {
			expected = initialCount + 1;
		} else {
			expected = initialCount;
		}

		assertEquals("unexpected result for expectSuccess=" + expectSuccess, expected, newCount);
		if (expectSuccess) {
			assertIsRedirect(mav);
		} else {
			assertNotRedirect(mav);
			assertHasViewName(mav, "/survey/passwordPrompt");
		}
	}

	@Test
	public void passwordProtected_Success_CorrectPassword() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		survey.setGlobalPassword(TEST_GLOBAL_PASSWORD);
		persist(survey);

		doPasswordProtectionAssertion(survey, TEST_GLOBAL_PASSWORD, true);
	}

	@Test
	public void passwordProtected_Success_IncorrectPassword() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		survey.setGlobalPassword(TEST_GLOBAL_PASSWORD);
		persist(survey);

		doPasswordProtectionAssertion(survey, "some random password", false);
	}

	@Test
	public void passwordProtected_Success_EmptyPassword() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		survey.setGlobalPassword(TEST_GLOBAL_PASSWORD);
		persist(survey);

		doPasswordProtectionAssertion(survey, "", false);
	}

	// ======================================================================

	@Test
	public void pausedMessage() {
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		survey.setPublished(false);
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.createResponse(request, new MockHttpServletResponse(), null, null);
		assertHasViewNamePart(mav, "isPaused");
	}

	@Test
	public void notAvailable() {

		// create a test user - this ensures that some random number hard coded
		// here will never happen to be a persisted Survey.
		AccountOwner notASurvey = createTestUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + notASurvey.getId() + ".html");
		ModelAndView mav = controller.createResponse(request, new MockHttpServletResponse(), null, null);
		assertHasViewNamePart(mav, "notAvailableGeneral");
	}

	// ======================================================================

	private void doInvitationCodeTest(Survey survey, String invitationCode, boolean expectSuccess, boolean expectThanks,
			boolean expectReusedResponse) {

		assertNotNull(survey);
		assertHasText(invitationCode);
		assertTrue("cannot expect success and thanks message at the same time", !(expectSuccess && expectThanks));

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.setParameter(WebKeys.INVITATION_CODE, invitationCode);

		int initialCount = countRowsInTable("oas.response");

		//
		ModelAndView mav = controller.createResponse(request, new MockHttpServletResponse(), null, null);

		flushAndClear();
		assertNotNull(mav);

		int newCount = countRowsInTable("oas.response");
		int expected = -1;

		if (expectSuccess && (!expectReusedResponse)) {
			expected = initialCount + 1;
		} else if (expectSuccess && expectReusedResponse) {
			expected = initialCount;
		} else {
			expected = initialCount;
		}

		assertEquals("unexpected result for expectSuccess=" + expectSuccess, expected, newCount);
		if (expectSuccess) {
			assertIsRedirect(mav);
		} else if (expectThanks) {
			assertNotRedirect(mav);
			assertHasViewName(mav, "/response/thanksMessage");
		} else {
			assertNotRedirect(mav);
			assertHasViewName(mav, "/response/invalidInvitation");
		}
	}

	@Test
	public void invitationCode_Success_UnsentInvite() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.UNSENT);
		originalInvitation.setInvitationCode("abc");
		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, originalInvitation.getId() + ".abc", true, false, false);
	}

	@Test
	public void invitationCode_CannotReuseAfterResponseClosed() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
		response.setClosed(true);
		persist(response);

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.RESPONDED);
		originalInvitation.setInvitationCode("abc");
		originalInvitation.setResponse(response);
		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, originalInvitation.getId() + "." + originalInvitation.getInvitationCode(), false, true,
				false);
	}

	@Test
	public void invitationCode_CanReuseAfterResponseClosed_ReuseRespondedInviteWhenResponseIsDeleted() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		// no Response data

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.RESPONDED);
		originalInvitation.setInvitationCode("abc");
		// note line below (kept for clarity): there is NO response linked even
		// though the Invite is marked as RESPONDED. the test shows that a used
		// invite can be reused when the response has been deleted
		// http://redmine.itsonlyasurvey.com/issues/show/110
		// NOTE: originalInvitation.setResponse(response);

		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, originalInvitation.getId() + "." + originalInvitation.getInvitationCode(), true, false,
				false);
	}

	@Test
	public void invitationCode_CanReuseBeforeResponseClosed() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false);
		survey.setPublished(true);
		persist(survey);

		Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
		persist(response);

		// add a question to the response to exercise the redirect-to-question
		// logic: see createTypicalScenario1
		{
			BooleanQuestion question = (BooleanQuestion) survey.getQuestions().get(0);
			BooleanAnswer answer = new BooleanAnswer(response, question, true);
			persist(answer);
			responseService.addQuestionToHistory(response, question);
		}
		{
			TextQuestion question = (TextQuestion) survey.getQuestions().get(1);
			TextAnswer answer = new TextAnswer(response, question, "some value");
			persist(answer);
			responseService.addQuestionToHistory(response, question);
		}

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.RESPONDED);
		originalInvitation.setInvitationCode("abc");
		originalInvitation.setResponse(response);

		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, originalInvitation.getId() + "." + originalInvitation.getInvitationCode(), true, false, true);
	}

	@Test
	public void invitationCode_BadCode_InvitationCode() {

		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
		persist(response);

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.RESPONDED);
		originalInvitation.setInvitationCode("abc");
		originalInvitation.setResponse(response);
		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, originalInvitation.getId() + "." + originalInvitation.getInvitationCode() + "not valid",
				false, false, false);
	}

	@Test
	public void invitationCode_BadCode_IdNotANumber() {

		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
		persist(response);

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.RESPONDED);
		originalInvitation.setInvitationCode("abc");
		originalInvitation.setResponse(response);
		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, "notANumber." + originalInvitation.getInvitationCode(), false, false, false);
	}

	@Test
	public void invitationCode_BadCode_IdNotAnInvite() {

		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
		persist(response);

		Invitation originalInvitation = new Invitation(survey, "x@x.com");
		originalInvitation.setStatus(InvitationStatusType.RESPONDED);
		originalInvitation.setInvitationCode("abc");
		originalInvitation.setResponse(response);
		persist(originalInvitation);

		flushAndClear();

		doInvitationCodeTest(survey, survey.getId() + "." + originalInvitation.getInvitationCode(), false, false, false);
	}

}
