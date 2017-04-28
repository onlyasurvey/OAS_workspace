package ca.inforealm.core.persistence;

import java.io.Serializable;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

public interface DataAccessObject {

	public void delete(Object entity);

	public <E> List<E> find(String queryString);

	public <E> List<E> find(String queryString, Object value);

	// public Collection find(String queryString, Object value);
	public <E> List<E> find(String queryString, Object[] values);

	/**
	 * Attempt to load an object from the backend, returning an empty proxy
	 * object if it doesn't exist. You might prefer get().
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	// public Object load(Class clazz, Serializable id);
	public <C> C load(Class<C> clazz, Serializable id);

	/**
	 * Attempt to load an object from the backend, returning NULL if it doesn't
	 * exist.
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	// public Object get(Class clazz, Serializable id);
	public <C> C get(Class<C> clazz, Serializable id);

	public HibernateTemplate getHibernateTemplate();

	public void persist(Object entity);

	public Object execute(HibernateCallback action);
}