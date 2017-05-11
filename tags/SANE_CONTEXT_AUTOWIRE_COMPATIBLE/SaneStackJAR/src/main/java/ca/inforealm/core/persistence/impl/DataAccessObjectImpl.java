package ca.inforealm.core.persistence.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import ca.inforealm.core.persistence.DataAccessObject;

/**
 * General Data Access Object for Hibernate persistence. Suitable for use
 * standalone or as a parent class.
 * 
 * @author Jason Mroz
 * 
 */
@Transactional(readOnly = false)
public class DataAccessObjectImpl extends HibernateDaoSupport implements DataAccessObject {

	@Override
	@Transactional(readOnly = false)
	final public void delete(Object entity) {
		getHibernateTemplate().delete(entity);
	}

	@Override
	final public <E> List<E> find(String queryString) {
		return getHibernateTemplate().find(queryString);
	}

	@Override
	final public <E> List<E> find(String queryString, Object value) {
		return getHibernateTemplate().find(queryString, value);
	}

	@Override
	final public <E> List<E> find(String queryString, Object[] values) {
		return getHibernateTemplate().find(queryString, values);
	}

	@Override
	final public <C> C load(Class<C> clazz, Serializable id) {
		return (C) getHibernateTemplate().load(clazz, id);
	}

	@Override
	final public <C> C get(Class<C> clazz, Serializable id) {
		return (C) getHibernateTemplate().get(clazz, id);
	}

	@Override
	@Transactional(readOnly = false)
	final public void persist(Object entity) {
		getHibernateTemplate().persist(entity);
	}

	@Override
	@Transactional(readOnly = false)
	final public Object execute(HibernateCallback action) {
		return getHibernateTemplate().execute(action);
	}

}
