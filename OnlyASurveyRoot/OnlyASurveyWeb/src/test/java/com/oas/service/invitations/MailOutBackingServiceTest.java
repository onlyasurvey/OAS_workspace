package com.oas.service.invitations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Survey;
import com.oas.model.invitations.InvitationMailQueue;
import com.oas.model.invitations.MailOut;
import com.oas.model.invitations.MailOutStatus;
import com.oas.model.invitations.MailOutType;

public class MailOutBackingServiceTest extends AbstractOASBaseTest {

	/** Service under test. */
	@Autowired
	private MailOutBackingService service;

	private static final String ERROR_STRING = "someUniqueError";

	// ======================================================================

	protected Survey createOneMailOutItemScenario() {
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		Invitation invitation = new Invitation(survey, TEST_EMAIL);
		invitation.setStatus(InvitationStatusType.UNSENT);
		persist(invitation);

		MailOut mailOut = new MailOut(survey, MailOutType.INVITATION, "subject", "body of email");
		mailOut.setFromAddress("me@home.tld");
		persist(mailOut);

		InvitationMailQueue newQueueItem = new InvitationMailQueue(mailOut, invitation);
		persist(newQueueItem);
		getHibernateTemplate().flush();

		return survey;
	}

	// ======================================================================

	/**
	 * TODO this method will fail if there is any existing data
	 */
	@Test
	public void findUnsentMailQueueAndMarkAsSending_NoRecords() {
		InvitationMailQueue item = service.findUnsentMailQueueAndMarkAsSending();
		assertIsNull(item);
	}

	/**
	 * TODO this method will fail if there is any existing data
	 */
	@Test
	public void findUnsentMailQueueAndMarkAsSending_OneRecord() {

		// sanity
		assertIsNull("test must be run with empty Mail Out queue", service.findUnsentMailQueueAndMarkAsSending());

		Survey survey = createOneMailOutItemScenario();
		assertNotNull("test data defect", survey);

		// the test
		InvitationMailQueue item = service.findUnsentMailQueueAndMarkAsSending();
		assertNotNull("expected an item", item);

		// the test
		InvitationMailQueue item2 = service.findUnsentMailQueueAndMarkAsSending();
		assertIsNull("got unexpected second item", item2);
	}

	// ======================================================================

	/**
	 * TODO this method will fail if there is any existing data
	 */
	@Test
	public void markSent_Invitation() {

		// sanity
		assertIsNull("test must be run with empty Mail Out queue", service.findUnsentMailQueueAndMarkAsSending());

		Survey survey = createOneMailOutItemScenario();
		assertNotNull("test data defect", survey);

		// the test
		InvitationMailQueue item = service.findUnsentMailQueueAndMarkAsSending();
		assertNotNull("expected an item", item);

		// change and persist
		item.getMailOut().setType(MailOutType.INVITATION);
		service.markSent(item);
		flushAndClear();

		InvitationMailQueue loaded = load(InvitationMailQueue.class, item.getId());
		assertEquals("expected error status", MailOutStatus.SENT, loaded.getStatus());
		assertEquals("expected associated invitation's status to update", InvitationStatusType.SENT, loaded.getInvitation()
				.getStatus());
	}

	/**
	 * TODO this method will fail if there is any existing data
	 */
	@Test
	public void markSent_Reminder() {

		// sanity
		assertIsNull("test must be run with empty Mail Out queue", service.findUnsentMailQueueAndMarkAsSending());

		Survey survey = createOneMailOutItemScenario();
		assertNotNull("test data defect", survey);

		// the test
		InvitationMailQueue item = service.findUnsentMailQueueAndMarkAsSending();
		assertNotNull("expected an item", item);

		// change and persist
		item.getMailOut().setType(MailOutType.REMINDER);
		service.markSent(item);
		flushAndClear();

		InvitationMailQueue loaded = load(InvitationMailQueue.class, item.getId());
		assertEquals("expected error status", MailOutStatus.SENT, loaded.getStatus());

		assertEquals("expected associated invitation's status to update", InvitationStatusType.REMINDED, loaded.getInvitation()
				.getStatus());
	}

	/**
	 * TODO this method will fail if there is any existing data
	 */
	@Test
	public void markError() {

		// sanity
		assertIsNull("test must be run with empty Mail Out queue", service.findUnsentMailQueueAndMarkAsSending());

		Survey survey = createOneMailOutItemScenario();
		assertNotNull("test data defect", survey);

		// the test
		InvitationMailQueue item = service.findUnsentMailQueueAndMarkAsSending();
		assertNotNull("expected an item", item);

		// change and persist
		service.markError(item, ERROR_STRING);
		flushAndClear();

		InvitationMailQueue loaded = load(InvitationMailQueue.class, item.getId());
		assertHasText("expected error string", loaded.getErrorString());
		assertEquals("expected error status", MailOutStatus.ERROR, loaded.getStatus());
	}

	/**
	 * A unit test of the link replacement code.
	 */
	@Test
	public void replaceSurveyVarWithLink_Success() {
		//
		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		String tstString = "asdfaasdf{survey_link}asdfasdf";

		Invitation invitation = new Invitation(survey, "you@hoo.com");
		invitation.setStatus(InvitationStatusType.UNSENT);
		persist(invitation);
		flushAndClear();

		String retval = service.replaceSurveyVarWithLink(tstString, invitation);
		assertTrue(retval.contains("/oas/html/srvy/resp/" + survey.getId() + ".html?ic=" + invitation.getId() + "."
				+ invitation.getInvitationCode()));
	}

}
