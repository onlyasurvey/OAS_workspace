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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.BaseObject;
import com.oas.model.Question;

/**
 * A "Entry Rule" for a question.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
@Entity
@Table(schema = "oas", name = "entry_rule")
@SequenceGenerator(name = "entryRuleSeq", sequenceName = "oas.entry_rule_id_seq")
public class EntryRule extends AbstractResourceModel implements BranchingRule, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 5344266294824142177L;

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "entryRuleSeq", strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false, insertable = true, updatable = false)
	private Question question;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "other_object_id", nullable = true)
	private BaseObject otherObject;

	@Enumerated(EnumType.STRING)
	@Column(name = "rule_type")
	private EntryRuleType ruleType;

	@Enumerated(EnumType.STRING)
	@Column(name = "rule_action")
	private EntryRuleAction action;

	@Column(name = "apply_order")
	private int applyOrder;

	// ======================================================================

	/** Default constructor. */
	public EntryRule() {
	}

	/** Complex constructor. */
	public EntryRule(Question question, BaseObject otherObject, EntryRuleType ruleType, EntryRuleAction action, int applyOrder) {
		setQuestion(question);
		setOtherObject(otherObject);
		setRuleType(ruleType);
		setAction(action);
		setApplyOrder(applyOrder);
	}

	// ======================================================================

	@Override
	public boolean isEntryRule() {
		return true;
	}

	@Override
	public boolean isExitRule() {
		return false;
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
	 * @return the otherObject
	 */
	public BaseObject getOtherObject() {
		return otherObject;
	}

	/**
	 * @param otherObject
	 *            the otherObject to set
	 */
	public void setOtherObject(BaseObject otherObject) {
		this.otherObject = otherObject;
	}

	/**
	 * @return the ruleType
	 */
	public EntryRuleType getRuleType() {
		return ruleType;
	}

	/**
	 * @param ruleType
	 *            the ruleType to set
	 */
	public void setRuleType(EntryRuleType ruleType) {
		this.ruleType = ruleType;
	}

	/**
	 * @return the action
	 */
	public EntryRuleAction getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(EntryRuleAction action) {
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

	// ======================================================================

}
