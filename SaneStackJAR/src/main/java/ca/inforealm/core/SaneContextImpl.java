package ca.inforealm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.PreferenceDefinition;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.service.event.ApplicationModelChangedEvent;
import ca.inforealm.core.support.autoinstall.ApplicationAutoInstaller;

/**
 * The "SANE" context in use for this application. Some model data is not thread
 * safe, but is also considered immutable outside of this class.
 * 
 * TODO revisit the synchronized blocks herein
 * 
 * @author Jason Mroz
 */
// @Component
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class SaneContextImpl extends HibernateDaoSupport implements SaneContext, ApplicationContextAware, ApplicationListener {

	/** The current application context, used here for publishing events. */
	private ApplicationContext applicationContext;

	/**
	 * The application model, as last loaded from the backend.
	 */
	private Application applicationModel = null;

	/** The application's identifier, used to load an Application model. */
	private String applicationIdentifier = null;

	/**
	 * Role definitions for the application, set by reloadAllModels().
	 */
	private Collection<RoleDefinition> roleDefinitions = null;
	private Map<String, RoleDefinition> roleKeyMap = null;

	/**
	 * Preference definitions for the application, set by reloadAllModels().
	 */
	private Collection<PreferenceDefinition> preferenceDefinitions = null;
	private Map<String, PreferenceDefinition> preferenceKeyMap = null;

	/** Logger. */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * Allows the deferral of loading of the Application model, to support test
	 * cases which need to create their Application object after this bean is
	 * initialized.
	 */
	private boolean deferApplicationModelLoading = false;

	/**
	 * (Optional) Implementation of the interface for auto-registering and
	 * configuring applications the first time they are run.
	 */
	@Autowired(required = false)
	private ApplicationAutoInstaller applicationAutoInstaller;

	// ======================================================================

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			if (!deferApplicationModelLoading) {
				// refreshed, so reload
				Assert.notNull(this.getApplicationIdentifier(), "cannot refresh SANE context: no application identifier is set");
				//
				reloadAllModels();
			} else {
				log.debug("deferring loading of application models after ContextRefreshedEvent due to configuration");
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#reloadAllModels()
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public synchronized void reloadAllModels() {

		log.info("reloading all application models: " + getApplicationIdentifier());

		reloadApplicationModel();
		reloadRoleDefinitions();
		reloadPreferenceDefinitions();

		sanityCheckModels();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#reloadApplicationModel()
	 */
	public synchronized void reloadApplicationModel() {

		log.info("reloading application model according to defined applicationIdentifier: " + getApplicationIdentifier());

		Application model = loadApplicationModel(getApplicationIdentifier());
		if (model == null) {
			String msg = "unable to load application model for identifier: " + getApplicationIdentifier();
			log.error(msg);
			throw new IllegalStateException(msg);
		} else {
			log.debug("reloaded application model");
			applicationModel = model;
		}

		applicationContext.publishEvent(new ApplicationModelChangedEvent(model));
	}

	@SuppressWarnings("unchecked")
	protected Application loadApplicationModel(String identifier) {

		Application retval = null;

		log.info("loading application model by identifier: " + identifier);

		Assert.notNull(identifier);
		Collection<Application> list = getHibernateTemplate().find("from Application where identifier = ?", identifier);

		final int count = list.size();

		// should have zero or one matches
		Assert.isTrue(count < 2, "unexpected number of matches for applicationIdentifier ('" + identifier + "'): " + list.size());

		if (count == 0) {
			log.info("No application registration was found for this application's identifier: " + identifier);
			if (applicationAutoInstaller != null) {
				//
				log.info("Calling application auto-installer: Inforealm registration");
				retval = applicationAutoInstaller.inforealmRegistration();

				Assert.isTrue(identifier.equals(retval.getIdentifier()),
						"auto-registration returned a different application identifier: " + retval.getIdentifier());

				Assert.notNull(retval, "auto-registration failed to return a model");

				//
				log.info("Calling application auto-installer: Custom installation");
				applicationAutoInstaller.customInstallation();

				// this ensures a clean path going forward on first request, and
				// that all registration operations did in fact complete
				// successfully
				getHibernateTemplate().flush();
			}
		} else {
			log.info("loaded application model by identifier: " + identifier);
			retval = list.iterator().next();
		}

		Assert.notNull(retval, "no application model exists");
		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#reloadRoleDefinitions()
	 */
	@SuppressWarnings("unchecked")
	public synchronized void reloadRoleDefinitions() {

		log.info("reloading role definitions");

		Assert.notNull(getApplicationModel());
		Collection<RoleDefinition> newDefs = getHibernateTemplate().find("from RoleDefinition where application=?",
				getApplicationModel());

		roleDefinitions = newDefs;
		roleKeyMap = new HashMap<String, RoleDefinition>();
		for (RoleDefinition rd : roleDefinitions) {
			// map it
			roleKeyMap.put(rd.getIdentifier(), rd);

			// remove it from Hibernate session
			getHibernateTemplate().evict(rd);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#reloadPreferenceDefinitions()
	 */
	@SuppressWarnings("unchecked")
	public synchronized void reloadPreferenceDefinitions() {

		log.info("reloading preference definitions");

		Assert.notNull(getApplicationModel());
		Collection<PreferenceDefinition> newDefs = getHibernateTemplate().find("from PreferenceDefinition where application=?",
				getApplicationModel());

		preferenceDefinitions = newDefs;
		preferenceKeyMap = new HashMap<String, PreferenceDefinition>();
		for (PreferenceDefinition pref : preferenceDefinitions) {
			// map it
			preferenceKeyMap.put(pref.getIdentifier(), pref);

			// remove it from Hibernate session
			getHibernateTemplate().evict(pref);
		}
	}

	// ======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#getMergedApplicationModel()
	 */
	@SuppressWarnings("unchecked")
	public Application getMergedApplicationModel() {

		// sanity
		Assert.notNull(getApplicationModel());
		Assert.notNull(getApplicationModel().getId());

		// query
		ArrayList<Application> list = (ArrayList<Application>) getHibernateTemplate().find(
				"from Application where id=" + getApplicationModel().getId());
		if (list.size() == 1) {
			return list.iterator().next();
		} else {
			// not a sane state - we're simply loading a copy of a previously
			// loaded application which has mysteriously disappeared from the
			// backend
			String message = "unable to load a copy of application.id#" + getApplicationModel().getId();
			log.error(message);
			throw new IllegalStateException(message);
		}
	}

	// ======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.SaneContextInterface#assertRoleExists(java.lang.String)
	 */
	public void assertRoleExists(String identifier) {
		Assert.notNull(roleKeyMap, "role map is not initialized: null");
		Assert.notNull(roleKeyMap.get(identifier), "invalid role definition requested");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.SaneContextInterface#getRoleDefinition(java.lang.String)
	 */
	public RoleDefinition getRoleDefinition(String identifier) {
		Assert.notNull(roleKeyMap, "role map is not initialized: null");

		RoleDefinition retval = roleKeyMap.get(identifier);

		Assert.notNull(retval, "invalid role definition requested: " + identifier);

		return retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.SaneContextInterface#getPreferenceDefinition(java.lang.
	 * String)
	 */
	public PreferenceDefinition getPreferenceDefinition(String identifier) {
		Assert.notNull(preferenceKeyMap, "preference map is not initialized: null");
		return preferenceKeyMap.get(identifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#getRoleDefinitions()
	 */
	public Collection<RoleDefinition> getRoleDefinitions() {
		return roleDefinitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#getPreferenceDefinitions()
	 */
	public Collection<PreferenceDefinition> getPreferenceDefinitions() {
		return preferenceDefinitions;
	}

	// ======================================================================

	/**
	 * Perform sanity (consistency) checks of the loaded domain model.
	 */
	protected void sanityCheckModels() {
		Assert.notNull(applicationIdentifier, "invalid applicationIdentifier: null");
		Assert.notNull(applicationModel, "invalid applicationModel: null");
		Assert.state(applicationIdentifier.equals(applicationModel.getIdentifier()),
				"invalid state: applicationIdentifier does not identify the loaded applicationModel");

		Assert.notNull(roleDefinitions, "invalid roleDefinitions: null");
	}

	// ======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#getApplicationIdentifier()
	 */
	public String getApplicationIdentifier() {
		return applicationIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.SaneContextInterface#setApplicationIdentifier(java.lang
	 * .String)
	 */
	public void setApplicationIdentifier(String applicationIdentifier) {
		this.applicationIdentifier = applicationIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.gc.ic.sane.SaneContextInterface#getApplicationModel()
	 */
	public Application getApplicationModel() {
		return applicationModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.gc.ic.sane.SaneContextInterface#setDeferApplicationModelLoading(boolean
	 * )
	 */
	public void setDeferApplicationModelLoading(boolean deferApplicationModelLoading) {
		this.deferApplicationModelLoading = deferApplicationModelLoading;
	}

}
