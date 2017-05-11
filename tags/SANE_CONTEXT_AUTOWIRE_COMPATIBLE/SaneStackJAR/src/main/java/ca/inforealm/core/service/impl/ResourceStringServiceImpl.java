package ca.inforealm.core.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.model.ResourceString;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.ResourceStringService;
import ca.inforealm.core.service.dao.ResourceStringServiceDAO;
import ca.inforealm.core.service.event.ResourceStringDataChangedEvent;
import ca.inforealm.core.service.impl.support.ResourceStringServiceMessageSourceSupport;
import ca.inforealm.core.service.impl.support.ResourceStringServiceSupport;

/**
 * Resource Property service, allowing resources to be stored in the backend and
 * managed and (re)loaded as required.
 * 
 * TODO review synchronization
 * 
 * @author Jason Mroz
 * @created June 17, 2008
 */
@Transactional
public class ResourceStringServiceImpl extends AbstractServiceImpl implements ResourceStringService, InitializingBean,
		ApplicationListener {

	/** Message source to fall back on if a key doesn't exist in the database. */
	private MessageSource fallbackSource;

	/** Newest string loaded as of the last full or differential reload. */
	private Date newestResourceString = null;

	/**
	 * A map of maps, key being a resource key and value being a language-keyed
	 * map of strings.
	 */
	protected Map<String, Map<String, String>> resourceMap;

	ResourceStringServiceMessageSourceSupport messageSourceSupport = new ResourceStringServiceMessageSourceSupport();

	// ======================================================================
	/**
	 * Initialize the service, loading all resources.
	 * 
	 * @throws Exception
	 */
	@Override
	@Unsecured
	public void afterPropertiesSet() throws Exception {
		performDifferentialReload();
	}

	// ======================================================================

	@Override
	@Unsecured
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {

		String[] codes = resolvable.getCodes();
		if (codes == null) {
			codes = new String[0];
		}
		for (int i = 0; i < codes.length; i++) {
			String msg = getMessage(codes[i], resolvable.getArguments(), null, locale);
			if (msg != null) {
				return msg;
			}
		}

		// no codes matched at this point
		if (resolvable.getDefaultMessage() != null) {
			return resolvable.getDefaultMessage();
		}

		throw new NoSuchMessageException(codes.length > 0 ? codes[codes.length - 1] : null, locale);
	}

	@Override
	@Unsecured
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		String retval = getMessage(code, args, null, locale);

		// try fallback source on null return
		if (retval == null && fallbackSource != null) {
			retval = fallbackSource.getMessage(code, args, locale);
		}

		// neither this class nor the fallback resolved the code, contract
		// dictates a throw
		if (retval == null) {
			throw new NoSuchMessageException(code, locale);
		}
		return retval;
	}

	@Override
	@Unsecured
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {

		String retval = null;

		Assert.notNull(resourceMap, "getMessage called with uninitialized internal map");

		// try to find an entry in the resource map for the code
		// (identifier/key)
		Map<String, String> entry = resourceMap.get(code);
		if (entry != null) {
			// entry is not null, try to find a string for the locale
			String language = locale.getISO3Language();

			// try to get a value
			retval = entry.get(language);
		}

		// use fallback source, if present
		if (retval == null && fallbackSource != null) {
			retval = fallbackSource.getMessage(code, args, defaultMessage, locale);
		}

		// use default, if any, where no value exists
		if (retval == null && defaultMessage != null) {
			retval = defaultMessage;
		}

		if (retval == null && defaultMessage == null) {
			// return default: null
		}

		//
		return retval;
	}

	// ======================================================================
	//
	// Defined by the Interface
	//
	// ======================================================================

	/**
	 * TODO review synchronization TODO review security
	 */
	@Override
	@Unsecured
	public void reloadResources() {

		// sanity
		assertValidDao();

		// return value
		Map<String, Map<String, String>> newMap;

		// get data from DAO
		Collection<ResourceString> list = getCustomDAO().loadAllResources();
		log.info("loaded " + list.size() + " resource strings to apply");
		// Collection<ResourceString> list = new ArrayList<ResourceString>();
		Assert.notNull(list);

		// convert the collection of resource strings to the internal
		// representation
		newMap = convertResourceStringsToMap(list);
		Assert.notNull(newMap, "failed to convert resource strings to internal representation");

		if (newMap.size() > 0) {

			// determine the newest string loaded to support differential
			// reloading
			Date updatedDate = ResourceStringServiceSupport.findNewestResourceString(list);
			Assert.notNull(updatedDate, "failed to determine newest resource string");

			//
			newestResourceString = updatedDate;
		} else {
			log.info("no resources to apply");
			newestResourceString = null;
		}

		// always set, even if empty
		resourceMap = newMap;
	}

	/**
	 * Perform a differential load, ie., those resources that have changed since
	 * this.newestResourceString.
	 * 
	 * TODO review synchronization
	 * 
	 * TODO review security
	 */
	@Override
	@Unsecured
	public void performDifferentialReload() {
		// sanity
		assertValidDao();

		// no newest date
		if (newestResourceString == null) {
			reloadResources();
			return;
		}

		// return value
		Map<String, Map<String, String>> changedStrings;

		// get data from DAO
		Collection<ResourceString> list = getCustomDAO().loadNewerResources(newestResourceString);
		log.info("loaded " + list.size() + " NEW resource strings to apply");
		Assert.notNull(list);

		// convert the collection of resource strings to the internal
		// representation
		changedStrings = convertResourceStringsToMap(list);
		Assert.notNull(changedStrings, "failed to convert resource strings to internal representation");

		if (changedStrings.size() > 0) {

			// determine the newest string loaded to support differential
			// reloading
			Date updatedDate = ResourceStringServiceSupport.findNewestResourceString(list);
			Assert.notNull(updatedDate, "failed to determine newest resource string");
			newestResourceString = updatedDate;

			// add/replace values in the internal map
			for (String key : changedStrings.keySet()) {
				// set
				resourceMap.put(key, changedStrings.get(key));

				log.debug("differential reload set resource: " + key);
			}
		} else {
			log.info("differential reload found no changed resource strings");
		}

	}

	/**
	 * @param fallbackSource
	 *            the fallbackSource to set
	 */
	@Unsecured
	public void setFallbackSource(MessageSource fallbackSource) {
		this.fallbackSource = fallbackSource;
	}

	// ======================================================================

	/**
	 * Convert a list of ResourceString objects to a simple map used internally.
	 * 
	 * @param list
	 * @return
	 */
	protected Map<String, Map<String, String>> convertResourceStringsToMap(Collection<ResourceString> list) {

		log.info("applying " + list.size() + " changes to resource strings");

		Map<String, Map<String, String>> retval = new HashMap<String, Map<String, String>>(list.size());

		// convert to the expected return value
		for (ResourceString string : list) {

			// get any existing value
			Map<String, String> entry = retval.get(string.getIdentifier());

			// duplicate keys are NOT allowed
			Assert.isNull(entry, "duplicate resource key from backend: " + string.getIdentifier());

			// no such entry yet - good
			entry = new HashMap<String, String>(2);

			String eng = string.getValueEn();
			String fre = string.getValueFr();

			entry.put(Locale.CANADA.getISO3Language(), eng);
			entry.put(Locale.CANADA_FRENCH.getISO3Language(), fre);

			// store the string
			retval.put(string.getIdentifier(), entry);
		}

		log.info("done applying " + list.size() + " changes to resource string data");

		return retval;
	}

	// ======================================================================

	/**
	 * Handles the ResourceStringDataChangedEvent to kickoff reloading resources
	 */
	@Unsecured
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ResourceStringDataChangedEvent) {
			log.info("reloading resources due to ResourceStringDataChangedEvent");
			reloadResources();
		}
	}

	// ======================================================================

	/**
	 * Redefines the DAO for this object to expect a service specific one.
	 * 
	 */
	protected ResourceStringServiceDAO getCustomDAO() {
		return (ResourceStringServiceDAO) super.getDataAccessObject();
	}

	protected void assertValidDao() {
		Assert.notNull(getCustomDAO());
		Assert.isInstanceOf(ResourceStringServiceDAO.class, getCustomDAO());
	}
}