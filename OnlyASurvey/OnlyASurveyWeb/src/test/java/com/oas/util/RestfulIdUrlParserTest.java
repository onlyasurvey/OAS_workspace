package com.oas.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RestfulIdUrlParserTest {

	private static final Long MAGIC_NUMBER = 55L;
	private static final String VALID_URL[] = new String[] { "http://foo.bar/a/b/c/" + MAGIC_NUMBER + ".html", "/55.html" };
	private static final String INVALID_URL[] = new String[] { "http://foo.bar/a/b/c/wecome.html", "/page.html", "5.5.html",
			"/55", "http://foo/bar/5923.html/" };

	private static final String VALID_PATH_PREFIX = "http://foo.bar/oas/content/marketing/blog/";
	private static final String VALID_PATH_CONTENT_PART = "20090128.html";
	private static final String VALID_PATH_URL = VALID_PATH_PREFIX + VALID_PATH_CONTENT_PART;
	private static final String VALID_POSITIONAL_PATH_URL = "/1/2/3.html";

	private void expectParseResult(String inputUrl, boolean expectNull) {
		Long id = RestfulIdUrlParser.parseTrailingId(inputUrl);
		assertEquals("unexpected result from parser for URL: " + inputUrl + " and return value: " + id, expectNull, id == null);
	}

	private void expectParsedId(String inputUrl, Long expectId) {
		Long id = RestfulIdUrlParser.parseTrailingId(inputUrl);
		assertEquals("unexpected result from parser", expectId, id);
	}

	private void expectParsedPath(String inputUrl, String expectedUrl, boolean includeExtension) {
		String path = RestfulIdUrlParser.parseLastPathPart(inputUrl, includeExtension);
		assertEquals("unexpected result from parser", expectedUrl, path);
	}

	@Test
	public void parseTrailingId_Success_PositionalValue() {
		assertEquals("unexpected result", Long.valueOf(3), RestfulIdUrlParser.parseTrailingId(VALID_POSITIONAL_PATH_URL));
		assertEquals("unexpected result", Long.valueOf(3), RestfulIdUrlParser.parseTrailingId(VALID_POSITIONAL_PATH_URL, 0));
		assertEquals("unexpected result", Long.valueOf(2), RestfulIdUrlParser.parseTrailingId(VALID_POSITIONAL_PATH_URL, 1));
		assertEquals("unexpected result", Long.valueOf(1), RestfulIdUrlParser.parseTrailingId(VALID_POSITIONAL_PATH_URL, 2));
	}

	@Test
	public void all_Success_TrailingValue() {

		for (String url : VALID_URL) {
			expectParseResult(url, false);
			expectParsedId(url, MAGIC_NUMBER);
			expectParsedPath(VALID_PATH_URL, VALID_PATH_CONTENT_PART, true);
		}
	}

	@Test
	public void fail_InvalidUrl() {

		for (String url : INVALID_URL) {
			expectParseResult(url, true);
			expectParsedId(url, null);
		}
	}
}
