package ca.inforealm.coreman.service;

import java.util.Collection;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.coreman.AbstractSaneManBaseTest;
import ca.inforealm.coreman.service.ConfigurationManagementService;

/**
 * Allows the application to manage configuration data.
 * 
 * @author Jason Mroz
 * 
 */
public class ConfigurationManagementServiceTest extends AbstractSaneManBaseTest {

	private static final String CONFIG_ITEM_IDENTIFIER = "someTestItem55";
	private static final String CONFIG_ITEM_OTHER_IDENTIFIER = "someOtherTestItem33";

	private ConfigurationManagementService serviceUnderTest;

	public void onSetUp() throws Exception {
		super.onSetUp();
		serviceUnderTest = (ConfigurationManagementService) getApplicationContext().getBean("configurationManagementService");

		// all tests require a current user
		createAndSetSecureUserWithRoleUser();
	}

	/**
	 * A simple success path.
	 */
	public void testLoadAndPersist_Pass() {

		Application app = getSaneContext().getApplicationModel();

		final String newValue = "newValue";
		final String newValue2 = "newValue2";

		assertNull("should not yet have any such item", serviceUnderTest.get(app.getId(), CONFIG_ITEM_IDENTIFIER));

		ConfigurationItem item = serviceUnderTest.save(app.getId(), CONFIG_ITEM_IDENTIFIER, newValue);
		assertNotNull("item should have been returned", item);
		assertNotNull("item should have been saved", item.getId());
		assertEquals("should have correct value", newValue, item.getValue());

		ConfigurationItem newItem = serviceUnderTest.save(app.getId(), CONFIG_ITEM_IDENTIFIER, newValue2);
		assertNotNull("newItem should have been returned", newItem);
		assertNotNull("newItem should have been saved", newItem.getId());
		assertEquals("should have correct value (2)", newValue2, newItem.getValue());
	}

	public void testGetItems() {

		// test data
		Application app = getSaneContext().getApplicationModel();

		int initialSize = serviceUnderTest.getConfigurationItems(app.getId()).size();

		ConfigurationItem item1 = serviceUnderTest.save(app.getId(), CONFIG_ITEM_IDENTIFIER, "value");
		ConfigurationItem item2 = serviceUnderTest.save(app.getId(), CONFIG_ITEM_OTHER_IDENTIFIER, "value");

		// clear any objects from memory
		flushAndClear();

		Collection<ConfigurationItem> list = serviceUnderTest.getConfigurationItems(app.getId());

		assertNotNull("list should have been returned", list);
		assertEquals("should have two new config items", initialSize + 2, list.size());
	}

}
