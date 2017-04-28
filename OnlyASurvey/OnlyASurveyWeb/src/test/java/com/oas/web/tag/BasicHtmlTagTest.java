package com.oas.web.tag;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.util.Assert;

public class BasicHtmlTagTest {

	/** Tag under test. */
	private BasicHtmlTag tagUnderTest = new BasicHtmlTag();

	// @Test
	// public void blessing_NonBlessedTag() {
	// String str = "<script type='text/javascript'>";
	// String blessed = tagUnderTest.getBlessed(str.toCharArray(), 0);
	// assertTrue(blessed == null);
	// }
	//
	// @Test
	// public void blessing_NonBlessedTag2() {
	// String str = "<html>";
	// String blessed = tagUnderTest.getBlessed(str.toCharArray(), 0);
	// assertTrue(blessed == null);
	// }
	//
	// @Test
	// public void blessing_NonBlessed_SelfClosingTag() {
	// String str = "<p/>";
	// String blessed = tagUnderTest.getBlessed(str.toCharArray(), 0);
	// assertTrue(blessed == null);
	// }
	//
	// @Test
	// public void blessing_NonBlessedAttribute() {
	// String str = "<p onclick='something;'>";
	// String blessed = tagUnderTest.getBlessed(str.toCharArray(), 0);
	// assertTrue(blessed == null);
	// }

	public void doHTMLCleanTest(String input, String expected) throws IOException {

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockPageContext pageContext = new MockPageContext(null, null, response);
		tagUnderTest.output(pageContext, true, input);

		String actual = response.getContentAsString();

		// assertEquals("expected unchanged string back", expected, actual);
		assertEquals("unexpected string back", expected, actual);
	}

	@Test
	public void escaping_HTMLTag_Fails() throws IOException {

		// AntiSamy likes newlines
		String input = "text<html>text";
		String expected = "texttext\n";

		doHTMLCleanTest(input, expected);
	}

	@Test
	public void escaping_Paragraph() throws IOException {

		// AntiSamy likes newlines
		String input = "text<p>text</p>text";
		String expected = "text<p>text</p>\ntext\n";

		doHTMLCleanTest(input, expected);
	}

	// @Test
	// public void escaping_Blessed_NBSP() {
	// doEntityEscapingTest("&nbsp;");
	// }
	//
	// @Test
	// public void escaping_Blessed_DIV() {
	// doTagEscapingTest("<div>");
	// }
	//
	// @Test
	// public void escaping_Blessed_LI() {
	// doTagEscapingTest("<li>");
	// }
	//
	// @Test
	// public void escaping_Blessed_LIEnd() {
	// doTagEscapingTest("</li>");
	// }
	//
	// @Test
	// public void escaping_Blessed_P() {
	// doTagEscapingTest("<p>");
	// }
	//
	// @Test
	// public void escaping_Blessed_PEnd() {
	// doTagEscapingTest("</p>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Strong() {
	// doTagEscapingTest("<strong>");
	// }
	//
	// @Test
	// public void escaping_Blessed_StrongEnd() {
	// doTagEscapingTest("</strong>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Em() {
	// doTagEscapingTest("<em>");
	// }
	//
	// @Test
	// public void escaping_Blessed_EmEnd() {
	// doTagEscapingTest("</em>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Ul() {
	// doTagEscapingTest("<ul>");
	// }
	//
	// @Test
	// public void escaping_Blessed_UlEnd() {
	// doTagEscapingTest("</ul>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Ol() {
	// doTagEscapingTest("<ol>");
	// }
	//
	// @Test
	// public void escaping_Blessed_OlEnd() {
	// doTagEscapingTest("</ol>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Dl() {
	// doTagEscapingTest("<dl>");
	// }
	//
	// @Test
	// public void escaping_Blessed_DlEnd() {
	// doTagEscapingTest("</dl>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Dt() {
	// doTagEscapingTest("<dt>");
	// }
	//
	// @Test
	// public void escaping_Blessed_DtEnd() {
	// doTagEscapingTest("</dt>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Dd() {
	// doTagEscapingTest("<dd>");
	// }
	//
	// @Test
	// public void escaping_Blessed_DdEnd() {
	// doTagEscapingTest("</dd>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Br() {
	// doTagEscapingTest("<br/>");
	// }
	//
	// @Test
	// public void escaping_Blessed_Br2() {
	// doTagEscapingTest("<br />");
	// }
	//
	// @Test
	// public void escaping_Blessed_Style_Underline() {
	// doTagEscapingTest("<span style=\"text-decoration: underline;\">");
	// }
	//
	// @Test
	// public void escaping_Blessed_Style_None() {
	// doTagEscapingTest("<span style=\"text-decoration: none;\">");
	// }
	//
	// @Test
	// public void escaping_Blessed_Style_Bold() {
	// doTagEscapingTest("<span style=\"font-weight: bold;\">");
	// }
	//
	// @Test
	// public void escaping_Blessed_Style_WeightNormal() {
	// doTagEscapingTest("<span style=\"font-weight: normal;\">");
	// }
	//
	// @Test
	// public void escaping_Blessed_Style_Italic() {
	// doTagEscapingTest("<span style=\"text-decoration: italic;\">");
	// }
	//
	// @Test
	// public void escaping_Blessed_Style_LineThrough() {
	// doTagEscapingTest("<span style=\"text-decoration: line-through;\">");
	// }
	//
	// @Test
	// public void escaping_Blessed_SpanEnd() {
	// doTagEscapingTest("</span>");
	// }

	// ======================================================================

	// @Test
	// public void doubleParagraphsRemoved() {
	// doDoubleTagRemoveTest("p");
	// doDoubleTagRemoveTest("/p");
	// }
	//
	// private void doDoubleTagRemoveTest(String tag) {
	// try {
	// StringWriter writer = new StringWriter();
	// String input = "<" + tag + "><" + tag + ">";
	// String expected = "<" + tag + ">";
	//
	// tagUnderTest.writeEscapedXml(input.toCharArray(), input.length(),
	// writer);
	//
	// String actual = writer.getBuffer().toString();
	//
	// assertEquals("did not perform expected replacement", expected, actual);
	//
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// // ======================================================================
	//
	// private void doTagEscapingTest(String tag) {
	// String blessed = tagUnderTest.getBlessed(tag.toCharArray(), 0);
	//
	// assertNotNull("expected non-null value", blessed);
	// assertEquals("expected un-escaped markup", tag, blessed);
	//
	// try {
	// StringWriter writer = new StringWriter();
	// String expected = "text" + tag + "text";
	// tagUnderTest.writeEscapedXml(expected.toCharArray(), expected.length(),
	// writer);
	// String actual = writer.getBuffer().toString();
	//
	// assertEquals("expected unchanged string back", expected, actual);
	// } catch (Exception e) {
	// // rather than refactor callers
	// throw new RuntimeException(e);
	// }
	// }
	//
	// private void doEntityEscapingTest(String tag) {
	// String blessed = tagUnderTest.getBlessedEntity(tag.toCharArray(), 0);
	//
	// assertNotNull("expected non-null value", blessed);
	// assertEquals("expected un-escaped markup", tag, blessed);
	//
	// try {
	// StringWriter writer = new StringWriter();
	// String expected = "text" + tag + "text";
	// tagUnderTest.writeEscapedXml(expected.toCharArray(), expected.length(),
	// writer);
	// String actual = writer.getBuffer().toString();
	//
	// assertEquals("expected unchanged string back", expected, actual);
	// } catch (Exception e) {
	// // rather than refactor callers
	// throw new RuntimeException(e);
	// }
	// }

	// ======================================================================

	@Test
	public void basicAntiSamy_SanityCheck() throws ScanException, PolicyException {
		doBasicAntiSamy_SanityCheck("<p onclick='javascript:boo();'>test</p>", "<p>test</p>");
		doBasicAntiSamy_SanityCheck("<html>test</html>", "test");
		doBasicAntiSamy_SanityCheck("<a href='javascript:boo();'>test</a>", "<a>test</a>");
		doBasicAntiSamy_SanityCheck("<a href=\"boo.html\" onclick=\"javascript:boo();\">test</a>",
				"<a href=\"boo.html\">test</a>");
		doBasicAntiSamy_SanityCheck("<div onclick='javascript:boo();'>test</div>", "<div>test</div>");
		doBasicAntiSamy_SanityCheck(
				"<script type='text/javascript'>alert(\"BAR\");</script><div onclick='javascript:boo();'>test</div>",
				"<div>test</div>");
	}

	public void doBasicAntiSamy_SanityCheck(String input, String expected) throws ScanException, PolicyException {

		URL url = getClass().getResource("/config/antisamy-usertext.xml");
		Assert.notNull(url, "unable to find HTML cleaning policy resource");
		String filename = url.getFile();
		// Policy policy = Policy.getInstance(POLICY_FILE_LOCATION);
		Policy policy = Policy.getInstance(filename);
		AntiSamy as = new AntiSamy();
		CleanResults cr = as.scan(input, policy);

		//
		String actual = cr.getCleanHTML().trim();

		assertEquals("expected clean to work", expected, actual);

	}
}
