package com.oas.controller.dashboard.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.TextQuestion;
import com.oas.model.report.abandonment.PartialResponseHighestQuestionSummary;

public class AbandonmentReportControllerTest extends AbstractOASBaseTest {

	@Autowired
	private AbandonmentReportController controller;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Test
	public void basic_Success() {

		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.getSupportedLanguages().add(getEnglish());
		persist(survey);

		// 3 questions
		survey.addQuestion(new TextQuestion(survey, 5, 20, 20, 0L));
		survey.addQuestion(new TextQuestion(survey, 5, 20, 20, 1L));
		survey.addQuestion(new TextQuestion(survey, 5, 20, 20, 2L));

		persist(survey);

		// 3 abandoned at the start
		addAbandonedAt(survey, 0, 3);

		// 1 abandoned at 2nd question
		addAbandonedAt(survey, 1, 1);

		// 2 abandoned at 3nd question
		addAbandonedAt(survey, 2, 2);

		flushAndClear();

		ModelAndView mav = controller.questionSummaryReport(new MockHttpServletRequest("GET", "/prefix/" + survey.getId()
				+ ".html"));
		assertNotNull(mav);
		assertHasViewNamePart(mav, "questionSummary");
		List<PartialResponseHighestQuestionSummary> list = (List<PartialResponseHighestQuestionSummary>) mav.getModel().get(
				"data");
		assertNotNull(list);
		assertNotEmpty(list);

		for (PartialResponseHighestQuestionSummary item : list) {
			assertEquals("wrong survey", survey.getId(), item.getId().getSurvey().getId());
			long questionIndex = item.getId().getQuestionIndex();
			switch ((int) questionIndex) {
			case 0:
				assertEquals(item.getCount(), 3);
				break;
			case 1:
				assertEquals(item.getCount(), 1);
				break;
			case 2:
				assertEquals(item.getCount(), 2);
				break;
			default:
				fail("Unknown question index: " + questionIndex);
			}
		}
	}

	private void addAbandonedAt(Survey survey, int questionIndex, int count) {

		// Question question = survey.getQuestions().get(questionIndex);
		// assertNotNull("no question at index " + questionIndex, question);

		// repeat $count times:
		for (int i = 0; i < count; i++) {

			// new response
			Response response = new Response(survey, new Date(), getEnglish(), LOCALHOST_IP);
			persist(response);

			// add answers up to $questionIndex
			for (int j = 0; j <= questionIndex; j++) {
				TextQuestion question = (TextQuestion) survey.getQuestions().get(j);
				TextAnswer answer = new TextAnswer(response, question, "text");

				// views are based on history
				responseService.addQuestionToHistory(response, question);
				persist(answer);
			}
		}
	}
}
