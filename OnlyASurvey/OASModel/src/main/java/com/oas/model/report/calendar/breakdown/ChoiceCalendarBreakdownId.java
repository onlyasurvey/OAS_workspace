package com.oas.model.report.calendar.breakdown;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Choice;

/**
 * A composite key consisting of a Choice and a Date.
 * 
 * @author xhalliday
 * @since December 9, 2008
 */

@Embeddable
public class ChoiceCalendarBreakdownId implements Serializable {

	/** Generated id. */
	private static final long serialVersionUID = -9200096347011370262L;

	/** The relevant choice. */
	@ManyToOne(optional = false)
	@JoinColumn(name = "choice_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Choice choice;

	/** Date range (month) to which it applies. */
	@Column(name = "target_date", updatable = false, insertable = false)
	private Date date;

	// ======================================================================

	public ChoiceCalendarBreakdownId() {
	}

	public ChoiceCalendarBreakdownId(Choice choice, Date date) {
		setChoice(choice);
		setDate(date);
	}

	// ======================================================================

	/**
	 * @return the choice
	 */
	public Choice getChoice() {
		return choice;
	}

	/**
	 * @param choice
	 *            the choice to set
	 */
	public void setChoice(Choice choice) {
		this.choice = choice;
	}

	/**
	 * @return the month
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param month
	 *            the month to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

}
