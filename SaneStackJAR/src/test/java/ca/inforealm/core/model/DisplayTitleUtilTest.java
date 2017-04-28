package ca.inforealm.core.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Test;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.model.annotation.DisplayTitle;

public class DisplayTitleUtilTest extends AbstractBaseTest {

	final String KAPLAH = "KAPLAH";

	// no hint, no ID; use fallback in "new" mode
	public class DisplayTitleWithNew extends AbstractResourceModel {
		public Long getId() {
			return null;
		}
	}

	// no hint, with ID; use fallback in "#id" mode
	public class DisplayTitleWithId extends AbstractResourceModel {
		public Long getId() {
			return 1L;
		}
	}

	public class KlingonFirst extends AbstractResourceModel {
		@DisplayTitle(language = "kli")
		public String getNameKlingon() {
			return KAPLAH;
		}

		@DisplayTitle(language = "eng")
		public String getNameEn() {
			return NAME_EN;
		}
	}

	public class NoBestMatch extends AbstractResourceModel {
		public Long getId() {
			return 1L;
		}
	}

	public class ThrowsException extends AbstractResourceModel {
		@DisplayTitle
		public String getSomehowThrows() {
			throw new IllegalStateException();
		}
	}

	public class PrivateMethodAnnotated extends AbstractResourceModel {

		/**
		 * since this is an inner class the privacy of properties is ignored,
		 * therefore this method simulates the effect of annotating a method of
		 * insufficient visibility by manually throwing an
		 * IllegalAccessException.
		 * 
		 * note that this method is actually public, since the class under test
		 * will never inspect a non-public method for the DisplayTitle
		 * annotation.
		 * 
		 * this class exists to inject a required throwable in the class under
		 * test
		 */
		@DisplayTitle
		public String getName() throws IllegalAccessException {
			throw new IllegalAccessException();
		}
	}

	// ======================================================================

	@Test
	public void testGetDisplayTitle() {

		// a single DisplayTitle vaue
		UserAccount user = new UserAccount();
		user.setUsername("testUserName");
		assertEquals("user should have displayTitle of $username", user.getUsername(), user.getDisplayTitle());

		DisplayTitleWithNew displayTitleWithNew = new DisplayTitleWithNew();
		assertEquals("should have used fallback algorithm with '#new'", "DisplayTitleWithNew#new", displayTitleWithNew
				.getDisplayTitle());

		DisplayTitleWithId displayTitleWithId = new DisplayTitleWithId();
		assertEquals("should have used fallback algorithm with '#id'", "DisplayTitleWithId#1", displayTitleWithId
				.getDisplayTitle());
	}

	@Test
	public void testEnglishFrench_DisplayTitleAnnotation() {

		ActorGroup group = new ActorGroup();
		group.setNameEn("en");
		group.setNameFr("fr");

		// go English mode
		setEnglish();
		assertEquals("expected English value from getDisplayTitle", "en", group.getDisplayTitle());

		// go French mode
		setFrench();
		assertEquals("expected French value from getDisplayTitle", "fr", group.getDisplayTitle());

		// TODO do reverse of below: setEnglish, setEn(null),
		// expect(nextMatchingMethod == fr)

		// set French title to null and confirm that it reverts to English (ie,
		// first) title
		group.setNameFr(null);
		assertEquals("expected fallback to English value from getDisplayTitle", "en", group.getDisplayTitle());

		// null both titles and expect fallback to "n/a"
		group.setNameEn(null);
		group.setNameFr(null);
		assertEquals("expected fallback to n/a value from getDisplayTitle", "n/a", group.getDisplayTitle());

	}

	@Test
	public void testBestMethodFallbackToFirstMethod() {

		KlingonFirst m = new KlingonFirst();

		// confirm we get the English value when expected
		setEnglish();
		assertEquals("should have returned a real best match", NAME_EN, m.getDisplayTitle());

		// should always return the klingon value because "eng" is no longer the
		// default language and there is no "fra" value
		setFrench();
		assertEquals("should have returned the first method with @DisplayTitle", NAME_EN, m.getDisplayTitle());
	}

	@Test
	public void testBestMethodFallbackToId() {

		NoBestMatch m = new NoBestMatch();
		assertEquals("should have returned the first method with @DisplayTitle", "NoBestMatch#1", m.getDisplayTitle());
	}

	@Test
	public void testOnException() {

		ThrowsException m = new ThrowsException();
		try {
			m.getDisplayTitle();
			fail("should have thrown a RuntimeException-wrapped object-specific exception");
		} catch (RuntimeException e) {
			// expected
		}
	}

	@Test
	public void testIllegalAccess() {
		PrivateMethodAnnotated m = new PrivateMethodAnnotated();
		try {
			m.getDisplayTitle();
			fail("should have thrown a RuntimeException-wrapped object-specific exception");
		} catch (RuntimeException e) {
			// expected
		}
	}
}
