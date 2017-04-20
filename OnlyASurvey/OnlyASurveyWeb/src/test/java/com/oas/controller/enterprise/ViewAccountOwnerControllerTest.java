package com.oas.controller.enterprise;

import static junit.framework.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;

import com.oas.AbstractOASBaseTest;
import com.oas.controller.enterprise.user.ViewAccountOwnerController;
import com.oas.util.EnterpriseRoles;

/**
 * Tests for the "View Account Owner" controller.
 * 
 * @author xhalliday
 */

public class ViewAccountOwnerControllerTest extends AbstractOASBaseTest {

	@Autowired
	private ViewAccountOwnerController controller;

	@Test
	public void viewAccountOwner_Success() throws Exception {
		UserAccount user = createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER, EnterpriseRoles.ROLE_ENTERPRISE_ADMIN },
				null);
		ModelAndView mav = controller.viewAccountOwner(new MockHttpServletRequest("GET", "/prefix/" + user.getId() + ".html"));
		assertNotNull("expected mav", mav);

		Map<String, Object> model = mav.getModel();
		assertNotNull("expected model", model);
		assertHasViewName(mav, "/enterprise/accountOwner/viewAccountOwner");

		assertNotNull("expected subject", model.get("subject"));
		assertNotNull("expected surveyList", model.get("surveyList"));
	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void dashboardView_Security_Fail_NoContext() throws Exception {
		// no context
		controller.viewAccountOwner(new MockHttpServletRequest("GET", "/prefix/" + createTestUser().getId() + ".html"));
	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void dashboardView_Security_Fail_MissingRole() throws Exception {

		// no enterprise admin roles
		createAndSetSecureUserWithRoleUser();
		controller.viewAccountOwner(new MockHttpServletRequest("GET", "/prefix/" + createTestUser().getId() + ".html"));
	}

}
