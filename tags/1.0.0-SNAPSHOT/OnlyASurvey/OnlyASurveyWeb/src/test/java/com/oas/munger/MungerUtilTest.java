package com.oas.munger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;

/**
 * Test for MungerUtil
 * 
 * @author xhalliday
 * @since February 1, 2009
 */
public class MungerUtilTest extends AbstractOASBaseTest {

	@Test
	public void testStripQueryString_Success_NoString() {

		// when no QS there is no change
		assertEquals("problem stripping query string", MungerTestConstants.PLAIN, MungerUtil
				.stripQueryString(MungerTestConstants.PLAIN));
	}

	@Test
	public void testStripQueryString_Success_WithString() {

		// when a QS is there, remove it
		assertEquals("problem stripping query string", MungerTestConstants.PLAIN, MungerUtil
				.stripQueryString(MungerTestConstants.PLAIN_WITH_QUERY_STRING));
	}

	@Test
	public void testStripQueryString_NoChangeForEmptyInput() {
		// when empty string there is no change
		assertEquals("should return empty string", "", MungerUtil.stripQueryString(""));

		// when null there is no change
		assertEquals("should return empty string", null, MungerUtil.stripQueryString(null));
	}

	@Test
	public void testStripAfterLastSlash_Success() {

		// where slashes exist, expect everything after the last one to be
		// removed
		assertEquals("unexpected result", "/foo/", MungerUtil.stripAfterLastSlash("/foo/bar.html"));
	}

	@Test
	public void testStripAfterLastSlash_NoChangeWithoutSlashes() {

		// where no slashes exist, expect result to be equal to input
		assertEquals("unexpected result", "jason.html", MungerUtil.stripAfterLastSlash("jason.html"));
	}

	@Test
	public void testStripAllLeadingSlashes_Success() {
		final String input = "/////db/home.html";
		final String expected = "db/home.html";

		assertEquals("unexpected result", expected, MungerUtil.stripAllLeadingSlashes(input));
	}

	@Test
	public void testStripDoubleLeadingSlashes_Success() {
		final String input = "/////db/home.html";
		final String expected = "/db/home.html";

		assertEquals("unexpected result", expected, MungerUtil.stripDoubleLeadingSlashes(input));
	}

	@Test
	public void testIsAbsoluteUrl_Success_WithAbsolute() {
		assertTrue(MungerUtil.isAbsoluteUrl("http://www.google.com"));
		assertTrue(MungerUtil.isAbsoluteUrl("http://www.google.com/"));
		assertTrue(MungerUtil.isAbsoluteUrl("https://www.google.com/fo.html"));
		assertTrue(MungerUtil.isAbsoluteUrl("http://www.google.com/fo.html?a=b"));
		assertTrue(MungerUtil.isAbsoluteUrl("https://www.google.com"));

		assertTrue(MungerUtil.isAbsoluteUrl("ftp://www.google.com"));
		assertTrue(MungerUtil.isAbsoluteUrl("ssh://www.google.com"));
		assertTrue(MungerUtil.isAbsoluteUrl("mailto:jason@onlyasurvey.com"));
	}

	@Test
	public void testIsAbsoluteUrl_Success_WithNonAbsolute() {
		assertFalse(MungerUtil.isAbsoluteUrl("I'm a happy penguin"));
		assertFalse(MungerUtil.isAbsoluteUrl("/foo.html"));
		assertFalse(MungerUtil.isAbsoluteUrl("http>://foo/bar"));
		assertFalse(MungerUtil.isAbsoluteUrl(""));
		assertFalse(MungerUtil.isAbsoluteUrl(null));
	}

	// ======================================================================

	@Test
	public void testStripContextPath_Success_WithExpectedPath() {

		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		final String contextPath = "/oas";
		final String expected = "/html/db/db.html";
		final String input = contextPath + expected;

		assertEquals("unexpected result", expected, MungerUtil.stripContextPath(input));
	}

	@Test
	public void testStripContextPath_Success_WithOtherPath() {

		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		final String contextPath = "/someOtherValue";
		final String input = contextPath + "/html/db/db.html";

		assertEquals("unexpected result", input, MungerUtil.stripContextPath(input));
	}

	@Test
	public void testStripServletPath_Success_WithExpectedPath() {

		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		final String expected = "/db/db.html";
		final String input = MungerFilter.getServletPath() + expected;

		assertEquals("unexpected result", expected, MungerUtil.stripServletPath(input));
	}

	@Test
	public void testStripServletPath_Success_WithOtherPath() {

		MungerFilter.setContextPath("/oas");
		MungerFilter.setServletPath("/html");

		final String expected = "/db/db.html";
		final String input = "/otherServletName" + expected;

		assertEquals("unexpected result", input, MungerUtil.stripServletPath(input));
	}

	// ======================================================================
}
