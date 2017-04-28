package com.oas.controller.dashboard;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.command.model.CreateQuestionCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.service.DashboardService;
import com.oas.service.SurveyService;

/**
 * Parent class for Create and Edit Question controllers.
 * 
 * @author xhalliday
 * @since October 26, 2008
 */
abstract public class AbstractQuestionManagementController extends AbstractOASController {

	@Autowired
	protected SurveyService surveyService;

	@Autowired
	protected DashboardService dashboardService;

	@Autowired
	public AbstractQuestionManagementController() {
	}

	@Autowired
	protected MessageSource messageSource;

	// ======================================================================
	// Callback Interfaces
	// ======================================================================

	interface SetUrlCallback {
		String getUrl(Question question);
	}

	// ======================================================================
	// OTHER STUFF
	// ======================================================================

	protected Question addOrUpdateQuestion(Survey survey, CreateQuestionCommand command, Question questionToUpdate) {
		if (questionToUpdate == null) {
			// add to model
			return surveyService.addQuestion(survey, command);
		} else {
			// update existing
			return surveyService.updateQuestion(survey, command, questionToUpdate);
		}
	}

	protected ModelAndView persistAndRedirect(Survey survey, CreateQuestionCommand command, Question questionToUpdate) {
		return persistAndRedirect(survey, command, questionToUpdate, null);
	}

	protected ModelAndView persistAndRedirect(Survey survey, CreateQuestionCommand command, Question questionToUpdate, String url) {

		//
		Question savedQuestion = addOrUpdateQuestion(survey, command, questionToUpdate);

		//
		if (StringUtils.hasText(url)) {
			//
			String useUrl = url.replaceAll("\\{questionId\\}", savedQuestion.getId().toString());
			return createRedirect(useUrl);
		} else {
			return redirectToSurvey(survey);
		}
	}

	protected ModelAndView redirectToSurvey(Survey survey) {
		return new ModelAndView(new RedirectView("/html/db/mgt/ql/" + survey.getId() + ".html", true));
	}

	/**
	 * Initialize a basic command object, returning it and adding it to the
	 * given model.
	 * 
	 * @param request
	 * @return
	 */
	protected CreateQuestionCommand initializeBasicCommand(Map<String, Object> model) {

		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey, "no survey");
		Assert.isNull(model.get("command"), "existing command");

		CreateQuestionCommand command = new CreateQuestionCommand(survey.getSupportedLanguages());
		model.put("command", command);

		return command;
	}

}
