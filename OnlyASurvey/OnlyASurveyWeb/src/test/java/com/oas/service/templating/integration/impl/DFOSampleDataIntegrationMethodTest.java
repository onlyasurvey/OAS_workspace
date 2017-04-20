package com.oas.service.templating.integration.impl;

import java.util.HashMap;
import java.util.Map;

public class DFOSampleDataIntegrationMethodTest extends AbstractClf2TIMSampleDataTest {

	Map<String, String> urlReplacementMap = new HashMap<String, String>();

	public DFOSampleDataIntegrationMethodTest() {
		// relative URLs
		urlReplacementMap.put("css/base-institution.css", getBaseUrl() + "/css/base-institution.css");
		urlReplacementMap.put("/Contact-eng.htm", getBaseUrl() + "/Contact-eng.htm");
		urlReplacementMap.put("css/pf-if.css", getBaseUrl() + "/css/pf-if.css");
		urlReplacementMap.put("/scripts/language.asp", getBaseUrl() + "/scripts/language.asp");
		urlReplacementMap.put("/images/sig-eng.gif", getBaseUrl() + "/images/sig-eng.gif");
		urlReplacementMap.put("@import url(\"css/base2.css\");", "@import url(\"" + getBaseUrl() + "/css/base2.css\");");
		urlReplacementMap
				.put("<link href=\"css/base.css\" media=\"screen, print\" rel=\"stylesheet\" type=\"text/css\" />",
						"<link href=\"http://www.dfo-mpo.gc.ca/css/base.css\" media=\"screen, print\" rel=\"stylesheet\" type=\"text/css\" />");

		// absolute URLs
		urlReplacementMap
				.put(
						"http://recherche-search.gc.ca/s_r?t3mpl1t34d=1&amp;s5t34d=dfo&amp;l7c1l3=eng&amp;S_08D4T.1ct57n=form&amp;S_08D4T.s3rv5c3=basic&amp;S_F8LLT2XT=&amp;S_S20RCH.l1ng91g3=eng",
						"http://recherche-search.gc.ca/s_r?t3mpl1t34d=1&amp;s5t34d=dfo&amp;l7c1l3=eng&amp;S_08D4T.1ct57n=form&amp;S_08D4T.s3rv5c3=basic&amp;S_F8LLT2XT=&amp;S_S20RCH.l1ng91g3=eng");
		urlReplacementMap.put("http://www.canada.gc.ca/home.html", "http://www.canada.gc.ca/home.html");
	}

	@Override
	public String getBaseUrl() {
		return "http://www.dfo-mpo.gc.ca";
	}

	@Override
	public String getImportUrl() {
		return "http://www.dfo-mpo.gc.ca/acts-loi-eng.htm";
	}

	@Override
	public Map<String, String> getUrlReplacements() {
		return urlReplacementMap;
	}

	@Override
	public String getSampleFilename() {
		return "dfo-integration-sample.html";
	}

}
