package com.oas.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Invitation;
import com.oas.model.Survey;
import com.oas.service.invitations.InvitationService;

public class InvitationServiceTest extends AbstractOASBaseTest {

	private final static String TEST_EMAIL2 = "someone2@somewhere.com";
	private final static String TEST_EMAIL2_SAME = "someone2@somewhere.com";
	// 4 tokens, 3valid
	private final static String TEST_USER_EMAILS_INPUT1 = "\t\n\r\nasdf asdf@asdf\nasdf2@aa,asdf@ff";

	@Autowired
	private InvitationService service;

	@Test
	public void createInvitation_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		int initialCount = countRowsInTable("oas.invitation");

		Invitation inv = service.createInvitation(survey, TEST_EMAIL);
		assertNotNull("expected invitation to be created", inv);
		assertNotNull("expected invitation to be created", inv.getId());

		//
		flushAndClear();

		int newCount = countRowsInTable("oas.invitation");

		assertEquals("expected 1 more in count", initialCount + 1, newCount);

	}

	@Test
	public void createInvitations_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		int initialCount = countRowsInTable("oas.invitation");

		List<String> emailList = new ArrayList<String>();
		emailList.add(TEST_EMAIL);
		emailList.add(TEST_EMAIL2);
		emailList.add(TEST_EMAIL2_SAME);

		List<Invitation> invArr = service.createInvitations(survey, emailList);
		assertEquals("expected 2 invitations", invArr.size(), 2);

		for (Invitation inv : invArr) {
			assertNotNull("expected invitation to be created", inv);
			assertNotNull("expected invitation to be created", inv.getId());
		}

		//
		flushAndClear();

		int newCount = countRowsInTable("oas.invitation");

		assertEquals("expected 2 more in count", initialCount + 2, newCount);

	}

	private List<Invitation> addTestInvitations(Survey survey, int num, String emailBase) {
		List<Invitation> retval = new ArrayList<Invitation>();
		for (int i = 0; i < num; i++) {
			retval.add(service.createInvitation(survey, emailBase + 1));
		}
		flushAndClear();
		return retval;
	}

	private List<Invitation> addTestInvitations(Survey survey, int num) {
		return this.addTestInvitations(survey, num, TEST_EMAIL);
	}

	@Test
	public void getPagedList_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		addTestInvitations(survey, 12);

		List<Invitation> pagedList = service.getPagedList(survey, 1, 5);
		assertEquals("expected full first page", pagedList.size(), 5);

		pagedList = service.getPagedList(survey, 1, 5);
		assertEquals("expected second full page", pagedList.size(), 5);

		pagedList = service.getPagedList(survey, 2, 5);
		assertEquals("looking for partial page", pagedList.size(), 2);

	}

	@Test
	public void purge_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = service.createInvitation(survey, TEST_EMAIL2);
		Long id = inv.getId();
		flushAndClear();

		Invitation getInv = service.getInvitation(id);
		assertNotNull(getInv);
		assertEquals("expected to load invitation", getInv.getId(), id);

		service.purge(getInv);

		flushAndClear();
		getInv = service.getInvitation(id);
		assertIsNull(getInv);

	}

	@Test
	public void sendNewInvitations_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		addTestInvitations(survey, 5, "aaa@aaa");
		assertTrue(service.sendNewInvitations(survey, "subject", "message: {survey_link}"));

		addTestInvitations(survey, 5, "bbb@bbb");
		assertTrue(service.sendNewInvitations(survey, "subject", "message: {survey_link}"));
	}

	@Test
	public void sendAllInvitations_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		addTestInvitations(survey, 5, "aaa@aaa");
		assertTrue(service.sendAllInvitations(survey, "subject", "message: {survey_link}"));
	}

	@Test
	public void sendReminders_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		addTestInvitations(survey, 5, "aaa@aaa");
		assertTrue(service.sendNewInvitations(survey, "subject", "message: {survey_link}"));

		assertTrue(service.sendReminders(survey, "subject", "message: {survey_link}"));
	}

	@Test
	public void validateUserEmailList_Success() {
		String tstData = TEST_USER_EMAILS_INPUT1;

		List<String> tokens = service.tokenizeUserEmailData(tstData);
		assertTrue(4 == tokens.size());

		List<String> validEmails = service.validateUserEmailList(tokens);
		assertTrue(3 == validEmails.size());
	}

	// ======================================================================

}
