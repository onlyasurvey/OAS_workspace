package com.oas.munger.tag;

import javax.servlet.jsp.JspException;

import com.oas.munger.EncoderDecoder;

public class HiddenInputTag extends org.springframework.web.servlet.tags.form.HiddenInputTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected String getName() throws JspException {
		return EncoderDecoder.encode(super.getName());
	}
}
