package ca.inforealm.core.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.PreferenceDefinition;
import ca.inforealm.core.model.PreferenceValue;
import ca.inforealm.core.model.UserAccount;

public class PreferenceServiceTest extends AbstractTestDataCreatingBaseTest {

	@Test
	public void testGetPreferences_EmptySet() throws Exception {

		// create a secure context
		// createDefaultUserRole();
		createAndSetSecureUserWithRoleUser();

		Map<String, String> emptyList = getPreferenceService().getPreferences();

		assertNotNull("should never be null", emptyList);
		assertEquals("should be an empty list", 0, emptyList.size());
	}

	@Test
	public void testGetPreferences_WithValues() throws Exception {

		// test data, actor is set automatically
		Collection<PreferenceValue> preferences = new ArrayList<PreferenceValue>(2);
		PreferenceValue pref1 = new PreferenceValue();
		PreferenceValue pref2 = new PreferenceValue();

		preferences.add(pref1);
		preferences.add(pref2);

		pref1.setPreferenceDefinition(createPreferenceDefinition("preference1"));
		pref1.setValue("value1");

		pref2.setPreferenceDefinition(createPreferenceDefinition("preference2"));
		pref2.setValue("value2");

		// create a secure context

		createAndSetSecureUser(new String[] { ROLE_USER }, preferences);

		Map<String, String> prefList = getPreferenceService().getPreferences();

		assertNotNull("should never be null", prefList);
		assertEquals("should have two preference values", 2, prefList.size());

		assertEquals("expected preference values to be the same", "value1", prefList.get("preference1"));
		assertEquals("expected preference values to be the same", "value2", prefList.get("preference2"));

	}

	@Test
	public void testSetPreference_InvalidDefinition() throws Exception {

		createAndSetSecureUserWithRoleUser();

		try {
			getPreferenceService().setPreference("someNonExistingPrefId", "smarmyValue");
			fail("should have received an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}

	}

	@Test
	public void testGetPreference_Null() throws Exception {
		// create a secure context, user with no preferences
		createAndSetSecureUserWithRoleUser();
		String value = getPreferenceService().getPreference("someNonExistingPrefId");
		assertEquals("should have empty value", "", value);
	}

	@Test
	public void testGetPreference_Empty() throws Exception {

		final String PREF_ID = "somePreferenceIdentifier";
		final String TEST_VALUE = "";

		// create a secure context, user with no preferences
		createAndSetSecureUserWithRoleUser();
		String value = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", value);

		// create a preference def
		createPreferenceDefinition(PREF_ID);

		// set a pref
		getPreferenceService().setPreference(PREF_ID, TEST_VALUE);

		// clear Hibernate
		flushAndClear();

		// get the pref
		String newValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have same preference value", TEST_VALUE, newValue);
	}

	@Test
	public void testGetPreference_NonEmpty() throws Exception {

		final String PREF_ID = "somePreferenceIdentifier";
		final String TEST_VALUE = "testPreferenceValue";

		// create a secure context, user with no preferences
		createAndSetSecureUserWithRoleUser();
		String value = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", value);

		// create a preference def
		createPreferenceDefinition(PREF_ID);

		// set a pref
		getPreferenceService().setPreference(PREF_ID, TEST_VALUE);

		// clear Hibernate
		flushAndClear();

		// get the pref
		String newValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have same preference value", TEST_VALUE, newValue);
	}

	@Test
	public void testGetPreference_EnsureNoInsertOnNullValue() throws Exception {

		final String PREF_ID = "somePreferenceIdentifier";

		// create a secure context, user with no preferences
		createAndSetSecureUserWithRoleUser();
		String value = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", value);

		//
		// now, we know user has no existing preference value, so a call to
		// setPreference should (internally) make the decision to delete the
		// object, which should (internally) simply mean that no insert occurs.
		//

		// create a preference def
		createPreferenceDefinition(PREF_ID);

		// set a pref
		getPreferenceService().setPreference(PREF_ID, null);

		// clear Hibernate
		flushAndClear();

		// get the pref
		String newValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", newValue);
	}

	@Test
	public void testSetPreference_UpdateToEmpty() throws Exception {

		final String PREF_ID = "somePreferenceIdentifier";
		final String TEST_VALUE = "testPreferenceValue";

		// create a secure context, user with no preferences
		createAndSetSecureUserWithRoleUser();
		String value = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", value);

		// create a preference def
		createPreferenceDefinition(PREF_ID);

		// set a pref
		getPreferenceService().setPreference(PREF_ID, TEST_VALUE);

		// clear Hibernate
		flushAndClear();

		// get the pref
		String newValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have same preference value", TEST_VALUE, newValue);

		// now set empty and ensure it comes back that way
		getPreferenceService().setPreference(PREF_ID, "");

		// clear Hibernate
		flushAndClear();

		// get the pref
		String emptyValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have same preference value", "", emptyValue);
	}

	@Test
	public void testSetPreference_UpdateToNullMeansDeleted() throws Exception {

		final String PREF_ID = "somePreferenceIdentifier";
		final String TEST_VALUE = "testPreferenceValue";

		// create a secure context, user with no preferences
		UserAccount user = createAndSetSecureUserWithRoleUser();
		String value = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", value);

		// create a preference def
		createPreferenceDefinition(PREF_ID);

		// set a pref
		getPreferenceService().setPreference(PREF_ID, TEST_VALUE);

		// clear Hibernate
		flushAndClear();

		// get the pref
		String newValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have same preference value", TEST_VALUE, newValue);

		// now set NULL and ensure it comes back deleted
		getPreferenceService().setPreference(PREF_ID, null);

		// clear Hibernate
		flushAndClear();

		// get the pref
		String nullValue = getPreferenceService().getPreference(PREF_ID);
		assertEquals("should have empty value", "", nullValue);

		// clear Hibernate
		flushAndClear();

		// ensure it's really deleted
		Collection<PreferenceValue> shouldBeEmpty = (Collection<PreferenceValue>) getHibernateTemplate()
				.find(
						"from PreferenceValue pv where pv.preferenceDefinition.application.id=? and pv.actor=? and pv.preferenceDefinition.identifier=?",
						new Object[] { getSaneContext().getApplicationModel().getId(), user, PREF_ID });

		assertEquals("should have no such entities", 0, shouldBeEmpty.size());
	}

	// ======================================================================

	@Test
	public void testHasPreference_WhenFalse() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		createPreferenceDefinition(TEST_ID);
		boolean hasPref = getPreferenceService().hasPreference(TEST_ID);

		assertFalse("should not have preference", hasPref);
	}

	@Test
	public void testHasPreference_WhenTrue() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		createPreferenceDefinition(TEST_ID);
		assertFalse("should not have preference", getPreferenceService().hasPreference(TEST_ID));

		// flushAndClear();

		setPreference(TEST_ID, "someValue");
		assertTrue("should not have preference", getPreferenceService().hasPreference(TEST_ID));
	}

	// ======================================================================

	@Test
	public void testSetPreferences_NullFails() throws Exception {

		createAndSetSecureUserWithRoleUser();

		try {
			getPreferenceService().setPreferences(null);
			fail("should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}
	}

	@Test
	public void testSetPreferences_EmptyMapErases() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		PreferenceDefinition pref = createPreferenceDefinition(TEST_ID);

		// reload to update cache
		getSaneContext().reloadPreferenceDefinitions();

		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> emptyMap = new HashMap<String, String>();

		map.put(pref.getIdentifier(), "someValue");
		getPreferenceService().setPreferences(map);

		// should exist
		assertTrue("should initially have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));

		// pass in an empty map
		getPreferenceService().setPreferences(emptyMap);

		// should no longer exist
		assertFalse("should no longer have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
	}

	@Test
	public void testSetPreferences_SetPerformsUpdate() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		final String OLD_VALUE = "oldValue";
		final String NEW_VALUE = "newValue";
		PreferenceDefinition pref = createPreferenceDefinition(TEST_ID);

		// reload to update cache
		getSaneContext().reloadPreferenceDefinitions();

		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> newMap = new HashMap<String, String>();

		map.put(pref.getIdentifier(), OLD_VALUE);
		getPreferenceService().setPreferences(map);

		// should exist
		assertTrue("should initially have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (old) value", OLD_VALUE, getPreferenceService().getPreference(TEST_ID));

		// update all preferences
		newMap.put(TEST_ID, NEW_VALUE);
		getPreferenceService().setPreferences(newMap);

		// should have updated values
		assertTrue("should still have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (updated) value", NEW_VALUE, getPreferenceService().getPreference(TEST_ID));
	}

	@Test
	public void testSetPreferences_SetWithSameValueDoesNotChangeValue() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		final String OLD_VALUE = "oldValue";
		final String NEW_VALUE = OLD_VALUE;
		PreferenceDefinition pref = createPreferenceDefinition(TEST_ID);

		// reload to update cache
		getSaneContext().reloadPreferenceDefinitions();

		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> newMap = new HashMap<String, String>();

		map.put(pref.getIdentifier(), OLD_VALUE);
		getPreferenceService().setPreferences(map);

		// should exist
		assertTrue("should initially have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (old) value", OLD_VALUE, getPreferenceService().getPreference(TEST_ID));

		// update all preferences
		newMap.put(TEST_ID, NEW_VALUE);
		getPreferenceService().setPreferences(newMap);

		// should have updated values
		assertTrue("should still have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (updated) value", NEW_VALUE, getPreferenceService().getPreference(TEST_ID));
	}

	@Test
	public void testSetPreferences_SetWithEmptyValueDeletes() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		final String OLD_VALUE = "oldValue";
		final String NEW_VALUE = "";
		PreferenceDefinition pref = createPreferenceDefinition(TEST_ID);

		// reload to update cache
		getSaneContext().reloadPreferenceDefinitions();

		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> newMap = new HashMap<String, String>();

		map.put(pref.getIdentifier(), OLD_VALUE);
		getPreferenceService().setPreferences(map);

		// should exist
		assertTrue("should initially have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (old) value", OLD_VALUE, getPreferenceService().getPreference(TEST_ID));

		// update all preferences
		newMap.put(TEST_ID, NEW_VALUE);
		getPreferenceService().setPreferences(newMap);

		// should have updated values
		assertFalse("should NOT still have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (updated) value", NEW_VALUE, getPreferenceService().getPreference(TEST_ID));
	}

	@Test
	public void testSetPreferences_SetWithNullValueDeletes() throws Exception {

		createAndSetSecureUserWithRoleUser();

		final String TEST_ID = "testHasPreference";
		final String OLD_VALUE = "oldValue";
		final String NEW_VALUE = null;
		final String NEW_EXPECTED_VALUE = ""; // API always returns at least
		// an empty string

		PreferenceDefinition pref = createPreferenceDefinition(TEST_ID);

		// reload to update cache
		getSaneContext().reloadPreferenceDefinitions();

		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> newMap = new HashMap<String, String>();

		map.put(pref.getIdentifier(), OLD_VALUE);
		getPreferenceService().setPreferences(map);

		// should exist
		assertTrue("should initially have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (old) value", OLD_VALUE, getPreferenceService().getPreference(TEST_ID));

		// update all preferences
		newMap.put(TEST_ID, NEW_VALUE);
		getPreferenceService().setPreferences(newMap);

		// should have updated values
		assertFalse("should NOT still have the preference", getPreferenceService().hasPreference(pref.getIdentifier()));
		assertEquals("should have the correct (updated) value", NEW_EXPECTED_VALUE, getPreferenceService().getPreference(TEST_ID));
	}

	// ======================================================================

	// ======================================================================

	@Test
	public void testPreferenceDefinition_DisplayTitle_En() {
		PreferenceDefinition subject = new PreferenceDefinition();
		subject.setNameEn(NAME_EN);
		subject.setNameFr(NAME_FR);

		setEnglish();
		assertEquals("expected English value", NAME_EN, subject.getDisplayTitle());
	}

	@Test
	public void testPreferenceDefinition_DisplayTitle_Fr() {
		PreferenceDefinition subject = new PreferenceDefinition();
		subject.setNameEn(NAME_EN);
		subject.setNameFr(NAME_FR);

		setFrench();
		assertEquals("expected French value", NAME_FR, subject.getDisplayTitle());
	}
}
