package com.oas.controller.survey;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.controller.AbstractOASController;
import com.oas.model.Response;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.templating.SurveyLogo;
import com.oas.service.DomainModelService;
import com.oas.validator.SurveyRespondabilityValidator;

/**
 * Basic Survey controller for showing to respondents.
 * 
 * @author xhalliday
 */
@Controller
public class SurveyController extends AbstractOASController {

	/** General domain model service. */
	@Autowired
	private DomainModelService domainModelService;

	/**
	 * Determines if a Survey can be responded to.
	 */
	@Autowired
	private SurveyRespondabilityValidator respondabilityValidator;

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
	public ModelAndView displaySurvey(HttpServletRequest request) {

		// 
		Map<String, Object> model = referenceData(request);
		Survey survey = getSurveyFromModel(model);
		if (survey == null) {
			return surveyNotAvailable(request);
		}

		model.put("welcomeMessage", domainModelService.findObjectText(survey, "welcomeMessage"));

		// validate expired, etc
		Errors errors = new BindException(survey, "cmd");
		respondabilityValidator.validate(survey, errors);

		applyDefaultSurveyResponseLayout(request);

		if (errors.hasErrors()) {
			model.put("errors", errors);
			return new ModelAndView("/survey/error", model);
		} else {
			return new ModelAndView("/survey/view", model);
		}
	}

	// ======================================================================

	/**
	 * Create a new Response and redirect the user into the survey taking flow.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/srvy/resp/*.html")
	public ModelAndView createResponse(HttpServletRequest request) {

		// 
		Survey survey = getSurveyFromModel(referenceData(request));
		if (survey == null) {
			return surveyNotAvailable(request);
		}

		// create new response
		Response response = responseService.createResponse(survey, supportedLanguageService.findByCode(LocaleContextHolder
				.getLocale().getISO3Language()), request.getRemoteAddr());
		log.info("Created new response (#" + response.getId() + ") for survey id#" + survey.getId());

		// Question firstQuestion = surveyService.findFirstQuestion(survey);

		// sanity
		Assert.notNull(response);
		Assert.notNull(response.getId());
		// Assert.notNull(firstQuestion);
		// Assert.notNull(firstQuestion.getId());

		// redirect user to the start of the response flow
		return new ModelAndView(new RedirectView("/html/res/" + response.getId() + ".html", true));
		// return new ModelAndView(new RedirectView("/html/res/q/" +
		// response.getId() + ".html?qId=" + firstQuestion.getId(), true));
	}

	/**
	 * Shows a message indicating that the Survey was not available.
	 */
	private ModelAndView surveyNotAvailable(HttpServletRequest request) {
		applyWideLayout(request);
		return new ModelAndView("/survey/notAvailableGeneral");
	}

	// ======================================================================

	/**
	 * Shows a message indicating that the Survey was not available.
	 * 
	 * @param request
	 *            The HTTP request
	 * @param p
	 *            The "p" parameter, which is either "l"eft or "r"ight
	 * @param output
	 *            The output stream to send the image
	 * @throws IOException
	 */
	@RequestMapping(value = "/srvy/lg/*", params = { "p" })
	public void logoImage(HttpServletRequest request, String p, OutputStream output) throws IOException {

		// do not assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, false);

		// this breaks Preview - review later, maybe check owner?
		// Errors errors = new BindException(survey, "cmd");
		// respondabilityValidator.validate(survey, errors);
		//
		// if (errors.hasErrors()) {
		// // this is an invalid argument
		// // TODO send error message instead
		// throw new IllegalArgumentException("not respondable");
		// }

		SurveyLogo.PositionType position = null;
		if ("l".equals(p)) {
			position = SurveyLogo.PositionType.LEFT;
		} else if ("r".equals(p)) {
			position = SurveyLogo.PositionType.RIGHT;
		}

		// TODO send error image instead
		Assert.notNull(position, "invalid request");

		//

		SupportedLanguage language = getCurrentSupportedLanguage();
		String overrideLanguage = request.getParameter("l");
		if (StringUtils.hasText(overrideLanguage)) {
			language = supportedLanguageService.findByCode(overrideLanguage);
			Assert.notNull(language, "invalid override language");
		}

		byte[] payload = surveyService.getLogoData(survey, language, position);
		try {
			output.write(payload);
		} catch (IOException e) {
			log.error("IOException streaming " + position + " logo for survey#" + survey.getId() + " to user", e);
			throw e;
		}
	}
	// ======================================================================
}
