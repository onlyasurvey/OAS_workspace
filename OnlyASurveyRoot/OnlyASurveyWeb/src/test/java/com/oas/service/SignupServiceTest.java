package com.oas.service;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractSignupRelatedTest;
import com.oas.command.model.SignupCommand;

public class SignupServiceTest extends AbstractSignupRelatedTest {

	@Autowired
	private SignupService signupService;

	@Test
	public void testSuccess() {
		SignupCommand command = newValidCommand();
		command.setUsername("testUser" + getMBUN());

		UserAccount user = signupService.storeSignup(command, LOCALHOST_IP);
		assertNotNull("no user created", user);
		assertNotNull("no user created (no ID)", user.getId());
	}

	@Test
	public void testSuccess_UsernameToLower() {

		final String username = "TEST_UPPER_CASE";
		final String expected = "test_upper_case";

		SignupCommand command = newValidCommand();
		command.setUsername(username);

		UserAccount user = signupService.storeSignup(command, LOCALHOST_IP);
		assertNotNull("no user created", user);
		assertNotNull("no user created (no ID)", user.getId());
		assertEquals("expected lower-case username", expected, user.getUsername());
	}

}
