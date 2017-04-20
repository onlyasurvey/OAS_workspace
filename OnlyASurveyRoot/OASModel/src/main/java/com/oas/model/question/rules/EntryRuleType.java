package com.oas.model.question.rules;

/**
 * Type enum for "Entry Rule" for a question.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
public enum EntryRuleType {

	/** Default. */
	DEFAULT,

	/** Another object (Question, Choice) was answered. */
	OTHER_ANSWERED,

	/** Another object (Question, Choice) was NOT answered. */
	OTHER_EMPTY;

}
