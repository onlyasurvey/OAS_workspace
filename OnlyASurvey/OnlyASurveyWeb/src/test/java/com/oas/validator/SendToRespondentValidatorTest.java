package com.oas.validator;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SendToRespondentCommand;

public class SendToRespondentValidatorTest extends AbstractOASBaseTest {

	@Autowired
	private SendToRespondentValidator validator;

	// ======================================================================

	private void validateAndExpect(SendToRespondentCommand command, int expectedErrors, String[] errorCodes) {
		validateAndExpect(validator, command, expectedErrors, errorCodes);
	}

	private void validateAndExpect(SendToRespondentCommand command, int expectedErrors) {
		validateAndExpect(validator, command, expectedErrors, null);
	}

	// ======================================================================

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(validator.supports(SendToRespondentCommand.class));
	}

	@Test
	public void testSubjectFails_Null() {
		SendToRespondentCommand command = new SendToRespondentCommand();

		command.setSubject(null);
		command.setMessage("{survey_link} asdf");

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.emptySubject" });
	}

	@Test
	public void testSubjectFails_Empty() {
		SendToRespondentCommand command = new SendToRespondentCommand();

		command.setSubject("");
		command.setMessage("{survey_link} asdf");

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.emptySubject" });
	}

	@Test
	public void testMessageFails_Null() {
		// valid data
		SendToRespondentCommand command = new SendToRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setSubject("test");
		command.setMessage(null);

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.emptyMessage" });
	}

	@Test
	public void testMessageFails_Empty() {
		// valid data
		SendToRespondentCommand command = new SendToRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setSubject("test");
		command.setMessage("");

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.emptyMessage" });
	}

	@Test
	public void testMessageFails_NoSurveyLinkVar() {
		// valid data
		SendToRespondentCommand command = new SendToRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setSubject("test");
		command.setMessage("test");

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.missingSurveyLinkVar" });
	}

	@Test
	public void testMessageFails_messageTooLarge() {

		SendToRespondentCommand command = new SendToRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setSubject("test");
		command.setMessage(String.format("%-" + SendToRespondentValidator.MAX_MESSAGE_SIZE + "s x", "{survey_link}"));

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.messageTooLarge" });

	}

	@Test
	public void testSubjectFails_subjectTooLarge() {

		SendToRespondentCommand command = new SendToRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setSubject(String.format("%-" + (SendToRespondentValidator.MAX_SUBJECT_SIZE) + "s x", "yoyo"));
		command.setMessage("{survey_link}");

		validateAndExpect(command, 1, new String[] { "sendToRespondent.errors.subjectTooLarge" });

	}

	@Test
	public void test_Success() {
		// valid data
		SendToRespondentCommand command = new SendToRespondentCommand();
		// explicitly set to prevent command regressions from affecting test
		command.setSubject("test");
		command.setMessage("test {survey_link}");

		validateAndExpect(command, 0);
	}

}
