package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Survey;

public class CloneQuestionControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private CloneQuestionController controller;

	@Test
	public void foreachQuestionType() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		int initialCount = -1;

		for (Question question : getSampleQuestionsForEachType(survey)) {

			// save it
			persist(question);

			initialCount = surveyService.countQuestions(survey);

			controller.cloneQuestion(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
			// flushAndClear();
			getHibernateTemplate().flush();

			int newCount = surveyService.countQuestions(survey);
			assertEquals("clone didn't happen, or happened more than once on: " + question.getClass().getSimpleName(),
					initialCount + 1, newCount);
		}
	}

	@Test
	public void simple_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();

		List<Question> src = survey.getQuestions();
		List<Question> list = new ArrayList<Question>(92);

		list.addAll(src);

		int numTested = 0;
		for (Question question : list) {

			int initialCount = surveyService.countQuestions(survey);

			controller.cloneQuestion(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
			// flushAndClear();

			int newCount = surveyService.countQuestions(survey);

			// complex tests are handled by the service test, the controller
			// only
			// delegates, thus this is enough
			assertEquals("clone didn't happen, or happened more than once", initialCount + 1, newCount);

			numTested++;
		}

		assertTrue("expected some types to be tested", numTested > 0);
	}
}
