package com.oas.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;

public class HomePageControllerTest extends AbstractOASBaseTest {

	@Autowired
	private HomePageController controller;

	@Test
	public void testShowsFormForAnonUser() throws Exception {
		ModelAndView mav = controller.homePage(new MockHttpServletRequest());
		assertNotNull(mav);
		assertNotNull(mav.getViewName());
		assertEquals("wrong view name", "/homePage", mav.getViewName());

		// ensure that the Public Content has been loaded
		assertModelHasAttribute(mav, "pageContent");
		String pageContent = (String) mav.getModel().get("pageContent");
		assertHasText("unable to load /homepage from Public Site Content: is zero length", pageContent);
	}
}
