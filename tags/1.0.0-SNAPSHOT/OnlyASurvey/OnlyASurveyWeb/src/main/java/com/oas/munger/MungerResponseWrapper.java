package com.oas.munger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class MungerResponseWrapper extends HttpServletResponseWrapper {

	/**
	 * 
	 * @param response
	 *            HttpServletResponse
	 */
	public MungerResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String encodeRedirectURL(String url) {

		// default impl
		String retval = super.encodeRedirectURL(url);

		if (MungerUtil.isAbsoluteUrl(retval)) {
			// TODO consider encoding here, but be careful not to add
			// context/servlet path prefix
			return retval;
		}

		//
		// String newQueryString = "";
		String anchor = "";

		//
		// int queryStringIndex = url.lastIndexOf("?");
		// String queryString = url.substring(queryStringIndex + 1);

		//
		// if (queryString.indexOf("#") != -1) {
		// int anchorIndex = queryString.lastIndexOf("#");
		// // has anchor
		// anchor = "#" + queryString.substring(anchorIndex);
		// queryString = queryString.substring(0, anchorIndex + 1);
		// }
		//
		if (retval.indexOf("#") != -1) {
			int anchorIndex = retval.lastIndexOf("#");
			// has anchor
			anchor = retval.substring(anchorIndex);
			retval = retval.substring(0, anchorIndex);
		}

		// if (queryStringIndex != -1) {
		//
		// if (queryString.indexOf("&") == -1) {
		// // for splitting
		// queryString += "&";
		// }
		//
		// // for each key:value pair in the query string...
		// String[] params = StringUtils.split(queryString, "&");
		// for (String keyValuePair : params) {
		// String[] keyValue = StringUtils.split(keyValuePair, "=");
		// if (keyValue != null && keyValue.length != 0 &&
		// keyValue[0].startsWith("0x") == false) {
		// String key = EncoderDecoder.encode(keyValue[0]);
		// String value = keyValue[1];
		//
		// newQueryString += key + "=" + value + "&";
		// }
		// }
		//
		// retval = retval.substring(0, queryStringIndex) + "?" +
		// newQueryString;
		// }

		retval = MungerUtil.stripDoubleLeadingSlashes(retval);

		String keepPrefix = MungerFilter.getContextPath() + MungerFilter.getServletPath() + "/";
		if (retval.startsWith(keepPrefix)) {
			retval = keepPrefix
					+ EncoderDecoder.encode(MungerUtil.stripDoubleLeadingSlashes(retval.substring(keepPrefix.length())));
		} else {
			retval = EncoderDecoder.encode(retval);
		}

		// add any #anchor
		retval += anchor;

		return retval;
	}

}
