package com.oas.service.templating.integration.impl;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.oas.model.templating.Template;

public class PWGSCSampleDataIntegrationMethodTest extends AbstractClf2TIMSampleDataTest {

	Map<String, String> urlReplacementMap = new HashMap<String, String>();

	public PWGSCSampleDataIntegrationMethodTest() {
		// relative URLs
		urlReplacementMap.put("/clf20/css/base-institution.css", getBaseUrl() + "/clf20/css/base-institution.css");
		urlReplacementMap.put("/clf20/images/wmms.gif", getBaseUrl() + "/clf20/images/wmms.gif");
		urlReplacementMap.put("/clf20/images/sig-eng.gif", getBaseUrl() + "/clf20/images/sig-eng.gif");
		urlReplacementMap.put("/comm/aide-help-eng.html", getBaseUrl() + "/comm/aide-help-eng.html");
		urlReplacementMap.put("/cgi-bin/language.pl", getBaseUrl() + "/cgi-bin/language.pl");

		// absolute URLs
		urlReplacementMap.put("http://www.canada.gc.ca/home.html", "http://www.canada.gc.ca/home.html");
		urlReplacementMap.put("http://recherche-search.gc.ca/s_r?s5t34d=tpsgcpwgsc&amp;t3mpl1t34d=1&amp;l7c1l3=eng",
				"http://recherche-search.gc.ca/s_r?s5t34d=tpsgcpwgsc&amp;t3mpl1t34d=1&amp;l7c1l3=eng");
	}

	@Override
	public String getBaseUrl() {
		return "http://www.tpsgc-pwgsc.gc.ca";
	}

	@Override
	public String getImportUrl() {
		return "http://www.tpsgc-pwgsc.gc.ca/comm/index-eng.html";
	}

	@Override
	public Map<String, String> getUrlReplacements() {
		return urlReplacementMap;
	}

	@Override
	public String getSampleFilename() {
		return "pwgsc-integration-sample.html";
	}

	@Test
	public void testAnchorLink() throws IOException {
		String markup = getMatchingMarkup();
		Template template = getIntegrationMethodUnderTest().processMarkup(getBaseUrl(), getImportUrl(), markup);

		final String find = "\"#tphp\"";
		assertTrue("starting markup must contain the anchor", markup.contains(find));
		assertTrue("ending markup must contain the same anchor", template.getAfterContent().contains(find));

	}
}
