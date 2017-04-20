package com.oas.controller.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.IdListCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.SurveyLanguage;
import com.oas.service.SupportedLanguageService;
import com.oas.service.SurveyService;
import com.oas.util.Constants;
import com.oas.validator.CreateSurveyValidator;

@Controller()
@RequestMapping("/db/crt.html")
public class CreateSurveyController extends AbstractOASController {

	/** Survey management service. */
	@Autowired
	private SurveyService surveyService;

	/** Service for i18n. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	/**
	 * Constructor that takes the validator as a parameter.
	 * 
	 * @param validator
	 */
	@Autowired
	public CreateSurveyController(CreateSurveyValidator validator) {
		setFormView("/dashboard/create/step1");
		setValidator(validator);
	}

	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> model = new HashMap<String, Object>();

		// don't mess with service's data
		List<SupportedLanguage> languageList = new ArrayList<SupportedLanguage>(supportedLanguageService.getSupportedLanguages());

		// sort according to user's preference
		Collections.sort(languageList);

		model.put("supportedLanguages", languageList);
		return model;
	}

	// ======================================================================

	/**
	 * Show the Create Survey form.
	 */
	@RequestMapping(value = "/db/crt.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doMain(HttpServletRequest request) {

		requireSecureContext();

		Map<String, Object> model = referenceData(request);
		model.put("command", newDefaultIdListCommand());
		return new ModelAndView(getFormView(), model);
	}

	// ======================================================================

	/**
	 * Cancel creating a question.
	 */
	private ModelAndView doCancel(HttpServletRequest request) {

		// ?rTo = URL
		String rTo = request.getParameter("rTo");

		// the default
		String redirectUrl = Constants.DEFAULT_HOME;

		if (StringUtils.hasText(rTo)) {
			// ignore anything that seems to have a protocol, as DiD for
			// outsiders crafting URLs (XSS/CSRF).
			if (rTo.indexOf(":") == -1) {
				redirectUrl = rTo;
			}
		}

		//
		return createRedirect(redirectUrl);
	}

	// ======================================================================

	/**
	 * Submit the list of supported languages.
	 * 
	 * @param request
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/crt.html", method = RequestMethod.POST)
	protected ModelAndView doSubmit(HttpServletRequest request) throws Exception {

		requireSecureContext();

		if (isCancel(request)) {
			return doCancel(request);
		}

		// must be the correct type
		IdListCommand command = new IdListCommand();
		BindException errors = new BindException(bindAndValidate(request, command).getBindingResult());

		if (errors.hasErrors()) {
			// nothing selected
			Map<String, Object> model = referenceData(request);
			model.put("command", command);
			model.put("errors", errors);

			// show the form with validation errors
			return showForm(request, errors, getFormView(), model);
		} else {
			// throw new RuntimeException("got ids");
			Survey survey = new Survey(getCurrentUser());

			// get a list of supported language objects by matching the request
			// parameter ?ids
			Collection<SupportedLanguage> languageList = supportedLanguageService.findByIdList(command.getIds());

			// add them into the survey
			for (SupportedLanguage language : languageList) {

				Locale locale = language.getLocale();

				survey.getSurveyLanguages().add(new SurveyLanguage(survey, language));
				// add a default title
				survey.addObjectName(language, getMessageSourceAccessor().getMessage("createSurvey.defaultNewName", locale));

				// default resources
				survey.addObjectResource(language, "welcomeMessage", getMessageSourceAccessor().getMessage(
						"createSurvey.defaultWelcomeMessage", locale));
				survey.addObjectResource(language, "thanksMessage", getMessageSourceAccessor().getMessage(
						"createSurvey.defaultThanksMessage", locale));
				survey.addObjectResource(language, "pausedMessage", getMessageSourceAccessor().getMessage(
						"createSurvey.defaultPausedMessage", locale));
				survey.addObjectResource(language, "closedMessage", getMessageSourceAccessor().getMessage(
						"createSurvey.defaultClosedMessage", locale));
			}

			surveyService.save(survey);
			return new ModelAndView(new RedirectView("/html/db/mgt/" + survey.getId() + ".html", true));
		}
	}

	private IdListCommand newDefaultIdListCommand() {
		IdListCommand retval = new IdListCommand();

		// add user's language as a default
		String isoCode = LocaleContextHolder.getLocale().getISO3Language();
		retval.addId(supportedLanguageService.findByCode(isoCode).getId());

		return retval;
	}
}
