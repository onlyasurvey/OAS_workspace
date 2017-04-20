package com.oas.service;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractOASBaseTest;

public class AccountServiceTest extends AbstractOASBaseTest {

	@Autowired
	private AccountService accountService;

	@Test
	public void testEmailAlreadyExists_True() {
		UserAccount user = createTestUser();

		assertTrue("expected true", accountService.emailAlreadyExists(user.getEmail()));
	}

	@Test
	public void testEmailAlreadyExists_False() {
		assertFalse("expected true", accountService.emailAlreadyExists("someRandom" + getMBUN() + "@fo.bar"));
	}
}
