package com.oas.model.question;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.util.Assert;

import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Multiple Choice question type.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "scale_question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class ScaleQuestion extends Question {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5278163376472358393L;

	/** Minimum value for the scale. */
	@Column(nullable = false)
	private Long minimum = 1L;

	@Column(nullable = false)
	/* Maximum value for the scale. */
	private Long maximum = 10L;

	// ======================================================================
	public ScaleQuestion() {
	}

	public ScaleQuestion(Survey survey) {
		super(survey);
	}

	public ScaleQuestion(Survey survey, Long minimum, Long maximum) {
		super(survey);
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public ScaleQuestion(Survey survey, Long minimum, Long maximum, Long displayOrder) {
		super(survey);
		setDisplayOrder(displayOrder);

		this.minimum = minimum;
		this.maximum = maximum;
	}

	// ======================================================================

	public long[] getPossibleValues() {

		Assert.notNull(minimum);
		Assert.notNull(maximum);

		int max = maximum.intValue() - minimum.intValue() + 1;
		long[] retval = new long[max];

		int count = 0;
		for (long i = minimum; i <= maximum; i++) {
			retval[count++] = i;
		}

		return retval;
	}

	// ======================================================================

	/**
	 * @return the minimum
	 */
	public Long getMinimum() {
		return minimum;
	}

	/**
	 * @param minimum
	 *            the minimum to set
	 */
	public void setMinimum(Long minimum) {
		this.minimum = minimum;
	}

	/**
	 * @return the maximum
	 */
	public Long getMaximum() {
		return maximum;
	}

	/**
	 * @param maximum
	 *            the maximum to set
	 */
	public void setMaximum(Long maximum) {
		this.maximum = maximum;
	}

}
