package com.oas.service.invitations.impl;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Invitation;
import com.oas.model.invitations.InvitationMailQueue;
import com.oas.model.invitations.MailOut;
import com.oas.service.invitations.MailOutBackingService;
import com.oas.service.invitations.MailOutService;

/**
 * Implementation of {@link MailOutBackingService}.
 * 
 * TODO make backing service a Web service
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
// @Service
public class MailOutServiceImpl extends AbstractServiceImpl implements MailOutService {

	/** Persistent service that backs this one. */
	@Autowired
	private MailOutBackingService backingService;

	/** Mail sending service. */
	@Autowired
	private JavaMailSender mailSender;

	/** Configured email address for invitation email From line. */
	@Autowired
	@Qualifier("invitationFromAddress")
	private String invitationFromAddress;

	// ======================================================================

	/**
	 * Default constructor.
	 */
	public MailOutServiceImpl() {
	}

	// ======================================================================

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Unsecured
	public void processOneItem() {

		// TODO change Unsecured to Secured and use a system user

		boolean isError = false;
		String errorMessage = null;
		Exception exception = null;

		// load one item
		InvitationMailQueue item = backingService.findUnsentMailQueueAndMarkAsSending();

		if (item == null) {
			// TODO change this to debug after the feature has soaked for awhile
			// log.info("no Invitation Mail Queue items to process");
			return;
		}

		Invitation invitation = item.getInvitation();
		MailOut mailOut = item.getMailOut();

		try {

			// process the mail body from the mail out, replacing link
			// placeholders and such
			String messageText = backingService.replaceSurveyVarWithLink(mailOut.getBody(), invitation);

			//
			String fromAddress = mailOut.getFromAddress();
			Assert.hasText("No From address for mail out");

			//
			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(fromAddress));
			message.addRecipients(RecipientType.TO, invitation.getEmailAddress());
			message.setSubject(mailOut.getSubject());
			message.setText(messageText);

			//
			mailSender.send(message);

			// all good
			backingService.markSent(item);

			log.info("Sent email for item#" + item.getId() + "/invitation#" + invitation.getId() + " successfully");

		} catch (Exception e) {

			// throwing Exception is nasty, but we need to catch any runtime
			// data access errors to ensure they're logged properly and to
			// ensure that the items are marked as "ERROR"

			isError = true;
			errorMessage = e.getMessage();
			exception = e;
		}

		// check for errors
		if (isError) {

			// log it
			log.error("Mail exception processing item #" + item.getId(), exception);

			// mark it
			backingService.markError(item, errorMessage);
		}

		// paranoia: this is business critical so we flush again, so any
		// failures will be thrown from this line rather than at some
		// unexpected point later
		// TODO this will fail in a web service backed component
		getHibernateTemplate().flush();
	}

}
