package com.oas.model.report;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.util.Assert;

import ca.inforealm.core.model.AbstractResourceModel;

/**
 * View that provides the number of responses to a survey per language;
 * 
 * @author xhalliday
 * @since November 16, 2008
 */
@Entity
@Table(schema = "oas", name = "vw_responses_per_language")
public class ResponsesPerLanguage extends AbstractResourceModel implements Comparable {

	@EmbeddedId
	private ResponseLanguageCompositeID id;

	@Column(name = "response_count", updatable = false, insertable = false)
	private Long count;

	// ======================================================================
	public ResponsesPerLanguage() {
		super();
	}

	// public ResponsesPerLanguage(Survey survey, SupportedLanguage
	// supportedLanguage, Long count) {
	// super();
	//
	// setId(new ResponseLanguageCompositeID(survey, supportedLanguage));
	// setCount(count);
	// }

	// ======================================================================

	@Override
	public int compareTo(Object o) {
		// must be the correct type
		Assert.isAssignable(ResponsesPerLanguage.class, o.getClass());

		ResponsesPerLanguage other = (ResponsesPerLanguage) o;

		if (!getId().getSurvey().getId().equals(other.getId().getSurvey().getId())) {
			// different surveys
			return getId().getSurvey().getId().compareTo(other.getId().getSurvey().getId());
		}

		// compare by language
		return getId().getSupportedLanguage().getId().compareTo(other.getId().getSupportedLanguage().getId());

	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public ResponseLanguageCompositeID getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	// public void setId(ResponseLanguageCompositeID id) {
	// this.id = id;
	// }
	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	// public void setCount(Long count) {
	// this.count = count;
	// }
}
