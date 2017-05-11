package ca.inforealm.coreman.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.controller.LoginController;

public class LoginControllerTest extends AbstractSaneManBaseTest {

	@Autowired
	private LoginController loginController;

	public void testLogin_ShowFormWhenNotLoggedIn() throws Exception {

		// clear security context
		clearSecurityContext();

		ModelAndView mav = loginController.doLogin(new MockHttpServletRequest(), new MockHttpServletResponse());

		assertNotNull("no mav", mav);
		assertNotNull("no viewName", mav.getViewName());

		assertEquals("should show login form", "/loginForm", mav.getViewName());
	}

	public void testLogin_RedirectOnAlreadyLoggedIn() throws Exception {

		// initialize security context
		createAndSetSecureUser(new String[] { GlobalRoles.ROLE_APPLICATION_ADMIN }, null);
		ModelAndView mav = loginController.doLogin(new MockHttpServletRequest(), new MockHttpServletResponse());

		assertNotNull("no mav", mav);
		assertNotNull("no view", mav.getView());
		assertTrue("should be a RediretView", mav.getView() instanceof RedirectView);

		RedirectView rv = (RedirectView) mav.getView();
		assertEquals("wrong redirect url", "/app/main.html", rv.getUrl());
	}
}
