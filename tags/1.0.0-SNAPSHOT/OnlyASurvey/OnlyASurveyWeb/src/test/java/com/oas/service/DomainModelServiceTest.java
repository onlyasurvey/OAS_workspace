package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.ObjectTextCommand;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;

/**
 * Tests for DomainModelService.
 * 
 * @author xhalliday
 */
public class DomainModelServiceTest extends AbstractOASBaseTest {

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	private DomainModelService service;

	@Test
	public void testWhatIs_Success_ByInstance() {

		assertEquals(Survey.class, service.whatIs(new Survey()));
		assertEquals(Question.class, service.whatIs(new Question()));
		assertEquals(Choice.class, service.whatIs(new Choice()));
	}

	@Test
	public void testWhatIs_Success_ById() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		assertEquals(Survey.class, service.whatIs(survey.getId()));
	}

	@Test
	public void testIsSurvey() {
		assertTrue(service.isSurvey(new Survey()));
		assertFalse(service.isSurvey(new Question()));
	}

	@Test
	public void testIsQuestion() {
		assertTrue(service.isQuestion(new Question()));
		assertFalse(service.isQuestion(new Survey()));
	}

	// ======================================================================

	@Test
	public void testDetermineAbsolutePathForContent_Success() {

		setEnglish();
		final String prefix = "/some/prefix";
		final String url = "/blog/20090120-1.html";

		String value = service.determineAbsolutePathForContent(prefix, url);

		// normalized here to force unix path separators
		assertEquals("unexpected result", prefix + url.substring(0, url.length() - 5) + ".eng.html", FilenameUtils
				.separatorsToUnix(value));

	}

	@Test
	public void testDetermineAbsolutePathForContent_Success_ImageNoLanguage() {

		setEnglish();
		final String prefix = "/some/prefix";
		final String url = "/images/icons/add-favorite.png";

		String value = service.determineAbsolutePathForContent(prefix, url);

		// normalized here to force unix path separators
		assertEquals("unexpected result", prefix + url, FilenameUtils.separatorsToUnix(value));

	}

	@Test
	public void testDetermineAbsolutePathForContent_Fail_TooManyParentDotDots() {

		setEnglish();
		final String prefix = "/some/prefix";
		final String actualPageName = "/blog-20090120-01.html";
		final String url = "foo/../bar/../../.." + actualPageName;

		String value = service.determineAbsolutePathForContent(prefix, url);

		String expected = FilenameUtils.separatorsToSystem(prefix + "/null");
		assertEquals("unexpected result", expected, value);
	}

	// ======================================================================

	@Test
	public void testDeleteObject_Success() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		persist(survey);

		assertNotNull("unable to load object", unique(find("from Survey where deleted=false and id=" + survey.getId())));
		service.deleteObject(survey);
		flushAndClear();

		assertNull("should be unable to load object when filtering by deleted",
				unique(find("from Survey where deleted=false and id=" + survey.getId())));
		assertNotNull("should be to load object when not filtering by deleted",
				unique(find("from Survey where deleted=true and id=" + survey.getId())));
	}

	// TODO: service method validates only that a user is authenticated
	// 
	// @Test public void testDeleteObject_Security_FailWrongUser() {
	// // the user
	// createAndSetSecureUserWithRoleUser();
	//
	// // some other user's data
	// Survey survey = new Survey(createTestUser());
	// persist(survey);
	//
	// try {
	// service.deleteObject(survey);
	// fail("expected AccessDeniedException");
	// } catch (AccessDeniedException e) {
	// // expected
	// }
	// }

	// ======================================================================

	@Test
	public void testFindObjectText_SingleKey_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		// scenario data should always have some object text.
		ObjectTextCommand command = service.findObjectText(survey, "welcomeMessage");
		assertNotNull("expected command (none in scenario data)", command);
		assertEquals("unexpected key", "welcomeMessage", command.getKey());
		assertNotNull("expected map", command.getMap());
		assertNotNull("expected some english text", command.getMap().get("eng"));
	}

	@Test
	public void testFindObjectText_MultipleKeys_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		// scenario data should always have some object text.
		Collection<ObjectTextCommand> list = service.findObjectText(survey, new String[] { "welcomeMessage", "thanksMessage" });
		assertNotNull("expected list (none in scenario data)", list);
		assertEquals("unexpected # entries", 2, list.size());
	}

	@Test
	public void testSetObjectText_Success() {

		String key = "someRandomKey" + getMBUN();

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		assertNull("expected no existing data", service.findObjectText(survey, key));

		ObjectTextCommand newCommand = new ObjectTextCommand(key, createNameMap());
		newCommand.getMap().put("eng", NAME_EN);
		newCommand.getMap().put("fra", NAME_FR);

		service.setObjectText(survey, newCommand);

		ObjectTextCommand command = service.findObjectText(survey, key);
		assertNotNull("expected command to be returned", command);
		assertNotNull("expected map", command.getMap());
		assertNotNull("expected some english text", command.getMap().get("eng"));

	}

	// ======================================================================

	@Test
	public void testGetContentFromURL_Success() {
		String url = "http://www.google.com/";
		String content = service.getContentFromURL(url);

		assertTrue("invalid content", content.contains("<title>"));
		assertTrue("invalid content", content.contains("google"));
	}

	// ======================================================================

	@Test
	public void testGetPublicContent_Fail_NoSuchFile() {
		try {
			service.getPublicContent("abt");
		} catch (RuntimeException e) {
			// expected
		}
	}

}
