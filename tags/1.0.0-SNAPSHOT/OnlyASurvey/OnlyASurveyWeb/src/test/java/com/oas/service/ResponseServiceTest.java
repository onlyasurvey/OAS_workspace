package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.answer.BooleanAnswer;

public class ResponseServiceTest extends AbstractOASBaseTest {

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void testSave() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		flushAndClear();
		assertNotNull(survey);
		assertNotNull(survey.getId());

		Response created = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP);

		// now invoke save
		getResponseService().save(created);
		assertNotNull(created);
		assertNotNull(created.getId());
	}

	@Test
	public void testFindBy_Id() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response created = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP);
		flushAndClear();

		Response found = getResponseService().findById(created.getId());
		assertNotNull(found);
		assertNotNull(found.getId());
		assertEquals(created.getId(), found.getId());
	}

	@Test
	public void testCreateResponse_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response subject = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP);
		flushAndClear();

		Survey found = getSurveyService().findNonDeletedSurvey(survey.getId());
		assertNotNull(found);
		assertNotNull(found.getId());

		assertNotNull("should have returned a response", subject);
		assertNotNull("should have persisted the response", subject.getId());
		assertEquals("should have same survey", survey, subject.getSurvey());
	}

	@Test
	public void testSaveAnswer_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		flushAndClear();
		assertNotNull(survey);
		assertNotNull(survey.getQuestions());
		Question question = survey.getQuestions().iterator().next();

		Survey found = getSurveyService().findNonDeletedSurvey(survey.getId());
		assertNotNull(found);
		assertNotNull(found.getId());

		Response response = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP);
		assertNotNull("should have returned a response", response);
		assertNotNull("should have persisted the response", response.getId());
		assertEquals("should have same survey", survey, response.getSurvey());

		Collection<Answer> answerList = new ArrayList<Answer>(1);
		answerList.add(new BooleanAnswer(response, question, true));
		getResponseService().saveAnswerList(answerList, response, question);
	}

	@Test
	public void testCloseResponse_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Response response = getResponseService().createResponse(survey, currentUserLanguage(), LOCALHOST_IP);
		assertFalse("response should be OPEN", response.isClosed());

		getResponseService().closeResponse(response);
		flushAndClear();

		assertTrue("response should be CLOSED", response.isClosed());
	}
}
