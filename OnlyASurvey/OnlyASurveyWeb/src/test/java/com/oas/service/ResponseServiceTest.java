package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.AccountOwner;
import com.oas.model.Answer;
import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.answer.BooleanAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.TextQuestion;

public class ResponseServiceTest extends AbstractOASBaseTest {

	@Autowired
	private DomainModelService domainModelService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	public interface CreateTestDataCallback {
		void getAnswers(List<Answer> answerList, Question question, Response response);
	}

	@Test
	public void save() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		flushAndClear();
		assertNotNull(survey);
		assertNotNull(survey.getId());

		Response created = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP, null, null);

		// now invoke save
		getResponseService().save(created);
		assertNotNull(created);
		assertNotNull(created.getId());
	}

	@Test
	public void createResponse_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response subject = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP, null, null);
		flushAndClear();

		Survey found = getSurveyService().findNonDeletedSurvey(survey.getId());
		assertNotNull(found);
		assertNotNull(found.getId());

		assertNotNull("should have returned a response", subject);
		assertNotNull("should have persisted the response", subject.getId());
		assertEquals("should have same survey", survey, subject.getSurvey());
	}

	public void doSaveAnswerList(CreateTestDataCallback callback) {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		assertNotNull(survey);
		assertNotNull(survey.getQuestions());
		Question question = survey.getQuestions().iterator().next();
		flushAndClear();

		Survey found = getSurveyService().findNonDeletedSurvey(survey.getId());
		assertNotNull(found);
		assertNotNull(found.getId());

		Response response = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP, null, null);
		assertNotNull("should have returned a response", response);
		assertNotNull("should have persisted the response", response.getId());
		assertEquals("should have same survey", survey, response.getSurvey());

		long initialCount = countBaseObjects();

		// caller's data population

		List<Answer> answerList = new ArrayList<Answer>();
		if (callback != null) {
			callback.getAnswers(answerList, question, response);
		}

		// persist
		getResponseService().saveAnswerList(answerList, response, question);
		flushAndClear();

		long newCount = countBaseObjects();

		assertEquals("unexpected # of objects in the database", initialCount + answerList.size(), newCount);
	}

	/** Saves answers when answers are provided. */
	@Test
	public void saveAnswerList_Success_RealAnswers() {
		doSaveAnswerList(new CreateTestDataCallback() {
			@Override
			public void getAnswers(List<Answer> answerList, Question question, Response response) {
				answerList.add(new BooleanAnswer(response, question, true));
			}
		});
	}

	/** Returns successfully when an empty collection is passed in. */
	@Test
	public void saveAnswerList_Success_EmptyAnswers() {

		doSaveAnswerList(null);
	}

	@Test
	public void closeResponse_FlagSet_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP, null, null);
		assertFalse("response should be OPEN", response.isClosed());

		getResponseService().closeResponse(response);
		flushAndClear();

		assertTrue("response should be CLOSED", response.isClosed());
		assertNotNull("response should have closedDate", response.getDateClosed());
	}

	/**
	 * When a Response is Closed, any answers that may exist but that are not
	 * referenced in the ResponseQuestionHistory are to be removed, because they
	 * can exist due to navigation and branching - for example, if a user
	 * responds to a few questions, then navigates back and selects a different
	 * choice somewhere, branching rules could render their previously-entered
	 * answers irrelevant because they were no longer part of the flow.
	 */
	@Test
	public void closeResponse_AnswersNotInHistoryGetRemoved() {

		AccountOwner owner = (AccountOwner) createAndSetSecureUserWithRoleUser();
		Survey survey = new Survey(owner);
		Question q1 = new TextQuestion(survey, 5, 10, 20, 0);
		Question q2 = new TextQuestion(survey, 5, 10, 20, 1);
		Question q3 = new TextQuestion(survey, 5, 10, 20, 2);
		survey.addQuestion(q1);
		survey.addQuestion(q2);
		survey.addQuestion(q3);

		persist(survey);
		flushAndClear();

		long initialCountOfAllAnswers = (Long) unique(find("select count(*) from Answer"));

		// add a response with answers for three questions
		// do NOT use the service, as it adds history which is done manually
		// (specifically) below
		Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
		response.addAnswer(new TextAnswer(response, q1, "a1"));
		response.addAnswer(new TextAnswer(response, q2, "a2"));
		response.addAnswer(new TextAnswer(response, q3, "a3"));

		persist(response);

		// this will add the history
		responseService.addQuestionToHistory(response, q1);
		// responseService.addQuestionToHistory(response, q2);
		responseService.addQuestionToHistory(response, q3);

		long newCountOfAllAnswers = (Long) unique(find("select count(*) from Answer"));
		assertEquals("unexpected count of ALL ANSWERS", initialCountOfAllAnswers + 3, newCountOfAllAnswers);

		// while the Response is not Closed, all 3 answers should exist
		long count1 = (Long) unique(find("select count(*) from Answer where response = ?", response));
		assertEquals("expected all 3 answers to be saved BEFORE response is closed", 3, count1);

		// FLAC
		flushAndClear();

		// set the history to only include the first and last question - q2
		// should not be saved
		getResponseService().closeResponse(get(Response.class, response.getId()));

		// FLAC
		flushAndClear();

		// verify history size
		long historyCount = (Long) unique(find("select count(*) from ResponseQuestionHistory where id.response = ?", response));
		assertEquals("expected only 2 items in the history!", 2, historyCount);

		// now only 2 answers should exist for this response
		long count2 = (Long) unique(find("select count(*) from Answer where response = ?", response));
		assertEquals("expected 2 answers to exist AFTER response is closed", 2, count2);
	}

	// ======================================================================

	@Test
	public void sendRespondentEmail_Success() throws MessagingException, IOException {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP, null, null);

		String hostname = domainModelService.getPublicHostname();
		assertHasText(hostname);

		responseService.sendRespondentLink(response, TEST_EMAIL);
		//
		List<Message> list = Mailbox.get(TEST_EMAIL);
		assertEquals("no mail sent", 1, list.size());
		Message message = list.get(0);
		assertEquals("unexpected content type", "text/plain; charset=utf8", message.getContentType());
		String content = (String) message.getContent();
		assertTrue("expected content to contain public hostname", content.contains(hostname));
	}

	// ======================================================================

	@Test
	public void invitationCodeGetsLinked() {
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		Invitation invite = new Invitation(survey, "x@x.com");
		invite.setInvitationCode("abc");
		invite.setStatus(InvitationStatusType.UNSENT);
		persist(invite);

		flushAndClear();

		Response response = responseService.createResponse(survey, getEnglish(), LOCALHOST_IP, null, get(Invitation.class, invite
				.getId()));

		flushAndClear();

		Invitation loaded = get(Invitation.class, invite.getId());
		assertTrue(loaded.isStarted());
		assertFalse(loaded.isResponded());
		assertNotNull(loaded.getResponse());
		assertEquals(loaded.getResponse().getId(), response.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void invitationCodeCannotBeReusedOnClosedResponse() {
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		Invitation invite = new Invitation(survey, "x@x.com");
		invite.setInvitationCode("abc");
		invite.setStatus(InvitationStatusType.UNSENT);
		persist(invite);

		flushAndClear();

		Response response = responseService.createResponse(survey, getEnglish(), LOCALHOST_IP, null, get(Invitation.class, invite
				.getId()));
		response.setClosed(true);
		persist(response);

		flushAndClear();

		Invitation loaded = get(Invitation.class, invite.getId());
		assertNotNull(loaded.getResponse());
		assertEquals(loaded.getResponse().getId(), response.getId());

		// this is expected to fail because the status is already RESPONDED, and
		// the response is linked in and not deleted
		responseService.createResponse(survey, getEnglish(), LOCALHOST_IP, null, get(Invitation.class, invite.getId()));
	}

	@Test
	public void invitationCode_CAN_BeReused_IfStartedAndNotResponded() {
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		Invitation invite = new Invitation(survey, "x@x.com");
		invite.setInvitationCode("abc");
		invite.setStatus(InvitationStatusType.UNSENT);
		persist(invite);

		flushAndClear();

		Response response = responseService.createResponse(survey, getEnglish(), LOCALHOST_IP, null, get(Invitation.class, invite
				.getId()));

		flushAndClear();

		Invitation loaded = get(Invitation.class, invite.getId());
		assertTrue(loaded.isStarted());
		assertFalse(loaded.isResponded());
		assertNotNull(loaded.getResponse());
		assertEquals(loaded.getResponse().getId(), response.getId());

		Response duplicateResponse = responseService.createResponse(survey, getEnglish(), LOCALHOST_IP, null, get(
				Invitation.class, invite.getId()));

		assertEquals("expected same response back", response.getId(), duplicateResponse.getId());
	}

}
