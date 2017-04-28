package com.oas.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.oas.command.model.SendToRespondentCommand;

/**
 * Validator used to send emails to respondents
 * 
 * @author jfchenier
 * @since March 17th, 2009
 */

@Component
public class SendToRespondentValidator implements Validator {

	public static int MAX_MESSAGE_SIZE = 4000;
	public static int MAX_SUBJECT_SIZE = 200;

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return SendToRespondentCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SendToRespondentCommand command = (SendToRespondentCommand) target;
		validateSubject(command, errors);
		validateMessage(command, errors);
	}

	protected void validateSubject(SendToRespondentCommand command, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subject", "sendToRespondent.errors.emptySubject");
		if (command.getSubject() != null && command.getSubject().length() > SendToRespondentValidator.MAX_SUBJECT_SIZE) {
			errors.rejectValue("message", "sendToRespondent.errors.subjectTooLarge");
		}

	}

	protected void validateMessage(SendToRespondentCommand command, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "message", "sendToRespondent.errors.emptyMessage");
		if (command.getMessage() != null && command.getMessage().length() > SendToRespondentValidator.MAX_MESSAGE_SIZE) {
			errors.rejectValue("message", "sendToRespondent.errors.messageTooLarge");
		}
		if (command.getMessage() != null && command.getMessage().trim().length() != 0) {
			if (command.getMessage().contains("{survey_link}") == false) {
				errors.rejectValue("message", "sendToRespondent.errors.missingSurveyLinkVar");
			}
		}
	}
}
