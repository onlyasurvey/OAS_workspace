package com.oas.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import ca.inforealm.core.service.ConfigurationService;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.ObjectTextCommand;
import com.oas.model.Attachment;
import com.oas.model.AttachmentPayload;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.Survey;
import com.oas.model.Attachment.AttachmentType;
import com.oas.util.Constants;

/**
 * s for DomainModelService.
 * 
 * @author xhalliday
 */
public class DomainModelServiceTest extends AbstractOASBaseTest {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	private DomainModelService service;

	@Test
	public void whatIs_Success_ByInstance() {

		assertEquals(Survey.class, service.whatIs(new Survey()));
		assertEquals(Question.class, service.whatIs(new Question()));
		assertEquals(Choice.class, service.whatIs(new Choice()));
	}

	@Test
	public void whatIs_Success_ById() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		assertEquals(Survey.class, service.whatIs(survey.getId()));
	}

	@Test
	public void isSurvey() {
		assertTrue(service.isSurvey(new Survey()));
		assertFalse(service.isSurvey(new Question()));
	}

	@Test
	public void isQuestion() {
		assertTrue(service.isQuestion(new Question()));
		assertFalse(service.isQuestion(new Survey()));
	}

	// ======================================================================

	// ======================================================================

	@Test
	public void determineAbsolutePathForContent_Success() {

		setEnglish();
		final String prefix = "/some/prefix";
		final String url = "/blog/20090120-1.html";

		String value = service.determineAbsolutePathForContent(prefix, url);

		// normalized here to force unix path separators
		assertEquals("unexpected result", prefix + url.substring(0, url.length() - 5) + ".eng.html", FilenameUtils
				.separatorsToUnix(value));

	}

	@Test
	public void determineAbsolutePathForContent_Success_ImageNoLanguage() {

		setEnglish();
		final String prefix = "/some/prefix";
		final String url = "/images/icons/add-favorite.png";

		String value = service.determineAbsolutePathForContent(prefix, url);

		// normalized here to force unix path separators
		assertEquals("unexpected result", prefix + url, FilenameUtils.separatorsToUnix(value));

	}

	@Test
	public void determineAbsolutePathForContent_Fail_TooManyParentDotDots() {

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
	@DirtiesContext
	public void getPublicHostname_Success_WithConfigItem() {

		final String expected = "www.testme.com";
		configurationService.setConfigurationItem(Constants.HOSTNAME_CONFIGURATION_KEY, expected);

		assertEquals("expected configured value", expected, service.getPublicHostname());
	}

	@Test
	@DirtiesContext
	public void getPublicHostname_Success_NoConfigItem_Fallback() {

		// fallback value goes to our public site
		final String expected = "www.onlyasurvey.com";
		configurationService.setConfigurationItem(Constants.HOSTNAME_CONFIGURATION_KEY, "");

		assertEquals("expected configured value", expected, service.getPublicHostname());
	}

	// ======================================================================

	@Test
	@DirtiesContext
	public void getShortUrlPrefix_Success_WithConfigItem() {

		final String expected = "http://www.testme.com/";
		configurationService.setConfigurationItem(Constants.SHORT_URL_PREFIX_CONFIGURATION_KEY, expected);

		assertEquals("expected configured value", expected, service.getShortUrlPrefix());
	}

	@Test
	@DirtiesContext
	public void getShortUrlPrefix_Success_NoConfigItem() {

		configurationService.setConfigurationItem(Constants.SHORT_URL_PREFIX_CONFIGURATION_KEY, "");

		assertNull("expected null value", service.getShortUrlPrefix());
	}

	// ======================================================================

	@Test
	public void deleteObject_Success() {
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
	// @Test public void deleteObject_Security_FailWrongUser() {
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
	public void findObjectText_SingleKey_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		// scenario data should always have some object text.
		ObjectTextCommand command = service.findObjectText(survey, "welcomeMessage");
		assertNotNull("expected command (none in scenario data)", command);
		assertEquals("unexpected key", "welcomeMessage", command.getKey());
		assertNotNull("expected map", command.getMap());
		assertNotNull("expected some english text", command.getMap().get("eng"));
	}

	@Test
	public void findObjectText_MultipleKeys_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUser(), true);
		// scenario data should always have some object text.
		Collection<ObjectTextCommand> list = service.findObjectText(survey, new String[] { "welcomeMessage", "thanksMessage" });
		assertNotNull("expected list (none in scenario data)", list);
		assertEquals("unexpected # entries", 2, list.size());
	}

	@Test
	public void setObjectText_Success() {

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
	public void getContentFromURL_Success() {
		String url = "http://www.google.com/";
		String content = service.getContentFromURL(url);

		assertTrue("invalid content", content.contains("<title>"));
		assertTrue("invalid content", content.contains("google"));
	}

	// ======================================================================

	@Test
	public void attachUpload_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		Attachment attachment = new Attachment(survey, AttachmentType.IMAGE, "image/png");
		attachment.addPayload(supportedLanguageService.findByCode("eng"), new AttachmentPayload(10240, "Alt Text", new Date(),
				"SSBhbSBhIGhhcHB5IHBlbmd1aW4h"));
		attachment.addPayload(supportedLanguageService.findByCode("fra"), new AttachmentPayload(10240, "Alt Text in French",
				new Date(), "SSBhbSBhIGhhcHB5IHBlbmd1aW4h"));

		{
			Attachment saved = service.attachUpload(survey, attachment);
			assertNotNull(saved);
			flushAndClear();
		}

		List<Attachment> list = service.findAttachments(survey, AttachmentType.IMAGE);
		assertNotNull(list);
		assertEquals("expected 1 attachment", 1, list.size());

		Attachment saved = unique(list);
		int newSize = saved.getPayloads().size();

		String expected = "I am a happy penguin!";
		assertEquals("unexpected # payloads", 2, newSize);
		AttachmentPayload payload = saved.getPayloads().get(supportedLanguageService.findByCode("eng"));
		String actual = new String(Base64.decodeBase64(payload.getPayload().getBytes()));

		assertEquals("unexpected decoded value", expected, actual);
	}
}
