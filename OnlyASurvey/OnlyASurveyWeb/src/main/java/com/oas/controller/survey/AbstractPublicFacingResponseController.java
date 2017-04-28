package com.oas.controller.survey;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import com.oas.command.model.ObjectTextCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;
import com.oas.service.DomainModelService;

/**
 * Parent class for controllers that are public-facing, i.e., that allow a
 * Respondent to interact with a Survey.
 * 
 * @author xhalliday
 * @since March 19, 2009
 */
public class AbstractPublicFacingResponseController extends AbstractOASController {

	@Autowired
	private DomainModelService domainModelService;

	/**
	 * Show the "Paused" message for a Survey.
	 * 
	 * @param survey
	 * @return
	 */
	protected ModelAndView showPausedMessage(Survey survey) {
		String message = "Survey is paused";
		ObjectTextCommand command = domainModelService.findObjectText(survey, "pausedMessage");
		if (command != null) {
			// message = command.getMap().get(getCurrentLanguage());
			message = command.getDisplayTitle();
		} else {
			log.warn("Survey #" + survey.getId()
					+ " has no paused message; not supposed to be possible.  Showing default response.paused.pageIntro");
			message = getMessageSourceAccessor().getMessage("response.paused.pageIntro");
		}

		if (!StringUtils.hasText(message)) {
			message = "Survey is paused.  Please try later.";
		}

		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("survey", survey);
		model.put("message", message);
		return new ModelAndView("/response/isPaused", model);
	}

	/**
	 * Show the "Closed" message for a Survey.
	 * 
	 * @param survey
	 * @return
	 */
	// protected ModelAndView showClosedMessage(Survey survey) {
	// String message = "Survey is closed";
	// ObjectTextCommand command = domainModelService.findObjectText(survey,
	// "pausedMessage");
	// if (command != null) {
	// message = command.getMap().get(getCurrentLanguage());
	// } else {
	// log.warn("Survey #" + survey.getId()
	// +
	// " has no paused message; not supposed to be possible.  Showing default response.paused.pageIntro");
	// message =
	// getMessageSourceAccessor().getMessage("response.paused.pageIntro");
	// }
	// Map<String, Object> model = new HashMap<String, Object>(2);
	// model.put("survey", survey);
	// model.put("message", message);
	// }
	// ======================================================================
}
