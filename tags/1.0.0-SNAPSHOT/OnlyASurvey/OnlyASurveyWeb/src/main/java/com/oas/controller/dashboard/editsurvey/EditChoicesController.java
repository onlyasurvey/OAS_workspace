package com.oas.controller.dashboard.editsurvey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.controller.dashboard.AbstractQuestionManagementController;
import com.oas.model.Choice;
import com.oas.model.ObjectName;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.security.SecurityAssertions;
import com.oas.util.Constants;
import com.oas.validator.CreateQuestionValidator;
import com.oas.validator.NameObjectCommandValidator;

/**
 * Controller for managing the Choices in a Question.
 * 
 * @author xhalliday
 * @since December 18, 2008
 */
@Controller
public class EditChoicesController extends AbstractQuestionManagementController {

	/** For validating Add and Edit. */
	@Autowired
	private NameObjectCommandValidator nameObjectCommandValidator;

	/** For validating Add Many. */
	@Autowired
	private CreateQuestionValidator createQuestionValidator;

	/**
	 * Default constructor.
	 * 
	 */
	public EditChoicesController() {
		// we do manual validation
		setValidateOnBinding(false);
	}

	/**
	 * Load data required by all methods.
	 */
	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {

		Assert.notNull(request);

		// must be a valid security context
		requireSecureContext();

		// the model
		Map<String, Object> model = new HashMap<String, Object>();

		return model;
	}

	// ======================================================================

	/**
	 * List all choices for a question.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html")
	public ModelAndView listChoices(HttpServletRequest request) {

		//
		Question question = getEntityFromRestfulUrl(Question.class, request);
		SecurityAssertions.assertOwnership(question.getSurvey());

		Map<String, Object> model = referenceData(request);
		model.put("question", question);

		//
		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/editChoices/listChoices", model);
	}

	// ======================================================================

	/**
	 * FORM: Add a new Choice to a question.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_a" }, method = RequestMethod.GET)
	public ModelAndView addChoice(HttpServletRequest request) {
		NameObjectCommand command = new NameObjectCommand();
		return addChoice(request, command, new BindException(command, "command"));
	}

	public ModelAndView addChoice(HttpServletRequest request, NameObjectCommand command, Errors errors) {
		Question question = getEntityFromRestfulUrl(Question.class, request);
		SecurityAssertions.assertOwnership(question.getSurvey());

		Map<String, Object> model = referenceData(request);
		model.put("question", question);
		model.put("command", command);
		model.put("errors", errors);
		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/editChoices/addChoiceForm", model);
	}

	/**
	 * SUBMIT: Add a new Choice to a question.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_a" }, method = RequestMethod.POST)
	public ModelAndView addChoiceSubmit(HttpServletRequest request, NameObjectCommand command) {
		ChoiceQuestion question = getEntityFromRestfulUrl(ChoiceQuestion.class, request);
		SecurityAssertions.assertOwnership(question.getSurvey());

		// user canceled
		if (isCancel(request)) {
			return getDefaultRedirect(question);
		}

		Errors errors = new BindException(command, "command");
		nameObjectCommandValidator.validate(command, errors);

		if (errors.hasErrors()) {
			return addChoice(request, command, errors);
		} else {
			// persist change
			Choice choice = surveyService.addChoice(question, command);
			Assert.notNull(choice, "unable to save choice");

			// redirect out
			return getDefaultRedirect(question);
		}
	}

	/**
	 * FORM: Add a many new Choices to a question.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_am" }, method = RequestMethod.GET)
	public ModelAndView addManyChoices(HttpServletRequest request) {

		// List<NameObjectCommand> command = new ArrayList<NameObjectCommand>();
		return addManyChoices(request, null, null);
	}

	public ModelAndView addManyChoices(HttpServletRequest request, CreateQuestionCommand command, Errors errors) {
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		// some callers may not initialize this
		if (command == null) {
			Collection<SupportedLanguage> languages = survey.getSupportedLanguages();
			command = new CreateQuestionCommand(survey.getSupportedLanguages());

			for (int i = 0; i < Constants.DEFAULT_MAX_CHOICES; i++) {
				command.getChoiceList().add(new ChoiceCommand(languages));
			}
		}

		// some callers may not initialize this
		if (errors == null) {
			errors = new BindException(command, "command");
		}

		//
		Map<String, Object> model = referenceData(request);
		model.put("survey", survey);
		model.put("question", question);
		model.put("command", command);
		model.put("errors", errors);
		applyWideLayout(request);
		return new ModelAndView("/dashboard/manage/editChoices/addManyChoicesForm", model);
	}

	/**
	 * SUBMIT: Add a many new Choices to a question.
	 * 
	 * @throws Exception
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_am" }, method = RequestMethod.POST)
	public ModelAndView addManyChoicesSubmit(HttpServletRequest request) throws Exception {

		// subject
		ChoiceQuestion question = getEntityFromRestfulUrl(ChoiceQuestion.class, request);

		// user canceled
		if (isCancel(request)) {
			return getDefaultRedirect(question);
		}

		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		CreateQuestionCommand command = new CreateQuestionCommand();
		// need to initialize internal choiceList so binder works
		for (int i = 0; i < Constants.DEFAULT_MAX_CHOICES; i++) {
			command.getChoiceList().add(new ChoiceCommand(survey.getSupportedLanguages()));
		}

		return addManyChoicesSubmit(request, question, command);
	}

	public ModelAndView addManyChoicesSubmit(HttpServletRequest request, ChoiceQuestion question, CreateQuestionCommand command)
			throws Exception {

		// user canceled
		if (isCancel(request)) {
			return getDefaultRedirect(question);
		}

		Survey survey = question.getSurvey();
		SecurityAssertions.assertOwnership(survey);

		// bind and invoke specific part of the validator
		ServletRequestDataBinder binder = bindAndValidate(request, command);
		Errors errors = new BindException(binder.getBindingResult());
		createQuestionValidator.validateMultipleChoiceCommon(command, errors);
		// Errors errors = new BindException(command, "command");

		if (errors.hasErrors()) {
			return addManyChoices(request, command, errors);
		} else {
			// persist change
			surveyService.addManyChoices(question, command.getChoiceList());

			// redirect out
			return getDefaultRedirect(question);
		}
	}

	// ======================================================================

	/**
	 * FORM: Edit a Choice.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_e" }, method = RequestMethod.GET)
	public ModelAndView editChoice(HttpServletRequest request) {
		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		ChoiceQuestion question = choice.getQuestion();
		SecurityAssertions.assertOwnership(question.getSurvey());

		NameObjectCommand command = new NameObjectCommand();
		for (ObjectName name : choice.getObjectNames()) {
			command.getMap().put(name.getLanguage().getIso3Lang(), name.getValue());
		}

		return editChoice(request, question, choice, command, new BindException(command, "command"));
	}

	public ModelAndView editChoice(HttpServletRequest request, Question question, Choice choice, NameObjectCommand command,
			Errors errors) {

		Map<String, Object> model = referenceData(request);
		model.put("question", question);
		model.put("choice", choice);
		model.put("command", command);
		model.put("errors", errors);

		applyWideLayout(request);

		return new ModelAndView("/dashboard/manage/editChoices/editChoiceForm", model);
	}

	/**
	 * SUBMIT: Edit a Choice.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_e" }, method = RequestMethod.POST)
	public ModelAndView editChoiceSubmit(HttpServletRequest request, NameObjectCommand command) {

		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		ChoiceQuestion question = choice.getQuestion();
		SecurityAssertions.assertOwnership(question.getSurvey());

		// user canceled
		if (isCancel(request)) {
			return getDefaultRedirect(question);
		}

		Errors errors = new BindException(command, "command");
		nameObjectCommandValidator.validate(command, errors);

		if (errors.hasErrors()) {
			return editChoice(request, question, choice, command, errors);
		} else {
			// persist change
			surveyService.updateChoice(question, choice, command);

			// redirect out
			return getDefaultRedirect(question);
		}
	}

	// ======================================================================

	/**
	 * Delete a Choice.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_d" }, method = RequestMethod.GET)
	public ModelAndView deleteChoiceForm(HttpServletRequest request) {

		// subject
		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		SecurityAssertions.assertOwnership(choice.getQuestion().getSurvey());

		Map<String, Object> model = referenceData(request);
		model.put("choice", choice);
		applyWideLayout(request);

		return new ModelAndView("/dashboard/manage/editChoices/deleteChoiceForm", model);
	}

	/**
	 * Delete a Choice.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_d" }, method = RequestMethod.POST)
	public ModelAndView deleteChoiceSubmit(HttpServletRequest request) {

		// subject
		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		SecurityAssertions.assertOwnership(choice.getQuestion().getSurvey());

		// for redirecting out
		Question question = choice.getQuestion();
		ModelAndView retval = getDefaultRedirect(question);

		if (isCancel(request)) {
			return retval;
		}

		// do the deed
		surveyService.deleteChoice(choice);

		//
		return retval;
	}

	// ======================================================================

	/**
	 * Move a Choice up.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_mu" }, method = RequestMethod.GET)
	public ModelAndView moveUp(HttpServletRequest request) {

		// subject
		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		SecurityAssertions.assertOwnership(choice.getQuestion().getSurvey());
		ChoiceQuestion question = choice.getQuestion();
		Assert.notNull(question);

		// persist the change
		surveyService.moveChoiceUp(choice);

		return createRedirect("/html/db/mgt/qchs/" + question.getId() + ".html");
	}

	// ======================================================================

	/**
	 * Move a Choice down.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_md" }, method = RequestMethod.GET)
	public ModelAndView moveDown(HttpServletRequest request) {

		// subject
		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		SecurityAssertions.assertOwnership(choice.getQuestion().getSurvey());
		ChoiceQuestion question = choice.getQuestion();
		Assert.notNull(question);

		// persist the change
		surveyService.moveChoiceDown(choice);

		return createRedirect("/html/db/mgt/qchs/" + question.getId() + ".html");
	}

	// ======================================================================

	/**
	 * Clone a Choice.
	 * 
	 */
	@ValidUser
	@RequestMapping(value = "/db/mgt/qchs/*.html", params = { "_cln" }, method = RequestMethod.GET)
	public ModelAndView cloneChoice(HttpServletRequest request) {

		// subject
		Choice choice = getEntityFromRestfulUrl(Choice.class, request);
		ChoiceQuestion question = choice.getQuestion();

		//
		SecurityAssertions.assertOwnership(question.getSurvey());

		//
		Choice clone = surveyService.cloneChoice(choice);
		Assert.notNull(clone, "failed to clone choice");

		//
		return getDefaultRedirect(question);
	}

	// ======================================================================

	protected ModelAndView getDefaultRedirect(Question question) {
		return createRedirect("/html/db/mgt/qchs/" + question.getId() + ".html");
	}
}
