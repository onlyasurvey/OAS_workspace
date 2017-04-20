package com.oas.model.report.calendar.value;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.oas.model.Survey;

/**
 * Breaks down response data by month for a text question.
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Entity
@Table(schema = "oas", name = "vw_text_monthly_values")
public class TextMonthlyValue implements TextCalendarValue {

	/** Composite primary key. */
	@Id
	private TextCalendarValueId id;

	/** The relevant survey. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	/** Number of times this choice was selected. */
	@Column(name = "answer_value")
	private String value;

	// ======================================================================

	/**
	 * @return the id
	 */
	public TextCalendarValueId getId() {
		return id;
	}

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}