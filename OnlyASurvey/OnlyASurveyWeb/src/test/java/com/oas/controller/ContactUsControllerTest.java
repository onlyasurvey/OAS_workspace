package com.oas.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import javax.mail.Message;
import javax.mail.internet.AddressException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractSiteServiceRelatedTest;
import com.oas.command.model.ContactUsCommand;
import com.oas.model.ContactUsMessage;
import com.oas.util.Constants;

public class ContactUsControllerTest extends AbstractSiteServiceRelatedTest {

	@Autowired
	private ContactUsController controller;

	@Test
	public void testDoForm() {
		ModelAndView mav = controller.doForm(new MockHttpServletRequest());

		assertNotNull("no mav", mav);
		assertEquals("wrong view name", "/contactUs/contactUsForm", mav.getViewName());
	}

	@Test
	public void testDoThanks() {
		ModelAndView mav = controller.doThanks(new MockHttpServletRequest());

		assertNotNull("no mav", mav);
		assertEquals("wrong view name", "/contactUs/thanks", mav.getViewName());
	}

	@Test
	public void testSubmit_Success_Anonymous() {

		// no authentication

		// execute success-condition test to validate that it works
		// unauthenticated
		doSubmitSuccess();
	}

	@Test
	public void testSubmit_Success_Authenticated() {

		// authenticate a user
		createAndSetSecureUserWithRoleUser();

		// execute success-condition test to validate that it works for
		// authenticated users
		doSubmitSuccess();
	}

	/**
	 * Used by Submit_Success tests to execute a command that should be
	 * successful, to test external conditions.
	 * 
	 * @param command
	 */
	private void doSubmitSuccess() {
		long initialCount = countContactMessages();

		ContactUsCommand command = new ContactUsCommand();
		command.setEmail(TEST_EMAIL);
		command.setMessage("I love your software!");

		ModelAndView mav = controller.doSubmit(new MockHttpServletRequest(), command);
		assertNotNull("no mav", mav);
		assertIsRedirect("wrong view name", mav, "/html/con/tx.html");

		assertEquals("expected one new message", initialCount + 1, countContactMessages());

		flushAndClear();

		ContactUsMessage message = (ContactUsMessage) execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return unique(session.createQuery("from ContactUsMessage order by id desc").setMaxResults(1).list());
			}
		});

		assertNotNull("unable to load most recent message", message);

		assertEquals(command.getEmail(), message.getEmail());
		assertEquals(command.getMessage(), message.getMessage());

	}

	@Test
	public void testSubmit_Fail_NoMessage() {
		ContactUsCommand command = new ContactUsCommand();
		command.setEmail(TEST_EMAIL);
		// invalid
		command.setMessage("");

		ModelAndView mav = controller.doSubmit(new MockHttpServletRequest(), command);
		assertNotNull("no mav", mav);
		assertEquals("wrong view name", "/contactUs/contactUsForm", mav.getViewName());
	}

	// ======================================================================

	@Test
	public void testSubmit_SendsMail() throws AddressException {

		// start fresh
		Mailbox.clearAll();

		final String testMessage = "this is my test";

		ContactUsCommand command = new ContactUsCommand();
		command.setEmail(TEST_EMAIL);
		command.setMessage(testMessage);

		//
		controller.doSubmit(new MockHttpServletRequest(), command);

		//
		List<Message> list = Mailbox.get(Constants.CONTACT_US_INTERNAL_EMAIL_DESTINATION);
		assertEquals("no mail sent", 1, list.size());
	}

	// ======================================================================

}
