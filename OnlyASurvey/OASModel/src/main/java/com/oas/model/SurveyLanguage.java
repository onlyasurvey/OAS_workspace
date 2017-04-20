package com.oas.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

/**
 * Language that is supported in a given survey.
 * 
 * @author xhalliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "survey_language")
public class SurveyLanguage extends BaseObject implements SurveyRelatedData, Comparable<SurveyLanguage> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1107270257727382942L;

	@OneToOne
	@JoinColumn(name = "survey_id")
	private Survey survey;

	@OneToOne
	@JoinColumn(name = "language_id")
	private SupportedLanguage language;

	// ======================================================================

	public SurveyLanguage() {
	}

	public SurveyLanguage(Survey survey, SupportedLanguage language) {
		setSurvey(survey);
		setLanguage(language);
	}

	// ======================================================================

	@Override
	public int compareTo(SurveyLanguage o2) {

		Assert.notNull(o2, "comparator doesn't handle nulls [2]");
		Assert.notNull(getLanguage(), "comparator doens't handle null language [1]");
		Assert.notNull(o2.getLanguage(), "comparator doens't handle null language [2]");

		String code1 = getLanguage().getIso3Lang();
		String code2 = o2.getLanguage().getIso3Lang();

		Assert.hasText(code1, "comparator doesn't handle empty text [1]");
		Assert.hasText(code2, "comparator doesn't handle empty text [2]");

		// the current locale's ISO3 code.
		String currentLanguage = LocaleContextHolder.getLocale().getISO3Language();

		// as per the contract
		if (StringUtils.equals(code1, code2)) {
			return 0;
		}

		// if the language is the same as the current locale (e.g., user's
		// preferred language) then push it to the top
		if (currentLanguage.equals(code1)) {
			return -1;
		} else if (currentLanguage.equals(code2)) {
			return 1;
		} else {
			// compare based on displayTitle, so it's always alphabetical in the
			// user's language
			return code1.compareTo(code2);
		}
	}

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
	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	/**
	 * @return the language
	 */
	public SupportedLanguage getLanguage() {
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}

}
