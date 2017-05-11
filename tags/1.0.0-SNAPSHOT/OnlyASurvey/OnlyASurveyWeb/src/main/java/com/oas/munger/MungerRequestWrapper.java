package com.oas.munger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

public class MungerRequestWrapper extends HttpServletRequestWrapper {

	/** RequestURI cached. */
	private String cachedRequestURI;

	/** Cached parameter values. */
	private HashMap<String, String[]> cachedParameterValues = new HashMap<String, String[]>();

	private String cachedPathInfo;

	// ======================================================================

	/**
	 * Default constructor.
	 * 
	 * @param request
	 *            HttpServletRequest
	 */
	public MungerRequestWrapper(HttpServletRequest request) {
		super(request);

		// pre-cache
		getRequestURI();

		cacheParameters();
	}

	// ======================================================================

	private void cacheParameters() {

		@SuppressWarnings("unchecked")
		Enumeration<String> superNames = super.getParameterNames();

		while (superNames.hasMoreElements()) {
			String key = superNames.nextElement();
			String values[] = EncoderDecoder.decode(super.getParameterValues(key));

			// stuff into cache
			String decodedKey = EncoderDecoder.decode(key);
			cachedParameterValues.put(decodedKey, values);
		}
	}

	// ======================================================================

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(cachedParameterValues.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getParameterValues(String key) {
		return cachedParameterValues.get(EncoderDecoder.decode(key));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParameter(String key) {

		String[] values = this.getParameterValues(key);

		if (values == null) {
			return null;
		}

		if (values.length > 1) {
			return StringUtils.join(values, ",");
		}

		return values[0];

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPathInfo() {
		String parentPathInfo = super.getPathInfo();
		String retval = parentPathInfo;

		if (parentPathInfo == null) {
			return parentPathInfo;
		}

		if (null != cachedPathInfo) {
			return cachedPathInfo;
		}

		String lastPart = parentPathInfo.substring(parentPathInfo.lastIndexOf("/") + 1);
		Assert.hasText(lastPart);

		if (lastPart.startsWith("0x")) {
			retval = parentPathInfo.substring(0, parentPathInfo.length() - lastPart.length());
			retval += EncoderDecoder.decode(lastPart);
		}

		retval = MungerUtil.stripDoubleLeadingSlashes(retval);

		cachedPathInfo = retval;
		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRequestURI() {

		if (cachedRequestURI != null) {
			return cachedRequestURI;
		}

		String retval = super.getRequestURI();

		int index = retval.lastIndexOf("0x");

		if (index != -1) {
			String start = retval.substring(0, index);
			String lastPart = retval.substring(index);

			Assert.isTrue(lastPart.lastIndexOf("/") == -1, "expected decoding only at end");
			retval = start + EncoderDecoder.decode(lastPart);
			// throw new RuntimeException(retval);
		}

		int queryStringIndex = retval.indexOf("?");
		if (queryStringIndex != -1) {
			addParametersFromEncodedUrl(retval.substring(queryStringIndex + 1));
			retval = retval.substring(0, queryStringIndex);
		}

		cachedRequestURI = retval;
		return retval;
	}

	/**
	 * Parses a key=value&key=value set to add parameters, eg., when included in
	 * an encoded URI.
	 * 
	 * @param url
	 */
	private void addParametersFromEncodedUrl(String url) {
		Assert.hasText(url, "passing null URL is not supported");
		Assert.isTrue(!url.startsWith("?"), "? must be removed prior to calling");

		String[] parameters = url.split("&");
		for (String keyValuePair : parameters) {
			String[] array = keyValuePair.split("=");
			String key = EncoderDecoder.decode(array[0]);
			String value = null;
			if (array.length > 1) {
				value = EncoderDecoder.decode(array[1]);
			}

			String[] currentList = cachedParameterValues.get(key);
			if (currentList != null) {
				// append
				List<String> current = new ArrayList<String>(Arrays.asList(currentList));
				current.add(value);
				cachedParameterValues.put(key, current.toArray(new String[0]));
			} else {
				// new value
				cachedParameterValues.put(key, new String[] { value });
			}
		}
	}

	@Override
	public StringBuffer getRequestURL() {

		StringBuffer url = new StringBuffer(getScheme());
		url.append("://").append(getServerName());

		// if not using the default port
		if (80 != getServerPort()) {
			url.append(':').append(getServerPort());
		}

		url.append(getRequestURI());

		return url;
	}

	/**
	 * TODO this doesn't seem like a good strategy - what is the parent doing to
	 * get the value?
	 * 
	 * TODO is munging working coincidentally
	 * 
	 * TODO generate from parameter map and cache
	 */
	@Override
	public String getQueryString() {
		// TODO generate from parameter map and cache
		String retval = super.getQueryString();
		return retval;
	}

	// @Override
	// public String getServletPath() {
	// return super.getServletPath();
	// }

}
