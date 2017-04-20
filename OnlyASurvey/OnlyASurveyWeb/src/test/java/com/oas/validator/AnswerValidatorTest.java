package com.oas.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.HashMap;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.util.Constants;
import com.oas.util.GeneralValidationErrors;

/**
 * Test for AnswerValidator.
 * 
 * @author xhalliday
 */
public class AnswerValidatorTest extends AbstractOASBaseTest {

	/** Class under test. */
	@Autowired
	private AnswerValidator answerValidator;

	/** expected codes for an illegal argument. */
	private static final String[] ILLEGAL_ARGUMENT = { "illegalArgument" };

	private AnswerValidator getValidator() {
		assertNotNull("invalid wiring: no answer validator", answerValidator);
		return answerValidator;
	}

	/**
	 * Need a new version of this to call the correct validate method
	 */
	protected void validateAndExpect(Object command, int expectedErrors, String[] errorCodes, Question question) {
		Errors errors = new BindException(command, "cmd");
		getValidator().validate(command, errors, question);
		assertEquals("did not have expected number of errors: " + errors.getAllErrors(), expectedErrors, errors.getAllErrors()
				.size());
		if (errorCodes != null) {
			for (String code : errorCodes) {
				assertHasError(errors, code);
			}
		}
	}

	// ======================================================================

	@Test
	public void supportsCorrectCommand() {
		assertTrue(getValidator().supports(SimpleAnswerCommand.class));
	}

	private Question getTextQuestionToValidate() {

		TextQuestion question = new TextQuestion();
		question.setRequired(true);
		question.setMaximumLength(35);

		return question;
	}

	/**
	 * Generate a ScaleQuestion, 0-11
	 * 
	 * @return
	 */
	private ScaleQuestion getScaleQuestionToValidate() {

		ScaleQuestion question = new ScaleQuestion();
		question.setRequired(true);
		question.setMinimum(0L);
		question.setMaximum(11L);

		return question;
	}

	private ChoiceQuestion getChoiceQuestionToValidate() {

		// need IDs for some validation
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), false);
		// return getFirstQuestionOfType(survey, ChoiceQuestion.class);

		ChoiceQuestion question = new ChoiceQuestion(survey, 99L);

		survey.addQuestion(question);

		question.setRequired(true);

		question.addChoice(new Choice(question, 0L));
		question.addChoice(new Choice(question, 1L));
		question.addChoice(new Choice(question, 2L));

		//
		persist(survey);

		return question;
	}

	// ======================================================================

	@Test
	public void textAnswer_Success() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("product quality");

		Errors errors = new BindException(target, "cmd");
		getValidator().validate(target, errors, getTextQuestionToValidate());
		assertFalse("should have zero errors", errors.hasErrors());
	}

	@Test
	public void textAnswer_Fail_HugeInput() {
		SimpleAnswerCommand command = new SimpleAnswerCommand();

		final String format = "%" + 128 * 1024 + "s";
		command.setAnswer(String.format(format, "input"));

		Question question = getTextQuestionToValidate();
		assertTrue(question.isRequired());

		Errors errors = new BindException(command, "cmd");
		getValidator().validate(command, errors, getTextQuestionToValidate());
		validateAndExpect(command, 1, new String[] { "question.error.answerTooLong" }, question);

	}

	@Test
	public void textAnswer_FailRequired() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("");

		Question question = getTextQuestionToValidate();
		assertTrue(question.isRequired());

		Errors errors = new BindException(target, "cmd");
		getValidator().validate(target, errors, question);
		assertTrue("should have errors", errors.hasErrors());
	}

	@Test
	public void textAnswer_AllowEmptyIfNotRequired() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("");

		Question question = getTextQuestionToValidate();
		question.setRequired(false);

		Errors errors = new BindException(target, "cmd");
		getValidator().validate(target, errors, question);
		assertFalse("should NOT have errors", errors.hasErrors());
	}

	@Test
	public void textAnswer_AllNullAnswers_FailsValidation() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		// neither a text answer nor id list answer

		Errors errors = new BindException(target, "cmd");
		getValidator().validate(target, errors, getTextQuestionToValidate());
		assertTrue("should have errors", errors.hasErrors());
	}

	// ======================================================================

	@Test
	public void scaleAnswer_Success() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("7");

		validateAndExpect(target, 0, null, getScaleQuestionToValidate());
	}

	@Test
	public void scaleAnswer_Fail_Underflow() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("-29");

		validateAndExpect(target, 1, ILLEGAL_ARGUMENT, getScaleQuestionToValidate());
	}

	@Test
	public void scaleAnswer_Fail_Overflow() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("10000");

		validateAndExpect(target, 1, ILLEGAL_ARGUMENT, getScaleQuestionToValidate());
	}

	@Test
	public void scaleAnswer_Fail_NotANumber() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("happy penguin");

		validateAndExpect(target, 1, ILLEGAL_ARGUMENT, getScaleQuestionToValidate());
	}

	@Test
	public void scaleAnswer_Fail_Empty() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("");

		ScaleQuestion question = getScaleQuestionToValidate();
		question.setRequired(true);
		validateAndExpect(target, 1, new String[] { GeneralValidationErrors.ANSWER_REQUIRED }, question);
	}

	@Test
	public void scaleAnswer_Success_EmptyButNotRequired() {
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setAnswer("");

		ScaleQuestion question = getScaleQuestionToValidate();
		question.setRequired(false);
		validateAndExpect(target, 0, null, question);
	}

	// ======================================================================

	@Test
	public void choiceList_PassWithSelection() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();
		target.setChoiceIdList(new long[] { 2L });

		validateAndExpect(target, 0, null, getChoiceQuestionToValidate());
	}

	/**
	 * UI should prevent this condition so it's an illegalArgument (potential
	 * hack attack or form defect).
	 */
	@Test
	public void choiceList_Fail_NoneOfTheAbove_OnRequired() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();

		ChoiceQuestion question = getChoiceQuestionToValidate();
		question.setRequired(true);
		target.setChoiceIdList(new long[] { Constants.NONE_OF_THE_ABOVE_ID });
		validateAndExpect(target, 1, ILLEGAL_ARGUMENT, question);
	}

	@Test
	public void choiceList_FailWithMultiSelectionForSingleSelectQuestion() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();

		ChoiceQuestion question = getChoiceQuestionToValidate();

		// one selection allowed
		question.setUnlimited(false);

		// two selections provided
		target.setChoiceIdList(new long[] { 2L, 3L });

		validateAndExpect(target, 1, ILLEGAL_ARGUMENT, question);
	}

	@Test
	public void choiceList_FailRequired() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();
		Errors errors = new BindException(target, "cmd");

		target.setChoiceIdList(new long[] {});

		Question question = getChoiceQuestionToValidate();
		assertTrue(question.isRequired());

		getValidator().validate(target, errors, question);
		assertTrue("should have errors", errors.hasErrors());
	}

	@Test
	public void choiceList_FailSingleSelectWithChoiceAndText() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();

		// single-selection that allows other text
		ChoiceQuestion question = getChoiceQuestionToValidate();
		question.setUnlimited(false);
		question.setRequired(true);
		question.setAllowOtherText(true);

		target.setChoiceIdList(new long[] { 2 });
		target.setOtherText("other text");

		validateAndExpect(target, 1, new String[] {}, question);
	}

	/**
	 * Question is Required, but Other (-1) is selected, and text is provided.
	 */
	@Test
	public void choiceList_Success_OtherText() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();

		target.setChoiceIdList(new long[] { Constants.OTHER_TEXT_ID });
		target.setOtherText("someOtherText");

		ChoiceQuestion question = getChoiceQuestionToValidate();
		assertTrue(question.isRequired());
		//
		question.setUnlimited(false);
		question.setAllowOtherText(true);

		validateAndExpect(target, 0, null, question);
	}

	/**
	 * Question is Required, but Other (-1) is selected, and WAY too much text
	 * is provided.
	 */
	@Test
	public void choiceList_Fail_HugeOtherText() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();

		target.setChoiceIdList(new long[] { Constants.OTHER_TEXT_ID });
		target.setOtherText(String.format("%" + (Constants.MAXIMUM_OTHER_TEXT_LENGTH + 1) + "s", "a"));

		ChoiceQuestion question = getChoiceQuestionToValidate();
		assertTrue(question.isRequired());
		//
		question.setUnlimited(false);
		question.setAllowOtherText(true);

		validateAndExpect(target, 1, new String[] { "question.error.answerTooLong" }, question);
	}

	/**
	 * Question is Required, but Other (-1) is selected, and text is provided.
	 */
	@Test
	public void choiceList_Fail_OtherSelectedButEmptyText() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();
		Errors errors = new BindException(target, "cmd");

		// "Other"
		target.setChoiceIdList(new long[] {});

		// but no Other Text
		target.setOtherText("");

		Question question = getChoiceQuestionToValidate();
		assertTrue(question.isRequired());
		//
		question.setAllowOtherText(true);

		getValidator().validate(target, errors, question);
		assertHasError(errors, GeneralValidationErrors.ANSWER_OR_OTHER_TEXT_REQUIRED);
	}

	@Test
	public void choiceList_AllowEmptyIfNotRequired() {

		SimpleAnswerCommand target = new SimpleAnswerCommand();
		Errors errors = new BindException(target, "cmd");

		target.setChoiceIdList(new long[] {});

		Question question = getChoiceQuestionToValidate();
		question.setRequired(false);

		getValidator().validate(target, errors, question);
		assertFalse("should NOT have errors", errors.hasErrors());
	}

	@Test
	public void failsWithoutQuestionParameter() {
		// validator REQUIRES question parameter: default validate() method is
		// unsupported
		SimpleAnswerCommand target = new SimpleAnswerCommand();
		Errors errors = new BindException(target, "cmd");
		try {
			getValidator().validate(target, errors);
			fail("expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void summingQuestion() {

		ChoiceQuestion question = getChoiceQuestionToValidate();

		question.setMaximumSum(100);

		assertTrue("should be identified as a summing question", question.isSummingQuestion());

		// not required: can have any numbers, up to maximum
		// test: zero selections
		{
			question.setRequired(false);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			validateAndExpect(command, 0, new String[0], question);
		}

		// not required: can have any numbers, up to maximum
		// test: partial (<maximumSum) selections
		{
			question.setRequired(false);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			// < maximumSum
			command.getSumByChoiceId().put(question.getChoices().get(0).getId(), 50);

			validateAndExpect(command, 0, new String[0], question);
		}

		// not required: can have any numbers, up to maximum
		// test: over-maximum fails
		{
			question.setRequired(false);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			// < maximumSum
			command.getSumByChoiceId().put(question.getChoices().get(0).getId(), question.getMaximumSum() + 1);

			validateAndExpect(command, 1, new String[] { GeneralValidationErrors.ANSWER_SUMS_MORE_THAN_TOTAL_REQUIRED }, question);
		}

		// not required: can have any numbers, up to maximum
		// test: negatives not allowed
		{
			question.setRequired(false);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			// < maximumSum
			command.getSumByChoiceId().put(question.getChoices().get(0).getId(), -10);

			validateAndExpect(command, 1, new String[] { GeneralValidationErrors.NEGATIVE_NUMBERS_NOT_ALLOWED }, question);
		}

		// ----

		// required: answers must equal maximumSum
		// test: zero selections
		{
			question.setRequired(true);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			validateAndExpect(command, 1, new String[] { GeneralValidationErrors.ANSWER_SUMS_LESS_THAN_TOTAL_REQUIRED }, question);
		}

		// required: answers must equal maximumSum
		// test: partial (<maximumSum) selections
		{
			question.setRequired(true);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			// < maximumSum
			command.getSumByChoiceId().put(question.getChoices().get(0).getId(), 50);

			validateAndExpect(command, 1, new String[] { GeneralValidationErrors.ANSWER_SUMS_LESS_THAN_TOTAL_REQUIRED }, question);
		}

		// required: answers must equal maximumSum
		// test: exactly equal
		{
			question.setRequired(true);
			SimpleAnswerCommand command = new SimpleAnswerCommand();
			command.setSumByChoiceId(new HashMap<Long, Integer>());

			// < maximumSum
			command.getSumByChoiceId().put(question.getChoices().get(0).getId(), 50);
			command.getSumByChoiceId().put(question.getChoices().get(1).getId(), null);
			command.getSumByChoiceId().put(question.getChoices().get(2).getId(), 50);

			validateAndExpect(command, 0, null, question);
		}
	}

	// ======================================================================

	@Test
	public void fail_UnknownQuestionType() {

		// some unknown question type
		@SuppressWarnings("serial")
		class SomeUnknownQuestionType extends Question {
		}

		SimpleAnswerCommand target = new SimpleAnswerCommand();
		Question question = new SomeUnknownQuestionType();

		validateAndExpect(target, 1, new String[] { GeneralValidationErrors.ANSWER_REQUIRED }, question);
	}

	// ======================================================================

	// ======================================================================
	// CHOICE question, NO CHOICES selected, TEST otherText
	// ======================================================================
	// @Test
	// public void choiceRequiredOtherTextAllowed_NoneSpecified() {
	// SimpleAnswerCommand target = new SimpleAnswerCommand();
	// Errors errors = new BindException(target, "cmd");
	//
	// // no choices selected, no other text specified
	// target.setChoiceIdList(new long[] {});
	// target.setOtherText("");
	//
	// Question question = getChoiceQuestionToValidate();
	// assertTrue(question.isRequired());
	//
	// //
	// question.setAllowOtherText(true);
	//
	// getValidator().validate(target, errors, question);
	// assertHasError(errors,
	// GeneralValidationErrors.ANSWER_OR_OTHER_TEXT_REQUIRED);
	// }
	//
	// @Test
	// public void choiceRequiredOtherTextAllowed_OtherTextSpecified() {
	// SimpleAnswerCommand target = new SimpleAnswerCommand();
	// Errors errors = new BindException(target, "cmd");
	//
	// // no choices selected, no other text specified
	// target.setChoiceIdList(new long[] {});
	// target.setOtherText("");
	//
	// Question question = getChoiceQuestionToValidate();
	// assertTrue(question.isRequired());
	//
	// //
	// question.setAllowOtherText(true);
	//
	// getValidator().validate(target, errors, question);
	// }
}
