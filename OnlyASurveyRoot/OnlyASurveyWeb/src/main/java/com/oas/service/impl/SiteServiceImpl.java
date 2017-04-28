package com.oas.service.impl;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.command.model.ContactUsCommand;
import com.oas.model.AccountOwner;
import com.oas.model.ContactUsMessage;
import com.oas.service.SiteService;
import com.oas.util.Constants;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SiteServiceImpl extends AbstractServiceImpl implements SiteService {

	/** Mail sending service. */
	@Autowired
	private JavaMailSender mailSender;

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void addContactMessage(ContactUsCommand command) {

		// command has been previously validated, assert here
		Assert.notNull(command);
		Assert.hasText(command.getMessage());

		// domain object with basic properties
		ContactUsMessage subject = new ContactUsMessage();
		subject.setEmail(command.getEmail());
		subject.setMessage(command.getMessage());

		// if there is an authenticated user then attach their info to the
		// entity
		if (SecurityUtil.isSecureContext()) {
			subject.setAccountOwner((AccountOwner) SecurityUtil.getCurrentUser());
		}

		//
		// this try/catch/finally exists because a failure in either mail
		// sending or object persisting must not affect the outcome of the other
		//
		try {
			if (StringUtils.hasText(subject.getEmail())) {
				// send mail
				// SimpleMailMessage mail = new SimpleMailMessage();
				MimeMessage mail = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(mail);

				// TODO if a user is logged in, add their name to the From
				// helper.setFrom("SiteService@onlyasurvey.com");
				helper.setFrom(subject.getEmail());
				helper.setTo(Constants.CONTACT_US_INTERNAL_EMAIL_DESTINATION);

				mail.setSubject("[Contact Us] New message from " + subject.getEmail());
				mail.setText(subject.getMessage());

				//
				mailSender.send(mail);
				log.info("sent a copy of a ContactUs message to the internal address: "
						+ Constants.CONTACT_US_INTERNAL_EMAIL_DESTINATION);
			}

		} catch (RuntimeException e) {
			// do not fail the method call as persist() has yet to be called
			log.error("failed to send internal mail to " + Constants.CONTACT_US_INTERNAL_EMAIL_DESTINATION, e);
			// throw e;
		} catch (Exception e) {
			// do not fail the method call as persist() has yet to be called
			log.error("failed to send internal mail to " + Constants.CONTACT_US_INTERNAL_EMAIL_DESTINATION, e);
			// throw new RuntimeException(e);
		} finally {

			//
			persist(subject);
		}
	}
}
