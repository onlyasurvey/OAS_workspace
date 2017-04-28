package ca.inforealm.core.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.SaneContext;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.ConfigurationService;
import ca.inforealm.core.service.event.ApplicationModelChangedEvent;
import ca.inforealm.core.service.event.ConfigurationItemChangedEvent;

/**
 * Service for loading and saving Configuration Items.
 * 
 * TODO performDifferentialReload
 * 
 * @author Jason Mroz
 * 
 */
@Service(value = "configurationService")
@Transactional
public class ConfigurationServiceImpl extends AbstractServiceImpl implements ConfigurationService, InitializingBean,
		ApplicationContextAware, ApplicationListener {

	/** SANE context. */
	@Autowired
	private SaneContext saneContext;

	/**
	 * The default valueType for a new CI being stored.
	 * 
	 * TODO revisit the default value type
	 */
	protected final static String DEFAULT_VALUE_TYPE = "string";

	private ApplicationContext applicationContext;

	private Map<String, ConfigurationItem> configuration = new HashMap<String, ConfigurationItem>();

	// ======================================================================

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// must be running in ApplicationContext
		Assert.notNull(applicationContext, "application context not set");

		// must have initialized sane context
		Assert.notNull(saneContext, "SANE context not set");
		// Assert.notNull(saneContext.getApplicationIdentifier(),
		// "SANE context does not define an application identifier");
		//
		// // load configuration
		// reloadConfiguration();
	}

	@Override
	@Unsecured
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationModelChangedEvent) {
			reloadConfiguration();
		}
	}

	// ======================================================================

	@Override
	@Unsecured
	public void reloadConfiguration() {

		Collection<ConfigurationItem> list = find("from ConfigurationItem where application.identifier = ?", saneContext
				.getApplicationIdentifier());

		synchronized (configuration) {

			// re-initialize map
			configuration = new HashMap<String, ConfigurationItem>();

			// add all values
			for (ConfigurationItem item : list) {
				configuration.put(item.getIdentifier(), item);
			}
		}

		// fire ConfigurationItemChangedEvent to notify any listeners
		fireEventForItems(list);
	}

	/**
	 * Publish an event indicating that each of the items passed has changed.
	 * 
	 * @param items
	 */
	protected void fireEventForItems(Collection<ConfigurationItem> items) {
		for (ConfigurationItem item : items) {
			configItemChanged(item);
		}
	}

	/**
	 * Update the internal map and publish an event indicating that the item
	 * passed has changed. IMPORTANT to call this from any method that changes
	 * the backend since it won't be automatically reflected in the internal
	 * map.
	 * 
	 * @param item
	 */
	protected void configItemChanged(ConfigurationItem item) {

		// update internal map
		synchronized (configuration) {
			configuration.put(item.getIdentifier(), item);
		}

		// fire event
		applicationContext.publishEvent(new ConfigurationItemChangedEvent(item));
	}

	// ======================================================================

	@Override
	@Unsecured
	public ConfigurationItem getConfigurationItem(String identifier) {
		return configuration.get(identifier);
	}

	@Override
	@Unsecured
	public Collection<ConfigurationItem> getConfigurationItems() {
		Collection<ConfigurationItem> retval = new ArrayList<ConfigurationItem>(configuration.keySet().size());

		for (Map.Entry<String, ConfigurationItem> entry : configuration.entrySet()) {
			retval.add(entry.getValue());
		}
		return retval;
	}

	/**
	 * TODO make this efficient, it currently calls getConfigurationItem
	 */
	@Override
	@Unsecured
	public boolean hasConfigurationItem(String identifier) {
		return getConfigurationItem(identifier) != null;
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void setConfigurationItem(String identifier, String value) {
		setConfigurationItem(identifier, value, DEFAULT_VALUE_TYPE);
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void setConfigurationItem(String identifier, String value, String valueType) {

		// cannot use the cache because it's detached

		ConfigurationItem item = (ConfigurationItem) unique(find(
				"from ConfigurationItem where application = ? and identifier = ?", new Object[] {
						saneContext.getApplicationModel(), identifier }));

		if (item == null) {
			// null properties are set below
			item = new ConfigurationItem(saneContext.getApplicationModel(), identifier, null, null);
		}

		item.setValueType(valueType);
		item.setValue(value);

		getHibernateTemplate().persist(item);

		// detach and put/overwrite in cache and indicate that the item has
		// changed
		configItemChanged(item);
	}

}
