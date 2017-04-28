package com.oas.model.question.rules;

import com.oas.model.Question;

/**
 * Convenience interface for branching rules.
 * 
 * @author xhalliday
 * @since March 14, 2009
 */
public interface BranchingRule {

	/**
	 * ID of the rule.
	 * 
	 * @return Long
	 */
	Long getId();

	/**
	 * The Question that this rule applies to.
	 * 
	 * @return Question
	 */
	Question getQuestion();

	/**
	 * Is this rule an Entry Rule?
	 * 
	 * @return boolean
	 */
	boolean isEntryRule();

	/**
	 * Is this rule an Exit Rule?
	 * 
	 * @return boolean
	 */
	boolean isExitRule();

	/**
	 * The order in which this rule is to be applied.
	 * 
	 * @return int
	 */
	int getApplyOrder();

}
