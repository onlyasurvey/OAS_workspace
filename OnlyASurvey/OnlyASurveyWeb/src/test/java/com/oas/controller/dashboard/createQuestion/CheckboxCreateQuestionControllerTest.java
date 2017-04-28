package com.oas.controller.dashboard.createQuestion;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.command.model.CreateQuestionCommand;
import com.oas.controller.dashboard.editsurvey.AbstractCreateQuestionControllerTest;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.util.QuestionTypeCode;

public class CheckboxCreateQuestionControllerTest extends AbstractCreateQuestionControllerTest {

	@Override
	public ModelAndView invokeCreateFormMethod(MockHttpServletRequest request) {
		return getCreateQuestionController().doCheckboxes(request);
	}

	@Override
	public ModelAndView invokeCreateSubmitMethod(MockHttpServletRequest request) throws Exception {
		return getCreateQuestionController().doCheckboxesSubmit(request);
	}

	// ======================================================================

	@Override
	public ModelAndView invokeEditFormMethod(MockHttpServletRequest request) throws Exception {
		return getEditQuestionController().showForm(request);
	}

	@Override
	public ModelAndView invokeEditSubmitMethod(MockHttpServletRequest request) throws Exception {
		return getEditQuestionController().doSubmit(request);
	}

	// ======================================================================

	@Override
	public void addValidRequestParameters(MockHttpServletRequest request) {
		request.setParameter("map[eng]", NAME_EN);
		request.setParameter("map[fra]", NAME_FR);

		request.setParameter("choiceList[0].map[eng]", NAME_EN);
		request.setParameter("choiceList[0].map[fra]", NAME_FR);

		request.setParameter("choiceList[1].map[eng]", NAME_EN);
		request.setParameter("choiceList[1].map[fra]", NAME_FR);
	}

	@Override
	public void onCreateSubmitSuccess(HttpServletRequest request, ModelAndView mav, Survey survey) {

		// NOTE if this gets slow, optimize the db
		// should always return the newest question for the survey
		@SuppressWarnings("unchecked")
		Collection<Question> list = getHibernateTemplate().find("from Question where survey=? order by id desc", survey);

		// MUST always have choices; the test will be in error otherwise
		ChoiceQuestion question = (ChoiceQuestion) list.iterator().next();

		assertEquals("should have two choices", 2, question.getChoices().size());
		for (Choice choice : question.getChoices()) {
			assertTrue("must have a matching name", NAME_EN.equals(choice.getDisplayTitle()));
		}
	}

	@Override
	public void addInvalidRequestParameters(MockHttpServletRequest request) {
		request.setParameter("map[eng]", "");
	}

	@Override
	public Question getPersistentQuestionOfAppropriateType(Survey survey) {
		// to avoid complex delete cascades (currently
		// unimplemented/not required), we create a new question without any
		// answers bound to a response

		CreateQuestionCommand command = new CreateQuestionCommand(survey.getSupportedLanguages());
		command.setTypeCode(QuestionTypeCode.RADIO);
		Question question = surveyService.addQuestion(survey, command);

		assertNotNull(question);
		assertNotNull(question.getId());

		return (Question) question;
	}

}
