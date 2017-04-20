package com.oas.web.tag;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;

public class NL2BRTagTest {

	/** Tag under test. */
	private NL2BRTag tagUnderTest = new NL2BRTag();

	public void doHTMLCleanTest(String input, String expected) throws IOException {

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockPageContext pageContext = new MockPageContext(null, null, response);
		tagUnderTest.output(pageContext, input);

		String actual = response.getContentAsString();

		assertEquals("unexpected string back", expected, actual);
	}

	@Test
	public void success_Basic() throws IOException {

		// AntiSamy likes newlines
		String input = "line1\nline2";
		String expected = "line1<br/>line2";

		doHTMLCleanTest(input, expected);
	}

	@Test
	public void success_EscapesTags_Brackets() throws IOException {

		// AntiSamy likes newlines
		String input = "line1\n<script/>line2";
		String expected = "line1<br/>&lt;script/&gt;line2";

		doHTMLCleanTest(input, expected);
	}

	@Test
	public void success_EscapesTags_Quotes() throws IOException {

		// AntiSamy likes newlines
		String input = "line1\n\"'\nline2";
		String expected = "line1<br/>&#034;&#039;<br/>line2";

		doHTMLCleanTest(input, expected);
	}

	@Test
	public void success_EscapesTags_Ampersand() throws IOException {

		// AntiSamy likes newlines
		String input = "line1\n&\nline2";
		String expected = "line1<br/>&amp;<br/>line2";

		doHTMLCleanTest(input, expected);
	}
}
