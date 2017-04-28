package ca.inforealm.core.service.dao;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ResourceString;
import ca.inforealm.core.service.ResourceStringService;

public class ResourceStringServiceDAOTest extends AbstractBaseTest {

	@Autowired
	private ResourceStringServiceDAO resourceStringServiceDAO;

	@Autowired
	private ResourceStringService resourceStringService;

	protected Application createScenario1(ResourceString[] strings) {
		Application application = createTestApplication("app" + getMBUN());

		if (strings != null) {
			for (ResourceString string : strings) {
				string.setApplication(application);
				getHibernateTemplate().persist(string);
			}
		}

		// change SANE context to our test app
		getSaneContext().setApplicationIdentifier(application.getIdentifier());
		getSaneContext().reloadAllModels();

		// tell service to reinitialize
		resourceStringService.reloadResources();

		return application;
	}

	@Test
	public void differentialReload() {

		Date olderDate = new Date();
		Date newerDate = new Date(olderDate.getTime() + 1L);

		ResourceString str1 = new ResourceString(null, "key1", "en", "fr");
		ResourceString str2 = new ResourceString(null, "key2", "en", "fr");
		ResourceString str3 = new ResourceString(null, "key3", "en", "fr");

		str1.setLastModifiedDate(olderDate);
		str2.setLastModifiedDate(olderDate);
		str3.setLastModifiedDate(newerDate);

		// scenario data
		createScenario1(new ResourceString[] { str1, str2, str3 });

		// differential reload should return zero because newerDate is the
		// newest in the database
		Collection<ResourceString> shouldBeEmpty = resourceStringServiceDAO.loadNewerResources(newerDate);
		assertNotNull(shouldBeEmpty);
		assertEquals("should have zero strings", 0, shouldBeEmpty.size());

		// differential reload should return one because one record is newer
		// than olderDate
		Collection<ResourceString> shouldHaveOne = resourceStringServiceDAO.loadNewerResources(olderDate);

		// differential reload should return three
		assertNotNull(shouldHaveOne);
		assertEquals("should have one string", 1, shouldHaveOne.size());
	}
	// ======================================================================

}
