package com.oas.model;

import java.io.Serializable;
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

import org.springframework.util.Assert;

import ca.inforealm.core.model.AbstractResourceModel;

/**
 * Invitation to respond to a {@link Survey}. May link in a {@link Response}.
 * 
 * @author xhalliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "invitation")
@SequenceGenerator(name = "invitationSequence", sequenceName = "oas.invitation_id_seq", allocationSize = 1)
public class Invitation extends AbstractResourceModel implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -6544903886415470338L;

	/** Primary key. */
	@Id
	@GeneratedValue(generator = "invitationSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/**
	 * The Survey for which this applies.
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id", insertable = true, updatable = false, nullable = false)
	private Survey survey;

	/** (Optional) The Response for which this applies. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "response_id", insertable = true, updatable = true, nullable = true)
	private Response response;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private InvitationStatusType status;

	/** Email Address. */
	@Column(name = "email_address")
	private String emailAddress;

	/** Reminder Count. */
	@Column(name = "reminder_count")
	private int reminderCount;

	/** Reminder Last Sent Date (if any). */
	@Column(name = "reminder_sent_date")
	private Date reminderSentDate;

	/** Error Flag. */
	@Column(name = "error_flag")
	private boolean errorFlag;

	/** (Optional) Error Message, if any. */
	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "invitation_code")
	private String invitationCode;

	// ======================================================================

	/** Default constructor. */
	public Invitation() {
	}

	/** Complex constructor. */
	public Invitation(Survey survey, String email) {

		Assert.notNull(survey);
		Assert.hasText(email);

		this.survey = survey;
		this.emailAddress = email;
	}

	// ======================================================================

	/** Is the invite unsent? */
	public boolean isUnsent() {
		return InvitationStatusType.UNSENT.equals(getStatus());
	}

	/** Has the initial invite been sent? */
	public boolean isSent() {
		return InvitationStatusType.SENT.equals(getStatus());
	}

	/** Has a reminder been sent? */
	public boolean isReminded() {
		return InvitationStatusType.REMINDED.equals(getStatus());
	}

	/** Has the invitation response been started? */
	public boolean isStarted() {
		return InvitationStatusType.STARTED.equals(getStatus());
	}

	/** Has the invitation been fully responded to? (Response is closed?) */
	public boolean isResponded() {
		return InvitationStatusType.RESPONDED.equals(getStatus());
	}

	// ======================================================================

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	public Survey getSurvey() {
		return survey;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public InvitationStatusType getStatus() {
		return status;
	}

	public void setStatus(InvitationStatusType status) {
		this.status = status;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public int getReminderCount() {
		return reminderCount;
	}

	public void setReminderCount(int reminderCount) {
		this.reminderCount = reminderCount;
	}

	public Date getReminderSentDate() {
		return reminderSentDate;
	}

	public void setReminderSentDate(Date reminderSentDate) {
		this.reminderSentDate = reminderSentDate;
	}

	public boolean isErrorFlag() {
		return errorFlag;
	}

	public void setErrorFlag(boolean errorFlag) {
		this.errorFlag = errorFlag;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the invitationCode
	 */
	public String getInvitationCode() {
		return invitationCode;
	}

	/**
	 * @param invitationCode
	 *            the invitationCode to set
	 */
	public void setInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}
}
