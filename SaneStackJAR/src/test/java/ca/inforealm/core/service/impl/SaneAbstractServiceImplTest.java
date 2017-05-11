package ca.inforealm.core.service.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.SaneContext;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.persistence.DataAccessObject;

/**
 * Test for AbstractServiceImpl
 * 
 * @author Jason Mroz
 */
public class SaneAbstractServiceImplTest extends AbstractBaseTest {

	/** SANE context. */
	@Autowired
	private SaneContext saneContext;

	/**
	 * Sample subclass of abstract class under test.
	 */
	private class SampleService extends AbstractServiceImpl {
		public SampleService(SaneContext saneContext, DataAccessObject dataAccessObject) {
			// this.setSaneContext(saneContext);
			this.setDataAccessObject(dataAccessObject);
		}
	}

	protected SampleService newSampleService() {
		return new SampleService(getSaneContext(), getDAO());
	}

	// ======================================================================

	@Test
	public void testFind_NoArg_Pass() {
		SampleService service = newSampleService();

		createTestApplication("someIdentifier");

		flushAndClear();

		Collection<Application> list = service.find("from Application");
		assertNotNull("find should never return null", list);
		assertTrue("should have returned at least 1 object", list.size() > 0);

		int count1 = list.size();

		createTestApplication("someFancyIdentifier");

		flushAndClear();

		Collection<Application> newList = service.find("from Application");
		assertNotNull("find should never return null", newList);
		assertTrue("should have one more object", newList.size() == count1 + 1);
	}

	@Test
	public void testFind_OneArg_Pass() {
		SampleService service = newSampleService();

		Application newApp = createTestApplication("someIdentifier");

		flushAndClear();

		Collection<Application> list = service.find("from Application where identifier = ?", newApp.getIdentifier());
		assertNotNull("find should never return null", list);
		assertEquals("should have returned 1 object", 1, list.size());
		assertEquals("should be the same object", newApp.getIdentifier(), list.iterator().next().getIdentifier());
	}

	// ======================================================================

	@Test
	public void testGet_Pass() {
		SampleService service = newSampleService();

		Application newApp = createTestApplication("someIdentifier");

		flushAndClear();

		Application application = service.get(Application.class, newApp.getId());
		assertNotNull("should have loaded app", application);
	}

	// ======================================================================

	@Test
	public void testUnique_ZeroElementsPass() {
		SampleService service = newSampleService();
		List<String> list = new ArrayList<String>();
		String value = service.unique(list);

		assertNull("should have no value", value);
	}

	@Test
	public void testUnique_OneElementPass() {
		SampleService service = newSampleService();
		List<String> list = new ArrayList<String>();
		list.add("foo");
		String value = service.unique(list);

		assertNotNull("should have value", value);
		assertEquals("should have correct value", "foo", value);
	}

	@Test
	public void testUnique_MultipleElementFail() {
		SampleService service = newSampleService();
		List<String> list = new ArrayList<String>();
		list.add("foo");
		list.add("bar");

		try {
			service.unique(list);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testLoad_LazyLoading_Pass() {
		SampleService service = newSampleService();

		Application newApp = createTestApplication("someIdentifier");

		flushAndClear();

		Application application = service.load(Application.class, newApp.getId());

		try {
			application.getIdentifier();
			// good
		} catch (ObjectNotFoundException e) {
			// not good
			fail("failed to lazily load newly created entity");
		}
	}

	@Test
	public void testLoad_LazyLoading_FailsOnInvalidId() {
		SampleService service = newSampleService();
		Application application = service.load(Application.class, -123L);

		try {
			application.getIdentifier();
			fail("should have thrown");
		} catch (ObjectNotFoundException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testExecute() {
		SampleService service = newSampleService();

		Application newApp = createTestApplication("someIdentifier");
		final Long newId = newApp.getId();

		flushAndClear();

		Application application = (Application) service.execute(new HibernateCallback() {
			@Override
			public Application doInHibernate(Session session) throws HibernateException, SQLException {
				Application application = (Application) session.get(Application.class, newId);
				return application;
			}
		});

		assertNotNull("should have loaded an object", application);
		assertEquals("should have same ids", newId, application.getId());
	}

}
