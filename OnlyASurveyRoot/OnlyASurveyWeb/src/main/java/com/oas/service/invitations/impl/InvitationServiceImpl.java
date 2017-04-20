package com.oas.service.invitations.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Survey;
import com.oas.model.invitations.InvitationMailQueue;
import com.oas.model.invitations.MailOut;
import com.oas.model.invitations.MailOutType;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;
import com.oas.service.invitations.InvitationService;
import com.oas.util.Constants;

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

	// ======================================================================

	/** {@inheritDoc} */
	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public Invitation createInvitation(Survey survey, String emailAddress) {
		//
		Assert.notNull(survey);
		Assert.hasText(emailAddress);

		//
		SecurityAssertions.assertOwnership(survey);

		Invitation retval = createInvitationDomainObject(survey, emailAddress);

		//
		persist(retval);

		Assert.notNull(retval.getId());

		return retval;
	}

	/** {@inheritDoc} */
	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public List<Invitation> createInvitations(Survey survey, List<String> emailAddresses) {
		//
		Assert.notNull(survey);
		Assert.notNull(emailAddresses);
		Assert.notEmpty(emailAddresses);

		//
		SecurityAssertions.assertOwnership(survey);

		// do work

		List<Invitation> retval = new ArrayList<Invitation>();
		List<String> registeredEmails = this.getRespondentEmailList(survey);

		for (String email : emailAddresses) {
			// skip email addresses that are already registered
			if (registeredEmails.contains(email)) {
				continue;
			}
			Invitation newInvitation = createInvitationDomainObject(survey, email);
			persist(newInvitation);

			// add new item to lists
			retval.add(newInvitation); // add new item
			registeredEmails.add(email); // guard against duplicate in list
		}

		return retval;

	}

	/**
	 * Create the Invitation domain object from the given arguments.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @param emailAddress
	 *            Email address
	 * @return {@link Invitation}, not persisted
	 */
	private Invitation createInvitationDomainObject(Survey survey, String emailAddress) {
		Invitation newInvitation = new Invitation(survey, emailAddress);
		newInvitation.setStatus(InvitationStatusType.UNSENT);
		newInvitation.setInvitationCode(generateInvitationCode(survey, emailAddress));
		return newInvitation;
	}

	/**
	 * Generate an invitation code.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @param emailAddress
	 *            Email address
	 * @return String
	 */
	protected String generateInvitationCode(Survey survey, String emailAddress) {
		String source = new Date().toString() + survey.getId() + emailAddress + "42";
		Md5PasswordEncoder encoder = new Md5PasswordEncoder();
		String encodePassword = encoder.encodePassword(source, "I'm a happy penguin");
		return encodePassword.substring(0, Constants.INVITATION_CODE_LENGTH);
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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
		String fromAddress = SecurityUtil.getCurrentUser().getEmail();

		//
		List<Invitation> data = find("from Invitation where survey = ?", survey);

		if (data.isEmpty()) {
			return true;
		}

		//
		MailOut mailOut = new MailOut(survey, MailOutType.INVITATION, subject, message);
		mailOut.setFromAddress(fromAddress);
		persist(mailOut);

		//
		int count = 0;
		for (Invitation invitation : data) {
			invitation.setStatus(InvitationStatusType.QUEUED);
			persist(invitation);

			InvitationMailQueue mail = new InvitationMailQueue(mailOut, invitation);
			persist(mail);

			count++;
		}

		//
		log.info("Queued " + count + " invitation messages (all invites)");

		return true == noErrors;

	}

	/** {@inheritDoc} */
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
		String fromAddress = SecurityUtil.getCurrentUser().getEmail();

		//
		List<Invitation> data = find("from Invitation where survey = ? AND status = ?", new Object[] { survey,
				InvitationStatusType.UNSENT });

		if (data.isEmpty()) {
			return true;
		}

		//
		MailOut mailOut = new MailOut(survey, MailOutType.INVITATION, subject, message);
		mailOut.setFromAddress(fromAddress);
		persist(mailOut);

		//
		int count = 0;
		for (Invitation invitation : data) {
			invitation.setStatus(InvitationStatusType.QUEUED);
			persist(invitation);

			InvitationMailQueue mail = new InvitationMailQueue(mailOut, invitation);
			persist(mail);

			count++;
		}

		//
		log.info("Queued " + count + " new invitation messages");

		return true == noErrors;
	}

	/** {@inheritDoc} */
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
		String fromAddress = SecurityUtil.getCurrentUser().getEmail();

		//
		List<Invitation> data = find("from Invitation where survey = ? AND (status = ? OR status = ? OR status = ?)",
				new Object[] { survey, InvitationStatusType.SENT, InvitationStatusType.REMINDED, InvitationStatusType.STARTED });

		if (data.isEmpty()) {
			return true;
		}

		//
		MailOut mailOut = new MailOut(survey, MailOutType.REMINDER, subject, message);
		mailOut.setFromAddress(fromAddress);
		persist(mailOut);

		//
		int count = 0;
		for (Invitation invitation : data) {
			// increment reminder count & update status
			invitation.setReminderCount(invitation.getReminderCount() + 1);
			invitation.setStatus(InvitationStatusType.QUEUED);
			persist(invitation);

			InvitationMailQueue mail = new InvitationMailQueue(mailOut, invitation);
			persist(mail);

			count++;
		}

		//
		log.info("Queued " + count + " reminder messages");

		return true == noErrors;
	}

	/** {@inheritDoc} */
	@Override
	@Unsecured
	public List<String> tokenizeUserEmailData(String userData) {
		//
		List<String> retval = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(userData, " ,\n\r\t");
		while (st.hasMoreTokens()) {
			retval.add(st.nextToken().trim());

		}
		return retval;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	@ValidUser
	public int countInvitations(Survey survey) {
		return ((Long) unique(find("select count(*) from Invitation where survey = ?", survey))).intValue();
	}

	/** {@inheritDoc} */
	@Override
	@ValidUser
	public int countInvitationResponses(Survey survey) {
		return ((Long) unique(find("select count(*) from Invitation where survey = ? and status = ?", new Object[] { survey,
				InvitationStatusType.RESPONDED }))).intValue();
	}

	/** {@inheritDoc} */
	@Override
	@ValidUser
	public int countSentInvitations(Survey survey) {
		return ((Long) unique(find("select count(*) from Invitation where survey = ? and status != ?", new Object[] { survey,
				InvitationStatusType.UNSENT }))).intValue();
	}

	/** {@inheritDoc} */
	@Override
	@Unsecured
	public Invitation getInvitation(long id) {
		return get(Invitation.class, id);
	}

	/** {@inheritDoc} */
	@Override
	@Unsecured
	public List<String> getRespondentEmailList(Survey survey) {
		return find("select emailAddress from Invitation where survey = ?", survey);
	}

}
