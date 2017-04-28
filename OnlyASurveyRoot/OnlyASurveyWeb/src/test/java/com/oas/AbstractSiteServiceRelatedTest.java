package com.oas;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

abstract public class AbstractSiteServiceRelatedTest extends AbstractOASBaseTest {

	// ======================================================================

	protected long countContactMessages() {
		Long retval = (Long) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("select count(m) from ContactUsMessage m").uniqueResult();
			}
		});

		// auto-boxing in use
		return retval;
	}
}
