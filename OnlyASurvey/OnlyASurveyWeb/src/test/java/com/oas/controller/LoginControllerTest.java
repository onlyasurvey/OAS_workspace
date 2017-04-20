package com.oas.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.oas.AbstractOASBaseTest;

public class LoginControllerTest extends AbstractOASBaseTest {

	@Autowired
	private LoginController controller;

	@Test
	public void testShowsFormForAnonUser() throws Exception {
		ModelAndView mav = controller.loginForm(new MockHttpServletRequest());
		assertNotNull(mav);
		assertNotNull(mav.getViewName());
		assertEquals("wrong view name", "/loginForm", mav.getViewName());
	}

	@Test
	public void testRedirectsForAuthenticatedUser() throws Exception {
		// new MockHttpServletRequest(), new MockHttpServletResponse()
		createAndSetSecureUserWithRoleUser();
		ModelAndView mav = controller.loginForm(null);
		assertNotNull(mav);
		assertNull(mav.getViewName());
		assertNotNull(mav.getView());
		assertTrue("not a redirect", RedirectView.class.isAssignableFrom(mav.getView().getClass()));
	}

}
