package com.oas.validator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Survey;
import com.oas.service.SupportedLanguageService;

public class SurveyRespondabilityValidatorTest extends AbstractOASBaseTest {

	@Autowired
	private SurveyRespondabilityValidator validator;

	@Autowired
	private SupportedLanguageService supportedLanguageService;

	private void validateAndExpect(Survey survey, int expectedErrors) {
		validateAndExpect(validator, survey, expectedErrors);
	}

	// ======================================================================

	private Survey newValidSurvey() {
		Survey retval = new Survey();

		return retval;
	}

	// ======================================================================

	@Test
	public void testSupportsCorrectCommand() {
		assertTrue(validator.supports(Survey.class));
		assertFalse(validator.supports(Long.class));
	}

	// ======================================================================

	@Test
	public void testSuccess() {
		Survey survey = newValidSurvey();

		survey.setPublished(true);
		survey.setPaidFor(true);

		validateAndExpect(survey, 0);
	}

	// ======================================================================

	@Test
	public void testFail_Unpublished() {
		Survey survey = new Survey();
		survey.setPublished(false);
		survey.setPaidFor(true);

		validateAndExpect(survey, 1);
	}

	@Test
	public void testFail_UnpaidFor() {
		Survey survey = new Survey();
		survey.setPublished(true);
		survey.setPaidFor(false);

		validateAndExpect(survey, 1);
	}
}
