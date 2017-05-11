package com.oas.munger.tag;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.tag.common.core.UrlSupport;

import com.oas.munger.EncoderDecoder;
import com.oas.munger.MungerFilter;
import com.oas.munger.MungerUtil;

/**
 * Replacement for <c:url/>
 * 
 * @author xhalliday
 * @since January 19, 2009
 */
public class UrlTag extends TagSupport {
	// extends org.apache.taglibs.standard.tag.el.core.UrlTag {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = 1L;

	/** The value attribute. */
	private String value;

	/** Disable URL munging and only prepend appropriate path? */
	private boolean disableMunger = false;

	// ======================================================================

	public UrlTag() {
		super();
	}

	// ======================================================================

	public void setValue(String theValue) {
		value = theValue;
	}

	public String getValue() {
		return value;
	}

	public void setDisableMunger(String theValue) {
		this.disableMunger = "true".equals(theValue);
	}

	@Override
	public int doEndTag() throws JspException {

		String outputValue = "BUG";

		String contextPath = ((HttpServletRequest) pageContext.getRequest()).getContextPath();

		if (MungerFilter.isDisabled()) {

			outputValue = UrlSupport.resolveUrl(getValue(), contextPath, pageContext);

		} else {

			// strip double-slashes
			String value = MungerUtil.stripDoubleLeadingSlashes(getValue());

			// encode string unless it's to be excluded as configured in the
			// MungerFilter
			// do not encode the prefix
			int secondSlash = value.indexOf("/", 1);
			if (secondSlash != -1) {
				// String prefix = MungerFilter.getContextPath() +
				// value.substring(0, secondSlash + 1);
				String prefix = contextPath + "/";
				String path = value.substring(secondSlash + 1);

				if (!(disableMunger || MungerFilter.excludeFromEncoding(value))) {
					path = EncoderDecoder.encode(path);
				}

				value = prefix + path;
			} else {
				// append context path as only op
				value = MungerFilter.getContextPath() + value;
			}

			outputValue = value;
		}
		try {
			pageContext.getOut().print(outputValue);
		} catch (IOException e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}
}
