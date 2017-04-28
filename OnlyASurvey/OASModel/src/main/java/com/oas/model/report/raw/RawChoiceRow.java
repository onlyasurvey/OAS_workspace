package com.oas.model.report.raw;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import com.oas.model.Choice;
import com.oas.model.answer.TextAnswer;

/**
 * Raw {@link TextAnswer} row for a raw Excel export.
 * 
 * @author xhalliday
 * @since September 25, 2009
 */
@Entity
@Table(schema = "oas", name = "vw_raw_choice")
@BatchSize(size = 25)
public class RawChoiceRow extends AbstractRawRow {

	/** Serialization ID. */
	private static final long serialVersionUID = -3643709823003668798L;

	/** Primary key. */
	@Id
	private RawChoiceRowId id;

	/** The {@link Choice}.id. */
	@Column(name = "sum_value")
	private Long sumValue;

	// ======================================================================

	@Override
	public RawChoiceRowId getId() {
		return id;
	};

	// ======================================================================

	/**
	 * @return the sumValue
	 */
	public Long getSumValue() {
		return sumValue;
	}

}
