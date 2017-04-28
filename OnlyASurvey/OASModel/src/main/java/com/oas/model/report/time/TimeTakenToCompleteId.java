package com.oas.model.report.time;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Survey;

/**
 * Composite key for PartialResponseHighestQuestionSummary.
 * 
 * @author xhalliday
 * @since October 17, 2009
 */
@Embeddable
public class TimeTakenToCompleteId implements Serializable {

	/** Serialization ID. */
	private static final long serialVersionUID = -900087808712593755L;

	/** Survey. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	/** Minutes spent. */
	@Column(name = "minutes")
	private int minutes;

	// ======================================================================

	/** Default constructor. */
	public TimeTakenToCompleteId() {
	}

	/** Testing constructor. */
	public TimeTakenToCompleteId(Survey survey, int minutes) {
		this.survey = survey;
		this.minutes = minutes;
	}

	// ======================================================================

	/**
	 * Accessor.
	 * 
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * Accessor.
	 * 
	 * @return the minutes
	 */
	public int getMinutes() {
		return minutes;
	}

}
