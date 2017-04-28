package com.oas;

import java.util.Date;

import com.oas.command.model.SignupCommand;

abstract public class AbstractSignupRelatedTest extends AbstractOASBaseTest {

	protected SignupCommand newValidCommand() {
		SignupCommand command = new SignupCommand();
		command.setUsername("testUser.newValidCommand" + getMBUN() + (new Date().getTime()));
		command.setPassword(new String[] { "password", "password" });
		String email = "testEmail.newValidCommand" + getMBUN() + (new Date().getTime() + "@TCemail.com");
		command.setEmail(new String[] { email, email });
		command.setFirstname("firstName");
		command.setLastname("lastName");
		command.setLearnedAbout("some guy");
		command.setTelephone("613-555-1212");
		command.setOrganization("My Company, Inc");
		command.setGovernment(false);

		return command;
	}
}
