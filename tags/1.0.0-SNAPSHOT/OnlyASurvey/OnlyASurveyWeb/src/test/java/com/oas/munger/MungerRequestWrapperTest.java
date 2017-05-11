package com.oas.munger;

import static junit.framework.Assert.assertEquals;

import java.util.Enumeration;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.oas.AbstractOASBaseTest;

public class MungerRequestWrapperTest extends AbstractOASBaseTest {

	@Test
	public void testParsesQueryStringFromOneBigEncodedURI_Success() {

		final String path = "/path";
		final String paramKey = "a";
		final String paramValue = "b";

		// encode
		String uri = EncoderDecoder.encode(path + "?" + paramKey + "=" + paramValue);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", uri);
		MungerRequestWrapper request = new MungerRequestWrapper(mockRequest);

		assertEquals("failed to decode path", path, request.getRequestURI());
		assertEquals("failed to decode parameter from URI", paramValue, request.getParameter(paramKey));

	}

	// ======================================================================

	@Test
	public void testGetParameterNames_Success() {
		//
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", "/foo?a=1&b=2");
		MungerRequestWrapper request = new MungerRequestWrapper(mockRequest);

		@SuppressWarnings("unchecked")
		Enumeration<String> names = request.getParameterNames();

		int matches = 0;
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			if ("a".equals(name) || "b".equals(name)) {
				matches++;
			}
		}

		assertEquals("expected matches for all parameters", 2, matches);

	}

	// ======================================================================

	@Test
	public void testGetParameter_Success_SingleValue() {
		//
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", "/foo?a=1&b=2&bar=");
		MungerRequestWrapper request = new MungerRequestWrapper(mockRequest);

		String value = request.getParameter("a");
		assertEquals("unexpected param value", "1", value);
	}

	@Test
	public void testGetParameter_Success_MultipleValues() {
		//
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", "/foo?a=1&b=2&a=3");
		MungerRequestWrapper request = new MungerRequestWrapper(mockRequest);

		String value = request.getParameter("a");
		assertEquals("unexpected param value", "1,3", value);
	}

	// ======================================================================

}
