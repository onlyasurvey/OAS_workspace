package com.oas.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Invitation;
import com.oas.model.Survey;

public class InvitationServiceTest extends AbstractOASBaseTest {

	// private final static String TEST_EMAIL

	@Autowired
	private InvitationService service;

	@Test
	public void createInvitation_Success() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(
				createAndSetSecureUserWithRoleUser(), true);

		int initialCount = countRowsInTable("oas.invitation");

		Invitation inv = service.createInvitation(survey, TEST_EMAIL);
		assertNotNull("expected invitation to be created", inv);
		assertNotNull("expected invitation to be created", inv.getId());

		//
		flushAndClear();

		int newCount = countRowsInTable("oas.invitation");

		assertEquals("expected 1 more in count", initialCount + 1, newCount);

	}
}
