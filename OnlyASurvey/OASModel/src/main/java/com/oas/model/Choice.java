package com.oas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.oas.model.question.ChoiceQuestion;

@Entity
@Table(schema = "oas", name = "choice")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Choice extends BaseObject implements Comparable<Choice> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3388447419814033048L;

	@OneToOne
	@JoinColumn(name = "question_id", nullable = false)
	private ChoiceQuestion question;

	@Column(name = "display_order", nullable = false)
	private Long displayOrder;

	// ======================================================================
	public Choice() {
	}

	public Choice(ChoiceQuestion question, Long displayOrder) {
		super();
		this.question = question;
		this.displayOrder = displayOrder;
	}

	// ======================================================================
	@Override
	public int compareTo(Choice o) {

		return getDisplayOrder().compareTo(o.getDisplayOrder());
	}

	// ======================================================================

	/**
	 * @return the question
	 */
	public ChoiceQuestion getQuestion() {
		return question;
	}

	/**
	 * @param question
	 *            the question to set
	 */
	// public void setQuestion(Question question) {
	// this.question = question;
	// }
	/**
	 * @return the displayOrder
	 */
	public Long getDisplayOrder() {
		return displayOrder;
		// return Long.valueOf(question.getChoices().indexOf(this));
	}

	/**
	 * @param displayOrder
	 *            the displayOrder to set
	 */
	public void setDisplayOrder(Long displayOrder) {
		this.displayOrder = displayOrder;
	}

	/**
	 * @param displayOrder
	 *            the displayOrder to set
	 */
	// public void setDisplayOrder(Long displayOrder) {
	// this.displayOrder = displayOrder;
	// }
}
