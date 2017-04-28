package com.oas.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.oas.command.model.AddRespondentCommand;
import com.oas.service.invitations.InvitationService;

/**
 * Validator used to add respondents
 * 
 * @author jfchenier
 * @since March 17th, 2009
 */

@Component
public class AddRespondentValidator implements Validator {

	/**
	 * String tokens and validation services
	 */
	@Autowired
	private InvitationService invitationService;

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return AddRespondentCommand.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		AddRespondentCommand command = (AddRespondentCommand) target;
		validateUserEmailList(command, errors);
	}

	protected void validateUserEmailList(AddRespondentCommand command, Errors errors) {

		// GENEREATE ERROR AND EXIT IF NULL OR EMPTY
		if (command.getUserEmailData() == null || command.getUserEmailData().length() == 0) {
			errors.rejectValue("userEmailData", "addRespondents.error.noDataAddRespondent");
			return;
		}

		// GENEREATE ERROR AND EXIT IF NO TOKENS
		List<String> tokens = invitationService.tokenizeUserEmailData(command.getUserEmailData());
		int tokenListSize = tokens.size();
		if (tokenListSize == 0) {
			errors.rejectValue("userEmailData", "addRespondents.error.cantParseAnyTokens");
			return;
		}

		// GENEREATE ERROR AND EXIT IF HAS NO VALID ADDRESS
		List<String> validEmails = invitationService.validateUserEmailList(tokens);
		int validListSize = validEmails.size();
		if (validListSize == 0) {
			errors.rejectValue("userEmailData", "addRespondents.error.cantValidateAnyTokens");
			return;
		}

		// GENERATE GLOBAL ERRORS FOR INVALID EMAILS
		if (tokenListSize != validListSize) {
			for (String email : tokens) {
				if (!validEmails.contains(email)) {
					errors.reject("addRespondents.error.invalidEmail", new Object[] { email }, "INVALID EMAIL");
				}
			}
		}
	}
}
