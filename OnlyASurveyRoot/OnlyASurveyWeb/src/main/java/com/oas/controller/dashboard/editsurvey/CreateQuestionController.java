package com.oas.controller.dashboard.editsurvey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.controller.dashboard.AbstractQuestionManagementController;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.util.QuestionTypeCode;
import com.oas.service.SupportedLanguageService;
import com.oas.util.Constants;
import com.oas.validator.CreateQuestionValidator;

@Controller
public class CreateQuestionController extends AbstractQuestionManagementController {

	/** Service for i18n. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	/**
	 * Default constructor which takes a validator as an argument.
	 * 
	 * @param validator
	 */
	@Autowired
	public CreateQuestionController(CreateQuestionValidator validator) {
		setFormView("/dashboard/manage/createQuestion/form");
		setValidator(validator);
	}

	/**
	 * Load data required by all methods.
	 */
	@Override
	@ValidUser
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		return referenceData(request, null);
	}

	/**
	 * Load data required by all methods.
	 */
	@ValidUser
	private Map<String, Object> referenceData(HttpServletRequest request, String detailsViewName) {

		Assert.notNull(request);

		requireSecureContext();

		applyWideLayout(request);

		Survey survey = getSurveyFromRestfulUrl(request, true);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("survey", survey);
		model.put("supportedLanguages", supportedLanguageService.getSupportedLanguages());
		if (detailsViewName != null) {
			model.put("detailsViewName", detailsViewName);
		}
		model.put("showOtherTextOption", false);

		return model;
	}

	// ======================================================================

	/**
	 * Show the Create Survey form - default first step.
	 */
	@RequestMapping(value = "/db/mgt/q/crt/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doSelectType(HttpServletRequest request) {
		return new ModelAndView("/dashboard/manage/createQuestion/selectType", referenceData(request));
	}

	// ======================================================================
	// PAGE/SECTION QUESTIONS
	// ======================================================================
	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.PAGE }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doPageQuestion(HttpServletRequest request) {

		Map<String, Object> model = referenceData(request, "details/page");

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(QuestionTypeCode.PAGE);
		initializePageContent(getSurveyFromModel(model), command);

		// default form values
		command.setShowBack(true);
		command.setShowForward(true);

		return new ModelAndView("/dashboard/manage/createQuestion/form", model);
	}

	private void initializePageContent(Survey survey, CreateQuestionCommand command) {
		ObjectTextCommand otc = new ObjectTextCommand(Constants.ObjectTextKeys.PAGE_CONTENT, survey.getSupportedLanguages());
		command.setPageContent(otc);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.PAGE }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doPageQuestionSubmit(HttpServletRequest request) throws Exception {
		//
		return basicSubmit(request, QuestionTypeCode.PAGE, "details/page");
	}

	// ======================================================================
	// TEXT QUESTIONS
	// ======================================================================
	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.TEXT }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doTextQuestion(HttpServletRequest request) {

		Map<String, Object> model = referenceData(request, "details/text");

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(QuestionTypeCode.TEXT);

		// default form values
		command.setMaximumLength(40);
		command.setFieldDisplayLength(30);
		command.setNumRows(1);

		return new ModelAndView("/dashboard/manage/createQuestion/form", model);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.TEXT }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doTextQuestionSubmit(HttpServletRequest request) throws Exception {
		//
		return basicSubmit(request, QuestionTypeCode.TEXT, "details/text");
	}

	// ======================================================================
	// ESSAY QUESTIONS
	// ======================================================================

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.ESSAY }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doEssayQuestion(HttpServletRequest request) {

		Map<String, Object> model = referenceData(request, "details/essay");

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(QuestionTypeCode.ESSAY);
		model.put("command", command);

		// default form values
		command.setMaximumLength(2000);
		command.setNumRows(8);

		return new ModelAndView("/dashboard/manage/createQuestion/form", model);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.ESSAY }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doEssayQuestionSubmit(HttpServletRequest request) throws Exception {
		//
		return basicSubmit(request, QuestionTypeCode.ESSAY, "details/essay");
	}

	// ======================================================================
	// MULTIPLE-CHOICE QUESTIONS - COMMON CODE
	// ======================================================================

	protected ModelAndView doMultipleChoice(HttpServletRequest request, String typeCode) {

		// specify the choice list; the multiple-choice fieldset JSP is only for
		// edits
		Map<String, Object> model = referenceData(request, "details/multipleChoice.create.choiceList");

		//
		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey, "no survey");
		Collection<SupportedLanguage> supportedLanguages = survey.getSupportedLanguages();
		Assert.notNull(supportedLanguages);

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(typeCode);

		model.put("command", command);
		// Constant Sum doesn't allow an Other Text option
		if (!QuestionTypeCode.CONSTANT_SUM.equals(typeCode)) {
			model.put("showOtherTextOption", true);
		}

		// default form values ONLY on new question
		if (!model.containsKey("question")) {
			for (int i = 0; i < Constants.DEFAULT_MAX_CHOICES; i++) {
				ChoiceCommand choice = new ChoiceCommand();
				for (SupportedLanguage language : supportedLanguages) {
					choice.getMap().put(language.getIso3Lang(), "");
				}
				command.getChoiceList().add(choice);
			}
		}

		return new ModelAndView("/dashboard/manage/createQuestion/form", model);
	}

	protected ModelAndView doMultipleChoiceSubmit(HttpServletRequest request, String typeCode) throws Exception {

		// specify the choice list; the multiple-choice fieldset JSP is only for
		// edits
		Map<String, Object> model = referenceData(request, "details/multipleChoice.create.choiceList");

		if (isCancel(request)) {
			return redirectToSurvey(getSurveyFromModel(model));
		}

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(typeCode);

		int maxChoices = Constants.DEFAULT_MAX_CHOICES;

		command.setChoiceList(new ArrayList<ChoiceCommand>(maxChoices));
		for (int i = 0; i < maxChoices; i++) {
			command.getChoiceList().add(new ChoiceCommand());
		}

		// must be the correct types
		BindException errors = new BindException(bindAndValidate(request, command).getBindingResult());

		if (errors.hasErrors()) {
			model.put("errors", errors);
			// model.put("detailsViewName", "details/multipleChoice");
			// model.put("typeCode", "t");
			return showForm(request, errors, "/dashboard/manage/createQuestion/form", model);
		} else {
			Survey survey = (Survey) model.get("survey");
			Assert.notNull(survey);

			// persist
			Question question = addOrUpdateQuestion(survey, command, null);

			if (StringUtils.hasText(request.getParameter("_saveAndEditChoices"))) {

				// redirect to the Add Many Choices form
				return createRedirect("/html/db/mgt/qchs/" + question.getId() + ".html");

			} else if (StringUtils.hasText(request.getParameter("_saveAndEdit"))) {

				// handles any multiple choice question that wants to redirect
				// to the edit page immediately (e.g., Scale question editor
				// does this to show the label editor).
				return createRedirect("/html/db/mgt/q/" + question.getId() + ".html");

			} else {
				// default
				return redirectToSurvey(survey);
			}
		}
	}

	// ======================================================================
	// RADIO QUESTIONS
	// ======================================================================

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.RADIO }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doRadioButtons(HttpServletRequest request) {

		return doMultipleChoice(request, QuestionTypeCode.RADIO);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.RADIO }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doRadioButtonsSubmit(HttpServletRequest request) throws Exception {

		return doMultipleChoiceSubmit(request, QuestionTypeCode.RADIO);
	}

	// ======================================================================
	// CHECKBOX QUESTIONS
	// ======================================================================

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.CHECKBOX }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doCheckboxes(HttpServletRequest request) {
		return doMultipleChoice(request, QuestionTypeCode.CHECKBOX);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.CHECKBOX }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doCheckboxesSubmit(HttpServletRequest request) throws Exception {

		return doMultipleChoiceSubmit(request, QuestionTypeCode.CHECKBOX);
	}

	// ======================================================================
	// SELECT QUESTIONS
	// ======================================================================

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.SELECT }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doSelectList(HttpServletRequest request) {
		return doMultipleChoice(request, QuestionTypeCode.SELECT);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.SELECT }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doSelectListSubmit(HttpServletRequest request) throws Exception {

		return doMultipleChoiceSubmit(request, QuestionTypeCode.SELECT);
	}

	// ======================================================================
	// CONSTANT SUM QUESTIONS
	// ======================================================================

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.CONSTANT_SUM }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doConstantSum(HttpServletRequest request) {
		return doMultipleChoice(request, QuestionTypeCode.CONSTANT_SUM);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.CONSTANT_SUM }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doConstantSumSubmit(HttpServletRequest request) throws Exception {

		return doMultipleChoiceSubmit(request, QuestionTypeCode.CONSTANT_SUM);
	}

	// ======================================================================
	// SCALE QUESTIONS
	// ======================================================================

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.SCALE }, method = RequestMethod.GET)
	@ValidUser
	public ModelAndView doScaleQuestion(HttpServletRequest request) {

		// specify the choice list; the multiple-choice fieldset JSP is only for
		// edits
		Map<String, Object> model = referenceData(request, "details/scale");

		//
		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey, "no survey");
		Collection<SupportedLanguage> supportedLanguages = survey.getSupportedLanguages();
		Assert.notNull(supportedLanguages);

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(QuestionTypeCode.SCALE);
		command.setMinimum(1L);
		command.setMaximum(Constants.DEFAULT_HIGHEST_SCALE);

		// initialize label list
		for (Long i = command.getMinimum(); i <= command.getMaximum(); i++) {
			NameObjectCommand noc = new NameObjectCommand(supportedLanguages);
			command.setLabel(i.intValue(), noc);
		}

		//
		model.put("command", command);
		model.put("showOtherTextOption", false);

		return new ModelAndView("/dashboard/manage/createQuestion/form", model);
	}

	@RequestMapping(value = "/db/mgt/q/crt/*.html", params = { QuestionTypeCode.SCALE }, method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doScaleQuestionSubmit(HttpServletRequest request) throws Exception {

		return basicSubmit(request, QuestionTypeCode.SCALE, "details/scale");
	}

	// ======================================================================
	// OTHER STUFF
	// ======================================================================

	private ModelAndView basicSubmit(HttpServletRequest request, String typeCode, String viewName) throws Exception {
		Map<String, Object> model = referenceData(request, viewName);

		if (isCancel(request)) {
			return redirectToSurvey(getSurveyFromModel(model));
		}

		// basic command
		CreateQuestionCommand command = initializeBasicCommand(model);
		command.setTypeCode(typeCode);
		if (QuestionTypeCode.SCALE.equals(typeCode)) {
			// this is never handled in the UI; enforce ==1 here
			command.setMinimum(1L);
		} else if (QuestionTypeCode.PAGE.equals(typeCode)) {
			// initialize the map
			initializePageContent(getSurveyFromModel(model), command);
		}

		// must be the correct type

		ServletRequestDataBinder binder = new ServletRequestDataBinder(command);
		binder.setIgnoreInvalidFields(true);
		binder.bind(request);
		BindException errors = new BindException(binder.getBindingResult());
		// BindException errors = new BindException(bindAndValidate(request,
		// command).getBindingResult());
		getValidator().validate(command, errors);

		if (errors.hasErrors()) {
			model.put("errors", errors);
			return showForm(request, errors, "/dashboard/manage/createQuestion/form", model);
		} else {
			Survey survey = (Survey) model.get("survey");
			Assert.notNull(survey);

			String url = null;

			if (StringUtils.hasText(request.getParameter("_saveAndEdit"))) {

				// handles any multiple choice question that wants to redirect
				// to the edit page immediately (e.g., Scale question editor
				// does this to show the label editor).
				url = "/html/db/mgt/q/edt/{questionId}.html";
			}

			return persistAndRedirect(survey, command, null, url);
		}
	}

	/**
	 * Initialize a basic command object, returning it and adding it to the
	 * given model.
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public CreateQuestionCommand initializeBasicCommand(Map<String, Object> model) {

		CreateQuestionCommand command = super.initializeBasicCommand(model);

		// TODO i18n in the supported language, not just the current user's
		// locale
		// String defaultTitle =
		// messageSource.getMessage("createQuestion.defaultNewTitle", null,
		// LocaleContextHolder.getLocale());

		Survey survey = getSurveyFromModel(model);
		Assert.notNull(survey);

		for (SupportedLanguage supportedLanguage : survey.getSupportedLanguages()) {
			command.getMap().put(supportedLanguage.getIso3Lang(), "");
		}

		return command;
	}

}
