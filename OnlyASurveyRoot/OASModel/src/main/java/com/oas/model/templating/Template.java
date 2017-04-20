package com.oas.model.templating;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.oas.model.BaseObject;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;

/**
 * User-supplied template data.
 * 
 * @author xhalliday
 * @since December 6, 2008
 */
@Entity
@Table(schema = "oas", name = "template")
public class Template extends BaseObject {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -8880194458336912358L;

	/** Survey of the template. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	private Survey survey;

	/** Language of the template. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id")
	private SupportedLanguage supportedLanguage;

	/** An MD5 hash of the target page when it was imported. */
	@Column(name = "hash_at_import")
	private String hashAtImport;

	/** The URL (if any) from which this template was imported. */
	@Column(name = "imported_from_url")
	private String importedFromUrl;

	/** Base URL for constructing absolute URLs. */
	@Column(name = "base_url")
	private String baseUrl;

	/** Markup that goes before the content. */
	@Column(name = "before_content")
	private String beforeContent;

	/** Markup that goes after the content. */
	@Column(name = "after_content")
	private String afterContent;

	/**
	 * Type of template - how was it imported, which can have effects on it's
	 * runtime rendering.
	 */
	@Column(name = "type_code")
	@Enumerated(EnumType.STRING)
	private TemplateType templateType;

	// ======================================================================

	public Template() {
	}

	public Template(Survey survey, SupportedLanguage supportedLanguage) {
		setSurvey(survey);
		setSupportedLanguage(supportedLanguage);
	}

	public Template(Survey survey, SupportedLanguage supportedLanguage, TemplateType type) {
		setSurvey(survey);
		setSupportedLanguage(supportedLanguage);
		setTemplateType(type);
	}

	// ======================================================================

	public boolean isClf2Template() {
		return TemplateType.CLF2_COMMENTS.equals(getTemplateType());
	}

	// ======================================================================

	/**
	 * @return the beforeContent
	 */
	public String getBeforeContent() {
		return beforeContent;
	}

	/**
	 * @param beforeContent
	 *            the beforeContent to set
	 */
	public void setBeforeContent(String beforeContent) {
		this.beforeContent = beforeContent;
	}

	/**
	 * @return the afterContent
	 */
	public String getAfterContent() {
		return afterContent;
	}

	/**
	 * @param afterContent
	 *            the afterContent to set
	 */
	public void setAfterContent(String afterContent) {
		this.afterContent = afterContent;
	}

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
	public void setSupportedLanguage(SupportedLanguage supportedLanguage) {
		this.supportedLanguage = supportedLanguage;
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @param baseUrl
	 *            the baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * @return the hashAtImport
	 */
	public String getHashAtImport() {
		return hashAtImport;
	}

	/**
	 * @param hashAtImport
	 *            the hashAtImport to set
	 */
	public void setHashAtImport(String hashAtImport) {
		this.hashAtImport = hashAtImport;
	}

	/**
	 * @return the importedFromUrl
	 */
	public String getImportedFromUrl() {
		return importedFromUrl;
	}

	/**
	 * @param importedFromUrl
	 *            the importedFromUrl to set
	 */
	public void setImportedFromUrl(String importedFromUrl) {
		this.importedFromUrl = importedFromUrl;
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
	 * @return the templateType
	 */
	public TemplateType getTemplateType() {
		return templateType;
	}

	/**
	 * @param templateType
	 *            the templateType to set
	 */
	public void setTemplateType(TemplateType templateType) {
		this.templateType = templateType;
	}

}
