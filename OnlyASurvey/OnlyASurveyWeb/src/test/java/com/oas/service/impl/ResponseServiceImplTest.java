package com.oas.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.model.Invitation;
import com.oas.model.InvitationStatusType;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.service.DomainModelService;

public class ResponseServiceImplTest {

	@Autowired
	private DomainModelService domainModelService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	/** Class under test. */
	private ResponseServiceImpl impl;

	// ======================================================================

	public ResponseServiceImplTest() {
		impl = new ResponseServiceImpl();
	}

	// ======================================================================

	@Test
	public void extractResponseFromInvitation_ALL_STATUS_NoResponse() {
		for (InvitationStatusType type : InvitationStatusType.values()) {
			Invitation invitation = new Invitation();
			invitation.setStatus(type);
			Response response = impl.extractResponseFromInvitation(invitation);
			assertTrue("expected no Response back", response == null);
		}
	}

	@Test
	public void extractResponseFromInvitation_ALL_STATUS_DeletedResponse() {
		for (InvitationStatusType type : InvitationStatusType.values()) {
			Invitation invitation = new Invitation();
			Response backingResponse = new Response();
			backingResponse.setDeleted(true);
			invitation.setResponse(backingResponse);
			invitation.setStatus(type);

			// should return the existing one
			Response response = impl.extractResponseFromInvitation(invitation);
			assertTrue("expected no Response back with deleted Response", response == null);
		}
	}

	// ======================================================================

	@Test
	public void extractResponseFromInvitation_ALL_STATUS_OpenResponse() {
		InvitationStatusType[] typeList = new InvitationStatusType[] { InvitationStatusType.STARTED,
				InvitationStatusType.RESPONDED };

		for (InvitationStatusType type : InvitationStatusType.values()) {
			Invitation invitation = new Invitation();
			Response backingResponse = new Response();
			backingResponse.setClosed(false); // explit to highlight it
			invitation.setResponse(backingResponse);
			invitation.setStatus(type);

			// response is open (is_closed=false); regardless of Invitation
			// status, the existing Response should be returend
			Response response = impl.extractResponseFromInvitation(invitation);
			assertFalse("expected existing Response back for status: " + type, response == null);
		}
	}

	@Test
	public void extractResponseFromInvitation_ALL_STATUS_ClosedResponse() {
		for (InvitationStatusType type : InvitationStatusType.values()) {
			Invitation invitation = new Invitation();
			Response backingResponse = new Response();
			backingResponse.setClosed(true);
			invitation.setResponse(backingResponse);
			invitation.setStatus(type);

			// should return the existing one
			Response response = impl.extractResponseFromInvitation(invitation);
			assertFalse("expected a Response back with closed Response", response == null);
		}
	}
}
