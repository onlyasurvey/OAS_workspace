package com.oas.munger;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Encoder/Decoder used by the Munger to translate URLs between human-readable
 * and hex-encoded strings.
 * 
 * @author xhalliday
 */
abstract public class EncoderDecoder {

	public static String[] decode(String[] valueList) {
		if (valueList == null || valueList.length == 0) {
			return valueList;
		}

		String[] retval = new String[valueList.length];

		int i = 0;
		for (String item : valueList) {
			//
			retval[i] = EncoderDecoder.decode(item);

			//
			i++;
		}

		return retval;
	}

	/**
	 * Decode a string into plaintext.
	 * 
	 * @param value
	 * @return
	 */
	public static String decode(String value) {

		if (!StringUtils.hasText(value)) {
			return value;
		}

		int startIndex = 2;
		boolean underscorePrefix = false;

		// Assert.isTrue(value.startsWith("0x"), "won't decode without 0x
		// prefix");
		if (!(value.startsWith("0x") || value.startsWith("_0x"))) {
			return value;
		}

		// this happens for form:radiobutton-type tags
		if (value.startsWith("_0x")) {
			startIndex++;
			underscorePrefix = true;
		}

		StringBuffer buffer = new StringBuffer(value.length());
		try {

			// since the hex strings are 2 chars long, go 2 at a time, starting
			// 2 chars in to remove prefix
			for (int i = startIndex; i < value.length(); i += 2) {
				String hexString = value.substring(i, i + 2);

				if ("0x".equals(hexString)) {
					// most likely concat'd encoded strings
					continue;
				}

				char character = (char) Integer.parseInt(hexString, 16);
				buffer.append(character);
			}
		} catch (NumberFormatException e) {
			return value;
		}

		if (underscorePrefix) {
			return "_" + buffer.toString();
		}

		return buffer.toString();

	}

	/**
	 * Encode plaintext into a hex string prefixed by "0x".
	 * 
	 * @param value
	 * @return
	 */
	public static String encode(String value) {

		if (MungerFilter.isDisabled() || MungerFilter.excludeFromEncoding(value)) {
			return value;
		}

		if (!StringUtils.hasText(value)) {
			return value;
		}
		if (value.startsWith("0x")) {
			// to prevent double-encoding
			return value;
		}

		StringBuffer buffer = new StringBuffer(value.length() * 2);

		int startIndex = 0;
		// if (value.startsWith("_")) {
		// startIndex++;
		// buffer.append("_");
		// }
		for (int x = startIndex; x < value.length(); x++) {
			int intValue = value.charAt(x);
			String hexString = Integer.toHexString(intValue);
			Assert.isTrue(hexString.length() == 2);
			buffer.append(hexString);
		}

		return "0x" + buffer.toString();
	}
}
