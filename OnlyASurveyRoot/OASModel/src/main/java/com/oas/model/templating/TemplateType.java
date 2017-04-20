package com.oas.model.templating;

/**
 * Type Code for Templates. This value can have effects on a template's runtime
 * rendering.
 * 
 * @author xhalliday
 * @since February 21, 2009
 */
public enum TemplateType {

	/** No specific type code. */
	NONE,

	/** <!-- OAS0x --> */
	OAS_COMMENTS,

	/** Variations on standard CLF2 comments for the content body. */
	CLF2_COMMENTS;

	// private final String typeCode;

	// private TemplateType(String typeCode) {
	// this.typeCode = typeCode;
	// }

	// public String getTypeCode() {
	// return typeCode;
	// }
}
