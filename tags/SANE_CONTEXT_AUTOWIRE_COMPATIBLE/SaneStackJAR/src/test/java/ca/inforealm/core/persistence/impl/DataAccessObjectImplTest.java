package ca.inforealm.core.persistence.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.UserAccount;

/**
 * Test the DataAccessObjectImpl class. All tests use the current Application
 * model as a base.
 * 
 * @author Jason Mroz
 * 
 */
public class DataAccessObjectImplTest extends AbstractTestDataCreatingBaseTest {

	protected void applicationEqualityAssertions(Application loadedModel) {

		Application expected = getSaneContext().getApplicationModel();

		assertNotNull(expected);
		assertNotNull(loadedModel);

		assertEquals(expected, loadedModel);
	}

	protected void applicationIdAssertions(Application loadedModel) {

		Long expectedId = getSaneContext().getApplicationModel().getId();

		assertNotNull(expectedId);
		assertNotNull(loadedModel);
		assertNotNull(loadedModel.getId());

		assertEquals(expectedId, loadedModel.getId());
	}

	// ======================================================================

	@Test
	public void testLoad() {

		Long id = getSaneContext().getApplicationModel().getId();
		Application subject = (Application) getDAO().load(Application.class, id);

		applicationIdAssertions(subject);
		applicationEqualityAssertions(subject);
	}

	@Test
	public void testFind_NoParams() {

		// Long id = getSaneContext().getApplicationModel().getId();
		String identifier = getSaneContext().getApplicationIdentifier();

		// there is no parameter, just a query string; this query is obviously
		// bad style, but convenient in a test
		Collection list = getDAO().find("from Application where identifier = '" + identifier + "'");

		assertNotNull(list);
		assertEquals(1, list.size());

		Application subject = (Application) list.iterator().next();

		applicationIdAssertions(subject);
		applicationEqualityAssertions(subject);
	}

	@Test
	public void testFind_SingleParam() {

		// Long id = getSaneContext().getApplicationModel().getId();
		String identifier = getSaneContext().getApplicationIdentifier();
		Collection list = getDAO().find("from Application where identifier=?", identifier);

		assertNotNull(list);
		assertEquals(1, list.size());

		Application subject = (Application) list.iterator().next();

		applicationIdAssertions(subject);
		applicationEqualityAssertions(subject);
	}

	@Test
	public void testFind_ObjectArrayParams() {

		// Long id = getSaneContext().getApplicationModel().getId();
		String identifier = getSaneContext().getApplicationIdentifier();
		Collection<Application> list = getDAO().find("from Application where identifier=?", new Object[] { identifier });

		assertNotNull(list);
		assertEquals(1, list.size());

		Application subject = list.iterator().next();

		applicationIdAssertions(subject);
		applicationEqualityAssertions(subject);
	}

	@Test
	public void testPersist_InvalidArgument_Null() {
		try {
			getDAO().persist(null);
			fail("should have thrown a IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}
	}

	@Test
	public void testPersist_InvalidArgument_InvalidIdentifier() {
		try {
			Application subject = new Application();
			subject.setNameEn("testPersist_InvalidArgument_InvalidIdentifier_en");
			subject.setNameFr("testPersist_InvalidArgument_InvalidIdentifier_fr");
			subject.setIdentifier(null);
			getDAO().persist(subject);
			flushAndClear();

			fail("should have thrown an Exception");
			// } catch (DataIntegrityViolationException iae) {
		} catch (Exception iae) {
			// expected
		}
		// this test intentionally mangles the transaction, meaning work done in
		// tearDownInTransaction cannot be run since it calls flush() to ensure
		// SQL constraint sanity.
		expectInvalidTransactionalState();
	}

	@Test
	public void testPersist_InvalidArgument_DupeIdentifier() {
		try {
			Application subject = new Application();
			subject.setIdentifier("someApp" + getMBUN());
			getDAO().persist(subject);
			flushAndClear();

			// should be ok so far
			Application badApp = new Application();
			badApp.setIdentifier(subject.getIdentifier());
			getDAO().persist(badApp);
			flushAndClear();

			fail("should have thrown a DataIntegrityViolationException");
		} catch (DataIntegrityViolationException iae) {
			// expected
		}
	}

	@Test
	public void testExecute_Pass() {

		// initial data
		final UserAccount first = createTestUser();

		// reload using execute()
		UserAccount second = (UserAccount) getDAO().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List<UserAccount> list = session.createQuery("from UserAccount where username = ?").setParameter(0,
						first.getUsername()).list();

				assertNotNull(list);
				assertEquals(1, list.size());

				UserAccount second = list.get(0);
				return second;
			}
		});

		// compare
		assertNotNull(second);
		assertEquals(second.getId(), first.getId());
		assertEquals(second.getUsername(), first.getUsername());
	}
}
