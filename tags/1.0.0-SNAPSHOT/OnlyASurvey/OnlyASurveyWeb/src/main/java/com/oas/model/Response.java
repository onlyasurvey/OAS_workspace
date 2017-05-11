package com.oas.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * One user's response to a survey.
 * 
 * @author xhalliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "response")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Response extends BaseObject implements SurveyRelatedData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656941013430833241L;

	/** The survey. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	// cache read only - Responses aren't moved between Surveys
	// @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
	private Survey survey;

	/** Collection of answers in this response. */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "response", fetch = FetchType.LAZY)
	@JoinColumn(name = "response_id")
	@LazyCollection(LazyCollectionOption.TRUE)
	private Collection<Answer> answers = new ArrayList<Answer>();

	/** Language of the user when the response was closed. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id")
	private SupportedLanguage supportedLanguage;

	/** IP Address (dotted quad) of the user when they created the response. */
	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "is_closed")
	private boolean closed;

	// ======================================================================

	public Response() {
	}

	public Response(Survey survey) {
		this.survey = survey;
	}

	// public Response(Survey survey, Date dateCreated) {
	// super();
	// setCreated(dateCreated);
	// this.survey = survey;
	// }

	public Response(Survey survey, Date dateCreated, SupportedLanguage supportedLanguage, String ipAddress) {
		super();
		setCreated(dateCreated);
		this.survey = survey;
		this.supportedLanguage = supportedLanguage;
		this.ipAddress = ipAddress;
	}

	// ======================================================================

	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}

	/**
	 * @return the answers
	 */
	public Collection<Answer> getAnswers() {
		return answers;
	}

	public void addAnswer(Answer answer) {
		answers.add(answer);
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
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress
	 *            the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the closed
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @param closed
	 *            the closed to set
	 */
	public void setClosed(boolean closed) {
		this.closed = closed;
	}

}
