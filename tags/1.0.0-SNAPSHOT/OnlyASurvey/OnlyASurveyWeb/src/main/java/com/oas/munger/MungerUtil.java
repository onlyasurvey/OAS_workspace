package com.oas.munger;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility methods used by munger components.
 * 
 * @author xhalliday
 * @since January 15, 2009
 */
abstract public class MungerUtil {

	/**
	 * Strip any query string.
	 * 
	 * @param url
	 * @return
	 */
	public static String stripQueryString(String url) {

		if (StringUtils.hasText(url)) {
			int index = url.indexOf("?");
			if (-1 != index) {
				return url.substring(0, index);
			}
		}

		return url;
	}

	/**
	 * Strips everything after the last "/".
	 * 
	 * @param url
	 * @return
	 */
	public static String stripAfterLastSlash(String url) {
		if (url.indexOf("/") == -1) {
			return url;
		}

		return url.substring(0, url.lastIndexOf("/") + 1);
	}

	public static String stripContextPath(String url) {
		url = stripDoubleLeadingSlashes(url);
		if (url.startsWith(MungerFilter.getContextPath())) {
			return url.substring(MungerFilter.getContextPath().length());
		}

		return url;
	}

	public static String stripServletPath(String url) {
		url = stripDoubleLeadingSlashes(url);
		if (url.startsWith(MungerFilter.getServletPath())) {
			return url.substring(MungerFilter.getServletPath().length());
		}

		return url;
	}

	/**
	 * Ensures the URL does not start with a slash - "/".
	 * 
	 * @param url
	 * @return
	 */
	public static String stripAllLeadingSlashes(String url) {
		String retval = url;
		int sanity = 0;
		while (retval.startsWith("/")) {
			retval = retval.substring(1);
			sanity++;
			Assert.isTrue(sanity < 50, "too many double-slashes in URL.");
		}
		return retval;
	}

	/**
	 * Ensures the URL does not start with a double-slash - "//".
	 * 
	 * @param url
	 * @return
	 */
	public static String stripDoubleLeadingSlashes(String url) {
		String retval = url;
		int sanity = 0;
		while (retval.startsWith("//")) {
			retval = retval.substring(1);
			sanity++;
			Assert.isTrue(sanity < 50, "too many double-slashes in URL.");
		}
		return retval;
	}

	/**
	 * From: org.apache.taglibs.standard.tag.common.core.ImportSupport
	 * 
	 * <p>
	 * Valid characters in a scheme.
	 * </p>
	 * <p>
	 * RFC 1738 says the following:
	 * </p>
	 * <blockquote> Scheme names consist of a sequence of characters. The lower
	 * case letters "a"--"z", digits, and the characters plus ("+"), period
	 * ("."), and hyphen ("-") are allowed. For resiliency, programs
	 * interpreting URLs should treat upper case letters as equivalent to lower
	 * case in scheme names (e.g., allow "HTTP" as well as "http").
	 * </blockquote>
	 * <p>
	 * We treat as absolute any URL that begins with such a scheme name,
	 * followed by a colon.
	 * </p>
	 */
	public static final String VALID_SCHEME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

	/**
	 * Returns <tt>true</tt> if our current URL is absolute, <tt>false</tt>
	 * otherwise.
	 * 
	 * @param url
	 *            the string to check
	 * @return if this is an absolute URL or not
	 */
	public static boolean isAbsoluteUrl(String url) {
		// a null URL is not absolute, by our definition
		if (url == null) {
			return false;
		}

		// do a fast, simple check first
		int colonPos = url.indexOf(":");
		if (colonPos == -1) {
			return false;
		}
		// if we DO have a colon, make sure that every character
		// leading up to it is a valid scheme character
		for (int i = 0; i < colonPos; i++) {
			if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1) {
				return false;
			}
		}

		// if so, we've got an absolute url
		return true;
	}
}
