package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.model.UserAccount;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.AddRespondentCommand;
import com.oas.command.model.SendToRespondentCommand;
import com.oas.model.Invitation;
import com.oas.model.Survey;
import com.oas.service.invitations.InvitationService;

public class InvitationControllerTest extends AbstractOASBaseTest {
	@Autowired
	private InvitationService invitationService;

	@Autowired
	private InvitationController controller;

	@Test
	public void testShowMain_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.mainView(request);

		assertModelHasSurvey(mav, survey);

		assertNotNull(mav.getModel());
		assertNotNull(mav.getModelMap().get("inviteList"));
		assertNotNull(mav.getModelMap().get("inviteListSize"));
		assertEquals(Integer.parseInt(mav.getModelMap().get("inviteListSize").toString()), 1);

		assertHasViewName(mav, "/dashboard/manage/publishTab/invites/invitesMainView");
	}

	@Test
	public void testDoConfirmDeleteRespondent_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + inv.getId() + "/" + survey.getId()
				+ ".html");

		ModelAndView mav = controller.doConfirmDeleteRespondent(request);

		assertNotNull(mav);
		assertNotNull(mav.getModel());
		assertNotNull(mav.getModelMap().get("subject"));

		assertHasViewName(mav, "/dashboard/manage/publishTab/invites/confirmDeleteRespondent");
	}

	@Test
	public void testDoConfirmDeleteRespondent_NoSuchInvitation() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);
		invitationService.purge(inv);
		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + inv.getId() + "/" + survey.getId()
				+ ".html");

		ModelAndView mav = controller.doConfirmDeleteRespondent(request);

		assertNotNull(mav);
		assertEmpty(mav.getModel().keySet());
		assertIsRedirect("if invitation already deleted, supposed to redirect to invites page", mav, //
				"/html/db/mgt/pb/inv/" + survey.getId() + ".html");
	}

	@Test
	public void testDoSubmitDeleteRespondent_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + inv.getId() + "/" + survey.getId()
				+ ".html");
		request.addParameter("_save", "1");

		ModelAndView mav = controller.doSubmitDeleteRespondent(request);

		assertNotNull(mav);
		assertEquals(mav.getView().getClass(), RedirectView.class);
		assertIsNull(invitationService.getInvitation(inv.getId()));

	}

	@Test
	public void testDoSubmitDeleteRespondent_NoSaveWithoutParam() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + inv.getId() + "/" + survey.getId()
				+ ".html");
		// missing _save param
		ModelAndView mav = controller.doSubmitDeleteRespondent(request);

		assertNotNull(mav);
		assertEquals(mav.getView().getClass(), RedirectView.class);
		assertNotNull(invitationService.getInvitation(inv.getId()));
	}

	@Test
	public void testDoAddRespondent_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.doAddRespondent(request);

		assertModelHasSurvey(mav, survey);

		assertNotNull(mav.getModel());
		assertNotNull(mav.getModelMap().get("command"));

		assertHasViewName(mav, "/dashboard/manage/publishTab/invites/addRespondent");
	}

	@Test
	public void testDoSubmitAddRespondent_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		AddRespondentCommand command = new AddRespondentCommand();
		command.setUserEmailData("test1@asdf");
		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doAddRespondentSubmit(command, errors, request);
			assertNotNull(mav);
			assertEquals(mav.getView().getClass(), RedirectView.class);
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testDoSubmitAddRespondent_FailureDuplicate() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		AddRespondentCommand command = new AddRespondentCommand();
		command.setUserEmailData("joblo@asdf.ca");
		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doAddRespondentSubmit(command, errors, request);
			assertNotNull(mav);
			assertModelHasSurvey(mav, survey);

			assertNotNull(mav.getModel());
			assertNotNull(mav.getModelMap().get("command"));
			assertNotNull(mav.getModelMap().get("errors"));
			assertHasViewName(mav, "/dashboard/manage/publishTab/invites/addRespondent");
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}

	@Test
	public void testInviteAllView_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.inviteAllView(request);

		assertModelHasSurvey(mav, survey);

		assertNotNull(mav.getModel());
		assertNotNull(mav.getModelMap().get("command"));

		assertHasViewName(mav, "/dashboard/manage/publishTab/invites/inviteAllView");
	}

	@Test
	public void testDoInviteAll_Success() throws Exception {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		SendToRespondentCommand command = new SendToRespondentCommand();
		command.setFromAddress("test@home.tld");
		command.setSubject("test subject");
		command.setMessage("test message {survey_link}");
		Errors errors = new BindException(command, "command");

		ModelAndView mav = controller.doInviteAll(command, errors, request);
		assertNotNull(mav);
		assertEquals(mav.getView().getClass(), RedirectView.class);
	}

	@Test
	public void testDoInviteAll_Failure() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		SendToRespondentCommand command = new SendToRespondentCommand();
		command.setSubject("test subject");
		command.setFromAddress("test@home.tld");
		// FAILS BECAUSE OF MISSING SURVEY_LINK VAR
		command.setMessage("test message");
		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doInviteAll(command, errors, request);
			assertModelHasSurvey(mav, survey);

			assertNotNull(mav.getModel());
			assertNotNull(mav.getModelMap().get("command"));
			assertNotNull(mav.getModelMap().get("errors"));
			assertHasViewName(mav, "/dashboard/manage/publishTab/invites/inviteAllView");

		} catch (Exception e) {
			fail("unexpected exception");
		}

	}

	@Test
	public void testInviteNewMailView_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.inviteNewMainView(request);

		assertModelHasSurvey(mav, survey);

		assertNotNull(mav.getModel());
		assertNotNull(mav.getModelMap().get("command"));

		assertHasViewName(mav, "/dashboard/manage/publishTab/invites/inviteNewView");
	}

	@Test
	public void testDoSubmitInviteNew_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		SendToRespondentCommand command = new SendToRespondentCommand();
		command.setFromAddress("test@home.tld");
		command.setSubject("test subject");
		command.setMessage("test message {survey_link}");
		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doSubmitInviteNew(command, errors, request);
			assertNotNull(mav);
			assertEquals(mav.getView().getClass(), RedirectView.class);
		} catch (Exception e) {
			fail("unexpected exception");
		}

	}

	@Test
	public void testDoSubmitInviteNew_Failure() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		SendToRespondentCommand command = new SendToRespondentCommand();
		command.setSubject("test subject");
		// FAILS BECAUSE OF MISSING SURVEY_LINK VAR
		command.setMessage("test message");
		command.setFromAddress("test@home.tld");
		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doSubmitInviteNew(command, errors, request);
			assertModelHasSurvey(mav, survey);

			assertNotNull(mav.getModel());
			assertNotNull(mav.getModelMap().get("command"));
			assertNotNull(mav.getModelMap().get("errors"));
			assertHasViewName(mav, "/dashboard/manage/publishTab/invites/inviteNewView");

		} catch (Exception e) {
			fail("unexpected exception");
		}

	}

	@Test
	public void testMainViewSendReminder_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		ModelAndView mav = controller.mainViewSendReminder(request);

		assertModelHasSurvey(mav, survey);

		assertNotNull(mav.getModel());
		assertNotNull(mav.getModelMap().get("command"));

		assertHasViewName(mav, "/dashboard/manage/publishTab/invites/inviteReminderView");
	}

	@Test
	public void testDoSubmitSendReminder_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		SendToRespondentCommand command = new SendToRespondentCommand();
		command.setSubject("test subject");
		command.setMessage("test message {survey_link}");
		command.setFromAddress("test@home.tld");

		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doSubmitSendReminder(command, errors, request);
			assertNotNull(mav);
			assertEquals(mav.getView().getClass(), RedirectView.class);
		} catch (Exception e) {
			fail("unexpected exception");
		}

	}

	@Test
	public void testDoSubmitSendReminder_Failure() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Invitation inv = invitationService.createInvitation(survey, "joblo@asdf.ca");
		persist(inv);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.addParameter("_save", "1");

		SendToRespondentCommand command = new SendToRespondentCommand();
		command.setSubject("test subject");
		command.setFromAddress("test@home.tld");

		// FAILS BECAUSE OF MISSING SURVEY_LINK VAR
		command.setMessage("test message");
		Errors errors = new BindException(command, "command");

		try {
			ModelAndView mav = controller.doSubmitSendReminder(command, errors, request);
			assertModelHasSurvey(mav, survey);

			assertNotNull(mav.getModel());
			assertNotNull(mav.getModelMap().get("command"));
			assertNotNull(mav.getModelMap().get("errors"));
			assertHasViewName(mav, "/dashboard/manage/publishTab/invites/inviteReminderView");

		} catch (Exception e) {
			fail("unexpected exception");
		}

	}

	@Test
	public void testSecurity_FailWrongUserAll() {
		// setup other user with survey and invitation
		UserAccount user = createAndSetSecureUserWithRoleUser();
		Survey survey = scenarioDataUtil.createTypicalScenario1(user, true);
		Invitation invite = invitationService.createInvitation(survey, "asdf@asdf");
		persist(invite);
		clearSecurityContext();

		// setup controller user
		createAndSetSecureUserWithRoleUser();

		MockHttpServletRequest surveyRequest = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		MockHttpServletRequest inviteRequest = new MockHttpServletRequest("GET", "/prefix/" + invite.getId() + ".html");

		try {
			controller.mainView(surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		try {
			controller.doConfirmDeleteRespondent(inviteRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		} catch (Exception e) {

		}
		try {
			controller.doSubmitDeleteRespondent(inviteRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		} catch (Exception e) {

		}

		try {
			controller.doAddRespondent(surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		AddRespondentCommand command = new AddRespondentCommand();
		try {
			controller.doAddRespondentSubmit(command, new BindException(command, "command"), surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected }
		} catch (Exception e) {
			fail("Expected ADE");
		}

		try {
			controller.inviteAllView(surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		SendToRespondentCommand command2 = new SendToRespondentCommand();
		try {
			controller.doInviteAll(command2, new BindException(command2, "command"), surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected }
		} catch (Exception e) {
			fail("Expected ADE");
		}

		try {
			controller.inviteNewMainView(surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		try {
			controller.doSubmitInviteNew(command2, new BindException(command2, "command"), surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected }
		} catch (Exception e) {
			fail("Expected ADE");
		}

		try {
			controller.mainViewSendReminder(surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected
		}

		try {
			controller.doSubmitSendReminder(command2, new BindException(command2, "command"), surveyRequest);
			fail("Expected ADE");
		} catch (AccessDeniedException e) {
			// expected }
		} catch (Exception e) {
			fail("Expected ADE");
		}

	}
}
