package ca.inforealm.core.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Date;
import java.util.Locale;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ResourceString;
import ca.inforealm.core.service.event.ResourceStringDataChangedEvent;

public class ResourceStringServiceTest extends AbstractTestDataCreatingBaseTest {

	// @Autowired
	// @Qualifier("fallbackMessageSource")
	private MessageSource fallbackMessageSource;

	protected Application createScenario1(ResourceString[] strings) {
		Application application = createTestApplication("app" + getMBUN());

		if (strings != null) {
			for (ResourceString string : strings) {
				string.setApplication(application);
				getHibernateTemplate().persist(string);
			}
		}

		// change SANE context to our test app
		getSaneContext().setApplicationIdentifier(application.getIdentifier());
		getSaneContext().reloadAllModels();

		// tell service to reinitialize
		getResourceStringService().reloadResources();

		return application;
	}

	// ======================================================================

	@Test
	public void fromScratch_Pass() {
		createScenario1(new ResourceString[] { new ResourceString(null, "app.name", "appNameEn", "appNameFr"),
				new ResourceString(null, "app.version", "1.0", "1.0") });

		// test
		assertEquals("should retrieve correct string", "appNameEn", getResourceStringService().getMessage("app.name", null,
				Locale.CANADA));
		assertEquals("should retrieve correct string", "appNameFr", getResourceStringService().getMessage("app.name", null,
				Locale.CANADA_FRENCH));

	}

	@Test
	public void getViaMessageSourceResolvableFromScratch_Pass() {
		createScenario1(new ResourceString[] { new ResourceString(null, "app.name", "appNameEn", "appNameFr"),
				new ResourceString(null, "app.version", "1.0", "1.0") });

		// test
		MessageSourceResolvable r1 = new DefaultMessageSourceResolvable(new String[] { "app.name" }, "someDefault");

		assertEquals("should retrieve correct string", "appNameEn", getResourceStringService().getMessage(r1, Locale.CANADA));
		assertEquals("should retrieve correct string", "appNameFr", getResourceStringService().getMessage(r1,
				Locale.CANADA_FRENCH));

	}

	@Test
	public void getViaMessageSourceResolvableFromScratch_NullCodesGracefullyHandled() {
		createScenario1(new ResourceString[] { new ResourceString(null, "app.name", "appNameEn", "appNameFr"),
				new ResourceString(null, "app.version", "1.0", "1.0") });

		// test
		MessageSourceResolvable r3 = new DefaultMessageSourceResolvable(null, "someDefault");

		assertEquals("should retrieve correct string", "someDefault", getResourceStringService().getMessage(r3,
				Locale.CANADA_FRENCH));
	}

	// DELETE ME: default has changed: returns the code if no message is found
	// public void
	// testGetViaMessageSourceResolvableFromScratch_NoMatchingKey_DefaultReturned()
	// {
	// createScenario1(new ResourceString[] { new ResourceString(null,
	// "app.name", "appNameEn", "appNameFr"),
	// new ResourceString(null, "app.version", "1.0", "1.0") });
	//
	// // test
	// MessageSourceResolvable r3 = new DefaultMessageSourceResolvable(new
	// String[] { "doesNot.exist" }, "someDefault");
	//
	// assertEquals("should retrieve correct string", "someDefault",
	// getResourceStringService().getMessage(r3, Locale.CANADA_FRENCH));
	// }

	// DELETE ME: default has changed: returns the code if no message is found
	// public void
	// testGetViaMessageSourceResolvableFromScratch_NoMatchingKey_Throws() {
	// createScenario1(new ResourceString[] { new ResourceString(null,
	// "app.name", "appNameEn", "appNameFr"),
	// new ResourceString(null, "app.version", "1.0", "1.0") });
	//
	// // test
	// MessageSourceResolvable r3 = new DefaultMessageSourceResolvable(new
	// String[] { "doesNot.exist" }, (String) null);
	// try {
	// getResourceStringService().getMessage(r3, Locale.CANADA_FRENCH);
	// fail("should have thrown");
	// } catch (NoSuchMessageException e) {
	// // expected
	// }
	// }

	@Test
	public void getViaMessageSourceResolvableFromScratch_NullCodesAndNullDefault_Throws() {
		createScenario1(new ResourceString[] { new ResourceString(null, "app.name", "appNameEn", "appNameFr"),
				new ResourceString(null, "app.version", "1.0", "1.0") });

		// test
		MessageSourceResolvable r3 = new DefaultMessageSourceResolvable(null, (String) null);
		try {
			getResourceStringService().getMessage(r3, Locale.CANADA_FRENCH);
			fail("should have thrown");
		} catch (NoSuchMessageException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void getMessage_DefaultValue() {
		final String DEFAULT = "someDefault";

		String string = getResourceStringService().getMessage("someNonExistingCode" + getMBUN(), null, DEFAULT, Locale.CANADA);
		assertNotNull("should have returned a string", string);
		assertEquals("should be default value", DEFAULT, string);
	}

	// ======================================================================

	@Test
	public void resourceStringDataChangedEvent() {
		final String ORIG = "orig";
		final String NEW = "newValue";

		ResourceString orig = new ResourceString(null, "key", ORIG, ORIG);

		createScenario1(new ResourceString[] { orig });
		assertEquals("should have orig value", ORIG, getResourceStringService().getMessage("key", null, Locale.CANADA));

		// modify value
		orig.setValueEn(NEW);
		orig.setValueFr(NEW);
		getHibernateTemplate().persist(orig);

		// kick off a reload
		applicationContext.publishEvent(new ResourceStringDataChangedEvent(this));

		assertEquals("should have NEW value", NEW, getResourceStringService().getMessage("key", null, Locale.CANADA));
	}

	@Test
	public void differentialReload_Pass() {

		final String ORIG = "orig";
		final String NEW = "newValue";

		Date olderDate = new Date();
		Date newerDate = new Date(olderDate.getTime() + 1L);

		ResourceString str1 = new ResourceString(null, "key1", ORIG, ORIG);
		ResourceString str2 = new ResourceString(null, "key2", ORIG, ORIG);
		ResourceString str3 = new ResourceString(null, "key3", ORIG, ORIG);

		str1.setLastModifiedDate(olderDate);
		str2.setLastModifiedDate(olderDate);
		str3.setLastModifiedDate(olderDate);

		// scenario data
		createScenario1(new ResourceString[] { str1, str2, str3 });

		// differential reload should change nothing because newerDate is the
		// newest in the database
		getResourceStringService().performDifferentialReload();
		assertEquals("should have original value", ORIG, getResourceStringService().getMessage("key2", null, Locale.CANADA));

		// update a string
		str2.setValueEn(NEW);
		str2.setLastModifiedDate(newerDate);
		getHibernateTemplate().persist(str2);

		// differential reload should change str2 because it's LMD is newer
		// than olderDate
		getResourceStringService().performDifferentialReload();

		// differential reload should return three
		assertEquals("should have NEW value", NEW, getResourceStringService().getMessage("key2", null, Locale.CANADA));
	}

	@Test
	public void differentialReload_ReloadsAllIfNoNewestDataSet() {

		final String ORIG = "orig";

		// scenario data WITHOUT any strings
		Application application = createScenario1(new ResourceString[] {});

		// now create strings

		Date olderDate = new Date();

		ResourceString str1 = new ResourceString(application, "key1", ORIG, ORIG);
		ResourceString str2 = new ResourceString(application, "key2", ORIG, ORIG);
		ResourceString str3 = new ResourceString(application, "key3", ORIG, ORIG);

		str1.setLastModifiedDate(olderDate);
		str2.setLastModifiedDate(olderDate);
		str3.setLastModifiedDate(olderDate);

		getHibernateTemplate().persist(str1);
		getHibernateTemplate().persist(str2);
		getHibernateTemplate().persist(str3);

		flushAndClear();

		// differential reload should change everything because it has no
		// "newest date" property value since it has not yet loaded any strings
		getResourceStringService().performDifferentialReload();

		assertEquals("should now have a value", ORIG, getResourceStringService().getMessage("key1", null, Locale.CANADA));
		assertEquals("should now have a value", ORIG, getResourceStringService().getMessage("key2", null, Locale.CANADA));
		assertEquals("should now have a value", ORIG, getResourceStringService().getMessage("key3", null, Locale.CANADA));
	}
	// ======================================================================

}
