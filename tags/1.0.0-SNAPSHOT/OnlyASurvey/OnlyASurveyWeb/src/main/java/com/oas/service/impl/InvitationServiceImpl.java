package com.oas.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.MimeMessage;

import org.hibernate.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;
import com.oas.service.InvitationService;

/**
 * Implementation of InvitationService.
 * 
 * @author xhalliday
 * @author jchenier
 * @since March 9, 2009
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class InvitationServiceImpl extends AbstractServiceImpl implements InvitationService {

	/** Generic domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	/** Mail sending service. */
	@Autowired
	private JavaMailSender mailSender;

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Invitation createInvitation(Survey survey, String emailAddress) {
		//
		Assert.notNull(survey);
		Assert.hasText(emailAddress);

		//
		SecurityAssertions.assertOwnership(survey);

		Invitation retval = new Invitation(survey, emailAddress);
		retval.setStatus(InvitationStatusType.UNSENT);

		//
		persist(retval);

		Assert.notNull(retval.getId());

		return retval;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public List<Invitation> createInvitations(Survey survey, List<String> emailAddresses) {
		int dupeCount = 0;
		//
		Assert.notNull(survey);
		Assert.notNull(emailAddresses);
		Assert.notEmpty(emailAddresses);

		//
		SecurityAssertions.assertOwnership(survey);

		// do work

		List<Invitation> retval = new ArrayList<Invitation>();
		List<String> registeredEmails = this.getRespondentEmailList(survey);

		Invitation newInvitation = null;
		for (String email : emailAddresses) {
			// skip email addresses that are already registered
			if (registeredEmails.contains(email)) {
				dupeCount++;
				continue;
			}
			newInvitation = new Invitation(survey, email);
			newInvitation.setStatus(InvitationStatusType.UNSENT);
			persist(newInvitation);
			retval.add(newInvitation);
		}
		// making sure that all emails have been processed
		Assert.isTrue(retval.size() + dupeCount == emailAddresses.size());

		return retval;

	}

	@Override
	@ValidUser
	public List<Invitation> getPagedList(Survey survey, int page, int count) {
		//
		Assert.notNull(survey);

		//
		SecurityAssertions.assertOwnership(survey);

		//
		return domainModelService.getPagedList("from Invitation where survey = ? order by id", page, count,
				new Object[] { survey });
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void purge(Invitation invitation) {
		//
		Assert.notNull(invitation);

		//
		Survey survey = invitation.getSurvey();
		Assert.notNull(survey);

		//
		SecurityAssertions.assertOwnership(survey);

		delete(invitation);

	}

	@Override
	@Unsecured
	public String replaceSurveyVarWithLink(String data, Survey survey) {
		Assert.notNull(survey);
		return data.replaceAll("\\{survey_link\\}", "http://www.onlyasurvey.com/oas/html/srvy/resp/" + survey.getId() + ".html");

	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public boolean sendAllInvitations(Survey survey, String subject, String message) {
		boolean noErrors = true;
		//
		Assert.notNull(survey);
		Assert.notNull(subject);
		Assert.notNull(message);

		//
		SecurityAssertions.assertOwnership(survey);

		//
		message = replaceSurveyVarWithLink(message, survey);

		//
		List<Invitation> data = find("from Invitation where survey = ? AND status != ?", new Object[] { survey,
				InvitationStatusType.RESPONDED });

		for (Invitation targetInv : data) {
			// if target is UNSENT, update it to SENT
			if (targetInv.getStatus().compareTo(InvitationStatusType.UNSENT) == 0) {
				targetInv.setStatus(InvitationStatusType.SENT);
				persist(targetInv);
			}

			// monitor sendEmail success
			if (false == sendEmail(targetInv, subject, message)) {
				noErrors = false;
			}
		}

		return true == noErrors;

	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public boolean sendNewInvitations(Survey survey, String subject, String message) {
		boolean noErrors = true;
		//
		Assert.notNull(survey);
		Assert.notNull(subject);
		Assert.notNull(message);

		//
		SecurityAssertions.assertOwnership(survey);

		//
		message = replaceSurveyVarWithLink(message, survey);

		//
		List<Invitation> data = find("from Invitation where survey = ? AND status = ?", new Object[] { survey,
				InvitationStatusType.UNSENT });

		for (Invitation targetInv : data) {
			// update status
			targetInv.setStatus(InvitationStatusType.SENT);
			persist(targetInv);

			// monitor sendEmail success
			if (false == sendEmail(targetInv, subject, message)) {
				noErrors = false;
			}
		}
		return true == noErrors;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public boolean sendReminders(Survey survey, String subject, String message) {
		boolean noErrors = true;
		//
		Assert.notNull(survey);
		Assert.notNull(subject);
		Assert.notNull(message);

		//
		SecurityAssertions.assertOwnership(survey);

		//
		message = replaceSurveyVarWithLink(message, survey);

		//
		List<Invitation> data = find("from Invitation where survey = ? AND (status = ? OR status = ?)", new Object[] { survey,
				InvitationStatusType.SENT, InvitationStatusType.REMINDED });

		//
		for (Invitation targetInv : data) {
			// increment reminder count & update status
			targetInv.setReminderCount(targetInv.getReminderCount() + 1);
			targetInv.setStatus(InvitationStatusType.REMINDED);
			persist(targetInv);

			// monitor sendEmail success
			if (false == sendEmail(targetInv, subject, message)) {
				noErrors = false;
			}
		}

		return true == noErrors;
	}

	@Override
	@Unsecured
	public List<String> tokenizeUserEmailData(String userData) {
		//
		List<String> retval = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(userData, " ,\n");
		while (st.hasMoreTokens()) {
			retval.add(st.nextToken().trim());

		}
		return retval;
	}

	@Override
	@ValidUser
	public boolean sendEmail(Invitation invitation, String subject, String message) {
		boolean retval = false;
		String email = invitation.getEmailAddress();
		try {
			MimeMessage mail = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail);
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(message);

			mailSender.send(mail);
			log.info("sent an email to a respondant with email: " + email);

			retval = true;

		} catch (Exception e) {
			log.error("failed to send internal mail to " + email);
		}
		return retval;
	}

	@Override
	@Unsecured
	public List<String> validateUserEmailList(List<String> userList) {
		List<String> retval = new ArrayList<String>();
		EmailValidator validator = new EmailValidator();
		validator.initialize(null);

		for (String targetString : userList) {
			if (validator.isValid(targetString)) {
				retval.add(targetString);
			}
		}

		return retval;
	}

	@Override
	@ValidUser
	public int countInvitations(Survey survey) {
		return ((Long) unique(find("select count(*) from Invitation where survey = ?", survey))).intValue();
	}

	@Override
	@ValidUser
	public int countInvitationResponses(Survey survey) {
		return ((Long) unique(find("select count(*) from Invitation where survey = ? and status = ?", new Object[] { survey,
				InvitationStatusType.RESPONDED }))).intValue();
	}

	@Override
	@ValidUser
	public int countSentInvitations(Survey survey) {
		return ((Long) unique(find("select count(*) from Invitation where survey = ? and status != ?", new Object[] { survey,
				InvitationStatusType.UNSENT }))).intValue();
	}

	@Override
	@Unsecured
	public Invitation getInvitation(long id) {
		return get(Invitation.class, id);
	}

	@Override
	@Unsecured
	public List<String> getRespondentEmailList(Survey survey) {
		List<String> retval = new ArrayList<String>();
		List<Invitation> data = find("from Invitation where survey = ?", survey);
		for (Invitation inv : data) {
			retval.add(inv.getEmailAddress());
		}
		return retval;
	}

}
