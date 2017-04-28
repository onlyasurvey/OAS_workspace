package com.oas.model.report.raw;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.oas.model.Survey;

/**
 * Composite key used in models that order data by survey_id, question_id,
 * response_id
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@MappedSuperclass
public abstract class AbstractRawRowId implements Serializable {

	/** Serialization ID. */
	private static final long serialVersionUID = 5825659319971755892L;

	/** The relevant question. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	/** The relevant question. */
	@Column(name = "question_id")
	private Long questionId;

	/** The relevant question. */
	@Column(name = "response_id")
	private Long responseId;

	// ======================================================================

	public AbstractRawRowId() {
	}

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @return the questionId
	 */
	public Long getQuestionId() {
		return questionId;
	}

	/**
	 * @return the responseId
	 */
	public Long getResponseId() {
		return responseId;
	}

}
