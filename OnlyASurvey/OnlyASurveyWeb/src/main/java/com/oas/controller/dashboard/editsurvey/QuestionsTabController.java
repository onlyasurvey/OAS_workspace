package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.util.Constants;

/**
 * Look & Feel for Surveys controller.
 * 
 * @author xhalliday
 * @since November 22, 2008
 */
@Controller
public class QuestionsTabController extends AbstractOASController {

	/**
	 * Show the Questions tab view.
	 * 
	 * @return {@link ModelAndView}
	 */
	@RequestMapping("/db/mgt/ql/*.html")
	@ValidUser
	public ModelAndView showTabView(HttpServletRequest request) {
		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);
		Map<String, Object> model = new HashMap<String, Object>();

		// number of questions, used by the preview
		model.put("numQuestions", surveyService.countQuestions(survey));

		// required for re-use of the question JSP fragments used when a
		// respondent is answering a survey.
		SimpleAnswerCommand simpleCommand = new SimpleAnswerCommand();
		simpleCommand.setSumByChoiceId(new HashMap<Long, Integer>());
		model.put("command", simpleCommand);

		// model.put("scaleCommands", getScaleCommands(survey));
		model.put("pageCommands", getPageCommands(survey));

		// this is used by question renderers to omit the @id on <input/>'s, to
		// avoid validation errors when questions are only being previewed.
		model.put("previewMode", "true");

		//
		model.put("survey", survey);

		// show view
		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/questionManagement/questionsTab", model);
	}

	private Map<Long, SimpleAnswerCommand> getPageCommands(Survey survey) {
		Map<Long, SimpleAnswerCommand> retval = new HashMap<Long, SimpleAnswerCommand>();

		for (Question question : survey.getQuestions()) {
			if (question.isPageQuestion()) {
				SimpleAnswerCommand command = new SimpleAnswerCommand();
				command.setPageContent(domainModelService.findObjectText(question, Constants.ObjectTextKeys.PAGE_CONTENT));
				retval.put(question.getId(), command);
			}
		}

		return retval;
	}

	// protected Map<Long, SimpleAnswerCommand> getScaleCommands(Survey survey)
	// {
	// Map<Long, SimpleAnswerCommand> retval = new HashMap<Long,
	// SimpleAnswerCommand>();
	//
	// for (Question question : survey.getQuestions()) {
	// if (question.isScaleQuestion()) {
	// SimpleAnswerCommand command = new SimpleAnswerCommand();
	// command.createChoiceIdListAsRange(1, ((ScaleQuestion)
	// question).getMaximum());
	// retval.put(question.getId(), command);
	// }
	// }
	//
	// return retval;
	// }
}
