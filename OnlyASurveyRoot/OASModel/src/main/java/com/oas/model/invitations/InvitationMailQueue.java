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

import com.oas.model.Invitation;

/**
 * An email sent to an Invitee of a Survey - i.e., one that has a corresponding
 * Invitation.
 * 
 * @author xhalliday
 * @since May 3, 2009
 */
@Entity
@Table(schema = "oas", name = "invitation_mail_queue")
@SequenceGenerator(name = "invitationMailQueueSequence", sequenceName = "oas.invitation_mail_queue_id_seq")
public class InvitationMailQueue extends AbstractResourceModel {

	/** ID. */
	@Id
	@GeneratedValue(generator = "invitationMailQueueSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/**
	 * The mail out this queue item belongs to - CANNOT be lazy because timed
	 * task is not connected.
	 */
	// @OneToOne(fetch = FetchType.LAZY, optional = false)
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "mail_out_id", updatable = false)
	private MailOut mailOut;

	/** Status of Mail Out. */
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private MailOutStatus status;

	/** The invitation. */
	@OneToOne(optional = false)
	@JoinColumn(name = "invitation_id", nullable = false, updatable = false)
	private Invitation invitation;

	@Column(name = "date_created")
	private Date dateCreated;

	@Column(name = "date_sent")
	private Date dateSent;

	@Column(name = "error_string")
	private String errorString;

	// ======================================================================

	/** Default constructor. */
	public InvitationMailQueue() {
	}

	/**
	 * Complex constructor. Also initializes Status and Date Created.
	 * 
	 * @param mailOut
	 *            Mail Out this queue item is for.
	 * @param invitation
	 *            Invitation this queue item is for.
	 */
	public InvitationMailQueue(MailOut mailOut, Invitation invitation) {
		setMailOut(mailOut);
		setInvitation(invitation);
		setStatus(MailOutStatus.UNSENT);
		setDateCreated(new Date());
	}

	// ======================================================================

	/**
	 * @return the mailOut
	 */
	public MailOut getMailOut() {
		return mailOut;
	}

	/**
	 * @param mailOut
	 *            the mailOut to set
	 */
	public void setMailOut(MailOut mailOut) {
		this.mailOut = mailOut;
	}

	/**
	 * @return the status
	 */
	public MailOutStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(MailOutStatus status) {
		this.status = status;
	}

	/**
	 * @return the invitation
	 */
	public Invitation getInvitation() {
		return invitation;
	}

	/**
	 * @param invitation
	 *            the invitation to set
	 */
	public void setInvitation(Invitation invitation) {
		this.invitation = invitation;
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
	 * @return the dateSent
	 */
	public Date getDateSent() {
		return dateSent;
	}

	/**
	 * @param dateSent
	 *            the dateSent to set
	 */
	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}

	/**
	 * @return the errorString
	 */
	public String getErrorString() {
		return errorString;
	}

	/**
	 * @param errorString
	 *            the errorString to set
	 */
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
}
