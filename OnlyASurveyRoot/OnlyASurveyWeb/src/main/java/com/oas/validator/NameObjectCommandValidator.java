package com.oas.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.oas.command.model.NameObjectCommand;
import com.oas.service.SupportedLanguageService;

/**
 * Generic validator for NameObjectCommand which confirms that each language in
 * the map (by the keySet) has a non-whitespace, non-empty value.
 * 
 * @author xhalliday
 * @since December 18, 2008
 */
@Component
public class NameObjectCommandValidator implements Validator {

	/** Maximum length of a text value, per language. */
	public static final int MAXIMUM_NAME_VALUE_LENGTH = 500;

	/** i18n. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Class clazz) {
		return clazz != null && NameObjectCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		// must be correct type
		Assert.isAssignable(NameObjectCommand.class, target.getClass());
		NameObjectCommand command = (NameObjectCommand) target;

		// basic validation
		validateNameObjectCommand(command, errors);
	}

	protected void validateNameObjectCommand(NameObjectCommand command, Errors errors) {
		for (String language : command.getMap().keySet()) {
			// String value = command.getMap().get(language);
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "map[" + language + "]", "error.nameObject.missingInLanguage");

			// length of value
			String value = command.getMap().get(language);
			if (value.length() > MAXIMUM_NAME_VALUE_LENGTH) {
				errors.rejectValue("map[" + language + "]", "error.nameObject.tooLong", new Object[] {
						supportedLanguageService.findByCode(language), MAXIMUM_NAME_VALUE_LENGTH }, "Maximum length for text is "
						+ MAXIMUM_NAME_VALUE_LENGTH + "characters");
			}
		}
	}
}
