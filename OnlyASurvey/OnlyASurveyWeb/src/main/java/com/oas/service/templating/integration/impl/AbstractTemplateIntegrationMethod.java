package com.oas.service.templating.integration.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.Assert;

import com.oas.model.templating.Template;
import com.oas.service.templating.integration.TemplateIntegrationMethod;

/**
 * Parent for TemplateIntegrationMethod implementations.
 * 
 * @author xhalliday
 * @since January 1, 2009
 */
abstract public class AbstractTemplateIntegrationMethod extends ApplicationObjectSupport implements TemplateIntegrationMethod {

	@Override
	public String runtimeBeforeContent(HttpServletRequest request, final Template template, String beforeContent) {
		return beforeContent;
	}

	@Override
	public String runtimeAfterContent(HttpServletRequest request, final Template template, String afterContent) {
		return afterContent;
	}

	/**
	 * Convert relative URLs to absolute URLs.
	 * 
	 * @param retval
	 * @param markup
	 */
	protected void convertRelativeUrls(Template retval) {
		Assert.hasText(retval.getBaseUrl());

		convertCssUrls(retval);
		convertLinkUrls(retval);
		convertSrcUrls(retval);
	}

	protected void convertCssUrls(Template retval) {

		Assert.notNull(retval);
		Assert.hasText(retval.getBaseUrl());

		{
			// handles url() where the url starts with a "/"
			// @import url("/css/base2.css");
			String regex = "(url\\(/(.*)\\))";
			String replacement = "url(" + retval.getBaseUrl() + "/$2)";

			doRegexReplacement(retval, regex, replacement);
		}

		String relativeUrlPrefix = getRelativeUrlPrefix(retval);
		{
			// handles url() where the url DOES NOT start with a "/" and DOES
			// NOT start with a non-word character (e.g., "." in "../../etc").
			// @import url("css/base2.css");
			String regex = "(url\\((['\"]?)(?!http)([^/].*)\\)(['\"]?))";
			String replacement = "url($2" + relativeUrlPrefix + "/$3$4)";

			doRegexReplacement(retval, regex, replacement);
		}

	}

	protected void convertLinkUrls(Template retval) {

		Assert.notNull(retval);
		Assert.hasText(retval.getBaseUrl());

		String relativeUrlPrefix = getRelativeUrlPrefix(retval);

		// handles "href='/css/foo.*'/>"
		{
			// String regex = "(href=(['\"]{1})/(\\w.*)(['\"]{1}))";
			String regex = "(href=(['\"]{1})/(.*)(['\"]{1}))";
			String replacement = "href=$2" + retval.getBaseUrl() + "/$3$4";

			doRegexReplacement(retval, regex, replacement);
		}

		// handles "href='css/foo.*'/>"
		// ignores "#anchor" links
		{
			String regex = "(href=(['\"]{1})(?!http)(?!#)([^/].*)(['\"]{1}))";
			String replacement = "href=$2" + relativeUrlPrefix + "/$3$4";

			doRegexReplacement(retval, regex, replacement);
		}

		// {
		// String regex = "(href=(['\"]{1})(?!http)(.*)(['\"]{1}))";
		// String replacement = "href=$2" + retval.getBaseUrl() + "$3$4";
		//
		// doRegexReplacement(retval, regex, replacement);
		// }

		{
			String regex = "(href=.(?!http://))(.*)(\\.css)";
			String replacement = "$1" + retval.getBaseUrl() + "$2$3";

			doRegexReplacement(retval, regex, replacement);
		}
	}

	protected void convertSrcUrls(Template retval) {

		Assert.notNull(retval);
		Assert.hasText(retval.getBaseUrl());

		String relativeUrlPrefix = getRelativeUrlPrefix(retval);
		Assert.hasText(relativeUrlPrefix);

		// src="/foo/bar.js"
		// MUST be before the relative version below
		{
			String regex = "(src=.)(/.*)>";
			String replacement = "$1" + retval.getBaseUrl() + "$2>";

			doRegexReplacement(retval, regex, replacement);
		}

		// src="foo/bar.js"
		{
			String regex = "(src=(['\"]{1})(?!http))([^/].*)>";
			String replacement = "$1" + relativeUrlPrefix + "/$3>";

			doRegexReplacement(retval, regex, replacement);
		}
	}

	protected void doRegexReplacement(Template retval, String regex, String replacement) {

		// content before the main body
		String newBefore = retval.getBeforeContent().replaceAll(regex, replacement);
		retval.setBeforeContent(newBefore);

		// content after the main body
		String newAfter = retval.getAfterContent().replaceAll(regex, replacement);
		retval.setAfterContent(newAfter);
	}

	protected String getRelativeUrlPrefix(Template template) {

		String retval = template.getImportedFromUrl();
		if (!retval.endsWith("/")) {
			// e.g., "http://localhost/foo/bar.html
			int lastSlash = retval.lastIndexOf("/");
			if (lastSlash == -1) {
				// makes no sense, really
				// TODO what here?
			} else {
				// e.g., "http://localhost/foo/"
				retval = retval.substring(0, lastSlash);
			}
		}

		return retval;
	}

}
