package com.oas.model.report;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;

/**
 * Composite key used in models that order data by survey_id and language_id.
 * 
 * @author xhalliday
 * @since
 */
@Embeddable
public class ResponseLanguageCompositeID implements Serializable {

	@ManyToOne(optional = false)
	@JoinColumn(name = "survey_id", nullable = false, updatable = false, referencedColumnName = "id")
	private Survey survey;

	@ManyToOne
	@JoinColumn(name = "language_id", nullable = false, updatable = false, referencedColumnName = "id")
	private SupportedLanguage supportedLanguage;

	// ======================================================================

	public ResponseLanguageCompositeID() {
	}

	// public ResponseLanguageCompositeID(Survey survey, SupportedLanguage
	// supportedLanguage) {
	// setSurvey(survey);
	// setSupportedLanguage(supportedLanguage);
	// }

	// ======================================================================

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
	// public void setSurvey(Survey survey) {
	// this.survey = survey;
	// }
	/**
	 * @return the supportedLanguage
	 */
	public SupportedLanguage getSupportedLanguage() {
		return supportedLanguage;
	}

	/**
	 * @param supportedLanguage
	 *            the supportedLanguage to set
	 */
	// public void setSupportedLanguage(SupportedLanguage supportedLanguage) {
	// this.supportedLanguage = supportedLanguage;
	// }
}
