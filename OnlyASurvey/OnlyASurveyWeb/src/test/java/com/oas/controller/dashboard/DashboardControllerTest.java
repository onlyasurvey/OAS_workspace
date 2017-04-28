package com.oas.controller.dashboard;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;

public class DashboardControllerTest extends AbstractOASBaseTest {

	@Autowired
	private DashboardController controller;

	@Test
	public void testReferenceData_Success() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		createAndSetSecureUserWithRoleUser();
		ModelAndView mav = controller.doMain(request);
		assertNotNull(mav);
		// assertNotNull(mav.getModel().get("draftList"));
		// assertNotNull(mav.getModel().get("publishedList"));
		assertNotNull(mav.getModel().get("summaryList"));
		assertIsNull("enterprise-quick-stats MUST NOT be populated for non-ROLE_ENTERPRISE_ADMIN", mav.getModel().get(
				"enterpriseQuickStats"));
	}

	@Test
	public void testReferenceData_FailWithoutSecurityContext() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		try {
			controller.doMain(request);
			fail("should have thrown security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}
}
