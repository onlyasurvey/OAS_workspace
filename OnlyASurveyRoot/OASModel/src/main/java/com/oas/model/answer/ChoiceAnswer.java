package com.oas.model.answer;

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

import org.springframework.util.Assert;

import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;

/**
 * Answer that ties a Choice to a Response.
 * 
 * @author xhalliday
 */
@Entity
@Table(schema = "oas", name = "choice_answer")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class ChoiceAnswer extends Answer {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 4126360300706698189L;

	/**
	 * Reference to a Choice that was selected.
	 */
	@OneToOne
	@JoinColumn(name = "answer_value", nullable = false)
	private Choice value;

	/**
	 * Value used for choice questions that ask for a sum (eg., Constant Sum).
	 */
	@Column(name = "sum_value")
	private Integer sumValue;

	// ======================================================================

	public ChoiceAnswer() {
	}

	public ChoiceAnswer(Response response, Question question, Choice value) {
		super(response, question);
		setValue(value);
	}

	// ======================================================================

	@Override
	public String getSimpleValue() {
		// property is not null
		Assert.notNull(value);

		// multilingual
		return value.getDisplayTitle();
	}

	@Override
	public String getDisplayTitle() {
		// property is not null
		Assert.notNull(value);

		// delegate to named choice object
		return getValue().getDisplayTitle();
	}

	// ======================================================================

	/**
	 * @return the value
	 */
	public Choice getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Choice value) {
		this.value = value;
	}

	/**
	 * @return the sumValue
	 */
	public Integer getSumValue() {
		return sumValue;
	}

	/**
	 * @param sumValue
	 *            the sumValue to set
	 */
	public void setSumValue(Integer sumValue) {
		this.sumValue = sumValue;
	}
}
