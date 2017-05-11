package com.oas.command.model;

/**
 * Command used by people creating accounts.
 * 
 * @author jfchenier
 * @since March 15th, 2009
 */
public class AddRespondentCommand {

	/** user input for email list. */
	private String userEmailData;

	public String getUserEmailData() {
		return userEmailData;
	}

	public void setUserEmailData(String userEmailData) {
		this.userEmailData = userEmailData;
	}
	/*
	 * public String[] getTokenizedUserInput() { return tokenizedUserInput; }
	 * 
	 * public void setTokenizedUserInput(String[] tokenizedUserInput) {
	 * this.tokenizedUserInput = tokenizedUserInput; }
	 * 
	 * public String[] getValidatedEmailAddresses() { return
	 * validatedEmailAddresses; }
	 * 
	 * public void setValidatedEmailAddresses(String[] validatedEmailAddresses)
	 * { this.validatedEmailAddresses = validatedEmailAddresses; }
	 */
	// ======================================================================
}
