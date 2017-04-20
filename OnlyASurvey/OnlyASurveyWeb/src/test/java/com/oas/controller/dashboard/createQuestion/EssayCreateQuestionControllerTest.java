package com.oas.controller.dashboard.createQuestion;

import static junit.framework.Assert.assertNotNull;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.controller.dashboard.editsurvey.AbstractCreateQuestionControllerTest;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.TextQuestion;

public class EssayCreateQuestionControllerTest extends AbstractCreateQuestionControllerTest {

	@Override
	public ModelAndView invokeCreateFormMethod(MockHttpServletRequest request) {
		return getCreateQuestionController().doEssayQuestion(request);
	}

	@Override
	public ModelAndView invokeCreateSubmitMethod(MockHttpServletRequest request) throws Exception {
		return getCreateQuestionController().doEssayQuestionSubmit(request);
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
		request.setParameter("maximumLength", "2000");
		request.setParameter("numRows", "8");
		request.setParameter("fieldDisplayLength", "0");
		request.setParameter("map[eng]", NAME_EN);
		request.setParameter("map[fra]", NAME_FR);
	}

	@Override
	public void addInvalidRequestParameters(MockHttpServletRequest request) {
		request.setParameter("numRows", "fancy\4:;'");
		request.setParameter("map[eng]", "");
	}

	@Override
	public Question getPersistentQuestionOfAppropriateType(Survey survey) {
		Question retval = null;

		for (Question question : survey.getQuestions()) {
			if (question.isTextQuestion()) {
				if (((TextQuestion) question).getNumRows() > 1) {
					retval = question;
					break;
				}
			}
		}

		assertNotNull("unable to find Essay question", retval);
		return retval;
	}
}
