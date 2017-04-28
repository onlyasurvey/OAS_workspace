package com.oas.validator;

import static junit.framework.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractSignupRelatedTest;
import com.oas.command.model.SignupCommand;
import com.oas.model.AccountOwner;

public class SignupCommandValidatorTest extends AbstractSignupRelatedTest {

	@Autowired
	private SignupCommandValidator validator;

	// ======================================================================

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(validator.supports(SignupCommand.class));
	}

	// ======================================================================

	@Test
	public void testSuccess() {
		// returns a valid command
		SignupCommand command = newValidCommand();

		validateAndExpect(validator, command, 0);
	}

	// ======================================================================

	// @Test public void testLanguageInvalid_Null() {
	// SignupCommand command = newValidCommand();
	// command.setLanguageId(null);
	// validateAndExpect(validator, command, 1);
	// }

	// ======================================================================

	@Test
	public void testUsernameEmpty() {
		SignupCommand command = newValidCommand();
		command.setUsername("");
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testAnyPasswordEmpty() {
		SignupCommand command = newValidCommand();
		command.setPassword(new String[0]);
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testPasswordTooShort() {
		SignupCommand command = newValidCommand();
		command.setPassword(new String[] { "a", "a" });
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testPasswordsDiffer() {
		SignupCommand command = newValidCommand();
		command.setPassword(new String[] { "oneThing", "someOther" });
		validateAndExpect(validator, command, 1);
	}

	// ======================================================================

	@Test
	public void testContactInfo_Names() {
		SignupCommand command = newValidCommand();

		// should be valid to start
		validateAndExpect(validator, command, 0);

		// should fail
		command.setFirstname("");
		command.setLastname("has value");
		validateAndExpect(validator, command, 1);

		// should fail
		command.setFirstname("has value");
		command.setLastname("");
		validateAndExpect(validator, command, 1);

		// should fail
		command.setFirstname("");
		command.setLastname("");
		validateAndExpect(validator, command, 2);
	}

	/**
	 * For example, a DOS sending us huge data. This also avoids nasty errors to
	 * the user in the event that they simply pasted too much text, etc.
	 */
	@Test
	public void testLearnedAboutUs_Fail_HugeData() {

		SignupCommand command = newValidCommand();

		// should be valid to start
		validateAndExpect(validator, command, 0);

		// should fail: 5000 characters
		command.setLearnedAbout(String.format("%5000s", "foo"));
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testContactInfo_Organization() {
		SignupCommand command = newValidCommand();

		// should be valid to start
		validateAndExpect(validator, command, 0);

		// should fail
		command.setOrganization("");
		validateAndExpect(validator, command, 1);
	}

	public void validateEmailAndExpect(String email1, String email2, int expected) {
		validateEmailAndExpect(email1, email2, expected, newValidCommand());
	}

	public void validateEmailAndExpect(String email1, String email2, int expected, SignupCommand command) {

		// should be valid to start
		validateAndExpect(validator, command, 0);

		command.setEmail(new String[] { email1, email2 });

		validateAndExpect(validator, command, expected);
	}

	@Test
	public void testEmail_Success() {
		String email = "email" + new Date().getTime() + "@email" + getMBUN() + "foo.com";
		validateEmailAndExpect(email, email, 0);
	}

	@Test
	public void testEmail_Fail_InvalidEmailData() {
		SignupCommand command = newValidCommand();

		// should be valid to start
		validateAndExpect(validator, command, 0);

		// totally null data
		command.setEmail(null);
		validateAndExpect(validator, command, 1);

		// unexpected # elements
		command.setEmail(new String[] { "justvvvvvOne@address.ca" });
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testEmail_Fail_MissingFirst() {
		validateEmailAndExpect("", "emavsd4il@email.com", 1);
	}

	@Test
	public void testEmail_Fail_MissingSecond() {
		validateEmailAndExpect("eewemail222sw@email.com", "", 1);
	}

	@Test
	public void testEmail_Fail_Different() {
		validateEmailAndExpect("emacwqerl123123dd@email.com", "different@address.net", 1);
	}

	@Test
	public void testEmail_Fail_AlreadyTaken() {
		//
		AccountOwner otherUser = createTestUser();
		// same addresses to prevent errors around sameness, but the email is
		// already taken by a user, therefore a validation error is expected
		SignupCommand command = newValidCommand();
		command.setEmail(new String[] { otherUser.getEmail(), otherUser.getEmail() });
		validateAndExpect(validator, command, 1);
	}

	@Test
	public void testUsername_Fail_AlreadyTaken() {

		//
		AccountOwner otherUser = createTestUser();
		// same addresses to prevent errors around sameness, but the email is
		// already taken by a user, therefore a validation error is expected
		SignupCommand command = newValidCommand();
		command.setUsername(otherUser.getUsername());
		validateAndExpect(validator, command, 1);
	}

}