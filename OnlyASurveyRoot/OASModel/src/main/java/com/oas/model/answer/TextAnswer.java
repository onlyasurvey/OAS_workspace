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
@Table(schema = "oas", name = "text_answer")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class TextAnswer extends Answer {

	@Column(name = "answer_value")
	private String value;

	// ======================================================================

	public TextAnswer() {
	}

	public TextAnswer(Response response, Question question, String value) {
		super(response, question);
		setValue(value);
	}

	// ======================================================================

	@Override
	public String getSimpleValue() {
		// property is not null
		Assert.notNull(value);

		// TODO i18n: if question.isMultilingual
		return value;
	}

	// ======================================================================
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
