package com.oas.model.report.abandonment;

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
public class PartialResponseHighestQuestionSummaryId implements Serializable {

	/** Serialization ID. */
	private static final long serialVersionUID = -900087808712593755L;

	/** Survey. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	/** Question index (zero-based). */
	@Column(name = "question_index")
	private Long questionIndex;

	// ======================================================================

	/** Default constructor. */
	public PartialResponseHighestQuestionSummaryId() {
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
	 * @return the questionIndex
	 */
	public Long getQuestionIndex() {
		return questionIndex;
	}

}
