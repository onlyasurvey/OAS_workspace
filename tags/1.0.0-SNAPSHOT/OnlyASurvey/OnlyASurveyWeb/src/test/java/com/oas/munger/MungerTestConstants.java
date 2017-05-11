package com.oas.munger;

/**
 * Constants used by various munger-related tests.
 * 
 * @author xhalliday
 * @since January 15, 2009
 */
abstract public class MungerTestConstants {

	/** An un-encoded test value. */
	public static final String PLAIN = "testValue";

	/** "testValue" (this.PLAIN) encoded into hex. */
	public static final String ENCODED = "0x7465737456616c7565";

	public static final String PLAIN_QUERY_STRING = "?query=string";

	/** Same as this.PLAIN but with a query string. */
	public static final String PLAIN_WITH_QUERY_STRING = PLAIN + PLAIN_QUERY_STRING;

	/** Encoded form of this.PLAIN_QUERY_STRING. */
	public static final String ENCODED_QUERY_STRING = "3f71756572793d737472696e67";

	/** Encoded form of this.PLAIN_WITH_QUERY_STRING. */
	public static final String ENCODED_WITH_QUERY_STRING = "0x7465737456616c7565" + ENCODED_QUERY_STRING;

	/** A "?" character encoded into hex. */
	public static final String ENCODED_QUESTION_MARK = "3f";

}
