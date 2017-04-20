package com.oas.service;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import javax.mail.Message;
import javax.mail.internet.AddressException;

import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractSiteServiceRelatedTest;
import com.oas.command.model.ContactUsCommand;
import com.oas.util.Constants;

/**
 * Test the "Site Service" - Contact Us, etc.
 * 
 * @author xhalliday
 * @since November 15, 2008
 */
public class SiteServiceTest extends AbstractSiteServiceRelatedTest {

	@Autowired
	private SiteService siteService;

	/**
	 * Test the case where no personally identifiable information was provided.
	 * 
	 * TODO validate stored message has owner_id (only count += 1 is tested
	 * here)
	 * 
	 * @throws AddressException
	 */
	@Test
	public void testAddContactMessage_Success_Anonymous() throws AddressException {

		// no user is created, thus not attached to the message

		long initialCount = countContactMessages();

		ContactUsCommand command = new ContactUsCommand();
		// 
		command.setEmail(null);
		command.setMessage("I really like your software!");

		// invoke
		siteService.addContactMessage(command);

		long newCount = countContactMessages();
		assertEquals("expected one new message in the database", initialCount + 1, newCount);

	}

	/**
	 * Test the case where a user is logged in and submits feedback.
	 * 
	 * TODO validate stored message has owner_id (only count += 1 is tested
	 * here)
	 * 
	 * @throws AddressException
	 */
	@Test
	public void testAddContactMessage_Success_AuthenticatedUser() throws AddressException {

		// start fresh
		Mailbox.clearAll();

		//
		long initialCount = countContactMessages();

		ContactUsCommand command = new ContactUsCommand();
		command.setEmail(TEST_EMAIL);
		command.setMessage("I really like your software!");

		// create a user and set them as the current principal; this exercises
		// the part of the service call that adds that information to entity
		createAndSetSecureUserWithRoleUser();

		// invoke
		siteService.addContactMessage(command);

		long newCount = countContactMessages();

		assertEquals("expected one new message", initialCount + 1, newCount);

		// validate that the internal notice email got sent
		List<Message> list = Mailbox.get(Constants.CONTACT_US_INTERNAL_EMAIL_DESTINATION);
		assertEquals("no mail sent", 1, list.size());
	}

}
