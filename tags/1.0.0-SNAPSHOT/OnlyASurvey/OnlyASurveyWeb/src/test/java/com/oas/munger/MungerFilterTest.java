package com.oas.munger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.oas.AbstractOASBaseTest;

/**
 * Test for MungerFilter
 * 
 * @author xhalliday
 * @since February 1, 2009
 */
public class MungerFilterTest extends AbstractOASBaseTest {

	// ======================================================================

	private MungerFilter newFilterInstance() throws ServletException {
		MungerFilter filter = new MungerFilter();
		return filter;
	}

	// ======================================================================

	@Test
	public void testExcludeFromEncoding_SpecialCases() {
		assertTrue(MungerFilter.excludeFromEncoding("/incl/foo.css"));
		assertTrue(MungerFilter.excludeFromEncoding("j_anything"));
	}

	@Test
	public void testExcludeFromEncoding_DisabledFlag() {
		MungerFilter.setDisabled(false);
		assertFalse(MungerFilter.excludeFromEncoding("/html/db/db.html"));

		MungerFilter.setDisabled(true);
		assertTrue(MungerFilter.excludeFromEncoding("/html/db/db.html"));
	}

	// ======================================================================

	/**
	 * Tests that should be unnecessary, but that a) prevent strange
	 * regressions, b) increase coverage
	 * 
	 * @throws ServletException
	 */
	@Test
	public void testSanity() throws ServletException {
		// these should never throw
		newFilterInstance().init(new MockFilterConfig());
		newFilterInstance().destroy();
	}

	// ======================================================================

	@Test
	public void testDoFilter_SetsContextPath_Success() throws IOException, ServletException {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oas/html/db/db.html");
		request.setContextPath("/oas");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// fresh instance
		MungerFilter filter = newFilterInstance();

		// flag is static, so set globally
		MungerFilter.setContextPath(null);
		assertNull("sanity: should be null before the test", MungerFilter.getContextPath());

		// invoke
		filter.doFilter(request, response, new MockFilterChain());

		assertEquals("context path should have been set", "/oas", MungerFilter.getContextPath());
	}

	@Test
	public void testDoFilter_SetsServletPath_Success() throws IOException, ServletException {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oas/html/db/db.html");
		request.setContextPath("/oas");
		request.setServletPath("/html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// fresh instance
		MungerFilter filter = newFilterInstance();

		// flag is static, so set globally
		MungerFilter.setServletPath(null);
		assertNull("sanity: should be null before the test", MungerFilter.getServletPath());

		// invoke
		filter.doFilter(request, response, new MockFilterChain());

		assertEquals("context path should have been set", "/html", MungerFilter.getServletPath());
	}

	// ======================================================================

	@Test
	public void testDoFilter_SetDisabledFlag_Success() throws ServletException, IOException {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oas/html/db/db.html");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// fresh instance
		MungerFilter filter = newFilterInstance();

		// init
		// flag is static, so set globally
		MungerFilter.setDisabled(false);
		assertFalse("sanity failed", MungerFilter.isDisabled());

		// invoke: true
		request.setParameter("MUNGER_DISABLED", "true");
		filter.doFilter(request, response, new MockFilterChain());
		assertTrue("should have been set TRUE", MungerFilter.isDisabled());

		// invoke: false
		request.setParameter("MUNGER_DISABLED", "false");
		filter.doFilter(request, response, new MockFilterChain());
		assertFalse("should have been set FALSE", MungerFilter.isDisabled());

		// invoke: anything-but-true
		request.setParameter("MUNGER_DISABLED", "happy place");
		filter.doFilter(request, response, new MockFilterChain());
		assertFalse("should have been set FALSE", MungerFilter.isDisabled());

	}

	// ======================================================================

}
