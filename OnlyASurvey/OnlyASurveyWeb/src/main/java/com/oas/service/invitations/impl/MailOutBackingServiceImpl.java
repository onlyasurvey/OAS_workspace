package com.oas.service.invitations.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.invitations.InvitationMailQueue;
import com.oas.model.invitations.MailOutStatus;
import com.oas.model.invitations.MailOutType;
import com.oas.service.DomainModelService;
import com.oas.service.invitations.MailOutBackingService;

/**
 * Implementation of {@link MailOutBackingService}. Note that many methods
 * flush() because errors here are considered business critical.
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
// @Service
// @Transactional
@Transactional
public class MailOutBackingServiceImpl extends AbstractServiceImpl implements MailOutBackingService {

	@Autowired
	private DomainModelService domainModelService;

	/** {@inheritDoc} */
	@Override
	@Unsecured
	public InvitationMailQueue findUnsentMailQueueAndMarkAsSending() {

		// TODO change Unsecured to Secured and use a system user

		//
		Criteria crit = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(InvitationMailQueue.class);
		crit.add(Restrictions.eq("status", MailOutStatus.UNSENT));
		crit.addOrder(Order.asc("dateCreated"));

		// customize
		crit.setLockMode(LockMode.UPGRADE);
		crit.setMaxResults(1);

		@SuppressWarnings("unchecked")
		List<InvitationMailQueue> list = crit.list();

		// log.info("found " + list.size() + " items");

		switch (list.size()) {
		case 0:
			// nothing to do;
			return null;
		case 1:
			// update status
			InvitationMailQueue item = list.get(0);
			item.setStatus(MailOutStatus.SENDING);
			persist(item);

			// ensure changes are reflected immediately
			getHibernateTemplate().flush();

			//
			return item;
		default:
			throw new IllegalStateException("expected maximum of 1 result for unsent mail queue item query");
		}
	}

	@Override
	@Unsecured
	public void markError(InvitationMailQueue item, String errorString) {
		// TODO change Unsecured to Secured and use a system user

		log.info("Marking #" + item.getId() + " with error");

		// object will be detached - need to reattach it to the session
		getHibernateTemplate().refresh(item);

		//
		item.setStatus(MailOutStatus.ERROR);
		item.setErrorString(errorString);

		//
		persistAndFlush(item);

		// update status of (user-facing) invitation
		Invitation invitation = item.getInvitation();
		invitation.setStatus(InvitationStatusType.ERROR);
		persist(invitation);
	}

	@Override
	@Unsecured
	public void markSent(InvitationMailQueue item) {
		// TODO change Unsecured to Secured and use a system user

		log.info("Marking #" + item.getId() + " as sent");

		// object will be detached - need to reattach it to the session
		getHibernateTemplate().refresh(item);

		//
		item.setStatus(MailOutStatus.SENT);

		//
		persistAndFlush(item);

		// update status of (user-facing) invitation
		InvitationStatusType newStatus = null;
		MailOutType type = item.getMailOut().getType();
		Invitation invitation = item.getInvitation();

		if (MailOutType.INVITATION.equals(type)) {

			//
			newStatus = InvitationStatusType.SENT;

		} else if (MailOutType.REMINDER.equals(type)) {

			//
			newStatus = InvitationStatusType.REMINDED;

		} else {
			// this is unexpected behavior but not sufficient to touch the user
			// experience with an error
			log.warn("Unknown mail out type: " + type);
			newStatus = InvitationStatusType.SENT;
		}

		//
		invitation.setStatus(newStatus);
		persist(invitation);
	}

	private void persistAndFlush(InvitationMailQueue item) {

		// persist
		persist(item);

		// paranoia: this is business critical so we flush again, so any
		// failures will be thrown from this line rather than at some
		// unexpected point later
		getHibernateTemplate().flush();
	}

	// ======================================================================

	/**
	 * Replace placeholders with real links.
	 * 
	 * @param input
	 *            Source message
	 * @param survey
	 *            Target survey
	 * @return Replaced string
	 */
	@Override
	@Unsecured
	public String replaceSurveyVarWithLink(String input, Invitation invitation) {

		Assert.notNull(invitation);
		Assert.notNull(domainModelService);

		// Invitation may be disconnected from the session due to being run from
		// a non-transactional state: refresh it
		getHibernateTemplate().refresh(invitation);

		// public hostname for sending mail
		String hostname = domainModelService.getPublicHostname();

		Long surveyId = invitation.getSurvey().getId();

		String invitationString = "";
		if (invitation != null) {
			invitationString = "?ic=" + generateInvitationCode(invitation);
		}

		return input.replaceAll("\\{survey_link\\}", "http://" + hostname + "/oas/html/srvy/resp/" + surveyId + ".html"
				+ invitationString);
	}

	/**
	 * Given the invitation generate a string to send with emails.
	 * 
	 * @param invitation
	 *            {@link Invitation}
	 * @return value part of a query parameter
	 */
	private String generateInvitationCode(Invitation invitation) {

		Assert.notNull(invitation);
		Assert.notNull(invitation.getId());

		return invitation.getId().toString() + "." + invitation.getInvitationCode();
	}
}
