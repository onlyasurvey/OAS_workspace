package com.oas.util;

abstract public class GeneralValidationErrors {

	/** An answer is required for the question. */
	public static final String ANSWER_REQUIRED = "question.error.answerRequired";

	/** An answer (or Other text) is required for the question. */
	public static final String ANSWER_OR_OTHER_TEXT_REQUIRED = "question.error.answerOrOtherTextRequired";

	/** An answer (or Other text) is required for the question BUT NOT BOTH. */
	public static final String ANSWER_OR_OTHER_TEXT_REQUIRED_BUT_NOT_BOTH = "question.error.answerOrOtherTextRequiredButNotBoth";

	/**
	 * The sum of all numeric values provided to a summing question are less
	 * than the total required.
	 */
	public static final String ANSWER_SUMS_LESS_THAN_TOTAL_REQUIRED = "question.error.sumsLessThanTotalRequired";

	/**
	 * The sum of all numeric values provided to a summing question are more
	 * than the total required.
	 */
	public static final String ANSWER_SUMS_MORE_THAN_TOTAL_REQUIRED = "question.error.sumsMoreThanTotalRequired";

	/** Negative numbers are not allowed here. */
	public static final String NEGATIVE_NUMBERS_NOT_ALLOWED = "question.error.negativeNumbersNotAllowed";

	/** Invalid data was sent to the server. */
	public static final String ILLEGAL_ARGUMENT = "illegalArgumentException";
}
