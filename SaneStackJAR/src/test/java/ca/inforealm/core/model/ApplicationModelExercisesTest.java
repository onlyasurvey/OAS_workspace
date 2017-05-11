package ca.inforealm.core.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import ca.inforealm.core.AbstractTestDataCreatingBaseTest;

public class ApplicationModelExercisesTest extends AbstractTestDataCreatingBaseTest {

	@Test
	public void testCastFromActor_ToUserAccount() {

		// new user
		UserAccount account = createTestUser();

		flushAndClear();
		getHibernateTemplate().evict(account);

		// should always be one match; exception here is error, not failure
		Actor actor = (Actor) getHibernateTemplate().find("from Actor where id=?", account.getId()).iterator().next();
		assertEquals(account.getId(), actor.getId());
		assertEquals(account.getUsername(), actor.getDisplayTitle());

		// test the cast
		UserAccount subject = null;
		try {
			subject = (UserAccount) actor;
		} catch (ClassCastException cce) {
			fail("UserAccount failed to cast naturally from Actor");
		}

		assertEquals("should be the same identifier", actor.getId(), subject.getId());

		assertEquals(account.getDisplayTitle(), subject.getDisplayTitle());
	}

	@Test
	public void testCastFromActor_ToActorGroup() {

		assertNotNull(getSaneContext().getApplicationModel());

		// new user
		ActorGroup group = new ActorGroup();

		getHibernateTemplate().merge(getSaneContext().getApplicationModel());
		group.setApplication(getSaneContext().getApplicationModel());
		group.setNameEn("nameEn#" + getMBUN());
		group.setNameFr("nameFr#" + getMBUN());
		// group.setId(account.getId());

		getHibernateTemplate().persist(group);

		flushAndClear();
		getHibernateTemplate().evict(group);

		// should always be one match; exception here is error, not failure
		Actor actor = (Actor) getHibernateTemplate().find("from Actor where id=?", group.getId()).iterator().next();
		assertEquals(group.getId(), actor.getId());
		assertEquals(group.getNameEn(), actor.getDisplayTitle());

		// test the cast
		ActorGroup subject = null;
		try {
			subject = (ActorGroup) actor;
		} catch (ClassCastException cce) {
			fail("ActorGroup failed to cast naturally from Actor");
		}

		assertEquals("should be the same identifier", actor.getId(), subject.getId());
		assertEquals(group.getDisplayTitle(), subject.getDisplayTitle());
	}

	@Test
	public void testModel1Scenario() {

		Application application = createModelScenario1();

		flushAndClear();

		String expectedStartUrl = application.getStartUrl();

		// change the application context and reload
		getSaneContext().setApplicationIdentifier(application.getIdentifier());
		getSaneContext().reloadApplicationModel();

		assertEquals(expectedStartUrl, application.getStartUrl());

		// config items
		{
			Collection<ConfigurationItem> configItemList = application.getConfigurationItems();
			assertEquals("should have 1 item", 1, configItemList.size());

			ConfigurationItem configItem = configItemList.iterator().next();
			assertEquals("should be assigned to correct app", application.getId(), configItem.getApplication().getId());
			assertEquals("should be assigned to correct value type", "boolean", configItem.getValueType());
		}

		// actor groups
		{
			Collection<ActorGroup> actorGroupList = application.getActorGroups();
			assertEquals("should have 1 item", 1, actorGroupList.size());

			ActorGroup actorGroup = actorGroupList.iterator().next();
			assertEquals("should be assigned to correct app", application.getId(), actorGroup.getApplication().getId());
		}

		/*
		 * // config item definition
		 * assertNotNull(application.getConfigurationItemDefinitions());
		 * assertEquals(1,
		 * application.getConfigurationItemDefinitions().size());
		 * 
		 * ConfigurationItemDefinition itemDef = (ConfigurationItemDefinition)
		 * application.getConfigurationItemDefinitions().iterator().next();
		 * assertEquals(1, itemDef.getItems().size());
		 * assertEquals("inventoryLocked", itemDef.getIdentifier());
		 * assertEquals("should have the same definition",
		 * itemDef.getValueType(), "boolean"); // config item value
		 * ConfigurationItem item = itemDef.getItems().iterator().next();
		 * assertEquals("false", item.getValue());
		 * 
		 * assertEquals("should be the same definition", itemDef.getId(),
		 * item.getDefinition().getId()); assertNull("should be the same item",
		 * item.getLanguage()); assertEquals("itemDef.application should match",
		 * application, itemDef.getApplication());
		 */
	}

	@Test
	public void testRoleDefinitionModels() {

		Application application = createModelScenario1();

		assertEquals("Expected 2 roles to be defined", 2, application.getRoleDefinitions().size());

		ArrayList<String> expectedRoles = new ArrayList<String>(2);
		expectedRoles.add("ROLE_USER");
		expectedRoles.add("ROLE_APP_ADMIN");

		for (RoleDefinition rd : application.getRoleDefinitions()) {
			if (expectedRoles.contains(rd.getIdentifier())) {
				// remove this from expected list
				expectedRoles.remove(rd.getIdentifier());
			} else {
				// error: an unexpected role was returned
				fail("an unexpected role was returned: " + rd.getIdentifier());
			}
		}

		assertEquals("should have found all expected roles, but some remain.", 0, expectedRoles.size());
	}

	@Test
	public void testPreferenceDefinitions() {
		Application application = createModelScenario1();

		assertEquals("unexpected # of preferences defined", 1, application.getPreferenceDefinitions().size());
		assertEquals("unexpected identifier", "fancyAjax", application.getPreferenceDefinitions().iterator().next()
				.getIdentifier());
		assertEquals("unexpected application", application, application.getPreferenceDefinitions().iterator().next()
				.getApplication());

	}

	@Test
	public void testActorRoleMapping() {

		Application application = createModelScenario1();

		assertEquals("Expected 2 roles to be defined", 2, application.getRoleDefinitions().size());

		Collection<ActorRole> actorList = getHibernateTemplate().find("from ActorRole where role.application=?", application);

		assertEquals("should have 1 actor-role mappin", 1, actorList.size());

		ActorRole ar = actorList.iterator().next();
		assertEquals("should have USER role", "ROLE_USER", ar.getRole().getIdentifier());
		assertEquals("should be in correct application", application, ar.getRole().getApplication());
		assertNotNull("should have a user", ar.getActor());
	}
}
