package com.oas.munger;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.oas.AbstractOASBaseTest;

/**
 * Tests for MungerResponseWrapper.
 * 
 * @author xhalliday
 * @since February 1, 2009
 */
public class MungerResponseWrapperTest extends AbstractOASBaseTest {

	@Test
	public void testEncodeRedirectURL_Success_WithAnchor() {

		// encoder uses these
		MungerFilter.setDisabled(false);
		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		MungerResponseWrapper wrapper = new MungerResponseWrapper(new MockHttpServletResponse());

		// anchor off a URL with no query string
		{
			String anchor = "#top";
			String expected = "/oas/html/" + MungerTestConstants.ENCODED + anchor;
			String actual = wrapper.encodeRedirectURL("/oas/html/" + MungerTestConstants.PLAIN + anchor);
			assertEquals("unexpected result", expected, actual);
		}

		// anchor off a URL with a query string
		{
			String anchor = "#top";
			String expected = "/oas/html/" + MungerTestConstants.ENCODED + MungerTestConstants.ENCODED_QUESTION_MARK
					+ MungerTestConstants.ENCODED.substring(2) + anchor;
			String actual = wrapper.encodeRedirectURL("/oas/html/" + MungerTestConstants.PLAIN + "?" + MungerTestConstants.PLAIN
					+ anchor);
			assertEquals("unexpected result", expected, actual);
		}
	}

	@Test
	public void testEncodeRedirectURL_Success_RelativeUrl() {

		// encoder uses these
		MungerFilter.setDisabled(false);
		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		MungerResponseWrapper wrapper = new MungerResponseWrapper(new MockHttpServletResponse());

		String expected = "/oas/html/" + MungerTestConstants.ENCODED;
		String actual = wrapper.encodeRedirectURL("/oas/html/" + MungerTestConstants.PLAIN);

		assertEquals("unexpected result", expected, actual);
	}

	@Test
	public void testEncodeRedirectURL_Success_UrlOutsideWebappButOnSameServer() {

		// encoder uses these
		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		MungerResponseWrapper wrapper = new MungerResponseWrapper(new MockHttpServletResponse());

		// this will encode the entire URL, and ignore any context/servlet path
		// since it doesn't start with our app's paths
		String expected = MungerTestConstants.ENCODED;
		String actual = wrapper.encodeRedirectURL(expected);

		assertEquals("unexpected result", expected, actual);
	}

	@Test
	public void testEncodeRedirectURL_Success_AbsoluteUrl() {
		MungerResponseWrapper wrapper = new MungerResponseWrapper(new MockHttpServletResponse());
		assertEquals("unexpected result", "http://www.google.com/", wrapper.encodeRedirectURL("http://www.google.com/"));
	}

	// @Test public void testEncodeRedirectURL
}
