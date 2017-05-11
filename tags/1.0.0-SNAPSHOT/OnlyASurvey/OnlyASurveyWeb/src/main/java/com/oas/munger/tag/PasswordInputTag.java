package com.oas.munger.tag;

import javax.servlet.jsp.JspException;

import com.oas.munger.EncoderDecoder;

public class PasswordInputTag extends org.springframework.web.servlet.tags.form.PasswordInputTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected String getName() throws JspException {
		return EncoderDecoder.encode(super.getName());
	}
}
