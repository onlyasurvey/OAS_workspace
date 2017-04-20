package com.oas.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.oas.command.model.PreferencesCommand;
import com.oas.service.SupportedLanguageService;

/**
 * Validate the PreferencesCommand class.
 * 
 * @author xhalliday
 * @since October 31, 2008
 */
@Component
public class PreferencesCommandValidator implements Validator {

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return PreferencesCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PreferencesCommand command = (PreferencesCommand) target;

		validateUserLanguage(command, errors);
		validateSurveyLanguages(command, errors);

	}

	protected void validateUserLanguage(PreferencesCommand command, Errors errors) {

		Long languageId = command.getLanguageId();

		// UI prevents this, so it's an illegal argument
		if ((languageId == null) || (!supportedLanguageService.getSupportedLanguageIds().contains(languageId))) {
			errors.rejectValue("languageId", "illegalArgument");
		}
	}

	protected void validateSurveyLanguages(PreferencesCommand command, Errors errors) {

		if (command.getSurveyLanguageIdList() == null) {
			// UI prevents this, so it's an illegal argument
			errors.rejectValue("surveyLanguageIdList", "illegalArgument");
			// no point continuing
			return;
		}

		if (!supportedLanguageService.isValidIdList(command.getSurveyLanguageIdList())) {
			errors.rejectValue("surveyLanguageIdList", "illegalArgument");
			return;
		}
	}
}
