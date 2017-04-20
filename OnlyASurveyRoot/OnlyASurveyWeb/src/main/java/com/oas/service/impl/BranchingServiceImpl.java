package com.oas.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.model.Answer;
import com.oas.model.BaseObject;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.question.rules.BranchingRule;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.EntryRuleAction;
import com.oas.model.question.rules.EntryRuleType;
import com.oas.model.question.rules.ExitRule;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;
import com.oas.security.SecurityAssertions;
import com.oas.service.BranchingService;

/**
 * Implementation of <code>BranchingService</code>.
 * 
 * @author xhalliday
 * @since March 10, 2009
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class BranchingServiceImpl extends AbstractServiceImpl implements BranchingService {

	/** General domain model service. */
	// @Autowired
	// private DomainModelService domainModelService;
	// ======================================================================
	@Override
	@Unsecured
	public List<EntryRule> findEntryRules(Question question) {
		Assert.notNull(question);
		return find("from EntryRule where question = ?", question);
	}

	@Override
	@Unsecured
	public List<ExitRule> findExitRules(Question question) {
		Assert.notNull(question);
		return find("from ExitRule where question = ?", question);
	}

	// ======================================================================

	@Override
	@Unsecured
	public EntryRule findEntryRule(Long entryRuleId) {
		return load(EntryRule.class, entryRuleId);
	}

	@Override
	@Unsecured
	public ExitRule findExitRule(Long exitRuleId) {
		return load(ExitRule.class, exitRuleId);
	}

	@Override
	@Unsecured
	public List<Question> findReferences(Question question) {

		Set<Question> set = new HashSet<Question>();

		List<Question> entry = find(
				"select a.question from EntryRule a where a.otherObject = ? order by a.question.displayOrder", question);

		List<Question> entry2 = find(
				"select a.question from EntryRule a where a.otherObject in ( from Choice where question = ? ) "
						+ " order by a.question.displayOrder", question);
		List<Question> exit = find(
				"select a.question from ExitRule a where a.jumpToQuestion = ? order by a.question.displayOrder", question);

		set.addAll(entry);
		set.addAll(entry2);
		set.addAll(exit);

		List<Question> retval = new ArrayList<Question>(set);

		Collections.sort(retval);

		return retval;
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void clearRules(Question question) {
		// valid input
		Assert.notNull(question);

		// security check
		SecurityAssertions.assertOwnership(question.getSurvey());

		// TODO this is slower, is it really safer?
		getHibernateTemplate().deleteAll(findEntryRules(question));
		getHibernateTemplate().deleteAll(findExitRules(question));
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteRule(BranchingRule rule) {
		//
		Assert.notNull(rule);

		// security check
		SecurityAssertions.assertOwnership(rule.getQuestion().getSurvey());

		log.info("delete rule #" + rule.getId() + " on question#" + rule.getQuestion().getId() + " by user #"
				+ getCurrentUser().getId());

		// work
		delete(rule);
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public EntryRule createEntryRule(Question question, EntryRuleType type, EntryRuleAction action, Long otherObjectId) {

		Assert.notNull(question);
		Assert.notNull(type);
		Assert.notNull(action);

		SecurityAssertions.assertOwnership(question.getSurvey());

		BaseObject otherObject = null;
		if (otherObjectId != null && !EntryRuleType.DEFAULT.equals(type)) {

			// Class whatIs = domainModelService.whatIs(otherObjectId);
			// Assert.notNull(whatIs, "unknown class");

			otherObject = get(BaseObject.class, otherObjectId);
			Assert.notNull(otherObject);

			if (Question.class.isAssignableFrom(otherObject.getClass())) {
				SecurityAssertions.assertOwnership(((Question) otherObject).getSurvey());
			} else if (Choice.class.isAssignableFrom(otherObject.getClass())) {
				SecurityAssertions.assertOwnership(((Choice) otherObject).getQuestion().getSurvey());
			} else {
				throw new IllegalArgumentException("unsupported object type: " + otherObject.getClass());
			}
		}

		//
		EntryRule rule = new EntryRule(question, otherObject, type, action, getNextRuleApplyOrder(EntryRule.class, question));
		persist(rule);

		return rule;
	}

	protected int getNextRuleApplyOrder(final Class<? extends BranchingRule> clazz, final Question question) {

		Assert.notNull(clazz);
		Assert.notNull(question);

		int retval = -1;

		BranchingRule rule = (BranchingRule) execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria crit = session.createCriteria(clazz);
				crit.addOrder(Order.desc("applyOrder"));
				crit.add(Restrictions.eq("question", question));
				crit.setMaxResults(1);

				return crit.uniqueResult();
			}
		});

		if (rule != null) {
			// get next index
			retval = rule.getApplyOrder() + 1;
		} else {
			// the start index
			retval = 0;
		}

		return retval;
	}

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public ExitRule createExitRule(Question question, ExitRuleType type, ExitRuleAction action, Long jumpToQuestionId,
			Long choiceId) {
		//
		Assert.notNull(question);
		Assert.notNull(type);
		Assert.notNull(action);

		SecurityAssertions.assertOwnership(question.getSurvey());

		//
		Choice choice = null;

		//
		Question jumpTo = null;
		if (jumpToQuestionId != null && ExitRuleAction.JUMP_TO_QUESTION.equals(action)) {
			jumpTo = get(Question.class, jumpToQuestionId);
			SecurityAssertions.assertOwnership(jumpTo.getSurvey());
			Assert.isTrue(question.getSurvey().getId().equals(jumpTo.getSurvey().getId()), "cannot jump to another survey");
		}

		//
		if (type.isChoiceType()) {
			if (choiceId != null) {
				choice = get(Choice.class, choiceId);
				Assert.notNull(choice);
				SecurityAssertions.assertOwnership(choice.getQuestion().getSurvey());
			}
		}

		//
		ExitRule rule = new ExitRule(question, choice, jumpTo, type, action, getNextRuleApplyOrder(EntryRule.class, question));
		persist(rule);

		//
		return rule;
	}

	// ======================================================================

	@Override
	@Unsecured
	public Question nextQuestion(Question afterThisOne, Response response) {

		Assert.notNull(afterThisOne);
		Assert.notNull(response);

		// may be empty
		List<Answer> answerList = find("from Answer where question = ? and response = ?", new Object[] { afterThisOne, response });
		Assert.notNull(answerList);

		//
		Question retval = null;

		//
		ExitRule ruleToApply = findApplicableExitRule(afterThisOne, answerList);

		List<Question> allQuestions = afterThisOne.getSurvey().getQuestions();

		// if no exit rules applied then find the next question in sequence
		if (ruleToApply == null) {
			//
			int order = allQuestions.indexOf(afterThisOne);

			// next may or may not be null, indicating a next question or that
			// this was the last one
			int listSize = allQuestions.size();
			int newOrder = order + 1;
			if (listSize - 1 >= newOrder) {
				retval = allQuestions.get(newOrder);
			} // else: retval is null, no next question

			//
		} else {
			// apply the exit rule to determine which is next
			retval = applyExitRule(afterThisOne, ruleToApply);
		}

		// resolve the final Next Question by inspecting any entry rules
		// recursively
		if (retval != null) {
			retval = applyEntryRules(retval, response);
		}

		//
		return retval;
	}

	private ExitRule findApplicableExitRule(Question question, List<Answer> answerList) {

		//
		ExitRule retval = null;

		// a default rule
		ExitRule defaultRule = null;

		// find all rules
		List<ExitRule> exitRules = findExitRules(question);
		for (ExitRule rule : exitRules) {

			// should this rule be marked as matching?
			boolean match = false;

			//
			ExitRuleType type = rule.getRuleType();

			if (ExitRuleType.HAS_ANSWER.equals(type) && !answerList.isEmpty()) {
				match = true;
			}

			if (ExitRuleType.NO_ANSWER.equals(type) && answerList.isEmpty()) {
				match = true;
			}

			if (ExitRuleType.CHOICE_ON.equals(type)) {

				Choice choice = rule.getChoice();
				Assert.notNull(choice, "illegal rule: match choice-on but no choice is specified");

				for (Answer answer : answerList) {
					if (answer.isChoiceAnswer()) {
						ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
						if (choice.getId().equals(choiceAnswer.getValue().getId())) {
							// user did select this answer
							match = true;
						}
					}
				}
			}

			if (ExitRuleType.CHOICE_OFF.equals(type)) {

				Choice choice = rule.getChoice();
				Assert.notNull(choice, "illegal rule: match choice-off but no choice is specified");
				boolean didAnswer = false;

				for (Answer answer : answerList) {
					if (answer.isChoiceAnswer()) {
						ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
						if (choice.getId().equals(choiceAnswer.getValue().getId())) {
							// user did select this answer
							didAnswer = true;
						}
					}
				}

				// match if this choice was not selected
				match = (!didAnswer);
			}

			if (ExitRuleType.DEFAULT.equals(type)) {

				// store a reference: this doesn't actually match as a rule
				// proper, it's only used if none of the rules inspected herein
				// match
				defaultRule = rule;
			}

			if (match) {
				// we have our match
				retval = rule;
				break;
			}
		}

		// if no specific match, then set the variable to the default, if any
		if (retval == null) {
			retval = defaultRule;
		}

		return retval;
	}

	protected Question applyExitRule(Question question, ExitRule ruleToApply) {

		Question retval = null;

		ExitRuleAction action = ruleToApply.getAction();
		if (ExitRuleAction.FORCE_FINISH.equals(action)) {
			// end the survey - go to thanks
			// null return value indicates end of survey to caller
			retval = null;
		}

		if (ExitRuleAction.JUMP_TO_QUESTION.equals(action)) {
			//
			Question jumpTo = ruleToApply.getJumpToQuestion();
			Assert.notNull(jumpTo, "illegal rule: type = jumpToQuestion but target question is empty");

			// apply skipping rules to the jumpTo question

			// security/sanity
			Assert.isTrue(jumpTo.getSurvey().getId().equals(question.getSurvey().getId()),
					"illegal rule: jumps to question in another survey");

			//
			retval = ruleToApply.getJumpToQuestion();
		}

		//
		return retval;
	}

	protected Question applyEntryRules(Question question, Response response) {
		Assert.notNull(question);

		// rules
		EntryRule match = null;
		EntryRule defaultRule = null;

		// for all entry rules
		List<EntryRule> entryRules = findEntryRules(question);
		for (EntryRule rule : entryRules) {

			// determine if this question matches an entry rule here - if so and
			// it's action != SHOW_QUESTION then recurse to the next question

			EntryRuleType type = rule.getRuleType();

			if (EntryRuleType.DEFAULT.equals(type)) {

				// set default for when nothing else matches
				defaultRule = rule;

			} else if (EntryRuleType.OTHER_ANSWERED.equals(type)) {
				//
				BaseObject other = rule.getOtherObject();

				if (hasAnswerToOther(response, other)) {
					// OTHER_EMPTY and hasAnswerToOther makes this a match
					match = rule;
					break;
				}

			} else if (EntryRuleType.OTHER_EMPTY.equals(type)) {
				//
				BaseObject other = rule.getOtherObject();

				if (!hasAnswerToOther(response, other)) {
					// OTHER_EMPTY and !hasAnswerToOther makes this a match
					match = rule;
					break;
				}
			} else {
				//
				log.error("unknown entry rule type: " + type);
				throw new IllegalArgumentException("unknown entry rule type");
			}
		}

		// if there are no matches at all then we always show the question
		EntryRuleAction finalAction = EntryRuleAction.SHOW_QUESTION;

		// if there is a specific, non-default matching rule
		if (match != null) {
			// match!

			Assert.isTrue(!EntryRuleType.DEFAULT.equals(match.getRuleType()), "illegal rule: matched on default!");

			// set the final action as resolving to the match's action
			finalAction = match.getAction();

		} else if (defaultRule != null) {
			//
			// fall back to default rule, if any
			//
			finalAction = defaultRule.getAction();
		}

		//
		// what is the final action to take?
		//
		if (EntryRuleAction.SHOW_QUESTION.equals(finalAction)) {

			// show this question - we can return immediately
			return question;

		} else if (EntryRuleAction.SKIP_QUESTION.equals(finalAction)) {

			// parent list of questions
			List<Question> allQuestions = question.getSurvey().getQuestions();

			// this question's index in parent's list
			int index = allQuestions.indexOf(question);

			// are there any further questions, sequentially? If so,
			// recurse, otherwise leave retval = null
			Question sequentiallyNext = null;
			if (allQuestions.size() > (index + 1)) {
				sequentiallyNext = allQuestions.get(index + 1);
			}

			//
			if (sequentiallyNext != null) {
				// recurse
				return applyEntryRules(sequentiallyNext, response);
			} else {
				// no next question - this indicates
				return null;
			}
		} else {
			//
			// error here - likely a regression - should always be set to a
			// default above where it is declared
			// 
			log.error("unknown entry rule action: " + finalAction);
			throw new IllegalArgumentException("illegal rule: unknown action");
		}
	}

	/**
	 * Determine if a Question or Choice (the "other" object) has an answer in
	 * the given response.
	 * 
	 * @param response
	 * @param other
	 * @return
	 */
	private boolean hasAnswerToOther(Response response, BaseObject other) {

		if (other.isChoiceType()) {

			// check that a particular choice has an answer for the response
			Long count = (Long) unique(find("select count(*) from ChoiceAnswer where response = ? and value = ?", new Object[] {
					response, other }));

			return count != 0;

		} else if (other.isQuestionType()) {

			// just check that any answer was provided, regardless of it's type
			Long count = (Long) unique(find("select count(*) from Answer where response = ? and question = ?", new Object[] {
					response, other }));

			return count != 0;

		} else {
			log.error("unknown other-object-type: #" + other.getId() + " class=" + other.getClass().getName());
			throw new IllegalArgumentException("unknown other-object-type");
		}
	}

}
