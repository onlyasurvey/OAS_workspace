package com.oas.validator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.oas.command.model.IdListCommand;
import com.oas.service.SupportedLanguageService;

/**
 * Validate a command into the CreateSurvey controller.
 * 
 * @author xhalliday
 * @since September 10, 2008
 */
@Component("createSurveyValidator")
public class CreateSurveyValidator implements Validator {

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		Assert.notNull(supportedLanguageService, "supportedLanguageService required");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Class clazz) {
		// the CreateSurvey controller only accepts a list of IDs as input
		return clazz.isAssignableFrom(IdListCommand.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Assert.isAssignable(IdListCommand.class, target.getClass());
		IdListCommand command = (IdListCommand) target;

		// the controller does not accept zero selections
		if (command == null || command.getIds() == null || command.getIds().size() == 0) {
			errors.reject("createSurvey.error.selectLanguageRequired");
			return;
		}

		// validate data
		if (!supportedLanguageService.isValidIdList(command)) {
			// invalid data from the client
			errors.reject("illegalArgument");
		}

	}

}
