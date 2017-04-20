package com.oas.controller.survey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.model.Invitation;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.service.DomainModelService;
import com.oas.service.invitations.InvitationService;
import com.oas.util.WebKeys;

/**
 * Basic Survey controller for showing to respondents.
 * 
 * @author xhalliday
 */
@Controller
public class SurveyController extends AbstractPublicFacingResponseController {

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	/**
	 * Determines if a Survey can be responded to.
	 */
	// @Autowired
	// private SurveyRespondabilityValidator respondabilityValidator;
	@Autowired
	private InvitationService invitationService;

	/**
	 * The Response Controller for invoking methods such as DoThanks.
	 */
	@Autowired
	private ResponseController responseController;

	// ======================================================================

	/**
	 * Load the Survey as reference data.
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) {

		// parse from URL, no ownership check since we're not changing it
		// Survey survey = getSurveyFromRestfulUrl(request, false);
		Survey survey = getEntityFromRestfulUrl(Survey.class, request, false);
		// callers check for null
		// Assert.notNull(survey);

		//
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);

		return model;
	}

	// ======================================================================

	@RequestMapping("/srvy/vw/*.html")
	public ModelAndView displaySurvey(HttpServletRequest request, HttpServletResponse response) {

		// 
		Map<String, Object> model = referenceData(request);

		Survey survey = getSurveyFromModel(model);
		if (survey == null) {
			return surveyNotAvailable(request);
		}

		// L&F
		applySurveyTemplateOption(request, response, survey);

		// determine if it's paused
		if (surveyService.isPaused(survey)) {
			return showPausedMessage(survey);
		}

		model.put("welcomeMessage", domainModelService.findObjectText(survey, "welcomeMessage"));

		return new ModelAndView("/survey/view", model);
	}

	// ======================================================================

	/**
	 * Create a new Response and redirect the user into the survey taking flow.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/srvy/resp/*.html")
	public ModelAndView createResponse(HttpServletRequest request, HttpServletResponse httpResponse,
			@RequestParam(value = "sw", required = false) String skipWelcomeFlag,
			@RequestParam(value = "spw", required = false) String surveyPassword) {

		//
		Survey survey = getSurveyFromModel(referenceData(request));
		if (survey == null) {
			return surveyNotAvailable(request);
		}

		// determine if it's paused
		if (surveyService.isPaused(survey)) {
			return showPausedMessage(survey);
		}

		// language of the response
		SupportedLanguage responseLanguage = supportedLanguageService.findByCode(LocaleContextHolder.getLocale()
				.getISO3Language());

		// do language override if current user's language is not supported
		// #25: http://redmine.itsonlyasurvey.com/issues/show/25
		if (!survey.getSupportedLanguages().contains(getCurrentLanguage())) {
			// get the first Supported Language
			responseLanguage = survey.getSupportedLanguages().get(0);

			// not all paths from here use responseLanguage - ie, any code that
			// might show a view before the response is actually created will
			// need the following to properly override language/template L&F

			// NOTE this changes their language IN SESSION, so if this is being
			// run from the Test Survey link then the user's interface language
			// will be changed.
			changeLocale(request, httpResponse, responseLanguage.getIso3Lang());
		}
		Assert.notNull(responseLanguage, "Survey must support at least one language.");

		// Password Protection
		if (StringUtils.hasText(survey.getGlobalPassword())) {
			// has a password that must be entered before creating a response
			boolean correctPassword = survey.getGlobalPassword().equals(surveyPassword);
			if (!correctPassword) {
				log.warn("User entered incorrect password for survey #" + survey.getId());

				boolean showError = false;

				if (StringUtils.hasText(surveyPassword)) {
					// password entered, but it was the wrong one
					showError = true;
				}

				return promptForPassword(request, httpResponse, survey, skipWelcomeFlag, showError);
			}
		}

		if (hasInvitationCode(request)) {

			// use the Invitation handler
			return handleInvitationCode(request, httpResponse, survey, responseLanguage, skipWelcomeFlag, surveyPassword);
		}

		// process any Invitation Code
		return doCreateResponse(survey, responseLanguage, request, skipWelcomeFlag, surveyPassword, null);
	}

	/**
	 * Worker method for createResponse and handleInvitationCode.
	 * 
	 * @param survey
	 * @param responseLanguage
	 * @param request
	 * @param skipWelcomeFlag
	 * @param surveyPassword
	 * @param invitation
	 * @return {@link ModelAndView}
	 * @see #createResponse
	 * @see #handleInvitationCode
	 */
	protected ModelAndView doCreateResponse(Survey survey, SupportedLanguage responseLanguage, HttpServletRequest request,
			String skipWelcomeFlag, String surveyPassword, Invitation invitation) {
		// create new response
		Response response = responseService.createResponse(survey, responseLanguage, request.getRemoteAddr(), surveyPassword,
				invitation);
		log.info("Created new response (#" + response.getId() + ") for survey id#" + survey.getId());

		// sanity
		Assert.notNull(response);
		Assert.notNull(response.getId());

		// redirect user to the start of the response flow
		if (skipWelcomeFlag != null) {

			Question firstQuestion = surveyService.findFirstQuestion(survey);
			Assert.notNull(firstQuestion);
			Assert.notNull(firstQuestion.getId());

			String url = "/html/res/q/" + response.getId() + ".html?n=1";
			return new ModelAndView(new RedirectView(url, true));
		} else {
			// start normally
			return new ModelAndView(new RedirectView("/html/res/" + response.getId() + ".html", true));
		}
	}

	/**
	 * Handle an InvitationCode, creating a response as appropriate. May
	 * redirect to Thanks page if Response is closed.
	 * 
	 * @param request
	 * @param httpResponse
	 * @param survey
	 * @param responseLanguage
	 * @param skipWelcomeFlag
	 * @param surveyPassword
	 * @return
	 */
	protected ModelAndView handleInvitationCode(HttpServletRequest request, HttpServletResponse httpResponse, //
			Survey survey, SupportedLanguage responseLanguage, String skipWelcomeFlag, String surveyPassword) {

		Invitation invitation = null;
		Response existingResponse = null;

		try {
			// load it
			invitation = getInvitationByCode(request);

			// validate it
			Assert.isTrue(isValidInvitationCode(invitation, parseInvitationCode(request)));

		} catch (IllegalArgumentException e) {
			// friendly error: both getInvitationByCode and Assert will
			// throw this
			return new ModelAndView("/response/invalidInvitation");
		}

		// handle re-use of invitations
		// if (invitation.isStarted() || invitation.isResponded()) {

		Response response = invitation.getResponse();
		if (response == null || response.isDeleted()) {
			// this can happen if the user wipes their response
			// database: previously-responded-to invitations will become
			// available again

			// let it go, do not set existingResponse, create a new one
		} else {
			existingResponse = invitation.getResponse();
			// it would be atypical that Invite is marked RESPONDED but the
			// Response is not closed, but could happen if the user deletes
			// their responses and someone reuses an Invitation without
			// invitations being re-sent by the user, therefore no check nor
			// error here
		}
		// }

		// 
		if (existingResponse != null) {
			// check to see if the response is complete
			if (existingResponse.isClosed()) {
				return responseController.doThanks(existingResponse, request, httpResponse);
			}

			// CHOOSE: welcome page or redirect to the response itself
			int highestQuestion = responseService.getHighestHistoryPosition(existingResponse);
			boolean noResponseHistory = highestQuestion == -1;
			boolean atFirstQuestion = highestQuestion == 0;
			if (noResponseHistory || atFirstQuestion) {
				// still at question #1
				if (skipWelcomeFlag == null) {
					// start normally
					return new ModelAndView(new RedirectView("/html/res/" + existingResponse.getId() + ".html", true));
				} // else: skip welcome message, fall through to below
			}

			// no ?n= - let the other controller figure it out
			return createRedirect("/html/res/q/" + existingResponse.getId() + ".html");
		}

		// 
		// default case: create a new Response to a (presumably unresponded)
		// Invitation
		//
		return doCreateResponse(survey, responseLanguage, request, skipWelcomeFlag, surveyPassword, invitation);
	}

	/**
	 * Load the appropriate Invitation for the request.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return {@link Invitation}
	 */
	private Invitation getInvitationByCode(HttpServletRequest request) {

		// format is
		// ?ic=ID.code
		// eg: ?ic=92395723.cf8ad9

		Long id = parseInvitationId(request);
		String code = parseInvitationCode(request);

		// load, trust the ID is a number
		Invitation invitation = invitationService.getInvitation(id);

		// must be true
		Assert.isTrue(isValidInvitationCode(invitation, code));

		return invitation;
	}

	private Long parseInvitationId(HttpServletRequest request) {
		// format is
		// ?ic=ID.code
		// eg: ?ic=92395723.cf8ad9
		String param = request.getParameter(WebKeys.INVITATION_CODE);
		// it is invalid to call this method without the request parameter
		Assert.hasText(param);

		try {
			String id = param.substring(0, param.indexOf("."));
			return Long.valueOf(id);
		} catch (Exception e) {
			// catching Exception is evil, but necessary here for logging
			// invalid attempts which may indicate trolling by a user
			log.warn("User supplied invalid invitation code: ID is not valid for parameter:" + param);
			throw new IllegalArgumentException("invalid invitation ID");
		}
	}

	private String parseInvitationCode(HttpServletRequest request) {
		// format is
		// ?ic=ID.code
		// eg: ?ic=92395723.cf8ad9
		String param = request.getParameter(WebKeys.INVITATION_CODE);
		// it is invalid to call this method without the request parameter
		Assert.hasText(param);

		String code = param.substring(param.indexOf(".") + 1);

		return code;
	}

	/**
	 * Determine if the {@link Invitation} and code pair are valid.
	 * 
	 * @param invitation
	 *            {@link Invitation}
	 * @param code
	 *            Code from the query string.
	 * @return boolean
	 */
	private boolean isValidInvitationCode(Invitation invitation, String code) {
		Assert.notNull(invitation, "no invitation specified");
		return StringUtils.hasText(code) && StringUtils.hasText(invitation.getInvitationCode())
				&& invitation.getInvitationCode().equals(code);
	}

	/**
	 * Determine if the request contains an Invitation Code.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return boolean
	 */
	private boolean hasInvitationCode(HttpServletRequest request) {
		return StringUtils.hasText(request.getParameter(WebKeys.INVITATION_CODE));
	}

	private ModelAndView promptForPassword(HttpServletRequest request, HttpServletResponse httpResponse, Survey survey,
			String skipWelcomeFlag, boolean showError) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("skipWelcomeFlag", skipWelcomeFlag);
		if (showError) {
			Errors errors = new BindException(survey, "survey");
			errors.reject("response.passwordPrompt.error");
			model.put("errors", errors);
		}

		//
		// applyDefaultSurveyResponseLayout(request);

		// apply the appropriate look and feel for the survey
		applySurveyTemplateOption(request, httpResponse, survey);

		//
		return new ModelAndView("/survey/passwordPrompt", model);
	}

	/**
	 * Shows a message indicating that the Survey was not available.
	 */
	private ModelAndView surveyNotAvailable(HttpServletRequest request) {
		applyWideLayout(request);
		return new ModelAndView("/survey/notAvailableGeneral");
	}

}
