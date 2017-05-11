package com.oas.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.oas.model.Survey;

/**
 * Validates a survey to ensure that it is available for responses - it is
 * published, paid for, not over any limits, not expired, etc.
 * 
 * @author xhalliday
 * @since October 1, 2008
 */
@Component("surveyRespondabilityValidator")
public class SurveyRespondabilityValidator implements Validator {

	@Override
	public boolean supports(Class clazz) {
		return Survey.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		Assert.isInstanceOf(Survey.class, target);
		Survey survey = (Survey) target;

		validatePublished(survey, errors);
		validatePaidFor(survey, errors);
	}

	protected void validatePublished(Survey survey, Errors errors) {
		if (!survey.isPublished()) {
			errors.reject("survey.error.notPublished");
		}
	}

	protected void validatePaidFor(Survey survey, Errors errors) {
		if (!survey.isPaidFor()) {
			// we don't tell our client's survey takers that they haven't paid -
			// it's just not available
			errors.reject("survey.error.notAvailable");
		}
	}
}
