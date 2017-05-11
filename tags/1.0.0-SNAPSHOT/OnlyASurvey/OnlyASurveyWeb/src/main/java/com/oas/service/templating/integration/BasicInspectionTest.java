package com.oas.service.templating.integration;

import java.util.List;

/**
 * Defines a method for inspecting user-supplied content and testing which
 * template integration methods appear viable.
 * 
 * @author xhalliday
 * @since December 6, 2008
 */
public interface BasicInspectionTest {

	/**
	 * Inspect the specified markup and return a list of
	 * TemplateIntegrationMethod's that would be viable for it.
	 * 
	 * @param markup
	 * @return
	 */
	List<TemplateIntegrationMethod> getIntegrationMethods(String markup);
}
