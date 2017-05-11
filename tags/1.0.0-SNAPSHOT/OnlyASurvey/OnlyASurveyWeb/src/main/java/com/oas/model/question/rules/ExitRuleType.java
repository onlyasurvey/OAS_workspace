package com.oas.model.question.rules;

/**
 * Type enum for "Exit Rule" for a question.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
public enum ExitRuleType {

	/** This rule sets the default. */
	DEFAULT,

	/** Match when this question has an answer. */
	HAS_ANSWER,

	/** Match when this question has no answer (including "other" text). */
	NO_ANSWER,

	/** Match when a choice on this question was selected. */
	CHOICE_ON,

	/** Match when a choice on this question was NOT selected. */
	CHOICE_OFF;

	// ======================================================================

	/**
	 * Is this rule type related to choices?
	 */
	public boolean isChoiceType() {
		return equals(CHOICE_ON) || equals(CHOICE_OFF);
	}

}
