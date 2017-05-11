package ca.inforealm.core.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.SaneContext;
import ca.inforealm.core.model.PreferenceDefinition;
import ca.inforealm.core.model.PreferenceValue;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.security.GlobalRoles;
import ca.inforealm.core.service.PreferenceService;

/**
 * Preferences Service
 * 
 * TODO use an optional ThreadLocal+filter scheme to cache preference data
 * per-request
 * 
 * TODO mock tests to cover IllegalStateException's where >1 rows are returned
 * (below)
 * 
 * @author Jason Mroz
 */
@Transactional
public class PreferenceServiceImpl extends AbstractServiceImpl implements PreferenceService {

	/** SANE context. */
	@Autowired
	private SaneContext saneContext;

	/** Query to load all preference values for a user in the application. */
	private final static String QUERY_GET_ALL_PREFERENCES = "from PreferenceValue where preferenceDefinition.application.id = ? and actor = ?";

	/** Query to load a single preference value. */
	private final static String QUERY_GET_PREFERENCE = "from PreferenceValue where preferenceDefinition.application.id = ? and actor = ? and preferenceDefinition.identifier = ?";

	/** Query to determine if the user has a given preference value set. */
	private final static String QUERY_HAS_PREFERENCE = "select count(p) from PreferenceValue p "
			+ "where p.preferenceDefinition.application.id = ? and p.actor = ? and p.preferenceDefinition.identifier = ?";

	// ======================================================================

	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public Map<String, String> getPreferences() {

		// ensure secure context exists
		requireSecureContext();

		Map<String, String> retval = new HashMap<String, String>();
		UserAccount user = getCurrentUser();

		Object[] queryParams = new Object[] { saneContext.getApplicationModel().getId(), user };
		Collection<PreferenceValue> list = find(QUERY_GET_ALL_PREFERENCES, queryParams);
		for (PreferenceValue pref : list) {
			String key = pref.getPreferenceDefinition().getIdentifier();
			String value = pref.getValue();

			retval.put(key, value);
		}

		return retval;
	}

	/**
	 * TODO this might be more efficient using a load/compare/update/insert
	 * algorithm
	 */
	@SuppressWarnings("unchecked")
	@Secured( { GlobalRoles.ROLE_USER })
	public void setPreferences(Map<String, String> newList) {

		Assert.notNull(newList, "setPreferences does not accept nulls");

		// ensure secure context exists
		requireSecureContext();
		UserAccount user = getCurrentUser();

		// list of new preference values
		Collection<String> newKeys = new ArrayList<String>();

		// get all existing preferences
		Object[] queryParams = new Object[] { saneContext.getApplicationModel().getId(), user };
		Collection<PreferenceValue> existing = find(QUERY_GET_ALL_PREFERENCES, queryParams);
		Collection<String> existingKeys = new ArrayList<String>();
		for (PreferenceValue pv : existing) {
			String prefId = pv.getPreferenceDefinition().getIdentifier();
			existingKeys.add(prefId);
		}

		for (String key : newList.keySet()) {
			if (existingKeys.contains(key)) {
				// this preference in the New List also existed in the old set
			} else {
				// this preference in the New List does not currently exist in
				// the back end and must be created from scratch
				newKeys.add(key);
			}
		}

		// delete any that have been emptied/null/aren't in the map
		for (Iterator iterator = existing.iterator(); iterator.hasNext();) {
			boolean doDelete = false;
			PreferenceValue pv = (PreferenceValue) iterator.next();

			String key = pv.getPreferenceDefinition().getIdentifier();
			if (newList.containsKey(key)) {
				// it still exists as a preference
				// if value is empty, delete it
				String newValue = newList.get(key);
				if (newValue == null || newValue.trim().length() == 0) {
					doDelete = true;
				}
			} else {
				// the existing preference is no longer in the preferences map
				doDelete = true;
			}

			if (doDelete) {
				delete(pv);
				iterator.remove();
				// existing.remove(pv);
			}
		}

		//
		// now we have deleted removed preferences and have a list of potential
		// changes; determine which preference values have changed, and
		// persist them
		//
		for (PreferenceValue pv : existing) {
			// determine if the object has changed
			String key = pv.getPreferenceDefinition().getIdentifier();
			String newValue = newList.get(key);

			if (newValue.equals(pv.getValue())) {
				// values are the same: no change required
			} else {
				// value changed, apply change and re-persist
				pv.setValue(newValue);
			}
		}

		//
		// finally, add any new preference values
		//
		for (String key : newKeys) {
			PreferenceDefinition prefDef = saneContext.getPreferenceDefinition(key);
			Assert.notNull(prefDef, "unable to find preference key: " + key);

			PreferenceValue pref = new PreferenceValue();
			pref.setActor(user);
			pref.setPreferenceDefinition(prefDef);
			pref.setValue(newList.get(key));

			persist(pref);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.service.PreferenceService#getPreference(java.lang.String)
	 */
	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public String getPreference(String identifier) {

		// ensure secure context exists
		requireSecureContext();

		String retval = null;
		UserAccount user = getCurrentUser();

		Object[] queryParams = new Object[] { saneContext.getApplicationModel().getId(), user, identifier };
		Collection<PreferenceValue> list = find(QUERY_GET_PREFERENCE, queryParams);

		Assert.isTrue(list.size() < 2, "unexpected: more than one preference matched");

		// existing value
		if (list.size() == 1) {
			PreferenceValue value = list.iterator().next();
			retval = value.getValue();
		}

		// ensure it's always at least an empty string since null values
		// aren't permitted
		if (retval == null) {
			retval = "";
		}
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.service.PreferenceService#hasPreference(java.lang.String)
	 */
	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public boolean hasPreference(final String identifier) {

		boolean retval; // NOT initialized

		// ensure secure context exists
		requireSecureContext();

		final UserAccount user = getCurrentUser();

		final Long applicationId = saneContext.getApplicationModel().getId();

		long count = (Long) execute(new HibernateCallback() {
			public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException,
					java.sql.SQLException {

				Query query = session.createQuery(QUERY_HAS_PREFERENCE);
				query.setParameter(0, applicationId);
				query.setParameter(1, user);
				query.setParameter(2, identifier);

				return query.uniqueResult();
			}
		});

		retval = (0 != count);

		// done
		return retval;
	}

	/**
	 * TODO it may be more efficient to just delete+insert rather than
	 * select+(insert|update)
	 */
	@Override
	@Secured( { GlobalRoles.ROLE_USER })
	public void setPreference(String identifier, String value) {

		// ensure secure context exists
		requireSecureContext();

		UserAccount user = getCurrentUser();
		final boolean deletePreference = (value == null);

		Object[] queryParams = new Object[] { saneContext.getApplicationModel().getId(), user, identifier };
		Collection<PreferenceValue> list = find(QUERY_GET_PREFERENCE, queryParams);

		Assert.isTrue(list.size() < 2, "unexpected: more than one preference matched");

		// existing value
		if (list.size() == 0) {
			// no such preference value
			if (!deletePreference) {
				insertNewPreference(identifier, value);
			}
		} else {
			// preference already exists - update
			PreferenceValue existingObject = list.iterator().next();
			if (!deletePreference) {
				existingObject.setValue(value);
				persist(existingObject);
			} else {
				// we're deleting here
				delete(existingObject);
			}
		}
	}

	// ======================================================================

	protected PreferenceDefinition getPreferenceDefinition(final String identifier) {

		// ensure secure context exists
		requireSecureContext();

		final Long applicationId = saneContext.getApplicationModel().getId();

		PreferenceDefinition retval = (PreferenceDefinition) execute(new HibernateCallback() {
			public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException,
					java.sql.SQLException {

				Query query = session.createQuery("from PreferenceDefinition where application.id = ? and identifier = ?");
				query.setParameter(0, applicationId);
				query.setParameter(1, identifier);

				return query.uniqueResult();
			}

		});

		// done
		return retval;
	}

	protected PreferenceValue insertNewPreference(String identifier, String value) {

		// ensure secure context exists
		requireSecureContext();

		UserAccount user = getCurrentUser();

		PreferenceDefinition preferenceDefinition = getPreferenceDefinition(identifier);
		// Assert.notNull(preferenceDefinition, "no such preference");
		if (preferenceDefinition == null) {
			// illegal argument - there is no such preference
			log.error("cannot insert preference for non-existent preference identifier: " + identifier);
			throw new IllegalArgumentException("no such preference");
		}

		PreferenceValue retval = new PreferenceValue();
		retval.setActor(user);
		retval.setPreferenceDefinition(preferenceDefinition);
		retval.setValue(value);

		persist(retval);

		return retval;
	}

}
