package com.oas.service.templating.integration.impl;

import java.util.HashMap;
import java.util.Map;

public class CRASampleDataIntegrationMethodTest extends AbstractClf2TIMSampleDataTest {

	Map<String, String> urlReplacementMap = new HashMap<String, String>();

	public CRASampleDataIntegrationMethodTest() {
		// relative URLs
		urlReplacementMap.put("/cntct/menu-eng.html", getBaseUrl() + "/cntct/menu-eng.html");
		urlReplacementMap.put("/gncy/frnss/rights-eng.html", getBaseUrl() + "/gncy/frnss/rights-eng.html");
		urlReplacementMap.put("/css/3col.css", getBaseUrl() + "/css/3col.css");
		urlReplacementMap.put("/scripts/pe-ap.js", getBaseUrl() + "/scripts/pe-ap.js");
		urlReplacementMap.put("/images/clf2/sig-eng.gif", getBaseUrl() + "/images/clf2/sig-eng.gif");

		// @import URLs
		urlReplacementMap.put("@import url(/css/base2.css)", "@import url(" + getBaseUrl() + "/css/base2.css);");

		// absolute URLs
		urlReplacementMap.put("http://www.canada.gc.ca/main_e.html", "http://www.canada.gc.ca/main_e.html");
		urlReplacementMap.put("http://www.taxpayersrights.gc.ca/menu-eng.html", "http://www.taxpayersrights.gc.ca/menu-eng.html");
	}

	@Override
	public String getBaseUrl() {
		return "http://www.cra-arc.gc.ca";
	}

	@Override
	public String getImportUrl() {
		return "http://www.cra-arc.gc.ca/menu-eng.html";
	}

	@Override
	public Map<String, String> getUrlReplacements() {
		return urlReplacementMap;
	}

	@Override
	public String getSampleFilename() {
		return "cra-integration-sample.html";
	}

}
