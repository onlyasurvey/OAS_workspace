package com.oas.service.invitations.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.util.StringUtils;

import com.oas.model.Survey;
import com.oas.util.Constants;

public class InvitationServiceImplTest {

	@Test
	public void generateInvitationCode() {
		Survey survey = new Survey();

		InvitationServiceImpl serviceUnderTest = new InvitationServiceImpl();
		String encoded = serviceUnderTest.generateInvitationCode(survey, "lsdkjf@lwsjker.com");

		assertTrue(StringUtils.hasText(encoded));
		assertTrue(encoded.length() == Constants.INVITATION_CODE_LENGTH);
	}
}
