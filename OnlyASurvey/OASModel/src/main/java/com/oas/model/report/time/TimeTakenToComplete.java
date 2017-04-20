package com.oas.model.report.time;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * View that shows how many partial responses stopped at a particular question
 * in a Survey.
 * 
 * @author xhalliday
 * @since October 17, 2009
 */
@Entity
@Table(schema = "oas", name = "vw_time_taken_to_complete")
public class TimeTakenToComplete implements Serializable {

	/** Serialization ID. */
	private static final long serialVersionUID = -4873978558129750227L;

	/** Composite key of survey, questionIndex. */
	@Id
	private TimeTakenToCompleteId id;

	/** Count of responses that fall into this time-taken. */
	@Column(name = "response_count", updatable = false, insertable = false)
	private int count;

	// ======================================================================

	/** Default constructor. */
	public TimeTakenToComplete() {
	}

	/** Testing constructor. */
	public TimeTakenToComplete(TimeTakenToCompleteId id, int count) {
		this.id = id;
		this.count = count;
	}

	// ======================================================================

	/**
	 * Accessor.
	 * 
	 * @return the id
	 */
	public TimeTakenToCompleteId getId() {
		return id;
	}

	/**
	 * Accessor.
	 * 
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

}
