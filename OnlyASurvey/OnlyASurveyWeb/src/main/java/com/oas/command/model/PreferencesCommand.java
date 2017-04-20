package com.oas.command.model;

/**
 * Command object for setting a user's preferences.
 * 
 * TODO timezone
 * 
 * @author xhalliday
 * @since October 31, 2008
 */
public class PreferencesCommand {

	/** Where to return user to after changing preferences. */
	private String redirectUrl;

	/** User's own language's ID. */
	private Long languageId;

	/** Default languages to select when creating a survey. */
	private IdListCommand surveyLanguageIdList;

	// ======================================================================

	/**
	 * @return the redirectUrl
	 */
	public String getRedirectUrl() {
		return redirectUrl;
	}

	/**
	 * @param redirectUrl
	 *            the redirectUrl to set
	 */
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	/**
	 * @return the languageId
	 */
	public Long getLanguageId() {
		return languageId;
	}

	/**
	 * @param languageId
	 *            the languageId to set
	 */
	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	/**
	 * @return the surveyLanguageIdList
	 */
	public IdListCommand getSurveyLanguageIdList() {
		return surveyLanguageIdList;
	}

	/**
	 * @param surveyLanguageIdList
	 *            the surveyLanguageIdList to set
	 */
	public void setSurveyLanguageIdList(IdListCommand surveyLanguageIdList) {
		this.surveyLanguageIdList = surveyLanguageIdList;
	}

}
