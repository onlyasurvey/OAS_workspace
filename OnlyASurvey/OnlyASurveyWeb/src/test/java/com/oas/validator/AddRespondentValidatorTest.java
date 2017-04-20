package com.oas.validator;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.AddRespondentCommand;

public class AddRespondentValidatorTest extends AbstractOASBaseTest {

	@Autowired
	private AddRespondentValidator validator;

	// ======================================================================

	private void validateAndExpect(AddRespondentCommand command, int expectedErrors) {
		validateAndExpect(validator, command, expectedErrors, null);
	}

	private void validateAndExpect(AddRespondentCommand command, int expectedErrors, String[] errorCodes) {
		validateAndExpect(validator, command, expectedErrors, errorCodes);
	}

	// ======================================================================

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(validator.supports(AddRespondentCommand.class));
	}

	@Test
	public void testUserEmailDataFails_Null() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData(null);

		validateAndExpect(command, 1, new String[] { "addRespondents.error.noDataAddRespondent" });
	}

	@Test
	public void testUserEmailDataFails_Empty() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData("");

		validateAndExpect(command, 1, new String[] { "addRespondents.error.noDataAddRespondent" });
	}

	@Test
	public void testUserEmailDataFails_NoTokens() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData("\t \n");

		validateAndExpect(command, 1, new String[] { "addRespondents.error.cantParseAnyTokens" });
	}

	@Test
	public void testUserEmailDataFails_NoValidTokens() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData("asdf\tss\n");

		validateAndExpect(command, 1, new String[] { "addRespondents.error.cantValidateAnyTokens" });
	}

	@Test
	public void testUserEmailDataPartialFail_InvalidEmails() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData("asdf\tss\nasdf@asdf.ca");

		validateAndExpect(command, 2, new String[] { "addRespondents.error.invalidEmail", "addRespondents.error.invalidEmail" });
	}

	@Test
	public void testUserEmailDataSuccess_OneAddress() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData("asdf@asdf.ca");

		validateAndExpect(command, 0);
	}

	@Test
	public void testUserEmailDataSuccess_ManyAddresses() {
		// valid data
		AddRespondentCommand command = new AddRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setUserEmailData("asdf1@asdf.ca\tasdf2@asdf.ca\n\nasdf3@asdf.ca");

		validateAndExpect(command, 0);
	}
}
