package com.oas.model.report.raw;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.Choice;

/**
 * Composite key used in models that order data by survey_id, question_id,
 * response_id
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Embeddable
public class RawChoiceRowId extends AbstractRawRowId {

	/** Serialization ID. */
	private static final long serialVersionUID = -5587330755632018461L;

	/** The choice. */
	@ManyToOne
	@JoinColumn(name = "choice_id")
	private Choice choice;

	// ======================================================================

	public RawChoiceRowId() {
	}

	// ======================================================================

	/**
	 * @return the choice
	 */
	public Choice getChoice() {
		return choice;
	}

}
