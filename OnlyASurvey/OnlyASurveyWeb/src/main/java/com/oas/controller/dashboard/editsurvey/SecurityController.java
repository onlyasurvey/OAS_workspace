package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.IdListCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;

/**
 * Controller for managing security options for Surveys.
 * 
 * @author xhalliday
 * @since April 25, 2009
 */
@Controller
public class SecurityController extends AbstractOASController {

	/**
	 * Show the Security tab view.
	 * 
	 * @param request
	 *            HTTP request
	 * @return {@link ModelAndView}
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/sec/*.html", method = RequestMethod.GET)
	public ModelAndView securityTab(HttpServletRequest request) {
		//
		Survey survey = getSurveyFromRestfulUrl(request);

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("statusLabel", determineSecurityLabel(survey));
		model.put("command", determineSecurityRadio(survey));

		//
		return new ModelAndView("/dashboard/manage/security/securityTab", model);
	}

	/**
	 * Construct an IdListCommand that represents the current state of the
	 * security option.
	 * 
	 * @param survey
	 *            Survey
	 * @return {@link IdListCommand}
	 */
	protected IdListCommand determineSecurityRadio(Survey survey) {

		if (StringUtils.hasText(survey.getGlobalPassword())) {
			return new IdListCommand(new Long[] { 1L });
		} else {
			return new IdListCommand(new Long[] { 0L });
		}
	}

	/**
	 * Determine the resource key to show for the current security option.
	 * 
	 * @param survey
	 *            Survey
	 * @return String
	 */
	protected String determineSecurityLabel(Survey survey) {
		if (StringUtils.hasText(survey.getGlobalPassword())) {
			return "securityTab.level.passwordPerSurvey";
		} else {
			return "securityTab.level.default";
		}
	}

	/**
	 * Process the Security tab's Change button.
	 * 
	 * @param request
	 *            HTTP request
	 * @return {@link ModelAndView}
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/sec/*.html", method = RequestMethod.POST)
	public ModelAndView securityTabChangeSubmit(HttpServletRequest request, @ModelAttribute IdListCommand command) {
		//
		Survey survey = getSurveyFromRestfulUrl(request);

		int securityOption = command.getIds().get(0).intValue();

		if (securityOption == 0) {
			// disable
			survey.setGlobalPassword(null);
			surveyService.save(survey);
			return createRedirect("/html/db/mgt/sec/" + survey.getId() + ".html");

		} else {
			// forward to form
			return createRedirect("/html/db/mgt/sec/pw/" + survey.getId() + ".html");
		}
	}

	/**
	 * Show the password prompting form.
	 * 
	 * @param request
	 *            HTTP request
	 * @return {@link ModelAndView}
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/sec/pw/*.html", method = RequestMethod.GET)
	public ModelAndView passwordPrompt(HttpServletRequest request) {
		//
		Survey survey = getSurveyFromRestfulUrl(request);

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);

		//
		return new ModelAndView("/dashboard/manage/security/surveyPasswordForm", model);
	}

	/**
	 * Show the password prompting form.
	 * 
	 * @param request
	 *            HTTP request
	 * @return {@link ModelAndView}
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/sec/pw/*.html", method = RequestMethod.POST)
	public ModelAndView passwordPromptSubmit(HttpServletRequest request, @RequestParam("pw1") String password1,
			@RequestParam("pw2") String password2) {
		//
		Survey survey = getSurveyFromRestfulUrl(request);

		if (isCancel(request)) {
			return createRedirect("/html/db/mgt/sec/" + survey.getId() + ".html");
		}

		//
		Errors errors = new BindException(survey, "command");
		if (StringUtils.hasLength(password1) && StringUtils.hasLength(password2)) {
			if (password1.equals(password2)) {
				if (password1.length() > 32) {
					errors.reject("securityTab.error.passwordTooLong");
				}
			} else {
				errors.reject("securityTab.error.mismatch");
			}
		} else {
			errors.reject("securityTab.error.empty");
		}

		//
		if (errors.hasErrors()) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("survey", survey);
			model.put("errors", errors);
			return new ModelAndView("/dashboard/manage/security/surveyPasswordForm", model);
		} else {
			//
			survey.setGlobalPassword(password1);
			surveyService.save(survey);

			return createRedirect("/html/db/mgt/sec/" + survey.getId() + ".html");
		}
	}

}
