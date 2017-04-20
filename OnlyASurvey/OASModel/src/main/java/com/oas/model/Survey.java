package com.oas.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.MapKeyManyToMany;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.UserAccount;

import com.oas.model.templating.SurveyLogo;
import com.oas.model.templating.Template;

/**
 * From the Domain Terms document:
 * 
 * Survey: A systematic way of collecting information from people, using a
 * questionnaire as an instrument.
 * 
 * Questionnaire: instrument that contains a set of standard questions that are
 * asked to the respondents in a survey.
 * 
 * This type defines both a Survey and a Questionnaire as one entity.
 * 
 * @author xhalliday
 */
@Entity
@Table(schema = "oas", name = "survey")
public class Survey extends BaseObject {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 7872321862090960399L;

	/** Questions, ordered by Question.displayOrder. */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "survey", fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	@LazyCollection(LazyCollectionOption.TRUE)
	@OrderBy("displayOrder ASC")
	private final List<Question> questions = new ArrayList<Question>();

	/** Owner of the survey. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private AccountOwner owner;

	/**
	 * Languages which are supported in this survey.
	 */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "survey", fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	@Sort(type = SortType.NATURAL)
	private final SortedSet<SurveyLanguage> surveyLanguages = new TreeSet<SurveyLanguage>();

	/**
	 * Which templating method to use. Note this is independent of
	 * this.templates, which only contains template payloads, but does not
	 * indicate their use.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "template_option")
	private SurveyTemplateOption templateOption = SurveyTemplateOption.DEFAULT;

	/**
	 * Templates applied to the Survey when respondents are taking it.
	 */
	@CollectionOfElements(targetElement = Template.class)
	@MapKeyManyToMany(joinColumns = @JoinColumn(name = "language_id"), targetEntity = SupportedLanguage.class)
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "survey", fetch = FetchType.LAZY)
	@Sort(type = SortType.NATURAL)
	private final SortedMap<SupportedLanguage, Template> templates = new TreeMap<SupportedLanguage, Template>();

	/** Logo images for a survey. */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "survey", fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	@LazyCollection(LazyCollectionOption.TRUE)
	private final Collection<SurveyLogo> logos = new ArrayList<SurveyLogo>();

	/**
	 * Has the survey been published (ie., now open for taking responses)?
	 */
	private boolean published;

	/**
	 * Has the survey been paid for?
	 */
	@Column(name = "paid_for")
	private boolean paidFor;

	/**
	 * Global password for creating a response to this Survey, i.e., if it is
	 * "password protected".
	 */
	@Column(name = "global_password")
	private String globalPassword;

	/** Percentage of visitors who receive the "opt-in lightbox." */
	@Column(name = "optin_percentage")
	private Long optinPercentage;

	// ======================================================================

	public Survey() {
	}

	public Survey(AccountOwner owner) {
		setOwner(owner);
	}

	/**
	 * Constructor for when the base Actor is known (eg., abstract parents). OAS
	 * only supports AccountOwner as the owner, so there is a cast here.
	 * 
	 * @param owner
	 */
	public Survey(Actor owner) {
		setOwner((AccountOwner) owner);
	}

	/**
	 * Constructor for when the base UserAccount is known (eg., abstract
	 * parents). OAS only supports AccountOwner as the owner, so there is a cast
	 * here.
	 * 
	 * @param owner
	 *            The owner of the Survey.
	 */
	public Survey(UserAccount owner) {
		setOwner((AccountOwner) owner);
	}

	// ======================================================================

	/**
	 * @param question
	 *            the question to add
	 */
	public void addQuestion(Question question) {
		questions.add(question);
		// question.setDisplayOrder((long) questions.indexOf(question));
	}

	/**
	 * @param response
	 *            the response to add
	 */
	// public void addResponse(Response response) {
	// responses.add(response);
	// }
	/**
	 * Get all SupportedLanguage objects correlating to surveyLanguges
	 */
	public List<SupportedLanguage> getSupportedLanguages() {
		// TODO this may be inefficient
		List<SupportedLanguage> retval = new ArrayList<SupportedLanguage>();
		for (SurveyLanguage language : getSurveyLanguages()) {
			retval.add(language.getLanguage());
		}
		return retval;
	}

	/**
	 * Flag indicating whether the survey can be edited as per the Published
	 * Survey Limitation Matrix.
	 * 
	 * @return
	 * @see http://spreadsheets.google.com/a/onlyasurvey.com/ccc?key=
	 *      p9bsi2QUV2JVb1mrOFlsWbg&hl=en
	 */
	public boolean isChangeAllowed() {
		return !isPublished();
		// return true;
	}

	// ======================================================================

	/**
	 * @return the questions
	 */
	public List<Question> getQuestions() {
		return questions;
	}

	/**
	 * @return the response
	 */
	// public Collection<Response> getResponses() {
	// // return responses;
	// return Collections.EMPTY_LIST;
	// }
	/**
	 * @return the owner
	 */
	public AccountOwner getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(AccountOwner owner) {
		this.owner = owner;
	}

	/**
	 * @return the SurveyLanguages
	 */
	public Collection<SurveyLanguage> getSurveyLanguages() {
		return surveyLanguages;
	}

	/**
	 * @return the published
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * @param published
	 *            the published to set
	 */
	public void setPublished(boolean published) {
		this.published = published;
	}

	/**
	 * @return the paidFor
	 */
	public boolean isPaidFor() {
		return paidFor;
	}

	/**
	 * @param paidFor
	 *            the paidFor to set
	 */
	public void setPaidFor(boolean paidFor) {
		this.paidFor = paidFor;
	}

	/**
	 * @return the templates
	 */
	public Map<SupportedLanguage, Template> getTemplates() {
		return templates;
	}

	/**
	 * @return the templateOption
	 */
	public SurveyTemplateOption getTemplateOption() {
		return templateOption;
	}

	/**
	 * @param templateOption
	 *            the templateOption to set
	 */
	public void setTemplateOption(SurveyTemplateOption templateOption) {
		this.templateOption = templateOption;
	}

	/**
	 * @return the logos
	 */
	public Collection<SurveyLogo> getLogos() {
		return logos;
	}

	/**
	 * @return the globalPassword
	 */
	public String getGlobalPassword() {
		return globalPassword;
	}

	/**
	 * @param globalPassword
	 *            the globalPassword to set
	 */
	public void setGlobalPassword(String globalPassword) {
		this.globalPassword = globalPassword;
	}

	/**
	 * Accessor.
	 * 
	 * @return the optinPercentage
	 */
	public Long getOptinPercentage() {
		return optinPercentage;
	}

	/**
	 * Accessor.
	 * 
	 * @param optinPercentage
	 *            the optinPercentage to set
	 */
	public void setOptinPercentage(Long optinPercentage) {
		this.optinPercentage = optinPercentage;
	}

}