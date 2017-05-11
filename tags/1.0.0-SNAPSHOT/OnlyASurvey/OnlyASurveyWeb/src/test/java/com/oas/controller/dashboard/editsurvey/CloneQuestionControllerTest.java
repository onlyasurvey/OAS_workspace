package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;

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
	public void testSuccess() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);

		int initialCount = surveyService.countQuestions(survey);

		controller.cloneQuestion(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
		flushAndClear();

		int newCount = surveyService.countQuestions(survey);

		// complex tests are handled by the service test, the controller only
		// delegates, thus this is enough
		assertEquals("clone didn't happen, or happened more than once", initialCount + 1, newCount);
	}
}
