package com.oas.munger.tag;

import org.apache.commons.lang.StringUtils;

import com.oas.munger.EncoderDecoder;

/**
 * Provides the
 * 
 * @author Jason Halliday
 * 
 */
abstract public class FunctionTag {

	/** Encode the parameter. */
	public static String encode(String param) {

		if (StringUtils.isEmpty(param)) {
			return param;
		}

		if (true) {
			throw new RuntimeException("UNIMPL");
		}
		return EncoderDecoder.encode(param);
	}

}
