package com.oas.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;

public class ContentPageControllerTest extends AbstractOASBaseTest {

	/** Class under test. */
	@Autowired
	private ContentPageController controller;

	@Test
	public void testTermsAndConditions() {
		assertHasViewName(controller.termsAndConditions(), "/content/termsAndConditions");
	}

	@Test
	public void testPrivacyPolicy() {
		assertHasViewName(controller.privacyPolicy(), "/content/privacyPolicy");
	}
}
