package com.oas.controller.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.persistence.DataAccessObject;

import com.oas.command.model.NameObjectCommand;
import com.oas.controller.AbstractOASController;
import com.oas.model.BaseObject;
import com.oas.model.Choice;
import com.oas.model.ObjectName;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.security.SecurityAssertions;
import com.oas.service.DomainModelService;
import com.oas.util.BaseObjectUtil;
import com.oas.validator.NameObjectCommandValidator;

/**
 * TODO REVIEW: usefulness of controller
 */
@Controller
public class NameObjectController extends AbstractOASController {

	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	@Autowired
	private DomainModelService domainModelService;

	/** Validator. */
	@Autowired
	private NameObjectCommandValidator nameObjectCommandValidator;

	/** Default constructor. */
	public NameObjectController() {
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {

		Map<String, Object> retval = new HashMap<String, Object>();

		// get the target object
		BaseObject object = getEntityFromRestfulUrl(BaseObject.class, request);
		Assert.notNull(object);

		Survey survey = getTargetSurvey(object);
		Assert.notNull(survey, "invalid request data: id");

		Collection<SupportedLanguage> supportedLanguages = survey.getSupportedLanguages();
		Assert.notNull(supportedLanguages);

		retval.put("subject", object);
		retval.put("survey", survey);
		retval.put("languageList", supportedLanguages);
		retval.put("cancelUrl", getDefaultRedirectUrl(survey));

		// set the supported language list

		return retval;
	}

	@RequestMapping(value = "/db/mgt/nm/*.html", method = RequestMethod.GET)
	protected ModelAndView doMain(HttpServletRequest request) throws Exception {

		requireSecureContext();

		Map<String, Object> data = referenceData(request);
		Survey survey = (Survey) data.get("survey");
		SecurityAssertions.assertOwnership(survey);

		BaseObject subject = (BaseObject) data.get("subject");
		Assert.notNull(subject, "no subject");

		@SuppressWarnings("unchecked")
		Collection<SupportedLanguage> languageList = (Collection<SupportedLanguage>) data.get("languageList");

		// the command's map is initialized for each surveyLanguage
		NameObjectCommand command = new NameObjectCommand(languageList);

		// set values where they exist
		for (ObjectName name : subject.getObjectNames()) {
			command.getM().put(name.getLanguage().getIso3Lang(), name.getValue());
		}

		data.put("command", command);

		// show login form
		return new ModelAndView("/dashboard/manage/nameObject", data);
	}

	@RequestMapping(value = "/db/mgt/nm/*.html", method = RequestMethod.POST, params = "_save")
	protected ModelAndView doSubmit(HttpServletRequest request, NameObjectCommand command) throws Exception {

		requireSecureContext();

		Map<String, Object> model = referenceData(request);
		Survey survey = (Survey) model.get("survey");
		Assert.notNull(survey);
		SecurityAssertions.assertOwnership(survey);

		BaseObject subject = (BaseObject) model.get("subject");
		// sanity
		Assert.notNull(subject);
		Assert.state(getTargetSurvey(subject).getId().equals(survey.getId()));

		// command model
		// ServletRequestDataBinder binder = new
		// ServletRequestDataBinder(command);
		// binder.bind(request);
		//
		// // since key is obfuscated, we have to set it manually
		// command.setKey(key);

		// validate
		Errors errors = new BindException(command, "command");
		nameObjectCommandValidator.validate(command, errors);

		if (errors.hasErrors()) {
			//
			model.put("command", command);
			model.put("errors", errors);
			return new ModelAndView("/dashboard/manage/nameObject", model);
		} else {
			// persist

			// save
			domainModelService.nameObject(subject, command);

			// show login form
			return getDefaultRedirectOut(survey);
		}
	}

	@RequestMapping(value = "/db/mgt/nm/*.html", method = RequestMethod.POST, params = "_cancel")
	protected ModelAndView doCancel(HttpServletRequest request) throws Exception {

		// load survey
		Survey survey = getSurveyFromRestfulUrl(request, true);

		// redirect out
		return getDefaultRedirectOut(survey);
	}

	protected Survey getTargetSurvey(BaseObject object) {
		Survey retval = null;

		if (BaseObjectUtil.isSurvey(object)) {
			// object is a survey
			retval = (Survey) object;
		} else if (BaseObjectUtil.isQuestion(object)) {
			// object is a question
			retval = ((Question) object).getSurvey();
		} else if (BaseObjectUtil.isChoice(object)) {
			// object is a choice
			retval = ((Choice) object).getQuestion().getSurvey();
		}

		// means we've got an invalid URL, or unhandled case
		Assert.notNull(retval, "unable to determine target survey");

		return retval;
	}

	/**
	 * The default URL to return to when the user is done here.
	 * 
	 * @return
	 */
	private ModelAndView getDefaultRedirectOut(Survey survey) {
		return new ModelAndView(new RedirectView(getDefaultRedirectUrl(survey), true));
	}

	private String getDefaultRedirectUrl(Survey survey) {
		return "/html/db/mgt/" + survey.getId() + ".html";
	}
}
