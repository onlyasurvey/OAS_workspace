package com.oas.munger.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.form.TagWriter;

import com.oas.munger.EncoderDecoder;
import com.oas.munger.MungerFilter;
import com.oas.munger.MungerUtil;

public class FormTag extends org.springframework.web.servlet.tags.form.FormTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected String resolveAction() throws javax.servlet.jsp.JspException {

		String resolvedAction = super.resolveAction();

		if (resolvedAction.startsWith("/")) {

			// strip context and servlet paths, and start after the "/" that
			// remains (to not encode it)
			String toEncode = MungerUtil.stripServletPath(MungerUtil.stripContextPath(resolvedAction));

			resolvedAction = MungerFilter.getContextPath() + MungerFilter.getServletPath() + "/" + EncoderDecoder.encode(toEncode.substring(1));
		} else {
			// relative to current URL
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

			// "/foo/bar/db/"
			String currentBasePath = MungerUtil.stripAfterLastSlash(request.getRequestURI());

			// "/foo/bar"
			String toStrip = MungerFilter.getContextPath() + MungerFilter.getServletPath();

			// "db/page.html"
			String toEncode = MungerUtil.stripAllLeadingSlashes((currentBasePath + resolvedAction).substring(toStrip.length()));

			resolvedAction = MungerFilter.getContextPath() + MungerFilter.getServletPath() + "/" + EncoderDecoder.encode(toEncode);

		}

		return resolvedAction;
	}

	/**
	 * We are calling this method so we can put a hidden field for the form.
	 * When using query parms as well as fields, the query ones were being lost.
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		int result = super.writeTagContent(tagWriter);

		return result;
	}

}
