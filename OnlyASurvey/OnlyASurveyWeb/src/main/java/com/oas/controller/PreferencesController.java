package com.oas.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;

import com.oas.model.SupportedLanguage;
import com.oas.service.AccountService;
import com.oas.service.SupportedLanguageService;
import com.oas.util.RestfulIdUrlParser;
import com.oas.validator.PreferencesCommandValidator;

/**
 * Allows user to set language and other preferences.
 * 
 * @author xhalliday
 * @since October 31, 2008
 */
@Controller
public class PreferencesController extends AbstractOASController {

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Autowired
	private AccountService accountService;

	@Autowired
	public PreferencesController(PreferencesCommandValidator validator) {
		setValidator(validator);
	}

	// @RequestMapping(value = "/db/prefs.html", method = RequestMethod.GET)
	// @ValidUser
	// public ModelAndView doForm(HttpServletRequest request) throws Exception {
	//
	// requireSecureContext();
	//
	// applyWideLayout(request);
	//
	// return new ModelAndView("/preferences/preferences",
	// referenceData(request));
	// }

	// @Override
	// protected Map<String, Object> referenceData(HttpServletRequest request)
	// throws Exception {
	// Map<String, Object> retval = new HashMap<String, Object>();
	// retval.put("languageList",
	// supportedLanguageService.getSupportedLanguages());
	// PreferencesCommand command = new PreferencesCommand();
	// //
	// command.setLanguageId(supportedLanguageService.findByCode(LocaleContextHolder.getLocale().getISO3Language()).getId());
	// //
	// command.setLanguageId(accountService.findOwnerOfAccount().getLanguage().getId());
	// command.setLanguageId(((AccountOwner)
	// getCurrentUser()).getLanguage().getId());
	// retval.put("command", command);
	//
	// return retval;
	// }

	// /**
	// * Submit the preference changes and redirect to either the default page
	// or
	// * one specified as ?redirectUrl.
	// *
	// * @param command
	// * @param errors
	// * @param request
	// * @param response
	// * @return
	// * @throws Exception
	// */
	// @RequestMapping(value = "/db/prefs.html", method = RequestMethod.POST)
	// @ValidUser
	// public ModelAndView doSubmit(@ModelAttribute("command")
	// PreferencesCommand command, @ModelAttribute("errors") Errors errors,
	// HttpServletRequest request, HttpServletResponse response) throws
	// Exception {
	//
	// if (isCancel(request)) {
	// // user canceled
	// return new ModelAndView(new RedirectView(determineRedirectUrl(command),
	// true));
	// }
	//
	// // validate form data
	// getValidator().validate(command, errors);
	//
	// if (errors.hasErrors()) {
	// Map<String, Object> model = referenceData(request);
	// //
	// applyWideLayout(request);
	//
	// model.put("errors", errors);
	// return new ModelAndView("/preferences/preferences", model);
	// }
	//
	// // set the user's language: already validated as a valid language
	// SupportedLanguage userLanguage =
	// supportedLanguageService.findById(command.getLanguageId());
	// accountService.setUserLanguage(userLanguage);
	//
	// // set the user's default survey language list
	// // TODO unimplemented
	// //
	// accountService.setDefaultSurveyLanguages(supportedLanguageService.findByIdListCommand(command.getSurveyLanguageIdList()));
	//
	// // set current user's locale
	// changeLocale(request, response, userLanguage.getIso3Lang());
	//
	// // TODO re-authenticate user
	//
	// //
	// return new ModelAndView(new RedirectView(determineRedirectUrl(command),
	// true));
	// }

	/**
	 * Change the user's language and redirect either home or back to a
	 * specified ?url.
	 */
	@RequestMapping(value = { "/eng.html", "/fra.html" })
	@Unsecured
	public ModelAndView doChangeLanguage(HttpServletRequest request, HttpServletResponse response, String url, String rTo) {

		// get the "eng" or "fra" part
		String language = RestfulIdUrlParser.parseLastPathPart(request.getRequestURI());
		Assert.hasText(language, "invalid language");
		SupportedLanguage userLanguage = supportedLanguageService.findByCode(language);
		Assert.notNull(userLanguage, "invalid language [2]");

		// change user's locale: this method takes care of validating the input
		changeLocale(request, response, language);

		// if a user is logged in then store it permanently
		if (SecurityUtil.isSecureContext()) {
			accountService.setUserLanguage(userLanguage);
		}

		// redirect to specified URL, or the dashboard if no URL specified
		return createRedirect(request, coalesce(url, rTo));
	}

}
