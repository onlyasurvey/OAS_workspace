package com.oas.controller.dashboard.createQuestion;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.command.model.CreateQuestionCommand;
import com.oas.controller.dashboard.editsurvey.AbstractCreateQuestionControllerTest;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.ScaleQuestion;
import com.oas.util.Constants;
import com.oas.util.QuestionTypeCode;

public class ScaleQuestionCreateEditControllerTest extends AbstractCreateQuestionControllerTest {

	/** isRequired() flag. */
	private static final boolean IS_REQUIRED = true;

	/** Maximum range. */
	private static final Long MAXIMUM = 11L;

	@Override
	public ModelAndView invokeCreateFormMethod(MockHttpServletRequest request) {
		return getCreateQuestionController().doScaleQuestion(request);
	}

	@Override
	public ModelAndView invokeCreateSubmitMethod(MockHttpServletRequest request) throws Exception {
		return getCreateQuestionController().doScaleQuestionSubmit(request);
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

		if (IS_REQUIRED) {
			request.setParameter("required", "on");
		}
		request.setParameter("maximum", "" + MAXIMUM);
	}

	@Override
	public void onCreateSubmitSuccess(HttpServletRequest request, ModelAndView mav, Survey survey) {

		// NOTE if this gets slow, optimize the db
		// should always return the newest question for the survey
		Collection<Question> list = getHibernateTemplate().find("from Question where survey=? order by id desc", survey);

		// MUST always have choices; the test will be in error otherwise
		ScaleQuestion question = (ScaleQuestion) list.iterator().next();
		assertEquals("unexpected required flag value", IS_REQUIRED, question.isRequired());
		assertEquals("unexpected maximum value", MAXIMUM, question.getMaximum());
	}

	@Override
	public void addInvalidRequestParameters(MockHttpServletRequest request) {
		request.setParameter("map[eng]", "");
		request.setParameter("maximum", "0");
	}

	@Override
	public Question getPersistentQuestionOfAppropriateType(Survey survey) {
		// to avoid complex delete cascades (currently
		// unimplemented/not required), we create a new question without any
		// answers bound to a response

		CreateQuestionCommand command = new CreateQuestionCommand(survey.getSupportedLanguages());
		command.setTypeCode(QuestionTypeCode.SCALE);
		command.setMinimum(1L);
		command.setMaximum(Constants.DEFAULT_HIGHEST_SCALE);
		Question question = surveyService.addQuestion(survey, command);

		assertNotNull(question);
		assertNotNull(question.getId());

		return (Question) question;
	}

}
