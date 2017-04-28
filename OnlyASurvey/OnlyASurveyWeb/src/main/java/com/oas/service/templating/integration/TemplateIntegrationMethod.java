package com.oas.service.templating.integration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.Errors;

import com.oas.model.templating.Template;

/**
 * Defines a method for integrating a user-supplied template.
 * 
 * @author xhalliday
 * @since December 6, 2008
 */
public interface TemplateIntegrationMethod {

	/**
	 * Determine if this integration method can successfully process the given
	 * markup.
	 * 
	 * @param markup
	 *            The markup to test
	 * 
	 * @param errors
	 *            If unable to process, indicates why.
	 * 
	 * @return
	 */
	void canProcess(String markup, Errors errors);

	/**
	 * Process the given markup, returning a (non-persistent) Template object
	 * that be persisted and used.
	 * 
	 * @param markup
	 *            Markup text, typically X/HTML.
	 * 
	 * @param importUrl
	 *            The URL the markup was downloaded from, used for
	 *            "../../css/base.css" type URLs.
	 * 
	 * @return Template on success, or null if the markup could not be
	 *         integrated
	 */
	Template processMarkup(String baseUrl, String importUrl, String markup);

	/**
	 * Called at runtime to do any request-specific transformation of the
	 * beginContent part of the template.
	 * 
	 * @param request
	 *            Runtime HTTP request
	 * @param beforeContent
	 *            Current beforeContent buffer to potentially modify and then
	 *            return
	 * @param template
	 *            The template; immutable; do not depend on it's beforeContent
	 * @return
	 */
	String runtimeBeforeContent(HttpServletRequest request, final Template template, String beforeContent);

	/**
	 * Called at runtime to do any request-specific transformation of the
	 * afterContent part of the template.
	 * 
	 * @param request
	 *            Runtime HTTP request
	 * @param afterContent
	 *            Current beforeContent buffer to potentially modify and then
	 *            return
	 * @param template
	 *            The template; immutable; do not depend on it's afterContent
	 * @return
	 */
	String runtimeAfterContent(HttpServletRequest request, final Template template, String afterContent);

}
