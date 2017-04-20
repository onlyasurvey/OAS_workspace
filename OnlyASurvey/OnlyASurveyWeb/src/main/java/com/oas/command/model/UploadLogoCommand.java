package com.oas.command.model;

import org.springframework.web.multipart.MultipartFile;

/**
 * Command for uploading a single logo file via the LookAndFeel tab.
 * 
 * @author xhalliday
 * @since February 26, 2009
 */
public class UploadLogoCommand {

	/** Uploaded file. */
	private MultipartFile upload;

	/** Alternate (title) text. */
	private String altText;

	// ======================================================================

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
