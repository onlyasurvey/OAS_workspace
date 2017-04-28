package com.oas.selenium;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Basic smoke test to validate that the system is not totally broken.
 * 
 * @author xhalliday
 * @since 2009-11-27
 */
public abstract class AbstractBaseFunctionalTest {

	/** Port that the Selenium Server will be running on. */
	private final static int SELENIUM_PORT = 5555;

	/**
	 * Create a new {@link Selenium} instance and return it.
	 * 
	 * @return {@link Selenium}
	 */
	protected Selenium newSeleniumInstance() {
		Selenium retval = new DefaultSelenium("localhost", SELENIUM_PORT, "*firefox", "http://localhost:8080/oas/");
		retval.start();
		return retval;
	}

	/**
	 * Create (if required) a QA user, returning the username. The password will
	 * be the same as the username.
	 * 
	 * @return String username
	 */
	public String createQAUser() {
		String retval = null;

		// UserAccount user = find("from")

		return retval;
	}

	/**
	 * Stop a {@link Selenium} instance.
	 * 
	 * @param selenium
	 *            {@link Selenium}
	 */
	public void teardownSelenium(Selenium selenium) {
		selenium.stop();
	}
}
