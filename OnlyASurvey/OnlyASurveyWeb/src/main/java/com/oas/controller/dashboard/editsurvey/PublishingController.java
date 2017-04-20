package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;
import com.oas.service.DomainModelService;
import com.oas.service.invitations.InvitationService;
import com.oas.validator.PublishSurveyValidator;

/**
 * Controller for Publishing Surveys to make them open to the public. Uses a
 * ready-for-publishing validator to ensure survey data is valid, and enforces
 * ownership and isPaidFor requirements.
 * 
 * @author xhalliday
 * @since October 6, 2008
 */
@Controller
public class PublishingController extends AbstractOASController {

	/**
	 * Service for interacting with the domain model, specifically the
	 * ObjectResource objects.
	 */
	@Autowired
	private DomainModelService domainModelService;

	/**
	 * Validator for determining if a Survey can be put into Published state.
	 */
	@Autowired
	private PublishSurveyValidator readyToPublishSurveyValidator;

	/**
	 * Service for interacting with the domain model, specifically the
	 * ObjectResource objects.
	 */
	@Autowired
	private InvitationService invitationService;

	/**
	 * Default view.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/pb/*.html", method = { RequestMethod.GET })
	public ModelAndView showTab(HttpServletRequest request) {

		// get survey from URL, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// model
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);

		// determine a public URL for it
		model.put("publicUrl", determineSurveyUrl(request, survey));
		model.put("shortUrl", determineShortSurveyUrl(request, survey));

		// URL for the opt-in lightbox
		model.put("optInScriptUrl", determineOptInScriptUrl(request, survey));
		model.put("optInCSSUrl", determineOptInCSSUrl(request, survey));

		// strings
		model.put("pausedMessage", domainModelService.findObjectText(survey, "pausedMessage"));
		model.put("closedMessage", domainModelService.findObjectText(survey, "closedMessage"));

		// respondents
		model.put("responseCount", surveyService.countClosedResponses(survey));

		// total invitation respondents
		model.put("totalRespondents", invitationService.countInvitations(survey));

		// total invitations sent
		model.put("totalInvitesSent", invitationService.countSentInvitations(survey));

		// total responses from invitations
		model.put("totalResponses", invitationService.countInvitationResponses(survey));

		// show view
		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/publishTab/publishTab", model);
	}

	/**
	 * Change the Survey to be in the Published state.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/pb/*.html", method = { RequestMethod.POST }, params = { "_pb" })
	public ModelAndView doPublishSubmit(HttpServletRequest request) {

		// get survey and assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, true);

		// TODO validator will fail if !isPaidFor, however payment function
		// unimplemented
		if (!survey.isPaidFor()) {
			survey.setPaidFor(true);
		}

		// perform all required validations before publishing
		// see business rules doc for explanation of validation rules
		Errors errors = new BindException(survey, "surveyToPublish");
		readyToPublishSurveyValidator.validate(survey, errors);

		if (errors.hasErrors()) {
			// fix errors before requiring payment: no surprises for our clients
			return doErrorsView(survey, errors);
		}

		Errors publishingErrors = surveyService.publishSurvey(survey);
		// validator should NEVER fail
		Assert.isTrue(!publishingErrors.hasErrors());

		// all good: generic Success message
		return new ModelAndView(new RedirectView("/html/db/mgt/pb/sc/" + survey.getId() + ".html", true));
	}

	/**
	 * Change the Survey to be in the Draft state.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/pb/*.html", method = { RequestMethod.POST }, params = { "_dr" })
	public ModelAndView doDraftSubmit(HttpServletRequest request) {

		// get survey and assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/pb/" + survey.getId() + ".html");
		}

		int count = surveyService.countResponses(survey);
		if (count > 0) {
			if (isSave(request)) {
				log.warn("User #" + getCurrentUser().getId() + " is setting survey #" + survey.getId()
						+ " with response data to Draft Status - user confirmation obtained.");
			} else {
				log.warn("User #" + getCurrentUser().getId() + " is attempting to set survey #" + survey.getId()
						+ " with response data back into draft status.  Sending to confirm page.");
				// has response data and no confirmation flag
				return new ModelAndView("/dashboard/manage/publishTab/confirmDraftStatus", "survey", survey);
			}
		}

		// commit the change
		surveyService.unpublishSurvey(survey);

		// all good: return to tab view
		return new ModelAndView(new RedirectView("/html/db/mgt/pb/" + survey.getId() + ".html", true));
	}

	/**
	 * Simple success view, redirected to from elsewhere.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/pb/sc/*.html")
	public ModelAndView doSuccessView(HttpServletRequest request) {
		// get survey and assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, true);

		return new ModelAndView("/dashboard/manage/publishTab/publishSuccess", "survey", survey);
	}

	/**
	 * Special case: show the Payment Required view.
	 * 
	 * @param survey
	 * @return
	 */
	protected ModelAndView doErrorsView(Survey survey, Errors errors) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("errors", errors);
		return new ModelAndView("/dashboard/manage/publishTab/errors", model);
	}

	/**
	 * Determine the Survey's public URL.
	 * 
	 * TODO move this to a service, make it configurable in the backend
	 * 
	 * @param request
	 * @param survey
	 * @return
	 */
	protected String determineSurveyUrl(HttpServletRequest request, Survey survey) {
		String retval = determineAbsoluteUrlPrefix(request) + "/html/srvy/vw/" + survey.getId() + ".html";
		return retval;
	}

	/**
	 * Determine the Survey's Opt-In Lightbox Script URL.
	 * 
	 * TODO move this to a service, make it configurable in the backend
	 * 
	 * @param request
	 * @param survey
	 * @return
	 */
	protected String determineOptInScriptUrl(HttpServletRequest request, Survey survey) {
		String retval = determineAbsoluteUrlPrefix(request) + "/html/srvy/wsi/lb/" + survey.getId() + ".js";
		return retval;
	}

	/**
	 * Determine the Survey's Opt-In Lightbox CSS URL.
	 * 
	 * TODO move this to a service, make it configurable in the backend
	 * 
	 * @param request
	 * @param survey
	 * @return
	 */
	protected String determineOptInCSSUrl(HttpServletRequest request, Survey survey) {
		String retval = determineAbsoluteUrlPrefix(request) + "/html/srvy/wsi/lb/" + survey.getId() + ".css";
		return retval;
	}

	/**
	 * Determine the Survey's "short" public URL.
	 * 
	 * TODO move this to a service, make it configurable in the backend
	 * 
	 * @param request
	 * @param survey
	 * @return
	 */
	protected String determineShortSurveyUrl(HttpServletRequest request, Survey survey) {
		// verbatum from configuration with the survey ID appended
		String prefix = domainModelService.getShortUrlPrefix();
		// even though the contract says null is returned when not configured,
		// we add the ID here, leaving just a number as the return value; needs
		// to be empty for the view to determine whether or not to show the
		// option
		if (StringUtils.hasText(prefix)) {
			return domainModelService.getShortUrlPrefix() + survey.getId();
		}
		return null;
	}

}
