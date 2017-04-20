package com.oas.model.report;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Survey;

/**
 * Composite key used in models that order data by survey_id and date_created.
 * 
 * @author xhalliday
 * @since
 */
@Embeddable
public class SurveyDateCompositeID implements Serializable {

	@ManyToOne(optional = false)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	@Column(name = "target_date", updatable = false, insertable = false)
	private Date date;

	public SurveyDateCompositeID() {
	}

	public SurveyDateCompositeID(Survey survey, Date date) {
		setSurvey(survey);
		setDate(date);
	}

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @param survey
	 *            the survey to set
	 */
	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
}
