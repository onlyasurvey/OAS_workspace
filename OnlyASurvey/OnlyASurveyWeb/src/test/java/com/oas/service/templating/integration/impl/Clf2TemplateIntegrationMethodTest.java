package com.oas.service.templating.integration.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.oas.AbstractOASBaseTest;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.templating.Template;

public class Clf2TemplateIntegrationMethodTest extends AbstractOASBaseTest {

	@Autowired
	private Clf2TemplateIntegrationMethod integrationMethod;

	private String[] linksToFrenchPage = { "<title>far</title>\nprefix stuff\non that note <a lang='fr' xml:lang='fr' title='Fran&ccedil;ais - Version fran&ccedil;aise de cette page'\n href='/scripts/language.asp'>Fran&ccedil;ais</a> and such\nyaynewlines" };

	@Test
	public void testConvertRelativeUrls_StartingWithSlash() {

		String before = "<link href=\"/styles/foo.css\" rel='stylesheet' type=\"text/css\"/>";
		String after = "<link href=\"http://localhost/styles/foo.css\" rel='stylesheet' type=\"text/css\"/>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertRelativeUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertRelativeUrls_StartingWithDotDot() {

		String before = "background-image: url('../vwimages/corners1280x18.gif/$file/corners1280x18.gif');";
		String after = "background-image: url('http://localhost/content/../vwimages/corners1280x18.gif/$file/corners1280x18.gif');";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/content/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertRelativeUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertCssUrls_IgnoreAbsolute() {

		String before = "<link type=\"text/css\" href=\"http://www.google.com/styles/foo.css\" rel='stylesheet'/>";
		String after = "<link type=\"text/css\" href=\"http://www.google.com/styles/foo.css\" rel='stylesheet'/>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertCssUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertLinkUrls() {

		String before = "<a href=\"/styles/foo.css\">foo</a>";
		String after = "<a href=\"http://localhost/styles/foo.css\">foo</a>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertLinkUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertLinkUrls_Relative() {

		String before = "<a href=\"content/page.html\">foo</a>";
		String after = "<a href=\"http://localhost/html/content/page.html\">foo</a>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/html/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertLinkUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertLinkUrls_IgnoreAbsolute() {

		String before = "<a href=\"http://www.google.com/styles/foo.css\">foo</a>";
		String after = "<a href=\"http://www.google.com/styles/foo.css\">foo</a>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertLinkUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertSrcUrls() {

		String before = "<img border='0' src=\"/styles/foo.jpg\"/>";
		String after = "<img border='0' src=\"http://localhost/styles/foo.jpg\"/>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertSrcUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertSrcUrls_RelativePath() {

		String before = "<img border='0' src=\"styles/foo.jpg\"/>";
		String after = "<img border='0' src=\"http://localhost/content/styles/foo.jpg\"/>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/content/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertSrcUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testConvertSrcUrls_IgnoreAbsolute() {

		String before = "<img border='0' src=\"http://www.google.com/styles/foo.jpg\"/>";
		String after = "<img border='0' src=\"http://www.google.com/styles/foo.jpg\"/>";

		Template template = new Template();
		template.setBaseUrl("http://localhost");
		template.setImportedFromUrl("http://localhost/foo.html");

		// this sets the entire content to consist only of the before string,
		// but that's enough
		template.setBeforeContent(before);
		template.setAfterContent(before);

		integrationMethod.convertSrcUrls(template);

		assertEquals("expected transformed value (before)", after, template.getBeforeContent());
		assertEquals("expected transformed value (after)", after, template.getAfterContent());
	}

	@Test
	public void testFrenchUrlsDetected() {
		assertNotNull(integrationMethod);
		for (String link : linksToFrenchPage) {
			assertNotNull("bad test data", link);

			assertFalse("should have returned false (test data assumption)", integrationMethod.hasLinkToEnglish(link));
			assertTrue("should have returned true (test data assumption)", integrationMethod.hasLinkToFrench(link));
		}
	}

	// ======================================================================

	@Test
	public void testRuntimeBegin() {

		SupportedLanguage english = supportedLanguageService.findByCode("eng");
		SupportedLanguage french = supportedLanguageService.findByCode("fra");

		// for the label
		setEnglish();
		final String languageLabel = english.getDisplayTitle();

		setFrench();

		// may as well be original here to DiD against regression in the class
		// under test
		String contextPath = "/oasOrSomething";

		// a rTo URI to match
		String rTo = "/html/myOperation.html";

		MockHttpServletRequest request = new MockHttpServletRequest("GET", rTo);
		request.setContextPath(contextPath);
		request.setPathInfo("/myOperation.html");

		Template template = new Template(new Survey(), french);
		String beforeContent = Clf2TemplateIntegrationMethod.LANGUAGE_TOGGLE_START[0]
				+ "<div><a class='testdata' href='/testdata/testdata'>" + languageLabel + "</a></div>"
				+ Clf2TemplateIntegrationMethod.LANGUAGE_TOGGLE_END[0];
		String expected = Clf2TemplateIntegrationMethod.LANGUAGE_TOGGLE_START[0]
				+ "<div><a href='/oasOrSomething/html/eng.html?rTo=" + rTo + "'>" + languageLabel + "</a></div>"
				+ Clf2TemplateIntegrationMethod.LANGUAGE_TOGGLE_END[0];

		String value = integrationMethod.runtimeBeforeContent(request, template, beforeContent);

		assertEquals("should have been transformed", expected, value);
	}
	// ======================================================================
}
