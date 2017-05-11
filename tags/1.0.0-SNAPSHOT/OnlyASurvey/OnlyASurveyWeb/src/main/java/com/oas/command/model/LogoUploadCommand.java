package com.oas.command.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * Command for uploading several logo files via the LookAndFeel tab.
 * 
 * @author xhalliday
 * @since February 26, 2009
 */
public class LogoUploadCommand {

	/** Maps left-logo file uploads by language. */
	private Map<String, MultipartFile> llgo = new HashMap<String, MultipartFile>();

	/** Maps right-logo file uploads by language. */
	private Map<String, MultipartFile> rlgo = new HashMap<String, MultipartFile>();

	/** Maps left-logo alt text by language. */
	private Map<String, String> lalt = new HashMap<String, String>();

	/** Maps right-logo alt text by language. */
	private Map<String, String> ralt = new HashMap<String, String>();

	// ======================================================================

	/**
	 * @return Left-side logo upload data
	 */
	public Map<String, MultipartFile> getLlgo() {
		return llgo;
	}

	/**
	 * @param llgo
	 *            the llgo to set
	 */
	public void setLlgo(Map<String, MultipartFile> llgo) {
		this.llgo = llgo;
	}

	/**
	 * @return Right-side logo upload data
	 */
	public Map<String, MultipartFile> getRlgo() {
		return rlgo;
	}

	/**
	 * @param rlgo
	 *            the rlgo to set
	 */
	public void setRlgo(Map<String, MultipartFile> rlgo) {
		this.rlgo = rlgo;
	}

	/**
	 * @return the Left-side logo alt text
	 */
	public Map<String, String> getLalt() {
		return lalt;
	}

	/**
	 * @param lalt
	 *            the lalt to set
	 */
	public void setLalt(Map<String, String> lalt) {
		this.lalt = lalt;
	}

	/**
	 * @return the Right-side logo alt text
	 */
	public Map<String, String> getRalt() {
		return ralt;
	}

	/**
	 * @param ralt
	 *            the ralt to set
	 */
	public void setRalt(Map<String, String> ralt) {
		this.ralt = ralt;
	}

}
