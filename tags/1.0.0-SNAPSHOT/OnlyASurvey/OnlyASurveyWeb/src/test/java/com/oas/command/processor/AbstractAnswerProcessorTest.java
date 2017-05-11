package com.oas.command.processor;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;

public class AbstractAnswerProcessorTest extends AbstractOASBaseTest {

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	public class TestAnswerProcessor extends AbstractAnswerProcessor {
	}

	@Test
	public void testValidateObjectRelationships_Success() {
		TestAnswerProcessor proc = new TestAnswerProcessor();
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), false);
		getHibernateTemplate().persist(survey);
		scenarioDataUtil.addDefaultResponse(survey);
		Response response = firstResponse(survey);
		Question question = survey.getQuestions().get(0);

		// should never throw
		proc.validateObjectRelationships(survey, response, question);
	}
}
