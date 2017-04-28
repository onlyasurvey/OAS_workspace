package com.oas.command.model;

import org.springframework.web.multipart.MultipartFile;

/**
 * Command for uploading attachments to BaseObjects.
 * 
 * @author xhalliday
 * @since March 24, 2009
 */
public class UploadAttachmentForm {

	/** ISO3 code. */
	private String language;

	/** Upload content. */
	private MultipartFile upload;

	/** Alternate/hover text. */
	private String altText;

	// ======================================================================

	/** Default constructor. */
	public UploadAttachmentForm() {
	}

	/** Basic constructor. */
	public UploadAttachmentForm(String languageCode) {
		setLanguage(languageCode);
	}

	// ======================================================================

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the upload
	 */
	public MultipartFile getUpload() {
		return upload;
	}

	/**
	 * @param upload
	 *            the upload to set
	 */
	public void setUpload(MultipartFile upload) {
		this.upload = upload;
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

}
