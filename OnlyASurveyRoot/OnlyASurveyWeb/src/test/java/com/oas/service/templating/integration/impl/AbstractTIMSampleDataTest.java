package com.oas.service.templating.integration.impl;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import com.oas.AbstractOASBaseTest;
import com.oas.model.templating.Template;
import com.oas.service.templating.integration.TemplateIntegrationMethod;

/**
 * Runs standard tests on any TemplateIntegrationMethod (aka TIM).
 * 
 * @author xhalliday
 * @since December 6, 2008
 */
abstract public class AbstractTIMSampleDataTest extends AbstractOASBaseTest {

	// ======================================================================
	// abstract methods
	// ======================================================================

	/**
	 * Return a reference to the TemplateIntegrationMethod that the subclass is
	 * testing.
	 * 
	 * @return
	 */
	abstract public TemplateIntegrationMethod getIntegrationMethodUnderTest();

	/**
	 * Get the Base URL that should be applied to generated templates.
	 * 
	 * @return
	 */
	abstract public String getBaseUrl();

	/**
	 * Get the Import URL that was used to download the template markup -
	 * required for "../../css/base.css" type URLs.
	 * 
	 * @return
	 */
	abstract public String getImportUrl();

	/**
	 * Return sample markup which should match this TIM.
	 * 
	 * @return
	 */
	abstract public String getMatchingMarkup() throws Exception;

	/**
	 * Return sample markup which should never match this TIM.
	 * 
	 * @return
	 */
	abstract public String getNonMatchingMarkup() throws Exception;

	// ======================================================================
	// common tests
	// ======================================================================

	@Test
	public void testNonMatchingMarkupFails() throws Exception {
		try {
			getIntegrationMethodUnderTest().processMarkup(getNonMatchingMarkup(), getImportUrl(), getBaseUrl());
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testMatchingMarkupFails() throws Exception {
		String markup = getMatchingMarkup();
		Errors errors = new BindException(markup, "markup");
		getIntegrationMethodUnderTest().canProcess(markup, errors);
		assertFalse("should be able to process markup", errors.hasErrors());

		Template template = getIntegrationMethodUnderTest().processMarkup(getBaseUrl(), getImportUrl(), markup);
		assertNotNull("expected integration method to return a template", template);
	}
}
