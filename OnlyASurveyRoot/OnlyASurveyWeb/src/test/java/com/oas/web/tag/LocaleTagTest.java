package com.oas.web.tag;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.util.Assert;

public class LocaleTagTest {

	private LocaleTag localeTag;

	@Before
	public void setup() {
		if (localeTag == null) {
			localeTag = new LocaleTag();
		}
	}

	@Test
	public void lenAttribute() throws JspException, UnsupportedEncodingException {
		localeTag.setLen(1);
		doLocaleTest(Locale.ENGLISH, "e");
	}

	@Test
	public void lenAttributeMax() throws JspException, UnsupportedEncodingException {
		localeTag.setLen(5);
		doLocaleTest(Locale.ENGLISH, "en");
	}

	@Test
	public void localeEnglish() throws JspException, UnsupportedEncodingException {
		doLocaleTest(Locale.ENGLISH, "en");
	}

	@Test
	public void localeCanadaEnglish() throws JspException, UnsupportedEncodingException {
		doLocaleTest(Locale.CANADA, "en");
	}

	@Test
	public void localeFrench() throws JspException, UnsupportedEncodingException {
		doLocaleTest(Locale.FRENCH, "fr");
	}

	@Test
	public void localeCanadaFrench() throws JspException, UnsupportedEncodingException {
		doLocaleTest(Locale.CANADA_FRENCH, "fr");
	}

	public void doLocaleTest(Locale localeToSet, String expectedOutput) throws JspException, UnsupportedEncodingException {

		Assert.hasText(expectedOutput);

		LocaleContextHolder.setLocale(localeToSet);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockPageContext pageContext = new MockPageContext(request.getServletContext(), request, response);
		localeTag.setPageContext(pageContext);

		localeTag.doEndTag();

		String content = response.getContentAsString();

		assertEquals("wrong content length", expectedOutput.length(), response.getContentAsByteArray().length);
		assertEquals("wrong content", expectedOutput, content);
	}
}
