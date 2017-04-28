package com.oas.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;

import ca.inforealm.core.model.AbstractResourceModel;

/**
 * A language-specific payload for an Attachment
 * 
 * @author xhalliday
 * @since March 24, 2009
 */
@Embeddable
public class AttachmentPayload extends AbstractResourceModel implements Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = -8206248284552368999L;

	// /** The object this attachment is for. */
	// private Attachment attachment;
	//

	/** Supported Language this attachment relates to. */
	// @Basic(fetch = FetchType.LAZY)
	// @Column(name = "language_id")
	// private SupportedLanguage supportedLanguage;
	/** Size in bytes of the decoded payload. */
	@Column(name = "size")
	private int size;

	/** Alternate or hover/title text. */
	@Column(name = "alt_text")
	private String altText;

	/** Timestamp when the image was uploaded, for caching. */
	@Column(name = "upload_time")
	private Date uploadTime;

	/** The attachment data, base64-encoded; lazy-loaded. */
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "payload")
	private String payload;

	// ======================================================================

	/** Default constructor. */
	public AttachmentPayload() {
	}

	/** Basic constructor. */
	public AttachmentPayload(int size, String altText, Date uploadTime, String payload) {
		setSize(size);
		setAltText(altText);
		setUploadTime(uploadTime);
		setPayload(payload);
	}

	/**
	 * @return the supportedLanguage
	 */
	// public SupportedLanguage getSupportedLanguage() {
	// return supportedLanguage;
	// }
	//
	// /**
	// * @param supportedLanguage
	// * the supportedLanguage to set
	// */
	// public void setSupportedLanguage(SupportedLanguage supportedLanguage) {
	// this.supportedLanguage = supportedLanguage;
	// }
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the altText
	 */
	public String getAltText() {
		return altText;
	}

	/**
	 * @param altText
	 *            the altText to set
	 */
	public void setAltText(String altText) {
		this.altText = altText;
	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	/**
	 * @return the uploadTime
	 */
	public Date getUploadTime() {
		return uploadTime;
	}

	/**
	 * @param uploadTime
	 *            the uploadTime to set
	 */
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}

	// ======================================================================

}
