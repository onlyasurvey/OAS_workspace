package com.oas.model.question;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Text question type.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "text_question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class TextQuestion extends Question {

	@Column(name = "num_rows")
	private int numRows;

	@Column(name = "max_length")
	private int maximumLength;

	@Column(name = "field_display_length")
	private int fieldDisplayLength;

	// ======================================================================

	public TextQuestion() {
	}

	public TextQuestion(Survey survey) {
		super(survey);
	}

	public TextQuestion(Survey survey, int numRows, int maximumLength, int fieldDisplayLength) {
		super(survey);
		setNumRows(numRows);
		setMaximumLength(maximumLength);
		setFieldDisplayLength(fieldDisplayLength);
	}

	public TextQuestion(Survey survey, int numRows, int maximumLength, int fieldDisplayLength, long displayOrder) {
		super(survey);
		setNumRows(numRows);
		setMaximumLength(maximumLength);
		setFieldDisplayLength(fieldDisplayLength);
		setDisplayOrder(displayOrder);
	}

	// ======================================================================

	/**
	 * @return the numRows
	 */
	public final int getNumRows() {
		return numRows;
	}

	/**
	 * @param numRows
	 *            the numRows to set
	 */
	public final void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	/**
	 * @return the maximumLength
	 */
	public final int getMaximumLength() {
		return maximumLength;
	}

	/**
	 * @param maximumLength
	 *            the maximumLength to set
	 */
	public final void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	/**
	 * @return the fieldDisplayLength
	 */
	public final int getFieldDisplayLength() {
		return fieldDisplayLength;
	}

	/**
	 * @param fieldDisplayLength
	 *            the fieldDisplayLength to set
	 */
	public final void setFieldDisplayLength(int fieldDisplayLength) {
		this.fieldDisplayLength = fieldDisplayLength;
	}

}
