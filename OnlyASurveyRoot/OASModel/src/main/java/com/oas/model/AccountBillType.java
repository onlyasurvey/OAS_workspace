package com.oas.model;

/**
 * Type enum for Account Bill Types.
 * 
 * @author Jason Halliday
 * @since July 1, 2009 (Happy Canada Day!)
 */
public enum AccountBillType {

	/** Demo (unpaid) account. */
	DEMO,

	/** Pay as You Go model. */
	PAY_AS_YOU_GO,

	/** Monthly subscription. */
	MONTHLY;

	public String getLabel() {
		return this.toString();
	}

	public String getValue() {
		return this.toString();
	}
}
