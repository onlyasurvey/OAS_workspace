package com.oas.validator;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.service.SupportedLanguageService;

/**
 * Validator for ObjectTextCommand which confirms that each language in the map
 * (by the keySet) has a non-whitespace, non-empty value and that a valid key is
 * specified (ie., hasText(key)).
 * 
 * @author xhalliday
 * @since February 4, 2009
 */
@Component
public class ObjectTextCommandValidator implements Validator {

	/** Logger. */
	protected Logger log = Logger.getLogger(this.getClass());

	/** Maximum length of a text value, per language. */
	private static final int MAXIMUM_VALUE_LENGTH = 30000; // was: 32 * 1024;

	/** i18n. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return clazz != null && NameObjectCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		// must be correct type
		Assert.isAssignable(NameObjectCommand.class, target.getClass());
		ObjectTextCommand command = (ObjectTextCommand) target;

		// validate the key
		validateKey(errors);

		// validate the text that goes with this command
		validateObjectText(command, errors);
	}

	protected void validateKey(Errors errors) {
		// this should not be possible, therefore client is submitting an
		// invalid request
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "key", "illegalArgument");
	}

	protected void validateObjectText(NameObjectCommand command, Errors errors) {
		Set<String> keySet = command.getMap().keySet();
		if (keySet.isEmpty()) {
			// NEVER allows zero languages!
			errors.reject("illegalArgument");

			log.error("User attempted to set object text where command had zero languages!");

			// no further validation possible
			return;
		}

		for (String language : keySet) {
			// missing value
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "map[" + language + "]", "error.objectText.missingInLanguage");

			// length of value
			String value = command.getMap().get(language);
			if (value == null) {
				errors.reject("illegalArgument");
				// no further validation
				return;

			} else {
				if (value.length() > MAXIMUM_VALUE_LENGTH) {
					errors.rejectValue("map[" + language + "]", "error.objectText.tooLong", new Object[] {
							supportedLanguageService.findByCode(language), MAXIMUM_VALUE_LENGTH }, "Maximum length for text is "
							+ MAXIMUM_VALUE_LENGTH + "characters");
				}
			}
		}
	}
}
