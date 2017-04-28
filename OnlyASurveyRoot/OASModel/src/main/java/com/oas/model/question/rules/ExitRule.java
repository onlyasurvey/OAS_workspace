package com.oas.model.question.rules;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.Choice;
import com.oas.model.Question;

/**
 * A "Exit Rule" for a question.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
@Entity
@Table(schema = "oas", name = "exit_rule")
@SequenceGenerator(name = "exitRuleSeq", sequenceName = "oas.exit_rule_id_seq")
public class ExitRule extends AbstractResourceModel implements BranchingRule, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 4913558141139744572L;

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "exitRuleSeq", strategy = GenerationType.SEQUENCE)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false, insertable = true, updatable = false)
	private Question question;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "choice_id", nullable = true)
	private Choice choice;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "jump_question_id", nullable = true)
	private Question jumpToQuestion;

	@Enumerated(EnumType.STRING)
	@Column(name = "rule_type")
	private ExitRuleType ruleType;

	@Enumerated(EnumType.STRING)
	@Column(name = "rule_action")
	private ExitRuleAction action;

	@Column(name = "apply_order")
	private int applyOrder;

	// ======================================================================

	/** Default constructor. */
	public ExitRule() {
	}

	/** Complex constructor. */
	public ExitRule(Question question, Choice choice, Question jumpToQuestion, ExitRuleType ruleType, ExitRuleAction action,
			int applyOrder) {
		setQuestion(question);
		setChoice(choice);
		setJumpToQuestion(jumpToQuestion);
		setRuleType(ruleType);
		setAction(action);
		setApplyOrder(applyOrder);
	}

	// ======================================================================

	@Override
	public boolean isEntryRule() {
		return false;
	}

	@Override
	public boolean isExitRule() {
		return true;
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

	/**
	 * @param question
	 *            the question to set
	 */
	public void setQuestion(Question question) {
		this.question = question;
	}

	/**
	 * @return the ruleType
	 */
	public ExitRuleType getRuleType() {
		return ruleType;
	}

	/**
	 * @param ruleType
	 *            the ruleType to set
	 */
	public void setRuleType(ExitRuleType ruleType) {
		this.ruleType = ruleType;
	}

	/**
	 * @return the action
	 */
	public ExitRuleAction getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(ExitRuleAction action) {
		this.action = action;
	}

	/**
	 * @return the applyOrder
	 */
	public int getApplyOrder() {
		return applyOrder;
	}

	/**
	 * @param applyOrder
	 *            the applyOrder to set
	 */
	public void setApplyOrder(int applyOrder) {
		this.applyOrder = applyOrder;
	}

	/**
	 * @return the choice
	 */
	public Choice getChoice() {
		return choice;
	}

	/**
	 * @param choice
	 *            the choice to set
	 */
	public void setChoice(Choice choice) {
		this.choice = choice;
	}

	/**
	 * @return the jumpToQuestion
	 */
	public Question getJumpToQuestion() {
		return jumpToQuestion;
	}

	/**
	 * @param jumpToQuestion
	 *            the jumpToQuestion to set
	 */
	public void setJumpToQuestion(Question jumpToQuestion) {
		this.jumpToQuestion = jumpToQuestion;
	}

	// ======================================================================

}
