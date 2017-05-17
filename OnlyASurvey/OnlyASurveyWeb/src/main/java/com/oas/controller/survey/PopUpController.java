package com.oas.controller.survey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.oas.controller.AbstractOASController;
import com.oas.model.Survey;
import com.oas.service.DomainModelService;

/**
 * Supports the use of pop-up (opt-in) functionality for Surveys.
 * 
 * @author xhalliday
 * @since February 24, 2010
 */
@Controller
public class PopUpController extends AbstractOASController {

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	// ======================================================================

	/**
	 * Send a Survey Logo image to the user.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param pc
	 *            The "pc" parameter, which allows overriding the
	 *            "percent of respondents" to show the pop-up to.
	 * @param response
	 *            {@link HttpServletResponse}
	 */
	@RequestMapping(value = "/srvy/wsi/lb/*.js")
	public ModelAndView sendLightboxScript(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = false) String pc) throws IOException {

		// TODO wrap this to catch exceptions, since output is to be JavaScript

		// TODO this is hardcoded
		int expireDays = 30;

		// do not assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, false);
		String surveyUrl = "http://" + domainModelService.getPublicHostname() + "/oas/html/srvy/vw/" + survey.getId() + ".html";

		// get default from survey
		Long percentChance = survey.getOptinPercentage();
		if (percentChance == null) {
			// paranoia: it's a nullable column
			percentChance = 50L;
		}

		// override param
		if (pc != null) {
			try {
				percentChance = Long.valueOf(pc);
			} catch (NumberFormatException e) {
				// it should still have the value assigned above
			}
		}

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("survey", survey);
		model.put("surveyUrl", surveyUrl);
		model.put("percentChance", percentChance);
		model.put("expireDays", expireDays);

		response.setContentType("text/javascript");
		return new ModelAndView("/survey/siteIntegration/optIn-popUpJS", model);
	}

	/**
	 * DEMO for lightbox.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param pc
	 *            The "pc" parameter, which allows overriding the
	 *            "percent of respondents" to show the pop-up to.
	 * @param response
	 *            {@link HttpServletResponse}
	 */
	@RequestMapping(value = "/srvy/wsi/lb/*.html")
	public ModelAndView demoLightboxScript(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = false) String pc) throws IOException {

		// TODO wrap this to catch exceptions, since output is to be JavaScript

		// do not assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, false);

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("survey", survey);

		// applyZeroLayout(request);

		return new ModelAndView("/survey/siteIntegration/DEMO-optIn-popUpJS", model);
	}

	// ======================================================================
}
