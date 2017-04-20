package com.oas.model.answer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.util.Assert;

import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;

@Entity
@Table(schema = "oas", name = "boolean_answer")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class BooleanAnswer extends Answer {

	@Column(name = "answer_value")
	private Boolean value;

	// ======================================================================
	public BooleanAnswer() {
	}

	public BooleanAnswer(Response response, Question question, Boolean value) {
		super(response, question);
		setValue(value);
	}

	// ======================================================================

	@Override
	public String getSimpleValue() {
		// property is not null
		Assert.notNull(value);

		// TODO i18n
		return value.toString();
	}

	// ======================================================================

	/**
	 * @return the value
	 */
	public Boolean getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Boolean value) {
		this.value = value;
	}
}
