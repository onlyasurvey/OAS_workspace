package com.oas.controller.dashboard.editsurvey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.servlet.view.RedirectView;

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
import com.oas.model.question.PageQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.ScaleQuestionLabel;
import com.oas.model.question.TextQuestion;
import com.oas.security.SecurityAssertions;
import com.oas.util.Constants;
import com.oas.validator.CreateQuestionValidator;

@Controller
@RequestMapping("/db/mgt/q/edt/*.html")
public class EditQuestionController extends AbstractQuestionManagementController {

	/**
	 * Default constructor which takes a validator as a parameter.
	 * 
	 * TODO: validator fails if no choices set, meaning controller has to load
	 * and set it in the command, because it's the same Create validator -
	 * separate EditQuestionValidator required
	 * 
	 * @param validator
	 */
	@Autowired
	public EditQuestionController(CreateQuestionValidator validator) {
		setValidator(validator);
		setCommandClass(CreateQuestionCommand.class);
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

		// get the question from a restful URL
		Question question = getEntityFromRestfulUrl(Question.class, request);
		Assert.notNull(question, "invalid question id");
		model.put("question", question);

		// get the related survey
		Survey survey = question.getSurvey();
		Assert.notNull(survey, "no survey attached");

		// must own the survey
		SecurityAssertions.assertOwnership(survey);

		model.put("survey", survey);
		model.put("supportedLanguages", survey.getSupportedLanguages());

		if ((question.isChoiceQuestion() && (!((ChoiceQuestion) question).isSummingQuestion())) || question.isBooleanQuestion()) {
			model.put("showOtherTextOption", true);
		} else {
			model.put("showOtherTextOption", false);
		}

		return model;
	}

	// ======================================================================

	/**
	 * Show the Edit Question form.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/q/edt/*.html", method = RequestMethod.GET)
	@ValidUser
	public ModelAndView showForm(HttpServletRequest request) throws Exception {
		Map<String, Object> model = referenceData(request);

		// initialize the command and add it to the model
		CreateQuestionCommand command = initializeBasicCommand(model);
		model.put("command", command);

		applyWideLayout(request);

		BindException errors = new BindException(command, "command");
		model.put("errors", errors);

		//
		return showForm(request, errors, "/dashboard/manage/editQuestion/form", model);
	}

	/**
	 * Store changes to the question.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/db/mgt/q/edt/*.html", method = RequestMethod.POST)
	@ValidUser
	public ModelAndView doSubmit(HttpServletRequest request) throws Exception {

		Map<String, Object> model = referenceData(request);

		if (isCancel(request)) {
			Survey survey = getSurveyFromModel(model);
			Assert.notNull(survey, "invalid survey id");
			return new ModelAndView(new RedirectView("/html/db/mgt/ql/" + survey.getId() + ".html", true));
		}

		// initialize the command and add it to the model
		CreateQuestionCommand command = initializeBasicCommand(model);

		ServletRequestDataBinder binder = new ServletRequestDataBinder(command);
		binder.setIgnoreInvalidFields(true);
		binder.bind(request);
		BindException errors = new BindException(binder.getBindingResult());
		// BindException errors = new BindException(bindAndValidate(request,
		// command).getBindingResult());
		getValidator().validate(command, errors);

		//
		model.put("errors", errors);
		model.put("command", command);

		if (errors.hasErrors()) {
			// redisplay form with errors
			applyWideLayout(request);
			return showForm(request, errors, "/dashboard/manage/editQuestion/form", model);
		} else {
			// persist the change and redirect back to the appropriate page

			Question question = (Question) model.get("question");
			Assert.notNull(question);

			Survey survey = question.getSurvey();

			// store; if redirectUrl stays null then redirects to Edit Survey -
			// Question Tab
			String redirectUrl = null;
			if (StringUtils.hasText(request.getParameter("_saveAndEditChoices"))) {
				redirectUrl = "/html/db/mgt/qchs/" + question.getId() + ".html";
			}

			return persistAndRedirect(survey, command, question, redirectUrl);
		}
	}

	// ======================================================================
	// OTHER STUFF
	// ======================================================================

	/**
	 * Initialize a basic command object, returning it and adding it to the
	 * given model.
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected CreateQuestionCommand initializeBasicCommand(Map<String, Object> model) {

		CreateQuestionCommand command = super.initializeBasicCommand(model);

		Survey survey = getSurveyFromModel(model);
		Assert.notNull(survey);

		Question question = (Question) model.get("question");
		Assert.notNull(question);

		List<SupportedLanguage> supportedLanguages = survey.getSupportedLanguages();

		// set names
		for (ObjectName name : question.getObjectNames()) {
			command.getMap().put(name.getLanguage().getIso3Lang(), name.getValue());
		}

		// set flags
		command.setRequired(question.isRequired());
		command.setAllowOtherText(question.isAllowOtherText());

		// type-specifics
		if (question.isTextQuestion()) {
			TextQuestion subject = (TextQuestion) question;
			command.setNumRows(subject.getNumRows());
			command.setFieldDisplayLength(subject.getFieldDisplayLength());
			command.setMaximumLength(subject.getMaximumLength());
		}

		if (question.isChoiceQuestion()) {
			// TODO: remove this: validator fails, because it's the same as the
			// Create validator - separate EditQuestionValidator required
			ChoiceQuestion subject = (ChoiceQuestion) question;
			appendChoicesToCommand(question, command);
			command.setRandomizeChoices(subject.isRandomize());
		}

		if (question.isScaleQuestion()) {
			ScaleQuestion subject = (ScaleQuestion) question;
			command.setMinimum(subject.getMinimum());
			command.setMaximum(subject.getMaximum());
			command.setLabelsOnly(subject.isLabelsOnly());
			Map<ScaleQuestionLabel, String> allLabels = subject.getLabels();

			long useMax = subject.getMaximum();
			for (ScaleQuestionLabel label : allLabels.keySet()) {
				if (label.getScaleValue() > useMax) {
					useMax = label.getScaleValue();
				}
			}

			// initialize label list
			for (Long i = subject.getMinimum(); i <= useMax; i++) {
				NameObjectCommand noc = new NameObjectCommand(supportedLanguages);
				command.setLabel(i.intValue(), noc);
			}

			// overwrite blank values with any existing values
			for (ScaleQuestionLabel label : allLabels.keySet()) {
				NameObjectCommand noc = command.getLabelList().get(label.getScaleValue());
				if (noc != null) {
					noc.addName(label.getLanguage().getIso3Lang(), allLabels.get(label));
					command.setLabel(label.getScaleValue(), noc);
				} else {
					// this can occur because the label list is initialized only
					// to command.getMaximum(), but there may be further data
					// that was created previously, i.e., labels exist past the
					// index specified by maximum.
				}
			}
		}

		if (question.isPageQuestion()) {
			PageQuestion subject = (PageQuestion) question;
			command.setShowBack(subject.isShowBack());
			command.setShowForward(subject.isShowForward());
			command.setPageContent(domainModelService.findObjectText(subject, Constants.ObjectTextKeys.PAGE_CONTENT));
		}

		// for views
		command.setTypeCode(question.getQuestionTypeCode());

		return command;
	}

	protected void appendChoicesToCommand(Question question, CreateQuestionCommand command) {

		Assert.notNull(question);
		Assert.notNull(command);

		Survey survey = question.getSurvey();

		// populate existing choices
		List<ChoiceCommand> list = new ArrayList<ChoiceCommand>();
		for (Choice choice : ((ChoiceQuestion) question).getChoices()) {
			ChoiceCommand choiceCommand = new ChoiceCommand(survey.getSupportedLanguages());
			Map<String, String> map = new HashMap<String, String>();
			for (ObjectName name : choice.getObjectNames()) {
				map.put(name.getLanguage().getIso3Lang(), name.getValue());
			}
			choiceCommand.setMap(map);
			list.add(choiceCommand);
		}

		Collection<SupportedLanguage> languages = survey.getSupportedLanguages();
		for (int i = list.size(); i < Constants.DEFAULT_MAX_CHOICES; i++) {
			list.add(new ChoiceCommand(languages));
		}

		command.setChoiceList(list);
	}
}
