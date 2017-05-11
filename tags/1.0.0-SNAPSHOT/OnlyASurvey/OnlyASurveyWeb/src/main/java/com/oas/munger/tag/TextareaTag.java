package com.oas.munger.tag;

import javax.servlet.jsp.JspException;

import com.oas.munger.EncoderDecoder;

public class TextareaTag extends org.springframework.web.servlet.tags.form.TextareaTag {

	@Override
	protected String getName() throws JspException {
		return EncoderDecoder.encode(super.getName());
	}
}
