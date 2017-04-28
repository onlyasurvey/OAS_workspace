package ca.inforealm.core.support.config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.model.annotation.SetByConfiguration;

public class ConfigurationMappingPostProcessorTest extends AbstractTestDataCreatingBaseTest {

	private final static String CONFIG_KEY = "ConfigurationMappingPostProcessorTest.baselineLocked";

	@Autowired
	ConfigurationMappingPostProcessor configurationMappingPostProcessor;

	private ConfigurationMappingPostProcessor getProcessor() {
		return configurationMappingPostProcessor;
	}

	private SampleConfig newSampleConfig() {

		SampleConfig config = new SampleConfig();
		assertNull("existing value?!", config.getBaselineLocked());

		return config;
	}

	@Test
	public void testPostProcessor_ConfigService_setConfigurationItem_Pass() {

		SampleConfig config = newSampleConfig();
		ConfigurationMappingPostProcessor processor = getProcessor();

		assertNotNull("configService not set", getConfigurationService());

		assertNotNull("unable to getProcessor()", processor);

		processor.postProcessAfterInitialization(config, "");
		assertNull("existing value?!", config.getBaselineLocked());

		// set a new value that matches the SampleConfig
		{
			getConfigurationService().setConfigurationItem(CONFIG_KEY, "fancyAjax");

			//
			String value = config.getBaselineLocked();
			assertNotNull("value missing", value);
			assertEquals("wrong value", "fancyAjax", value);
		}
	}

	@Test
	public void testPostProcessor_ConfigService_reloadConfiguration_Pass() {

		SampleConfig config = newSampleConfig();
		ConfigurationMappingPostProcessor processor = getProcessor();

		assertNotNull("unable to getProcessor()", processor);

		processor.postProcessAfterInitialization(config, "");
		assertNull("existing value?!", config.getBaselineLocked());

		// persist outside of the config service and clear Hibernate session to
		// ensure writes are reflected
		getHibernateTemplate().persist(
				new ConfigurationItem(getSaneContext().getApplicationModel(), CONFIG_KEY, "string", "fancyAjax"));
		flushAndClear();

		// perform a full reload to ensure it sets the config object
		{
			getConfigurationService().reloadConfiguration();

			//
			String value = config.getBaselineLocked();
			assertNotNull("value missing", value);
			assertEquals("wrong value", "fancyAjax", value);
		}
	}

	@Test
	public void testEventHandler_MethodThrows_Fail() {
		SampleConfig config = newSampleConfig();
		ConfigurationMappingPostProcessor processor = getProcessor();
		assertNotNull("unable to getProcessor()", processor);

		// process the object for inclusion in updates
		processor.postProcessAfterInitialization(config, "");

		// set a value in a way that will cause the event to be thrown
		try {
			getConfigurationService().setConfigurationItem("someKey", "fancyAjax");
			// this will attempt to set a value for "someKey" which is
			// hard-coded to throw an exception, which should be wrapped in a
			// RuntimeException by the event handler
			fail("should have thrown");
		} catch (RuntimeException e) {
			// expected
		}
	}

	/**
	 * Sample test class
	 * 
	 * @author Jason Mroz
	 */
	private class SampleConfig {
		private String baselineLocked;

		public String getBaselineLocked() {
			return baselineLocked;
		}

		@SetByConfiguration(CONFIG_KEY)
		public void setBaselineLocked(String baselineLocked) {
			this.baselineLocked = baselineLocked;
		}

		@SetByConfiguration("someKey")
		public void setThrowableThingie(String value) {
			throw new RuntimeException("throwing from a setter FTL for value " + value);
		}
	}
}
