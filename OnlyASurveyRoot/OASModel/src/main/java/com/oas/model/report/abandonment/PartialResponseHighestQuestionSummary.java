package com.oas.model.report.abandonment;

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
@Table(schema = "oas", name = "vw_partial_response_highest_question_summary")
public class PartialResponseHighestQuestionSummary implements Serializable {

	/** Serialization ID. */
	private static final long serialVersionUID = 5824068532389741036L;

	/** Composite key of survey, questionIndex. */
	@Id
	private PartialResponseHighestQuestionSummaryId id;

	/** Count of respondents who abandonded the Survey at this question index. */
	@Column(name = "abandoned_count", updatable = false, insertable = false)
	private int count;

	// ======================================================================

	/** Default constructor. */
	public PartialResponseHighestQuestionSummary() {
	}

	// ======================================================================

	/**
	 * Accessor.
	 * 
	 * @return the id
	 */
	public PartialResponseHighestQuestionSummaryId getId() {
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
