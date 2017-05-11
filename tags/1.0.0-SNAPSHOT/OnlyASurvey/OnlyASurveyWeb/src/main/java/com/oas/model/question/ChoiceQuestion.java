package com.oas.model.question;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Multiple Choice question type.
 * 
 * @author xhalliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "choice_question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class ChoiceQuestion extends Question {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5960673670127629146L;

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "question", fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@JoinColumn(name = "question_id")
	@OrderBy("displayOrder ASC")
	// @IndexColumn(name = "display_order", base = 0, nullable = false)
	private List<Choice> choices = new ArrayList<Choice>();

	/** Whether the question allows unlimited choices to be selected. */
	private boolean unlimited;

	/**
	 * If the question allows for sum answers, this is the maximum. If null then
	 * no summing is supported.
	 */
	@Column(name = "maximum_sum")
	private Integer maximumSum;

	// ======================================================================

	public ChoiceQuestion() {
	}

	public ChoiceQuestion(Survey survey) {
		super(survey);
	}

	/**
	 * Convenience constructor for test cases to use.
	 * 
	 * @param survey
	 * @param choices
	 */
	public ChoiceQuestion(Survey survey, List<Choice> choices) {
		super(survey);
		getChoices().addAll(choices);
	}

	public ChoiceQuestion(Survey survey, Long displayOrder) {
		super(survey);
		setDisplayOrder(displayOrder);
	}

	// ======================================================================

	public boolean isSummingQuestion() {
		return maximumSum != null;
	}

	// ======================================================================

	/**
	 * @return the choices
	 */
	public List<Choice> getChoices() {
		return choices;
	}

	/**
	 * @param choice
	 *            the choice to add
	 */
	public void addChoice(Choice choice) {
		choices.add(choice);
	}

	/**
	 * @return the unlimited
	 */
	public boolean isUnlimited() {
		return unlimited;
	}

	/**
	 * @param unlimited
	 *            the unlimited to set
	 */
	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
	 * @return the maximumSum
	 */
	public Integer getMaximumSum() {
		return maximumSum;
	}

	/**
	 * @param maximumSum
	 *            the maximumSum to set
	 */
	public void setMaximumSum(Integer maximumSum) {
		this.maximumSum = maximumSum;
	}
}
