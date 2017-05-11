package com.oas.controller.enterprise;

import static junit.framework.Assert.assertNotNull;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.web.servlet.ModelAndView;

import ca.inforealm.core.security.GlobalRoles;

import com.oas.AbstractOASBaseTest;
import com.oas.model.ContactUsMessage;
import com.oas.util.EnterpriseRoles;

/**
 * Tests for the "Enterprise Dashboard" controller.
 * 
 * @author xhalliday
 */
public class EnterpriseDashboardControllerTest extends AbstractOASBaseTest {

	@Autowired
	private EnterpriseDashboardController controller;

	@Test
	public void dashboardView() throws Exception {
		createAndSetSecureUser(new String[] { GlobalRoles.ROLE_USER, EnterpriseRoles.ROLE_ENTERPRISE_ADMIN }, null);
		ModelAndView mav = controller.dashboardView(new MockHttpServletRequest());
		assertNotNull("expected mav", mav);

		Map<String, Object> model = mav.getModel();
		assertNotNull("expected model", model);
		assertHasViewName(mav, "/enterprise/dashboard/dashboard");

		Collection<ContactUsMessage> contactMessages = (Collection<ContactUsMessage>) model.get("contactMessages");
		assertNotNull("expected full dump of contact messages (to be deprecated in future version)", contactMessages);
	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void dashboardView_Security_Fail_NoContext() throws Exception {
		controller.dashboardView(new MockHttpServletRequest());
	}

	@Test
	@ExpectedException(AccessDeniedException.class)
	public void dashboardView_Security_Fail_MissingRole() throws Exception {

		createAndSetSecureUserWithRoleUser();
		// no enterprise admin roles
		controller.dashboardView(new MockHttpServletRequest());
	}

}
