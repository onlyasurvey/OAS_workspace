package com.oas.munger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;

public class EncoderDecoderTest extends AbstractOASBaseTest {

	/** Some value that looks like a hex string at first, but isn't. */
	private static final String COINCIDENTALLY_SAME_PREFIX = MungerTestConstants.ENCODED + "-foo";

	// ======================================================================

	@Test
	public void testEncode_Success_String() {
		MungerFilter.setDisabled(false);
		assertEquals("expected encoded value", MungerTestConstants.ENCODED, EncoderDecoder.encode(MungerTestConstants.PLAIN));
	}

	@Test
	public void testEncode_Success_PreventsDoubleEncoding() {
		MungerFilter.setDisabled(false);
		assertEquals("expected encoded value", MungerTestConstants.ENCODED, EncoderDecoder.encode(MungerTestConstants.ENCODED));
	}

	/**
	 * Ensures that paths that are coded to be excluded from munger, are, at the
	 * encoder level.
	 * 
	 */
	@Test
	public void testEncode_Success_Excludes() {
		// only relevant when this is set
		MungerFilter.setDisabled(false);

		assertEquals("expected encoded value", "/incl/css/styles.css", EncoderDecoder.encode("/incl/css/styles.css"));
	}

	@Test
	public void testEncode_Success_EncodeNullReturnsNull() {
		assertNull("expected null value", EncoderDecoder.encode(null));
	}

	// ======================================================================

	@Test
	public void testDecode_Success() {
		assertEquals("expected encoded value", MungerTestConstants.PLAIN, EncoderDecoder.decode(MungerTestConstants.ENCODED));
	}

	@Test
	public void testDecode_Success_StringArray() {

		String[] input = new String[] { MungerTestConstants.ENCODED, MungerTestConstants.ENCODED };
		String[] expected = new String[] { MungerTestConstants.PLAIN, MungerTestConstants.PLAIN };
		assertEquals("test defect", input.length, expected.length);

		String[] result = EncoderDecoder.decode(input);

		assertEquals(input.length, result.length);

		for (int i = 0; i < result.length; i++) {
			assertEquals("unexpected value", expected[i], result[i]);
		}
	}

	/**
	 * When two encoded strings are concat'd together they should get decoded as
	 * one block, not fail on the second string's "0x"
	 */
	@Test
	public void testDecode_Success_ConcatEncodedStrings() {
		assertEquals("expected encoded value", MungerTestConstants.PLAIN + MungerTestConstants.PLAIN, EncoderDecoder
				.decode(MungerTestConstants.ENCODED + MungerTestConstants.ENCODED));
	}

	/**
	 * form:radiobuttons and it's ilk send a special _{name} parameter
	 */
	@Test
	public void testDecode_Success_SpecialForRadios() {
		assertEquals("expected encoded value", "_" + MungerTestConstants.PLAIN, EncoderDecoder.decode("_"
				+ MungerTestConstants.ENCODED));
	}

	@Test
	public void testDecode_Null() {
		assertNull("expected null value", EncoderDecoder.decode((String) null));
		assertNull("expected null value", EncoderDecoder.decode((String[]) null));
	}

	/**
	 * If a parameter starting with "0x" turns out to have non-hex characters as
	 * part of it's data then the parameter only coincidentally starts with the
	 * same prefix, or the data is otherwise invalid: the former is assumed and
	 * the expected output is the same as the input.
	 */
	@Test
	public void testDecode_NotActuallyHex() {
		// doh
		assertTrue("should have correct prefix", COINCIDENTALLY_SAME_PREFIX.startsWith("0x"));

		assertEquals("expected same value", COINCIDENTALLY_SAME_PREFIX, EncoderDecoder.decode(COINCIDENTALLY_SAME_PREFIX));
	}

	/**
	 * Decoding a plain value returns the plain value.
	 */
	@Test
	public void testDecode_IgnoreUnencoded() {
		assertEquals("expected same value", MungerTestConstants.PLAIN, EncoderDecoder.decode(MungerTestConstants.PLAIN));
	}
	//
}
