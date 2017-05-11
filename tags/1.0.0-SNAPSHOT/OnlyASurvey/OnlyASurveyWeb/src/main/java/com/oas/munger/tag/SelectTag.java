package com.oas.munger.tag;

import javax.servlet.jsp.JspException;

import com.oas.munger.EncoderDecoder;

public class SelectTag extends org.springframework.web.servlet.tags.form.SelectTag {
	@Override
	protected String getName() throws JspException {
		return EncoderDecoder.encode(super.getName());
	}
}
