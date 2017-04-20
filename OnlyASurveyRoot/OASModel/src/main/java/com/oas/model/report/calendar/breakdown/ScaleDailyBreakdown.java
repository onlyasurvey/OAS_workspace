package com.oas.model.report.calendar.breakdown;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.oas.model.Survey;

/**
 * Breaks down response data by month for a multiple-Scale question.
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Entity
@Table(schema = "oas", name = "vw_scale_daily_breakdown")
public class ScaleDailyBreakdown implements ScaleCalendarBreakdown {

	/** Composite primary key. */
	@Id
	private ScaleCalendarBreakdownId id;

	/** The relevant survey. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	/** Number of times this Scale was selected. */
	@Column(name = "response_count")
	private Long count;

	// ======================================================================
	public ScaleDailyBreakdown() {
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public ScaleCalendarBreakdownId getId() {
		return id;
	}

	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

}