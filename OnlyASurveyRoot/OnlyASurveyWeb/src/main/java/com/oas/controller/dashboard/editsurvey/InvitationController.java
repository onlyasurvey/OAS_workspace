package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.AddRespondentCommand;
import com.oas.command.model.SendToRespondentCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.Invitation;
import com.oas.model.Survey;
import com.oas.service.invitations.InvitationService;
import com.oas.validator.AddRespondentValidator;
import com.oas.validator.SendToRespondentValidator;

/**
 * Invitations for Surveys controller.
 * 
 * @author xhalliday
 * @author jfchenier
 * @since March 9, 2009
 */
@Controller
public class InvitationController extends AbstractOASController {

	/** How many results to show per page. */
	private final static int RESULTS_PER_PAGE = 20;

	/** Invitations and reminders service. */
	@Autowired
	private InvitationService invitationService;

	/** Validators */
	@Autowired
	private AddRespondentValidator addRespondentValidator;

	@Autowired
	private SendToRespondentValidator sendToRespondentValidator;

	/**
	 * Private redirect-home helper function
	 * 
	 * @param survey
	 * @return
	 */
	private ModelAndView redirectToManageInvitations(Survey survey) {
		return createRedirect("/html/db/mgt/pb/inv/" + survey.getId() + ".html");
	}

	// ======================================================================
	// MANAGE INVITATIONS
	// ======================================================================

	/**
	 * Show the main view.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView mainView(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("inviteListSize", invitationService.countInvitations(survey));
		model
				.put("inviteList", invitationService.getPagedList(survey, getDisplayTagPageNumber(request, "lid"),
						RESULTS_PER_PAGE));

		// show view
		return new ModelAndView("/dashboard/manage/publishTab/invites/invitesMainView", model);
	}

	// ======================================================================
	// DELETE RESPONDENT
	// ======================================================================

	/**
	 * Confirm Delete View.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/dl/*/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doConfirmDeleteRespondent(HttpServletRequest request) {

		requireSecureContext();
		Survey survey = getSurveyFromRestfulUrl(request);

		// get invitation from URL, get survey & assert ownership
		Invitation invitation = invitationService.getInvitation(getRestfulId(request, 1));
		if (invitation == null) {
			// http://redmine.itsonlyasurvey.com/issues/show/153
			// handle case where the invitation has already been deleted
			return createRedirect("/html/db/mgt/pb/inv/" + survey.getId() + ".html");
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("subject", invitation);
		model.put("survey", survey);

		// show view
		return new ModelAndView("/dashboard/manage/publishTab/invites/confirmDeleteRespondent", model);

	}

	// ======================================================================

	/**
	 * Delete Respondent Submit Action.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/dl/*/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doSubmitDeleteRespondent(HttpServletRequest request) {
		requireSecureContext();
		Survey survey = getSurveyFromRestfulUrl(request);

		// get invitation from URL, get survey & assert ownership
		Invitation invitation = invitationService.getInvitation(getRestfulId(request, 1));
		if (invitation == null) {
			// http://redmine.itsonlyasurvey.com/issues/show/153
			// handle case where the invitation has already been deleted
			return createRedirect("/html/db/mgt/pb/inv/" + survey.getId() + ".html");
		}

		if (isSave(request)) {
			invitationService.purge(invitation);
		}

		return redirectToManageInvitations(survey);

	}

	// ======================================================================
	// ADD RESPONDENT
	// ======================================================================

	/**
	 * Show Add Respondent view.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/nwrsp/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doAddRespondent(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);
		AddRespondentCommand command = new AddRespondentCommand();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", command);

		// show view
		return new ModelAndView("/dashboard/manage/publishTab/invites/addRespondent", model);
	}

	// ======================================================================

	/**
	 * Create new Respondents
	 * 
	 * @param request
	 * @param command
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/nwrsp/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doAddRespondentSubmit(@ModelAttribute("command") AddRespondentCommand command,
			@ModelAttribute("errors") Errors errors, HttpServletRequest request) throws Exception {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		if (isSave(request)) {
			addRespondentValidator.validate(command, errors);

			// If there is at least one valid email address to process, go
			if (!errors.hasFieldErrors()) {
				// generate valid tokens
				List<String> formatedInput = invitationService.tokenizeUserEmailData(command.getUserEmailData().trim());
				List<String> validInput = invitationService.validateUserEmailList(formatedInput);
				Assert.notEmpty(validInput);

				// notify user that duplicate email addresses will be ignored
				List<String> registeredEmailList = invitationService.getRespondentEmailList(survey);
				for (String email : validInput) {
					if (registeredEmailList.contains(email)) {
						errors.reject("addRespondents.errors.emailAlreadyRegistered", new Object[] { email }, "default?");
					}
				}

				invitationService.createInvitations(survey, validInput);

			}
			if (errors.hasErrors()) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("survey", survey);
				model.put("command", command);
				model.put("errors", errors);
				// show view
				return new ModelAndView("/dashboard/manage/publishTab/invites/addRespondent", model);
			}

		}
		return redirectToManageInvitations(survey);

	}

	// ======================================================================
	// INVITE ALL
	// ======================================================================

	/**
	 * Show the main view.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/nvtll/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView inviteAllView(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", new SendToRespondentCommand());

		// show view
		return new ModelAndView("/dashboard/manage/publishTab/invites/inviteAllView", model);
	}

	// ======================================================================

	/**
	 * Invite All Respondents
	 * 
	 * @param request
	 * @param command
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/nvtll/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doInviteAll(@ModelAttribute("command") SendToRespondentCommand command,
			@ModelAttribute("errors") Errors errors, HttpServletRequest request) throws Exception {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		if (isSave(request)) {
			sendToRespondentValidator.validate(command, errors);
			if (errors.hasErrors()) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("survey", survey);
				model.put("command", command);
				model.put("errors", errors);
				// show view
				return new ModelAndView("/dashboard/manage/publishTab/invites/inviteAllView", model);
			}

			// else do service
			invitationService.sendAllInvitations(survey, command.getSubject(), command.getMessage());
		}
		return redirectToManageInvitations(survey);

	}

	// ======================================================================
	// INVITE NEW
	// ======================================================================
	/**
	 * Show invite new view.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/nvtnw/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView inviteNewMainView(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", new SendToRespondentCommand());

		// show view
		return new ModelAndView("/dashboard/manage/publishTab/invites/inviteNewView", model);
	}

	// ======================================================================

	/**
	 * Invite New Submit Action
	 * 
	 * @param request
	 * @param command
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/nvtnw/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doSubmitInviteNew(@ModelAttribute("command") SendToRespondentCommand command,
			@ModelAttribute("errors") Errors errors, HttpServletRequest request) throws Exception {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		if (isSave(request)) {
			sendToRespondentValidator.validate(command, errors);
			if (errors.hasErrors()) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("survey", survey);
				model.put("command", command);
				model.put("errors", errors);
				// show view
				return new ModelAndView("/dashboard/manage/publishTab/invites/inviteNewView", model);

			}
			// else do service
			invitationService.sendNewInvitations(survey, command.getSubject(), command.getMessage());
		}
		return redirectToManageInvitations(survey);

	}

	// ======================================================================
	// SEND REMINDER PAGES
	// ======================================================================
	/**
	 * Show the send reminder view.
	 * 
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/sndrmndr/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView mainViewSendReminder(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("command", new SendToRespondentCommand());

		// show view
		return new ModelAndView("/dashboard/manage/publishTab/invites/inviteReminderView", model);
	}

	// ======================================================================

	/**
	 * Send Reminder Submit Action
	 * 
	 * @param request
	 * @param command
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/pb/inv/sndrmndr/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doSubmitSendReminder(@ModelAttribute("command") SendToRespondentCommand command,
			@ModelAttribute("errors") Errors errors, HttpServletRequest request) throws Exception {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		if (isSave(request)) {
			sendToRespondentValidator.validate(command, errors);
			if (errors.hasErrors()) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("survey", survey);
				model.put("command", command);
				model.put("errors", errors);
				// show submit view with errors
				return new ModelAndView("/dashboard/manage/publishTab/invites/inviteReminderView", model);

			}
			// else do service
			invitationService.sendReminders(survey, command.getSubject(), command.getMessage());
		}

		return redirectToManageInvitations(survey);

	}

}
