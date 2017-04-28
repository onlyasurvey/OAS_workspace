package com.oas.controller.dashboard.editsurvey;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.ObjectTextCommand;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.ObjectName;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;
import com.oas.validator.ObjectTextCommandValidator;

/**
 * Controller for managing existing surveys.
 * 
 * @author xhalliday
 * @since September 15, 2008
 */
@Controller
public class ManageSurveyController extends AbstractOASController {

	/**
	 * Service for interacting with the domain model, specifically the
	 * ObjectResource objects.
	 */
	@Autowired
	private DomainModelService domainModelService;

	/** Validates commands that set object resource text. */
	@Autowired
	private ObjectTextCommandValidator objectTextCommandValidator;

	/** Default constructor. */
	public ManageSurveyController() {
	}

	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {

		requireSecureContext();

		// get the survey ID from the URL
		Map<String, Object> model = new HashMap<String, Object>();
		Survey survey = getSurveyFromRestfulUrl(request);

		// required for re-use of the question JSP fragments used when a
		// respondent is answering a survey.
		model.put("command", new SimpleAnswerCommand());

		// set the model
		model.put("survey", survey);

		Map<String, ObjectName> objectNameMap = new HashMap<String, ObjectName>();
		for (ObjectName objectName : survey.getObjectNames()) {
			objectNameMap.put(objectName.getLanguage().getIso3Lang(), objectName);
		}
		model.put("objectNameMap", objectNameMap);

		// number of questions, used by the preview
		model.put("numQuestions", surveyService.countQuestions(survey));

		//
		return model;
	}

	/**
	 * Main View.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/db/mgt/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doMain(HttpServletRequest request, HttpServletResponse response) {

		requireSecureContext();

		Map<String, Object> model = referenceData(request);
		Assert.notNull(model);

		Survey survey = getSurveyFromModel(model);

		model.put("welcomeMessage", domainModelService.findObjectText(survey, "welcomeMessage"));
		model.put("thanksMessage", domainModelService.findObjectText(survey, "thanksMessage"));
		model.put("pausedMessage", domainModelService.findObjectText(survey, "pausedMessage"));
		model.put("closedMessage", domainModelService.findObjectText(survey, "closedMessage"));

		applyWideLayout(request);

		return new ModelAndView("/dashboard/manage/viewSurveyToManage", model);
	}

	// ======================================================================

	/**
	 * Change Text (ObjectResource) form action.
	 */
	@RequestMapping(value = "/db/mgt/ct/*.html", method = RequestMethod.GET, params = { "0" })
	@ValidUser
	public ModelAndView changeTextForm(HttpServletRequest request) {

		requireSecureContext();

		//
		Survey survey = getSurveyFromRestfulUrl(request);
		SecurityAssertions.assertOwnership(survey);

		String key = request.getParameter("0");
		assertValidTextKey(key);

		// command model
		ObjectTextCommand command = domainModelService.findObjectText(survey, key);
		if (command == null) {
			// no existing text
			command = new ObjectTextCommand(key, survey.getSupportedLanguages());
		}

		// currently never happens: see validator
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put("survey", survey);
		model.put("command", command);
		return new ModelAndView("/dashboard/manage/changeTextView", model);
	}

	/**
	 * When ?torc is set, saves a Thank You Message or Redirect URL command.
	 */
	@RequestMapping(value = "/db/mgt/ct/*.html", method = RequestMethod.POST, params = { "0" })
	@ValidUser
	public ModelAndView changeTextSubmit(HttpServletRequest request) {

		requireSecureContext();

		// load survey from id in URL and assert ownership
		Survey survey = getSurveyFromRestfulUrl(request, true);

		if (isCancel(request)) {
			return createRedirect(request, "/html/db/mgt/" + survey.getId() + ".html");
		}

		String key = request.getParameter("0");
		assertValidTextKey(key);

		// command model
		ObjectTextCommand command = new ObjectTextCommand();
		Errors errors = new BindException(command, "command");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(command);
		binder.bind(request);

		// since key is obfuscated, we have to set it manually
		command.setKey(key);

		// validate
		objectTextCommandValidator.validate(command, errors);

		if (errors.hasErrors()) {
			//
			Map<String, Object> model = new HashMap<String, Object>(3);
			model.put("survey", survey);
			model.put("errors", errors);
			model.put("command", command);
			return new ModelAndView("/dashboard/manage/changeTextView", model);
		} else {
			// persist
			domainModelService.setObjectText(survey, command);

			// done
			return createRedirect(request, "/html/db/mgt/" + survey.getId() + ".html");
		}
	}

	private void assertValidTextKey(String key) {
		// TODO refactor out ugliness
		Assert.isTrue("welcomeMessage".equals(key) || "thanksMessage".equals(key) || "pausedMessage".equals(key)
				|| "closedMessage".equals(key));
	}

	// ======================================================================

	@RequestMapping(value = "/db/mgt/*.html", method = RequestMethod.GET, params = { "_dord" })
	@ValidUser
	public ModelAndView changeDisplayOrder(HttpServletRequest request,
			@RequestParam(required = true, value = "qid") Long questionId,
			@RequestParam(required = true, value = "ud") String upOrDown) {

		//
		Survey survey = getSurveyFromRestfulUrl(request);

		Question question = surveyService.findQuestionById(questionId);
		Assert.notNull(question, "invalid question");

		// must be in correct survey
		Assert.isTrue(question.getSurvey().getId().equals(survey.getId()));

		Assert.isTrue("u".equals(upOrDown) || "d".equals(upOrDown));
		if ("u".equals(upOrDown)) {
			surveyService.moveQuestionUp(survey, question);
		}
		if ("d".equals(upOrDown)) {
			surveyService.moveQuestionDown(survey, question);
		}

		// return new ModelAndView(new RedirectView("/html/db/mgt/ql/" +
		// survey.getId() + ".html", true));
		return createRedirect(request, "/html/db/mgt/ql/" + survey.getId() + ".html#q" + question.getId());
	}
	// ======================================================================

}
