package com.oas.controller.dashboard.editsurvey;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;

/**
 * Clones a question.
 * 
 * @author xhalliday
 * @since November 22, 2008
 */
@Controller
public class CloneQuestionController extends AbstractOASController {

	@RequestMapping(value = "/db/mgt/clq/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView cloneQuestion(HttpServletRequest request) {

		// load question from URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		// do the deed
		surveyService.cloneQuestion(question);

		return createRedirect("/html/db/mgt/ql/" + survey.getId() + ".html");
	}
}
