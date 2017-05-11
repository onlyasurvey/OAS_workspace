package com.oas.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.oas.model.Choice;
import com.oas.model.ObjectName;
import com.oas.model.ObjectResource;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;

@Component
public class PublishSurveyValidator implements Validator {

	// protected Logger log = Logger.getLogger(getClass());

	@Override
	public boolean supports(Class clazz) {
		return Survey.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		Assert.notNull(target);
		Assert.notNull(errors);
		Assert.isTrue(supports(target.getClass()), "unsupported class for validation");

		//
		Survey survey = (Survey) target;

		// validate the name of the survey
		validateSurveyName(survey, errors);

		// validate Welcome Message
		validateWelcomeMessage(survey, errors);

		// validate Thanks Message
		validateThanksMessage(survey, errors);

		// validate Paused Message
		validatePausedMessage(survey, errors);

		// validate Closed Message
		validateClosedMessage(survey, errors);

		// validate Question data
		validateQuestions(survey, errors);

		// if (errors.hasErrors()) {
		// log.warn("USABILITY: user failed to complete all steps for survey to be valid for publishing ("
		// + errors.getErrorCount() + " errors)");
		// }
	}

	protected void validateSurveyName(Survey survey, Errors errors) {

		Assert.notNull(survey);
		Assert.notNull(errors);

		Collection<SupportedLanguage> supportedLanguageList = survey.getSupportedLanguages();
		Assert.notNull(supportedLanguageList, "no languages supported");

		Collection<ObjectName> nameList = survey.getObjectNames();

		// need to validate that each language is actually specified, to avoid
		// counting historical values, for example
		List<SupportedLanguage> nameLanguageList = new ArrayList<SupportedLanguage>();
		for (ObjectName objectName : nameList) {
			nameLanguageList.add(objectName.getLanguage());
		}

		for (SupportedLanguage supportedLanguage : supportedLanguageList) {
			if (!nameLanguageList.contains(supportedLanguage)) {
				errors.reject("publish.error.surveyTitlesMissingForLanguage",
						new Object[] { supportedLanguage.getDisplayTitle() }, "");
			}
		}
	}

	protected void validateQuestions(Survey survey, Errors errors) {

		Assert.notNull(survey);
		Assert.notNull(errors);

		if (survey.getQuestions() == null || survey.getQuestions().size() == 0) {
			errors.reject("publish.error.hasNoQuestions");
			// no further validation possible
			return;
		}

		validateChoiceQuestions(survey, errors);
	}

	/**
	 * Ensure all multiple-choice questions have at least one choice.
	 * 
	 * @param survey
	 * @param errors
	 */
	protected void validateChoiceQuestions(Survey survey, Errors errors) {

		Assert.notNull(survey);
		Assert.notNull(survey.getQuestions());
		Assert.notNull(errors);

		for (Question question : survey.getQuestions()) {
			if (question.isChoiceQuestion()) {
				Collection<Choice> choices = ((ChoiceQuestion) question).getChoices();
				if (choices.size() == 0) {
					// no choices for this multiple-choice question: not valid
					errors.reject("publish.error.noChoices");
				}

				for (Choice choice : choices) {
					validateChoice(survey, question, choice, errors);
				}
			}
		}
	}

	protected void validateChoice(Survey survey, Question question, Choice choice, Errors errors) {

		Assert.notNull(survey);
		Assert.notNull(choice);
		Assert.notNull(errors);

		Collection<SupportedLanguage> supportedLanguageList = survey.getSupportedLanguages();
		Assert.notNull(supportedLanguageList, "no languages supported");

		Collection<ObjectName> nameList = choice.getObjectNames();

		// need to validate that each language is actually specified, to avoid
		// counting historical values, for example
		List<SupportedLanguage> nameLanguageList = new ArrayList<SupportedLanguage>();
		for (ObjectName objectName : nameList) {
			nameLanguageList.add(objectName.getLanguage());
		}

		for (SupportedLanguage supportedLanguage : supportedLanguageList) {

			if (!nameLanguageList.contains(supportedLanguage)) {
				errors.reject("publish.error.choiceTitlesMissingForLanguage", new Object[] { question.getDisplayTitle(),
						choice.getDisplayTitle(), supportedLanguage.getDisplayTitle() }, "");
			}
		}
	}

	protected void validateWelcomeMessage(Survey survey, Errors errors) {

		Map<String, ObjectResource> map = new HashMap<String, ObjectResource>();

		// TODO this is potentially expensive because it will load all
		// ObjectResource's
		for (ObjectResource resource : survey.getObjectResources()) {
			if ("welcomeMessage".equals(resource.getKey())) {
				// missing text
				map.put(resource.getSupportedLanguage().getIso3Lang(), resource);
			}
		}

		for (SupportedLanguage language : survey.getSupportedLanguages()) {
			String isoCode = language.getIso3Lang();

			if (map.get(isoCode) == null || !StringUtils.hasText(map.get(isoCode).getValue())) {
				// missing text
				errors.reject("publish.error.noWelcomeMessage");
				// show this only once, regardless of number of languages which
				// have empty values
				return;
			}
		}
	}

	protected void validateThanksMessage(Survey survey, Errors errors) {

		Map<String, ObjectResource> map = new HashMap<String, ObjectResource>();

		// TODO this is potentially expensive because it will load all
		// ObjectResource's
		for (ObjectResource resource : survey.getObjectResources()) {
			if ("thanksMessage".equals(resource.getKey())) {
				map.put(resource.getSupportedLanguage().getIso3Lang(), resource);
			}
		}

		for (SupportedLanguage language : survey.getSupportedLanguages()) {
			String isoCode = language.getIso3Lang();

			if (map.get(isoCode) == null || !StringUtils.hasText(map.get(isoCode).getValue())) {
				// missing text
				errors.reject("publish.error.noThanksMessage");
				// show this only once, regardless of number of languages which
				// have empty values
				return;
			}
		}
	}

	protected void validatePausedMessage(Survey survey, Errors errors) {

		Map<String, ObjectResource> map = new HashMap<String, ObjectResource>();

		// TODO this is potentially expensive because it will load all
		// ObjectResource's
		for (ObjectResource resource : survey.getObjectResources()) {
			if ("pausedMessage".equals(resource.getKey())) {
				map.put(resource.getSupportedLanguage().getIso3Lang(), resource);
			}
		}

		for (SupportedLanguage language : survey.getSupportedLanguages()) {
			String isoCode = language.getIso3Lang();

			if (map.get(isoCode) == null || !StringUtils.hasText(map.get(isoCode).getValue())) {
				// missing text
				errors.reject("publish.error.noPausedMessage");
				// show this only once, regardless of number of languages which
				// have empty values
				return;
			}
		}
	}

	protected void validateClosedMessage(Survey survey, Errors errors) {

		Map<String, ObjectResource> map = new HashMap<String, ObjectResource>();

		// TODO this is potentially expensive because it will load all
		// ObjectResource's
		for (ObjectResource resource : survey.getObjectResources()) {
			if ("closedMessage".equals(resource.getKey())) {
				map.put(resource.getSupportedLanguage().getIso3Lang(), resource);
			}
		}

		for (SupportedLanguage language : survey.getSupportedLanguages()) {
			String isoCode = language.getIso3Lang();

			if (map.get(isoCode) == null || !StringUtils.hasText(map.get(isoCode).getValue())) {
				// missing text
				errors.reject("publish.error.noClosedMessage");
				// show this only once, regardless of number of languages which
				// have empty values
				return;
			}
		}
	}

}
