package com.oas.service.templating.integration.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.oas.model.templating.Template;
import com.oas.service.templating.integration.TemplateIntegrationMethod;

abstract public class AbstractClf2TIMSampleDataTest extends AbstractTIMSampleDataTest {

	/** Used in getMatchingMarkup(). */
	private String markupCache = null;

	@Autowired
	private Clf2TemplateIntegrationMethod integrationMethod;

	// ======================================================================
	// abstract methods
	// ======================================================================

	/**
	 * Return a filename prefix to use to load prefix-header.html and
	 * prefix-header.html file content.
	 */
	abstract public String getSampleFilename();

	/**
	 * Map of old-url to new-url strings that should be replaced in the template
	 * to make them absolute. Absolute URLs should be included in this test data
	 * to ensure they do not get mangled by the processor (ie., key and value
	 * are the same).
	 * 
	 * @return
	 */
	abstract public Map<String, String> getUrlReplacements();

	// ======================================================================

	@Override
	public TemplateIntegrationMethod getIntegrationMethodUnderTest() {
		return integrationMethod;
	}

	@Override
	public String getMatchingMarkup() throws IOException {

		if (StringUtils.hasText(markupCache)) {
			return markupCache;
		}

		File file = new File("src/test/resources/template-integration/clf2/" + getSampleFilename());
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);

		assertTrue("huge sample data", file.length() < 1048576);

		byte[] buffer = new byte[(int) file.length()];

		bis.read(buffer, 0, (int) file.length());
		String retval = new String(buffer);

		markupCache = retval;
		return retval;
	}

	@Override
	public String getNonMatchingMarkup() {
		return "<html><head><title>Some site</title></head><body>some broken markup<p>is whatI'm <font size=72><blink>woot!</font></html>";
	}

	// ======================================================================
	// common tests
	// ======================================================================

	@Test
	public void testUrlsAreAbsolute() throws IOException {

		String markup = getMatchingMarkup();
		Map<String, String> map = getUrlReplacements();

		// ensure all the test URLs do exist in the sample data
		for (String key : map.keySet()) {
			log.error("comparing[1] " + key + " vs " + map.get(key));
			assertTrue("expected pre-convert URL to exist in test data: " + key, markup.contains(key));
		}

		Template template = getIntegrationMethodUnderTest().processMarkup(getBaseUrl(), getImportUrl(), markup);
		assertNotNull("no template returned", template);
		assertNotNull("template has no Before content", template.getBeforeContent());
		assertNotNull("template has no After content", template.getAfterContent());

		// ensure none of the test URLs exist in the sample data, but that all
		// replacements do
		for (String key : map.keySet()) {
			String replacement = map.get(key);
			log.error("checking[2] " + replacement);

			// doesn't work where the URL is embedded in the search string,
			// e.g., "@import url(urlToChange);"
			// assertTrue("expected PRE-convert URL to exist in test data: " +
			// key, template.getBeforeContent().contains(key)
			// || template.getAfterContent().contains(key));
			assertTrue("expected POST-convert URL to exist in test data: " + replacement, template.getBeforeContent().contains(
					replacement)
					|| template.getAfterContent().contains(replacement));
		}

		// for debugging
		if (false) {
			File output = new File("/output.txt.html");
			FileWriter writer = new FileWriter(output);
			writer.write(template.getBeforeContent() + "<h1>Survey on Business</h1><p>Welcome and such!</p>"
					+ template.getAfterContent());
			writer.close();
		}
	}

	/**
	 * TODO support multiple sample data files for different languages; this
	 * tests a known-true condition
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDetermineTemplateLanguage_English() throws IOException {
		// NOTE this is ALWAYS English data, as per sample-data:
		// NOTE if this test fails, check language of sample data
		Template template = getIntegrationMethodUnderTest()
				.processMarkup("http://localhost", getImportUrl(), getMatchingMarkup());
		assertNotNull("no template returned", template);
		assertNotNull("expected some language", template.getSupportedLanguage());
		assertEquals("expected English data", "eng", template.getSupportedLanguage().getIso3Lang());
	}
}
