/**
 * 
 */
package com.oas.model.invitations;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ca.inforealm.core.model.AbstractResourceModel;

import com.oas.model.Survey;

/**
 * An email sent to the Invitees of a Survey, either an invitation or a
 * reminder.
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
@Entity
@Table(schema = "oas", name = "invitation_mail_out")
@SequenceGenerator(name = "mailOutSequence", sequenceName = "oas.invitation_mail_out_id_seq")
public class MailOut extends AbstractResourceModel {

	/** ID. */
	@Id
	@GeneratedValue(generator = "mailOutSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** Type of Mail Out - invitation or reminder. */
	@Enumerated(EnumType.STRING)
	@Column(name = "mail_out_type", nullable = false)
	private MailOutType type;

	/** Survey this Mail Out pertains to. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id")
	private Survey survey;

	/** Date the Mail Out was created. */
	@Column(name = "date_created")
	private Date dateCreated;

	/** From address. */
	@Column(name = "from_address")
	private String fromAddress;

	/** Subject line. */
	private String subject;

	/** Body of the Mail Out. */
	private String body;

	// ======================================================================

	/**
	 * Default constructor.
	 */
	public MailOut() {
	}

	/**
	 * Complex constructor. Sets <code>dateCreated</code>.
	 * 
	 * @param survey
	 *            {@link Survey} this Mail Out is for.
	 * @param type
	 *            Type of Mail Out - invitation, reminder - see
	 *            <code>{@link MailOutType}</code>.
	 * @param subject
	 *            Subject of the email.
	 * @param body
	 *            Body of the email.
	 */
	public MailOut(Survey survey, MailOutType type, String subject, String body) {
		setSurvey(survey);
		setType(type);
		setSubject(subject);
		setBody(body);
		// now()
		dateCreated = new Date();
	}

	// ======================================================================

	/**
	 * Is this Mail Out an Invitation?
	 * 
	 * @return boolean
	 */
	public boolean isInvitation() {
		return MailOutType.INVITATION.equals(getType());
	}

	/**
	 * Is this Mail Out a Reminder?
	 * 
	 * @return boolean
	 */
	public boolean isReminder() {
		return MailOutType.REMINDER.equals(getType());
	}

	// ======================================================================

	/**
	 * @return the type
	 */
	public MailOutType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(MailOutType type) {
		this.type = type;
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
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * Accessor.
	 * 
	 * @return the fromAddress
	 */
	public String getFromAddress() {
		return fromAddress;
	}

	/**
	 * Accessor.
	 * 
	 * @param fromAddress
	 *            the fromAddress to set
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

}
