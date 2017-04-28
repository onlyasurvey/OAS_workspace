package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;

public class ManageQuestionsControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private QuestionsTabController controller;

	// ======================================================================

	@Test
	public void testShowTabView() {

		// authorized
		ModelAndView mav = controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
				+ scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true).getId() + ".html"));
		assertNotNull(mav);
		assertEquals("unexpected view name", "/dashboard/manage/questionManagement/questionsTab", mav.getViewName());
	}

	@Test
	public void testShowTabView_Security_Fail_NoUser() {

		try {
			// no current user
			controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
					+ scenarioDataUtil.createTypicalScenario1(createTestUser(), true).getId() + ".html"));
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testShowTabView_Security_Fail_WrongUser() {

		try {
			// owner is not the same as the current user
			createAndSetSecureUserWithRoleUser();
			controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
					+ scenarioDataUtil.createTypicalScenario1(createTestUser(), true).getId() + ".html"));
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

}
