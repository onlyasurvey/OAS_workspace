package ca.inforealm.core.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.ConfigurationItem;

public class ConfigurationServiceTest extends AbstractTestDataCreatingBaseTest {
	private final static String IS_DISABLED = "isDisabled";
	private final static String IS_BASELINE_LOCKED = "isBaselineLocked";
	private final static String QUALIFIED_CONFIG_ITEM_TABLE_NAME = "configuration_item";

	// ======================================================================

	@Override
	public void onSetUpInTransaction() throws Exception {

		super.onSetUpInTransaction();

		Collection<ConfigurationItem> items = find("from ConfigurationItem where application = ?", getSaneContext()
				.getApplicationModel());
		assertTrue("should have configuration items in parent onSetUpInTransaction", items.size() > 0);
		boolean hasExpectedItem = false;
		for (ConfigurationItem item : items) {
			if (item.getIdentifier().equals(OFFLINE_FOR_MAINTENANCE)) {
				hasExpectedItem = true;
				break;
			}
		}
		assertTrue("should have OFFLINE_FOR_MAINTENANCE item", hasExpectedItem);
	}

	// ======================================================================

	@Test
	public void getConfigurationItem_Pass() {
		// default onSetUp creates a scenario with a config item

		ConfigurationItem item = getConfigurationService().getConfigurationItem(OFFLINE_FOR_MAINTENANCE);
		assertNotNull(item);
	}

	@Test
	public void getConfigurationItem_NoItem() {
		// default onSetUp creates a scenario with a config item

		ConfigurationItem item = getConfigurationService().getConfigurationItem("someRandomThing" + getMBUN());
		assertNull(item);
	}

	// ======================================================================

	@Test
	public void testGetConfigurationItems_Pass() {

		// default onSetUp creates a scenario with a config item
		Collection<ConfigurationItem> list = getConfigurationService().getConfigurationItems();

		assertNotNull("subject returned null", list);
		assertEquals("should have one item by default", 1, list.size());
		ConfigurationItem item = list.iterator().next();
		assertEquals("should have expected identifier", OFFLINE_FOR_MAINTENANCE, item.getIdentifier());
		assertEquals("should have expected value", "false", item.getValue());
	}

	// ======================================================================

	@Test
	public void testHasConfigurationItem_Pass() {
		// default onSetUp creates a scenario with a config item
		assertTrue("should have OFFLINE_FOR_MAINTENANCE", getConfigurationService().hasConfigurationItem(OFFLINE_FOR_MAINTENANCE));
	}

	@Test
	public void testHasConfigurationItem_NoSuchItem() {
		// default onSetUp creates a scenario with a config item
		assertFalse("should NOT have random item", getConfigurationService().hasConfigurationItem("someItem" + getMBUN()));
	}

	// ======================================================================

	@Test
	public void testSetConfigurationItem_ChangeExisting_Pass() {
		assertTrue("should have default value from parent setup", getConfigurationService().hasConfigurationItem(
				OFFLINE_FOR_MAINTENANCE));
		assertEquals("should have default value from parent setup", "false", getConfigurationService().getConfigurationItem(
				OFFLINE_FOR_MAINTENANCE).getValue());
		getConfigurationService().setConfigurationItem(OFFLINE_FOR_MAINTENANCE, "true");
		assertTrue("should have new value", getConfigurationService().hasConfigurationItem(OFFLINE_FOR_MAINTENANCE));
		assertEquals("should have new value", "true", getConfigurationService().getConfigurationItem(OFFLINE_FOR_MAINTENANCE)
				.getValue());
	}

	@Test
	public void testSetConfigurationItem_AddNew_Pass() {

		final String IDENTIFIER = "SOME_RANDOM_THING" + getMBUN();
		final String VALUE = "fancyAjax";

		assertFalse("should NOT have default value from parent setup", getConfigurationService().hasConfigurationItem(IDENTIFIER));
		getConfigurationService().setConfigurationItem(IDENTIFIER, VALUE);
		assertTrue("should have new value", getConfigurationService().hasConfigurationItem(IDENTIFIER));
		assertEquals("should have new value", VALUE, getConfigurationService().getConfigurationItem(IDENTIFIER).getValue());
	}

	// ======================================================================

}