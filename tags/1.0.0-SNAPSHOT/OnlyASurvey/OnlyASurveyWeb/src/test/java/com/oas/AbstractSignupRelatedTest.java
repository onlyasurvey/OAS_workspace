package com.oas;

import com.oas.command.model.SignupCommand;

abstract public class AbstractSignupRelatedTest extends AbstractOASBaseTest {

	protected SignupCommand newValidCommand() {
		SignupCommand command = new SignupCommand();
		command.setUsername("testUser");
		command.setPassword(new String[] { "password", "password" });
		command.setEmail(new String[] { "email@email.com", "email@email.com" });
		command.setFirstname("firstName");
		command.setLastname("lastName");
		command.setLearnedAbout("some guy");
		command.setTelephone("613-555-1212");
		command.setOrganization("My Company, Inc");
		command.setGovernment(false);

		return command;
	}
}
