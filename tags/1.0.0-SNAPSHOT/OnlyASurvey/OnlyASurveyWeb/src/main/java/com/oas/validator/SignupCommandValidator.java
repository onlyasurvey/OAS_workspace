package com.oas.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.oas.command.model.SignupCommand;
import com.oas.service.AccountService;

@Component
public class SignupCommandValidator implements Validator {

	/**
	 * User lookup service for determining if a username is already taken.
	 */
	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Account service for determining if an email address is already taken.
	 */
	@Autowired
	private AccountService accountService;

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return SignupCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SignupCommand command = (SignupCommand) target;
		validateUsername(command, errors);
		validatePassword(command, errors);
		// validateUserLanguage(command, errors);
		validateContactInformation(command, errors);
	}

	protected void validateUsername(SignupCommand command, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "signup.error.usernameEmpty");

		// check for existing user
		if (StringUtils.hasText(command.getUsername()) && null != userDetailsService.loadUserByUsername(command.getUsername())) {
			errors.rejectValue("username", "signup.error.usernameTaken");
		}

	}

	protected void validatePassword(SignupCommand command, Errors errors) {

		// check input has two passwords
		if (command.getPassword() == null || command.getPassword().length != 2) {
			// form should always provide 2 passwords - this is invalid input
			errors.reject("illegalArgument");
			return;
		}

		// two passwords specified
		if (!command.getPassword()[0].equals(command.getPassword()[1])) {
			errors.rejectValue("password[1]", "signup.error.passwordsDiffer");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password[0]", "signup.error.passwordEmpty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password[1]", "signup.error.passwordConfirmEmpty");
		}

		// passwords are the same
		if (command.getPassword()[0].length() < 5) {
			errors.rejectValue("password[0]", "signup.error.passwordTooShort");
		}

	}

	protected void validateContactInformation(SignupCommand command, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "firstname", "signup.error.firstNameRequired");
		ValidationUtils.rejectIfEmpty(errors, "lastname", "signup.error.lastNameRequired");
		ValidationUtils.rejectIfEmpty(errors, "organization", "signup.error.organizationRequired");
		ValidationUtils.rejectIfEmpty(errors, "telephone", "signup.error.phoneRequired");

		// sanity
		String learnedAbout = (String) errors.getFieldValue("learnedAbout");
		if (learnedAbout != null && learnedAbout.length() > 2000) {
			// this is huge
			errors.rejectValue("learnedAbout", "signup.error.tooMuchLearnedAboutData");
		}

		// more complex validation for email
		validateEmailAddresses(command, errors);
	}

	protected void validateEmailAddresses(SignupCommand command, Errors errors) {

		// check input has two Email Addresses
		if (command.getEmail() == null || command.getEmail().length != 2) {
			// form should always provide 2 email addresses - this is invalid
			// input
			errors.reject("illegalArgument");
			return;
		}

		// two emails specified
		if (!command.getEmail()[0].equals(command.getEmail()[1])) {
			errors.rejectValue("email[1]", "signup.error.emailAddressesDiffer");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email[0]", "signup.error.emailRequired");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email[1]", "signup.error.confirmEmailRequired");
		}

		if (command.getEmail() != null && StringUtils.hasText(command.getEmail()[0])
				&& StringUtils.hasText(command.getEmail()[1])) {
			// if email addresses are different validator will catch it - only
			// do this check with valid email data
			if (command.getEmail()[0].equals(command.getEmail()[1])) {
				if (accountService.emailAlreadyExists(command.getEmail()[0])) {
					errors.rejectValue("email[0]", "signup.error.emailTaken");
				}
			}
		}

		// TODO: validate that email address is valid format

	}

	// protected void validateUserLanguage(SignupCommand command, Errors errors)
	// {
	// Long languageId = command.getLanguageId();
	// if (languageId == null) {
	// // UI prevents this, so it's invalid input
	// errors.rejectValue("languageId", "illegalArgument");
	//
	// // no point continuing
	// return;
	// }
	//
	// SupportedLanguage language =
	// supportedLanguageService.findById(languageId);
	// if (language == null) {
	// // UI prevents this, so it's invalid input
	// errors.rejectValue("languageId", "illegalArgument");
	//
	// // no point continuing
	// return;
	// }
	// }
}
