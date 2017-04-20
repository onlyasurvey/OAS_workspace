package com.oas.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Tag that outputs the user's locale as a string.
 * 
 * @author xhalliday
 * @since May 18, 2009
 */
public class LocaleTag extends TagSupport {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = 8270267803743998186L;

	/** How many characters to output. */
	private int len = 2;

	// ======================================================================

	@Override
	public int doEndTag() throws JspException {

		int length = getLen();
		String language = LocaleContextHolder.getLocale().getLanguage();

		if (length > language.length()) {
			length = language.length();
		}

		String output = language.substring(0, length);

		try {
			pageContext.getOut().write(output);
		} catch (IOException ex) {
			throw new JspException(ex);
		}
		return EVAL_PAGE;
	}

	// ======================================================================

	/**
	 * @return the len
	 */
	public int getLen() {
		return len;
	}

	/**
	 * @param len
	 *            the len to set
	 */
	public void setLen(int len) {
		this.len = len;
	}

}
