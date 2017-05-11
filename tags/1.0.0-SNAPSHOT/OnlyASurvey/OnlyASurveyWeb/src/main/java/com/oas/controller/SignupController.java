package com.oas.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;

import com.oas.command.model.SignupCommand;
import com.oas.model.SupportedLanguage;
import com.oas.service.SignupService;
import com.oas.util.Constants;
import com.oas.validator.SignupCommandValidator;

/**
 * Provides Sign Up functionality
 * 
 * @author xhalliday
 * @since October 4, 2008
 */
@Controller
public class SignupController extends AbstractOASController {

	/**
	 * Sign Up service for creating new accounts.
	 */
	@Autowired
	private SignupService signupService;

	/**
	 * Authentication provider for logging in new users.
	 */
	@Autowired
	@Qualifier("authenticationProvider")
	private AuthenticationProvider authenticationProvider;

	/**
	 * Default constructor.
	 * 
	 * @param validator
	 *            The command validator
	 */
	@Autowired
	public SignupController(SignupCommandValidator validator) {
		setValidator(validator);
	}

	/**
	 * Show the initial Sign Up form.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sgnup.html", method = RequestMethod.GET)
	@Unsecured
	public ModelAndView doForm(HttpServletRequest request) {

		applyWideLayout(request);

		if (SecurityUtil.isSecureContext()) {
			// user already logged in
			return getDefaultHome();
		}

		SignupCommand command = new SignupCommand();
		command.setPassword(new String[] { "", "" });

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("command", command);
		// model.put("languageList",
		// supportedLanguageService.getSupportedLanguages());

		return new ModelAndView("/signup/signupForm", model);
	}

	/**
	 * Submit the Sign Up form data.
	 * 
	 * @param command
	 * @param errors
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/sgnup.html", method = RequestMethod.POST)
	@Unsecured
	public ModelAndView doSubmit(@ModelAttribute("command") SignupCommand command, @ModelAttribute("errors") Errors errors,
			HttpServletRequest request) {

		// set language via inspection of current user's locale
		setLanguageInCommand(command, RequestContextUtils.getLocale(request));

		// validate form data
		getValidator().validate(command, errors);

		if (errors.hasErrors()) {
			Map<String, Object> model = new HashMap<String, Object>();
			// model.put("command", command);
			applyWideLayout(request);
			model.put("languageList", supportedLanguageService.getSupportedLanguages());
			model.put("errors", errors);
			return new ModelAndView("/signup/signupForm", model);
		}

		// is valid: store
		UserAccount user = signupService.storeSignup(command, request.getRemoteAddr());
		Assert.notNull(user, "no user data was stored");

		// set the new user as the current user
		Authentication auth = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(command.getUsername(),
				command.getPassword()[0]));
		SecurityContextHolder.getContext().setAuthentication(auth);

		//
		return new ModelAndView(new RedirectView(Constants.DEFAULT_HOME, true));
	}

	// ======================================================================

	/**
	 * Use the current session's locale to set the language in the command.
	 * 
	 * @param command
	 */
	private void setLanguageInCommand(SignupCommand command, Locale locale) {
		String isoCode = locale.getISO3Language();
		SupportedLanguage language = supportedLanguageService.findByCode(isoCode);
		Assert.notNull(language);

		// command.setLanguageId(language.getId());
	}

}
