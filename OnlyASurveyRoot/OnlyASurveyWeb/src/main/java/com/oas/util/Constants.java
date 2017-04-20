package com.oas.util;

abstract public class Constants {

	/** When in doubt, redirect here. */
	public static final String DEFAULT_HOME = "/html/db/db.html";

	/** When "None of the above" radio is selected, this is it's pseudo-id. */
	public static final long NONE_OF_THE_ABOVE_ID = -2;

	/** When "Other" radio/checkbox is selected, this is it's pseudo-id. */
	public static final long OTHER_TEXT_ID = -1;

	/** Decorator to use when a user goes through the Respond to Survey flow. */
	public static final String SURVEY_RESPONSE_DECORATOR_NAME = "surveyResponseLayout";

	/** Decorator to use when a single-column layout is needed. */
	public static final String WIDE_LAYOUT_DECORATOR_NAME = "wideLayout";

	/** Decorator to use when a no layout is needed. */
	public static final String ZERO_LAYOUT_DECORATOR_NAME = "zeroLayout";

	/** Header that an upstream reverse proxy might set. */
	public static final String X_FORWARDED_HOST = "X-Forwarded-Host";

	/** The default number of choices to show on an Edit Multiple Choice form. */
	public static final int DEFAULT_MAX_CHOICES = 10;

	/** The default maximumValue for a scale question. */
	public static final Long DEFAULT_HIGHEST_SCALE = 5L;

	/** Default maximum "Other Text" length. */
	public static final int MAXIMUM_OTHER_TEXT_LENGTH = 500;

	/** Length of generated Invitation Code strings. */
	public static final int INVITATION_CODE_LENGTH = 6;

	/**
	 * If not overridden in configuration, this is the default filesystem prefix
	 * (directory) where public content pages are loaded from.
	 */
	public static final String DEFAULT_CONTENT_FILESYSTEM_PREFIX = "/home/oaspubliccontent/public";

	/**
	 * Destination email address for sending messages from the "Contact Us"
	 * function.
	 */
	public static final String CONTACT_US_INTERNAL_EMAIL_DESTINATION = "info@onlyasurvey.com";

	/**
	 * Configuration Item key for the hostname to use when sending absolute URLs
	 * (eg., in emails).
	 */
	public static final String HOSTNAME_CONFIGURATION_KEY = "publicHostname";

	/**
	 * Configuration Item key for the "short URL" feature. May be empty.
	 */
	public static final String SHORT_URL_PREFIX_CONFIGURATION_KEY = "shortUrlPrefix";

	/** Keys used in Object Text. */
	public class ObjectTextKeys {
		/** Page Content for Page Question types. */
		public static final String PAGE_CONTENT = "PAGE_CONTENT";
	};
}
