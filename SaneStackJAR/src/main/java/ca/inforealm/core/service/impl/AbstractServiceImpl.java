package ca.inforealm.core.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.security.SecurityUtil;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.service.AbstractServiceInterface;

/**
 * Superclass for all service layer implementations.
 */

abstract public class AbstractServiceImpl implements AbstractServiceInterface, DataAccessObject {

	/** Logger */
	protected Logger log = Logger.getLogger(this.getClass());

	/** SANE context */
	// @Autowired
	// @Qualifier("saneContext")
	// protected SaneContext saneContext;
	/** Data Access Object for interacting with the persistence layer. */
	@Autowired
	@Qualifier("dataAccessObject")
	protected DataAccessObject dataAccessObject;

	// ======================================================================
	// ======================================================================

	/**
	 * Require a secure context, ensuring that calling code has an authenticated
	 * user with at least one role in the current application.
	 */
	protected void requireSecureContext() {
		SecurityUtil.requireSecureContext();
	}

	/**
	 * Require a secure context, ensuring that calling code has an authenticated
	 * user.
	 */
	protected void requireSecureContext(boolean requireRoles) {
		SecurityUtil.requireSecureContext(requireRoles);
	}

	/**
	 * Get the current user from the security context; this returns the actual
	 * UserAccount in use.
	 * 
	 * @return {@link UserAccount}
	 */
	protected UserAccount getCurrentUser() {
		return SecurityUtil.getCurrentUser();
	}

	/**
	 * Get the current locale from LocaleContextHolder.
	 * 
	 * @return Locale
	 */
	protected Locale getCurrentLocale() {
		return LocaleContextHolder.getLocale();
	}

	// /**
	// * Returns a (disconnected) reference to the Application model in use for
	// * this application.
	// *
	// * @return
	// */
	// protected Application getApplicationModel() {
	// return saneContext.getApplicationModel();
	// }

	// ======================================================================
	// convenience access to the DAO
	// ======================================================================
	@Override
	@Unsecured
	final public void delete(Object entity) {
		assertValidDao();
		getDataAccessObject().delete(entity);
	}

	@Override
	@Unsecured
	final public <E> List<E> find(String queryString) {
		assertValidDao();
		return getDataAccessObject().find(queryString);
	}

	@Override
	@Unsecured
	final public <E> List<E> find(String queryString, Object value) {
		assertValidDao();
		return getDataAccessObject().find(queryString, value);
	}

	@Override
	@Unsecured
	final public <E> List<E> find(String queryString, Object[] values) {
		assertValidDao();
		return getDataAccessObject().find(queryString, values);
	}

	@Override
	@Unsecured
	final public <C> C get(Class<C> clazz, Serializable id) {
		assertValidDao();
		return getDataAccessObject().get(clazz, id);
	}

	@Override
	@Unsecured
	final public <C> C load(Class<C> clazz, Serializable id) {
		assertValidDao();
		return getDataAccessObject().load(clazz, id);
	}

	@Override
	@Unsecured
	final public void persist(Object entity) {
		assertValidDao();
		getDataAccessObject().persist(entity);
	}

	@Override
	@Unsecured
	final public Object execute(HibernateCallback action) {
		assertValidDao();
		return getHibernateTemplate().execute(action);
	}

	@Unsecured
	final public <T> T unique(List<T> list) {

		assertValidDao();
		Assert.notNull(list);

		switch (list.size()) {
		case 0:
			return null;
		case 1:
			return list.get(0);
		default:
			throw new IllegalArgumentException("passed collection has more than one element");
		}
	}

	// ======================================================================

	@Override
	@Unsecured
	public HibernateTemplate getHibernateTemplate() {
		return getDataAccessObject().getHibernateTemplate();
	}

	// ======================================================================

	/**
	 * @param saneContext
	 *            the saneContext to set
	 */
	// @Required
	// @Unsecured
	// public void setSaneContext(SaneContext saneContext) {
	// this.saneContext = saneContext;
	// }
	// /**
	// * @return the saneContext
	// */
	// @Unsecured
	// public SaneContext getSaneContext() {
	// return saneContext;
	// }
	// ======================================================================
	@Unsecured
	public DataAccessObject getDataAccessObject() {
		return dataAccessObject;
	}

	@Unsecured
	public void setDataAccessObject(DataAccessObject dataAccessObject) {
		this.dataAccessObject = dataAccessObject;
	}

	// ======================================================================

	private void assertValidDao() {
		Assert.notNull(getDataAccessObject(), "no dataAccessObject is available");
	}
}
