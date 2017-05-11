package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.IdListCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;

/**
 * Controller for managing survey preferences.
 * 
 * @author xhalliday
 * @since November 22, 2008
 */
@Controller
public class SurveyPreferencesController extends AbstractOASController {

	/**
	 * Default constructor.
	 */
	public SurveyPreferencesController() {
		setFormView("/dashboard/manage/surveyLanguagesForm");
	}

	/**
	 * Load data common to each request.
	 */
	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {

		requireSecureContext();

		// get the survey ID from the URL, load it and assert ownership
		Map<String, Object> model = new HashMap<String, Object>();
		Survey survey = getSurveyFromRestfulUrl(request);

		// i18n
		model.put("supportedLanguages", supportedLanguageService.getSupportedLanguages());

		// set the model
		model.put("survey", survey);

		//
		return model;
	}

	// ======================================================================

	/**
	 * Form View.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/pref/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doForm(HttpServletRequest request) {

		requireSecureContext();
		Map<String, Object> model = referenceData(request);
		Assert.notNull(model);

		// get the survey from the model
		Survey survey = getSurveyFromModel(model);
		Assert.notNull(survey, "survey data missing");

		Long[] languageIds = new Long[survey.getSupportedLanguages().size()];
		int i = 0;
		for (SupportedLanguage language : survey.getSupportedLanguages()) {
			languageIds[i++] = language.getId();
		}

		// required for re-use of the question JSP fragments used when a
		// respondent is answering a survey.
		model.put("command", new IdListCommand(languageIds));

		//
		applyWideLayout(request);
		return new ModelAndView(getFormView(), model);
	}

	// ======================================================================

	/**
	 * Submit Form.
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/pref/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doSubmit(HttpServletRequest request, IdListCommand command) throws Exception {

		requireSecureContext();
		Map<String, Object> model = referenceData(request);
		Assert.notNull(model);

		//
		applyWideLayout(request);

		// get the survey from the model
		Survey survey = getSurveyFromModel(model);
		Assert.notNull(survey, "survey data missing");

		if (isCancel(request)) {
			return createRedirect("/html/db/mgt/" + survey.getId() + ".html");
		}

		BindException errors = new BindException(bindAndValidate(request, command).getBindingResult());

		// validate data
		if (CollectionUtils.isEmpty(command.getIds())) {
			// need at least one
			errors.reject("setSurveyPreferences.error.atLeastOneLanguageRequired");

		} else if (!supportedLanguageService.isValidIdList(command)) {
			// invalid data from the client
			errors.reject("illegalArgument");
		}

		if (errors.hasErrors()) {
			model.put("errors", errors);
			model.put("command", command);

			return new ModelAndView(getFormView(), model);
		} else {
			// submit

			// persist
			surveyService.setSurveyLanguages(survey, command);

			// redirect out
			return createRedirect("/html/db/mgt/" + survey.getId() + ".html");
		}
	}
}
