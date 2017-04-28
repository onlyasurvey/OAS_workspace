package com.oas.model.report.calendar.breakdown;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Breaks down response data by month for a multiple-choice question.
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Entity
@Table(schema = "oas", name = "vw_choice_monthly_breakdown")
public class ChoiceMonthlyBreakdown implements ChoiceCalendarBreakdown {

	/** Composite primary key. */
	@Id
	private ChoiceCalendarBreakdownId id;

	/** The relevant survey. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	/** The relevant question. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "question_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Question question;

	/** Number of times this choice was selected. */
	@Column(name = "response_count")
	private Long count;

	// ======================================================================
	public ChoiceMonthlyBreakdown() {

	}

	public ChoiceMonthlyBreakdown(Survey survey, Question question, Choice choice, Date month, Long count) {
		// set ID
		id = new ChoiceCalendarBreakdownId(choice, month);
		this.survey = survey;
		this.question = question;
		this.count = count;
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public ChoiceCalendarBreakdownId getId() {
		return id;
	}

	/**
	 * @return the question
	 */
	public Question getQuestion() {
		return question;
	}

	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}

	// /**
	// * @param count
	// * the count to set
	// */
	// public void setCount(Long count) {
	// this.count = count;
	// }

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

}