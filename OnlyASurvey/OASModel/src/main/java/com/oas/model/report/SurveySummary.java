package com.oas.model.report;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.Survey;

/**
 * View that provides summary information (e.g., total number of responses) for
 * a survey.
 * 
 * @author xhalliday
 * @since December 12, 2008
 */
@Entity
@Table(schema = "oas", name = "vw_survey_summaries")
public class SurveySummary extends AbstractResourceModel {

	/**
	 * This primary key definition exists so that Hibernate is happy. It is
	 * never used by anything other than Hibernate internals. Use getSuvey()
	 * instead.
	 */
	@Id
	@Column(name = "survey_id")
	private Long surveyId;

	/**
	 * Real key is the survey. Fetch eagerly since it's always used with the
	 * survey data (eg., name, etc.)
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "survey_id", referencedColumnName = "id")
	private Survey survey;
	// private Survey survey;

	@Column(name = "response_count", updatable = false, insertable = false)
	private Long count;

	// ======================================================================
	public SurveySummary() {
		super();
	}

	// public ResponsesTotal(Survey survey, Long count) {
	// super();
	//
	// this.survey = survey;
	// this.count = count;
	// }

	// ======================================================================

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}
}
