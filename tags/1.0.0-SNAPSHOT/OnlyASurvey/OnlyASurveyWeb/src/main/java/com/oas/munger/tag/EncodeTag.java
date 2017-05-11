package com.oas.munger.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.oas.munger.EncoderDecoder;

/**
 * <oas:encode value=''/>
 * 
 * @author xhalliday
 * @since January 31, 2009
 */
public class EncodeTag extends TagSupport {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = 243236671L;

	/** The value attribute. */
	private String value;

	// ======================================================================

	public EncodeTag() {
		super();
	}

	// ======================================================================

	public void setValue(String theValue) {
		value = theValue;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int doEndTag() throws JspException {

		try {
			pageContext.getOut().print(EncoderDecoder.encode(value));
		} catch (IOException e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}
}
