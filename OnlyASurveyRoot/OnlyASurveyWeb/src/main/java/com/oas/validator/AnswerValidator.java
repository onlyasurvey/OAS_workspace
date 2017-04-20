package com.oas.validator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.util.Constants;
import com.oas.util.GeneralValidationErrors;

/**
 * Validate answer data from the user.
 * 
 * @author xhalliday
 * @since September 10, 2008
 */
@Component("answerValidator")
public class AnswerValidator implements Validator {

	/** Field for general answer value. */
	private static final String FIELD_ANSWER_TEXT = "answer";

	/** Field for "Other" text. */
	private static final String FIELD_OTHER_TEXT = "otherText";

	/** Field for multiple-choice answers. */
	private static final String FIELD_ANSWER_CHOICE_ID_LIST = "choiceIdList";

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		boolean retval = clazz.isAssignableFrom(SimpleAnswerCommand.class);
		return retval;
	}

	@Override
	public void validate(Object target, Errors errors) {
		throw new UnsupportedOperationException("calling default validate method unsupported");
	}

	public void validate(Object target, Errors errors, Question question) {

		String answerText = (String) errors.getFieldValue(FIELD_ANSWER_TEXT);

		if (question.isTextQuestion()) {
			//
			validateTextAnswer(answerText, errors, (TextQuestion) question);
		} else if (question.isChoiceQuestion()) {
			//
			ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
			if (choiceQuestion.isSummingQuestion()) {
				// summing multiple-choice
				validateSummingAnswer(((SimpleAnswerCommand) target).getSumByChoiceId(), choiceQuestion, errors);
			} else {
				// typical multiple-choice
				long[] choiceIdList = (long[]) errors.getFieldValue(FIELD_ANSWER_CHOICE_ID_LIST);
				validateChoiceIdListAnswer(choiceIdList, (String) errors.getFieldValue(FIELD_OTHER_TEXT), errors, choiceQuestion);
			}
		} else if (question.isPageQuestion()) {
			// no validation for "Page" type

		} else if (question.isScaleQuestion()) {
			// long[] idList = (long[])
			// errors.getFieldValue(FIELD_ANSWER_CHOICE_ID_LIST);
			validateScaleAnswer(answerText, errors, (ScaleQuestion) question);
		} else {
			// TODO revisit this decision
			// generic error
			errors.reject(GeneralValidationErrors.ANSWER_REQUIRED);
		}
	}

	protected void validateTextAnswer(String answerText, Errors errors, TextQuestion question) {
		if (question.isRequired()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, FIELD_ANSWER_TEXT, GeneralValidationErrors.ANSWER_REQUIRED);
		}

		// check for strings which are too long
		if (StringUtils.hasText(answerText)) {
			if (answerText.length() > question.getMaximumLength()) {
				errors.rejectValue(FIELD_ANSWER_TEXT, "question.error.answerTooLong",
						new Object[] { question.getMaximumLength() }, "Answer is too long.");
			}
		}
	}

	protected void validateScaleAnswer(String answer, Errors errors, ScaleQuestion question) {

		if (StringUtils.hasText(answer)) {

			try {
				long value = Long.valueOf(answer);
				if (value < question.getMinimum() || value > question.getMaximum()) {
					// illegal argument: UI never allows this
					errors.rejectValue("choiceIdList", "illegalArgument");
					return;
				}
			} catch (NumberFormatException e) {
				// not a number
				errors.rejectValue("choiceIdList", "illegalArgument");
				return;
			}

			// value is valid
		} else {
			// required but missing
			if (question.isRequired()) {
				errors.rejectValue("choiceIdList", GeneralValidationErrors.ANSWER_REQUIRED);
			}
		}
	}

	protected void validateChoiceIdListAnswer(long[] choiceIdList, String otherText, Errors errors, ChoiceQuestion question) {
		Assert.notNull(choiceIdList, "caller must provide non-null ID list");

		// check: single-selection allowed but both choice and Other Text
		// provided
		if ((!question.isUnlimited()) && choiceIdList.length == 1 && StringUtils.hasText(otherText)) {
			if (choiceIdList[0] != Constants.OTHER_TEXT_ID) {
				//
				errors.rejectValue(FIELD_ANSWER_CHOICE_ID_LIST,
						GeneralValidationErrors.ANSWER_OR_OTHER_TEXT_REQUIRED_BUT_NOT_BOTH);

				// no further validation possible
				return;
			}
		}

		if (question.isRequired()) {

			//
			if (isNoneOfTheAbove(choiceIdList)) {
				// required, but "none of the above" selected
				// should not be possible
				errors.rejectValue(FIELD_ANSWER_CHOICE_ID_LIST, "illegalArgument");
			}

			if (isEmptyOrOther(choiceIdList)) {
				// if the question allows Other text, and user did not select
				// any choices but did enter Other text, then it's valid
				if (question.isAllowOtherText()) {
					// if other text specified, let it go, otherwise show a
					// customized error indicating it's availability
					ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherText",
							GeneralValidationErrors.ANSWER_OR_OTHER_TEXT_REQUIRED);
				} else {
					errors.rejectValue(FIELD_ANSWER_CHOICE_ID_LIST, GeneralValidationErrors.ANSWER_REQUIRED);
					// no further validation possible
					return;
				}

			} else {
				// has selections
			}
		} else {
			// no selection required
		}

		if (StringUtils.hasText(otherText)) {
			if (otherText.length() > Constants.MAXIMUM_OTHER_TEXT_LENGTH) {
				errors.rejectValue(FIELD_ANSWER_TEXT, "question.error.answerTooLong",
						new Object[] { Constants.MAXIMUM_OTHER_TEXT_LENGTH }, "Other Text is too long");
			}
		}

		// check: single-select but multiple values passed
		if ((!question.isUnlimited()) && choiceIdList.length > 1) {
			// UI prevents this, it's illegal input
			errors.rejectValue(FIELD_ANSWER_CHOICE_ID_LIST, "illegalArgument");
		}
	}

	protected void validateSummingAnswer(Map<Long, Integer> map, ChoiceQuestion question, Errors errors) {

		Assert.notNull(map);
		Assert.notNull(question);
		Assert.notNull(errors);
		Assert.isTrue(question.isSummingQuestion(), "not a summing question");

		int totalSum = 0;

		Map<Long, Choice> choiceMap = new HashMap<Long, Choice>();
		for (Choice choice : question.getChoices()) {
			choiceMap.put(choice.getId(), choice);
		}

		for (Long id : map.keySet()) {

			Assert.isTrue(choiceMap.containsKey(id), "choice specified from another question");

			Integer value = map.get(id);
			if (value != null) {
				if (value < 0) {
					// illegal argument
					errors.reject("question.error.negativeNumbersNotAllowed");

					// ignore further processing: summing results will be
					// invalid
					return;
				}

				totalSum += value;
			}

		}

		if (question.isRequired() && totalSum != question.getMaximumSum()) {
			errors.reject(GeneralValidationErrors.ANSWER_SUMS_LESS_THAN_TOTAL_REQUIRED, new String[] { question.getMaximumSum()
					.toString() }, "Please distribute all points.");
		} else if (totalSum > question.getMaximumSum()) {
			errors.reject(GeneralValidationErrors.ANSWER_SUMS_MORE_THAN_TOTAL_REQUIRED, new String[] { question.getMaximumSum()
					.toString() }, "Please distribute all points.");
		}
	}

	private boolean isNoneOfTheAbove(long[] idList) {
		Assert.notNull(idList);
		// one item and it's -2 - NONE OF THE ABOVE
		return (idList.length == 1 && idList[0] == Constants.NONE_OF_THE_ABOVE_ID);
	}

	private boolean isEmptyOrOther(long[] idList) {
		Assert.notNull(idList);
		// empty list, or one item and it's -1 - OTHER
		return (idList.length == 0 || (idList.length == 1 && idList[0] == Constants.OTHER_TEXT_ID));
	}
}
