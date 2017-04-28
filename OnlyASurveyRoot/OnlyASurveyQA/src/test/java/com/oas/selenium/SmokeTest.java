package com.oas.selenium;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.Selenium;

/**
 * Basic smoke test to validate that the system is not totally broken.
 * 
 * @author xhalliday
 * @since 2009-11-27
 */
public class SmokeTest extends AbstractBaseFunctionalTest {

	/** Selenium instance. */
	private Selenium selenium;

	@Before
	public void setupSelenium() {
		selenium = newSeleniumInstance();
	}

	@After
	public void teardownSelenium() {
		teardownSelenium(selenium);
	}

	@Test
	public void smokeTest1() throws InterruptedException {
		selenium.open("http://localhost/oas/html/oas.html");
		assertTrue("expected header", selenium.isTextPresent("Accessible Online Surveys"));
		selenium.type("name=j_username", "qauser");
		Thread.sleep(5000);
	}
}
