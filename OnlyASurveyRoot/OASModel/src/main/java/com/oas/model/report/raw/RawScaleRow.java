package com.oas.model.report.raw;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.oas.model.answer.TextAnswer;

/**
 * Raw {@link TextAnswer} row for a raw Excel export.
 * 
 * @author xhalliday
 * @since September 26, 2009
 */
@Entity
@Table(schema = "oas", name = "vw_raw_scale")
@BatchSize(size = 25)
public class RawScaleRow extends AbstractRawRow {

	/** Serialization ID. */
	private static final long serialVersionUID = -3643209823303618798L;

	/** Primary key. */
	@Id
	private RawGenericRowId id;

	/** {@inheritDoc} */
	@Column(name = "answer_value")
	private Integer answerValue;

	// ======================================================================

	/** {@inheritDoc} */
	@Override
	public RawGenericRowId getId() {
		return id;

	}

	/**
	 * @return the answerValue
	 */
	public Integer getAnswerValue() {
		return answerValue;
	}
}
