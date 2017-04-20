package com.oas.model.report.raw;

import javax.persistence.Embeddable;

/**
 * Composite key used in models that order data by survey_id, question_id,
 * response_id.
 * 
 * @author xhalliday
 * @since November 23, 2008
 */
@Embeddable
public class RawGenericRowId extends AbstractRawRowId {

	/** Serialization ID. */
	private static final long serialVersionUID = -5587330755632018461L;

	// ======================================================================

	public RawGenericRowId() {
	}

	// ======================================================================

}
