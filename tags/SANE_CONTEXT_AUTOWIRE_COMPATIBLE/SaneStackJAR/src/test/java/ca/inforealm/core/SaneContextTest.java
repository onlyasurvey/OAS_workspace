package ca.inforealm.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.Collection;

import org.junit.Test;

import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.PreferenceDefinition;
import ca.inforealm.core.security.GlobalRoles;

public class SaneContextTest extends AbstractTestDataCreatingBaseTest {

	@Test
	public void testApplicationModelLoad() {
		Application application = getSaneContext().getApplicationModel();
		assertNotNull("applicationModel should be non-null", application);
		assertNotNull("applicationModel should have a proper ID", application.getId());

	}

	@Test
	public void testReloadApplicationModel_SuccessPath() {

		Application before = getSaneContext().getApplicationModel();
		getSaneContext().reloadApplicationModel();
		Application after = getSaneContext().getApplicationModel();

		assertEquals("expected the same ID", before.getId(), after.getId());
	}

	@Test
	public void testReloadApplicationModel_InvalidState() {

		// Application before = getSaneContext().getApplicationModel();
		getSaneContext().reloadApplicationModel();

		try {
			getSaneContext().setApplicationIdentifier("someInvalidThing");
			getSaneContext().reloadApplicationModel();
			fail("reloadApplicationModel should have failed for an invalid identifier");
		} catch (IllegalArgumentException ise) {
			// expected
		}
	}

	@Test
	public void testSanity_getMergedApplicationModel() {

		Application model = getSaneContext().getMergedApplicationModel();

		// should be in a sane state currently
		assertNotNull("should be able to load the merged model", model);

		try {
			getHibernateTemplate().delete(model);
			flushAndClear();

			// reload
			getSaneContext().getMergedApplicationModel();

			fail("should have received a IllegalStateException");
		} catch (IllegalStateException ise) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testGetPreferenceDefinitions() {
		Application application = createModelScenario1();
		getSaneContext().setApplicationIdentifier(application.getIdentifier());
		getSaneContext().reloadAllModels();

		//
		Collection<PreferenceDefinition> list = getSaneContext().getPreferenceDefinitions();
		assertNotNull("should have received a list", list);
		assertEquals("should have 1 preference definition", 1, list.size());
	}

	// ROLE_USER is supposed to exist for every application
	@Test
	public void testAssertRoleExists_Pass() {
		try {
			getSaneContext().assertRoleExists(GlobalRoles.ROLE_USER);
			// good
		} catch (IllegalArgumentException e) {
			fail("unexpected exception: " + e.getMessage());
		}
	}

	@Test
	public void testAssertRoleExists_Fail() {
		try {
			getSaneContext().assertRoleExists("ROLE_BAR" + getMBUN());
			fail("should NOT have random role");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================
}
