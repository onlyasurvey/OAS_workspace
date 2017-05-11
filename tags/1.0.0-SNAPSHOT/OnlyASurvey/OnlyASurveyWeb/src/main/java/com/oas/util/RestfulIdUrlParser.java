package com.oas.util;

import java.util.StringTokenizer;

import org.springframework.util.Assert;

/**
 * Parses a "RESTful ID URL" as defined below to return the inlined ID.
 * 
 * Format: [http://etc]/55.html
 * 
 * @author Jason Halliday
 * @since September 8, 2008
 */
abstract public class RestfulIdUrlParser {

	/**
	 * Parse a RESTful URL from the inputUrl - eg., /prefix/123.html returns
	 * Long(123). Allows setting how many URL parts to go back, so
	 * /prefix/1/2/3.html with position=2 would return "1", with 0 would return
	 * "3.html"
	 * 
	 * @param inputUrl
	 * @return
	 */
	public static Long parseTrailingId(String inputUrl, int position) {

		Assert.hasLength(inputUrl);

		// have a string that starts with "/" and ends with ".html"
		String lastPart = parseLastPathPart(inputUrl, position);
		try {
			// return the number part, if it's a valid number
			return new Long(lastPart);
		} catch (NumberFormatException e) {
			// not valid
			return null;
		}
	}

	/**
	 * Parse a RESTful URL from the inputUrl - eg., /prefix/123.html returns
	 * Long(123).
	 * 
	 * @param inputUrl
	 * @return
	 */
	public static Long parseTrailingId(String inputUrl) {

		Assert.hasLength(inputUrl);

		// have a string that starts with "/" and ends with ".html"
		String lastPart = parseLastPathPart(inputUrl);
		try {
			// return the number part, if it's a valid number
			return new Long(lastPart);
		} catch (NumberFormatException e) {
			// not valid
			return null;
		}
	}

	public static String parseLastPathPart(String inputUrl) {
		// return parseLastPathPart(inputUrl, 0, false);
		return parseLastPathPart(inputUrl, 0);
	}

	public static String parseLastPathPart(String inputUrl, int position) {
		// return parseLastPathPart(inputUrl, position, false);
		return parseLastPathPart(inputUrl, position, false);
	}

	public static String parseLastPathPart(String inputUrl, boolean includeExtension) {
		return parseLastPathPart(inputUrl, 0, includeExtension);
	}

	/**
	 * Parse out the last part of the path between the last "/" and the last
	 * ".".
	 * 
	 * @param inputUrl
	 * @return
	 */
	public static String parseLastPathPart(String inputUrl, int position, boolean includeExtension) {

		//
		String retval = null;

		//
		Assert.hasLength(inputUrl);
		Assert.isTrue((!includeExtension) || (includeExtension && position == 0),
				"includeExtension==true only valid when position=0");

		if (!includeExtension) {
			if (inputUrl.endsWith("/")) {
				// not valid
				return null;
			}

			int trailingDot = inputUrl.lastIndexOf(".");
			if (trailingDot != -1) {
				inputUrl = inputUrl.substring(0, trailingDot);
			} else {
				// not valid for the "includeExtension" mode, because there is
				// none (i.e., "/55.html" is valid here, "/55" is not
				return null;
			}
		}

		StringTokenizer tok = new StringTokenizer(inputUrl, "/");
		Assert.isTrue(tok.countTokens() > 0, "unable to parse URL");
		Assert.isTrue(tok.countTokens() > position, "unable to parse URL: has fewer tokens than requested");

		// eg
		// /html/db/mgt/ct/25/100.html
		// position=1, count = 6, targetPosition = 6-1 = 5 - 1 = 4
		int targetPosition = tok.countTokens() - position - 1;

		int count = 0;
		while (tok.hasMoreTokens()) {
			String value = tok.nextToken();
			if (count == targetPosition) {
				retval = value;
				break;
			}
			count++;
		}

		//
		Assert.notNull(retval, "unable to parse token");
		return retval;

		// int trailingSlash = inputUrl.lastIndexOf("/");
		// int trailingDot = inputUrl.lastIndexOf(".");
		//
		// // missing a "/" and ".html", or the ".html" is before the final "/"
		// if (trailingDot == -1 || trailingSlash == -1 || (trailingDot <
		// trailingSlash)) {
		// // not valid
		// return null;
		// }
		//
		// // do not remove the extension if this flag is on
		// if (includeExtension) {
		// trailingDot = inputUrl.length();
		// }
		// String lastPart = inputUrl.substring(trailingSlash + 1, trailingDot);
		// return lastPart;
	}

}
