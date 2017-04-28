package com.oas.controller.dashboard;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.BaseObject;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;
import com.oas.util.Constants;

/**
 * Allows deleting various types of objects.
 * 
 * @author xhalliday
 */
@Controller
public class DeleteController extends AbstractOASController {

	/** The subject types that can be deleted. */
	private enum SubjectType {
		SURVEY, QUESTION
	};

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	/**
	 * Allow the user to delete a survey that has no response data. Shows a
	 * confirmation message.
	 * 
	 * TODO this method is too complex
	 * 
	 * @param request
	 * @param confirmed
	 * @return
	 */
	@RequestMapping("/db/mgt/rm/*.html")
	@ValidUser
	public ModelAndView deleteItem(HttpServletRequest request) {

		applyWideLayout(request);

		ModelAndView successRedirect = null;
		BaseObject subject = getBaseObjectFromRestfulUrl(request);
		Assert.notNull(subject);

		// get survey, assert ownership
		Survey survey = null;

		SubjectType subjectType = null;
		String pageTitleKey = "deleteItem.pageTitle";

		// this is a business decision to present an obstacle to accidentally
		// deleting a Survey and it's associated Responses
		boolean rejectIfHasResponseData = false;

		if (domainModelService.isSurvey(subject)) {
			subjectType = SubjectType.SURVEY;
			survey = (Survey) subject;
			// rejectIfHasResponseData = true;
			successRedirect = new ModelAndView(new RedirectView(Constants.DEFAULT_HOME, true));
			pageTitleKey = "deleteItem.pageTitle.survey";

		} else if (domainModelService.isQuestion(subject)) {
			subjectType = SubjectType.QUESTION;
			Question question = (Question) subject;
			survey = question.getSurvey();
			successRedirect = new ModelAndView(new RedirectView("/html/db/mgt/ql/" + survey.getId() + ".html", true));
			pageTitleKey = "deleteItem.pageTitle.question";
		}

		// must resolve to one eventually
		Assert.notNull(survey, "unable to determine survey");

		// must be owner
		SecurityAssertions.assertOwnership(survey);

		// handle cancel early
		if (isCancel(request)) {
			// redirect back to the appropriate view based on type
			return successRedirect;
		}

		// can't delete this type of object once a survey has response data
		if (rejectIfHasResponseData && surveyService.hasNonDeletedResponses(survey)) {
			// can't remove when response data exists
			return new ModelAndView("/dashboard/delete/hasResponseDataError", "survey", survey);
		}

		// handles both SAVE and CANCEL, but only persisting on SAVE, but
		// redirecting on ANY
		if (isSave(request) || isCancel(request)) {
			//
			// depending on subject type..
			//
			switch (subjectType) {
			case SURVEY:
				if (isSave(request)) {
					surveyService.deleteSurvey(survey);
				}
				return successRedirect;
			case QUESTION:
				if (isSave(request)) {
					surveyService.deleteQuestion((Question) subject);
				}
				return successRedirect;
			default:
				// should not be possible
				log.error("user specified unhandled ID (#" + subject.getId() + "): unknown class");
				throw new IllegalArgumentException("unknown subject type");
			}

		} else {
			// show confirmation
			ModelMap model = new ModelMap();
			model.addAttribute("subject", subject);
			model.addAttribute("pageTitleKey", pageTitleKey);
			return new ModelAndView("/dashboard/delete/confirmation", model);
		}
	}

	/**
	 * Allows the user to delete all response data associated with a Survey.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/rm/rsp/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView deleteResponseDataForm(HttpServletRequest request) {

		applyWideLayout(request);

		// get survey, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, true);

		// must resolve to one eventually
		Assert.notNull(survey, "unable to determine survey");

		if (isCancel(request)) {
			return createRedirect(request);
		}

		// show confirmation
		return new ModelAndView("/dashboard/delete/deleteResponseDataConfirmation", "subject", survey);
	}

	/**
	 * Allows the user to delete all response data associated with a Survey.
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/rm/rsp/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView deleteResponseDataSubmit(HttpServletRequest request) {

		applyWideLayout(request);

		// get survey, assert ownership
		Survey survey = getSurveyFromRestfulUrl(request);

		// must resolve to one eventually
		Assert.notNull(survey, "unable to determine survey");

		if (isCancel(request)) {
			// user backing out
			log.info("User#" + getCurrentUser().getId() + " canceled request to delete response data for id#" + survey.getId());
		} else if (isSave(request)) {

			// mark any existing responses as deleted
			if (surveyService.hasNonDeletedResponses(survey)) {
				surveyService.deleteResponseData(survey);
			} else {
				// nothing to do if there are no responses
				log.warn("possible usability failure: " + "user requested to delete response data for Survey #" + survey.getId()
						+ " but there is no response data.");
			}

		} else {
			// invalid request, redirect as usual
			log.warn("User#" + getCurrentUser().getId() + " submitted invalid request to delete response data for Survey#"
					+ survey.getId() + " (neither save nor cancel). Redirecting as usual.");
		}

		// return to the Response Data tab
		return createRedirect(request);
	}
}
