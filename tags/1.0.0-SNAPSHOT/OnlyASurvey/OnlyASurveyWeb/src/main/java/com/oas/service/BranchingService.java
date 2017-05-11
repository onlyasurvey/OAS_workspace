package com.oas.service;

import java.util.List;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.question.rules.BranchingRule;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.EntryRuleAction;
import com.oas.model.question.rules.EntryRuleType;
import com.oas.model.question.rules.ExitRule;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;

/**
 * Service for managing branching configuration and applying rules.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
public interface BranchingService extends AbstractServiceInterface {

	// ======================================================================
	// FIND RULES
	// ======================================================================

	/**
	 * Find all Entry Rules pertaining to this Question.
	 * 
	 * @param question
	 * @return
	 */
	List<EntryRule> findEntryRules(Question question);

	/**
	 * Find all Exit Rules pertaining to this Question.
	 * 
	 * @param question
	 * @return
	 */
	List<ExitRule> findExitRules(Question question);

	/**
	 * Load a particular Entry Rule.
	 * 
	 * @param findEntryRule
	 * @return
	 */
	EntryRule findEntryRule(Long entryRuleId);

	/**
	 * Load a particular Exit Rule.
	 * 
	 * @param findEntryRule
	 * @return
	 */
	ExitRule findExitRule(Long exitRuleId);

	/**
	 * Find BranchingRule's (EntryRule and ExitRule) that reference a particular
	 * question, eg., by checking their answers or jumping to them.
	 * 
	 * @param question
	 * @return
	 */
	List<Question> findReferences(Question question);

	// ======================================================================
	// CREATE RULES
	// ======================================================================

	/**
	 * Create an Entry Rule.
	 * 
	 * @param question
	 * @param type
	 * @param action
	 * @param otherObjectId
	 * 
	 * @return EntryRule
	 */
	EntryRule createEntryRule(Question question, EntryRuleType type, EntryRuleAction action, Long otherObjectId);

	/**
	 * Create an Exit Rule.
	 * 
	 * @param question
	 * @param type
	 * @param action
	 * @param jumpToQuestionId
	 * @param choiceId
	 * 
	 * @return ExitRule
	 */
	ExitRule createExitRule(Question question, ExitRuleType type, ExitRuleAction action, Long jumpToQuestionId, Long choiceId);

	// ======================================================================
	// REMOVE RULES
	// ======================================================================

	/**
	 * Clear all rules associated with the Question.
	 */
	void clearRules(Question question);

	/**
	 * Delete a rule.
	 * 
	 * @param rule
	 */
	void deleteRule(BranchingRule rule);

	// ======================================================================
	// APPLY RULES
	// ======================================================================

	/**
	 * Determine the next question after the one specified here is completed,
	 * using Entry and Exit rules of both this question and subsequent questions
	 * (ie use Exit rules and subsequent Entry rules).
	 * 
	 * @param afterThisOne
	 *            The question whose next sibling is to be found
	 * @param response
	 *            The response to retrieve answers from
	 * @return Question or null if specified question is the last one
	 */
	Question nextQuestion(Question afterThisOne, Response response);

	/**
	 * Determine if the specified question should be skipped given it's
	 * EntryRule list
	 * 
	 * @param question
	 *            Question to compare to the response
	 * @param response
	 *            The response to retrieve answers from
	 * 
	 * @return boolean
	 */
	boolean skipQuestion(Question question, Response response);

}
