package com.oas;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.model.AbstractResourceModel;
import ca.inforealm.core.util.PasswordUtils;

import com.oas.model.AccountBillType;
import com.oas.model.AccountOwner;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.ScenarioDataUtilInterface;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.PageQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.templating.Template;
import com.oas.model.templating.TemplateType;
import com.oas.service.ResponseService;
import com.oas.service.SupportedLanguageService;
import com.oas.service.SurveyService;

@ContextConfiguration(inheritLocations = false, locations = { "classpath:/applicationContext-sane.xml",
		"file:src/main/resources/applicationContext.xml", "file:src/main/resources/applicationContext-persistence.xml",
		"file:src/main/resources/applicationContext-services.xml", "file:src/main/resources/applicationContext-security.xml",
		"file:src/main/resources/oas-servlet.xml", "file:src/test/resources/applicationContext-test.xml" })
abstract public class AbstractOASBaseTest extends AbstractBaseTest {

	@Autowired
	protected SurveyService surveyService;

	@Autowired
	protected ResponseService responseService;

	@Autowired
	protected ScenarioDataUtilInterface scenarioDataUtil;

	@Autowired
	protected SupportedLanguageService supportedLanguageService;

	/** Test URL. */
	public static final String TEST_URL = "http://test/url";

	/** Test email address. */
	public static final String TEST_EMAIL = "nobody@no.tld";

	/** Test Global Password. */
	public static final String TEST_GLOBAL_PASSWORD = "ultraSecurePassword";

	/** Local host IP. */
	public static final String LOCALHOST_IP = "127.0.0.1";

	/** Cancel parameter. */
	public static final String CANCEL = "_cancel";

	// ======================================================================

	/**
	 * Used to invoke a generic callback against a controller, returning a
	 * ModelAndView.
	 */
	public interface GenericControllerCallback {
		ModelAndView doCallback();
	}

	/**
	 * Used to invoke a generic callback against a service, returning any object
	 * or possibly null.
	 */
	public interface GenericServiceCallback {
		Object doCallback();
	}

	/**
	 * Used to invoke the same functionality repeatedly, once for each
	 * SupportedLanguage.
	 * 
	 * @since April 11, 2009
	 */
	public interface LanguageCallback {
		void doCallback(SupportedLanguage language);
	}

	// ======================================================================

	@Override
	@Before
	public void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();

		// check for OAS application
		if (getSaneContext().getApplicationModel() == null) {
			throw new RuntimeException("OAS application must be registered in the backend.");
		}
		if (getTestApplicationIdentifier().equals(getSaneContext().getApplicationModel().getIdentifier())) {
			// all good
		} else {
			throw new RuntimeException("unexpected application model loaded: "
					+ getSaneContext().getApplicationModel().getIdentifier());
		}

	}

	// ======================================================================

	protected Map<String, Object> getModel(ModelAndView mav) {

		@SuppressWarnings("unchecked")
		Map<String, Object> retval = mav.getModel();

		assertNotNull("expected a model", retval);
		return retval;
	}

	// ======================================================================

	public String getTestApplicationIdentifier() {
		return "OAS";
	}

	public boolean alwaysCreateUserRole() {
		return true;
	}

	public SurveyService getSurveyService() {
		return surveyService;
	}

	public ResponseService getResponseService() {
		return responseService;
	}

	public SupportedLanguageService getSupportedLanguageService() {
		return supportedLanguageService;
	}

	// ======================================================================

	/**
	 * Return same question objects attached to the specified {@link Survey}.
	 * 
	 * @param survey
	 *            Survey to create for
	 * @return List<Question>
	 */
	public List<Question> getSampleQuestionsForEachType(Survey survey) {
		List<Question> retval = new ArrayList<Question>();

		long displayOrder = survey.getQuestions().size();

		TextQuestion text = new TextQuestion(survey, 5, 200, 30);
		text.setDisplayOrder(displayOrder++);

		ChoiceQuestion choice = new ChoiceQuestion(survey, displayOrder++);

		ScaleQuestion scale = new ScaleQuestion(survey, displayOrder++);

		PageQuestion page = new PageQuestion(survey, displayOrder++);

		retval.add(text);
		retval.add(choice);
		retval.add(scale);
		retval.add(page);

		return retval;
	}

	// ======================================================================

	@Override
	public AccountOwner createTestUser() {
		AccountOwner retval = new AccountOwner();

		retval.setBillType(AccountBillType.DEMO);

		retval.setUsername("testUser" + getMBUN());
		retval.setEmail("testEmailAddress@host" + getMBUN() + ".nonTld");
		retval.setMd5password(PasswordUtils.encode("password"));

		retval.setFirstname("firstName");
		retval.setLastname("lastName");
		retval.setOrganization("organization");
		retval.setTelephone("telephone");
		retval.setGovernment(false);
		retval.setIpOnJoin(LOCALHOST_IP);
		retval.setLearnedAbout("some guy, you know?");
		retval.setJoinDate(new Date());
		retval.setLanguage(supportedLanguageService.findByCode("eng"));

		persist(retval);

		assertNotNull("newly persisted user has no ID", retval.getId());

		return retval;
	}

	// ======================================================================

	public Template createTestTemplate(Survey survey, boolean persist) {

		assertNotNull("no survey specified", survey);
		// assertNotNull("survey has no ID - unpersisted", survey.getId());

		Template retval = new Template(survey, supportedLanguageService.findByCode("eng"));
		retval.setBaseUrl("http://localhost");
		retval.setImportedFromUrl("http://localhost/content/foo.html");

		retval.setBeforeContent("<html>");
		retval.setAfterContent("</html>");

		retval.setTemplateType(TemplateType.CLF2_COMMENTS);

		if (persist) {
			persist(retval);
			assertNotNull("newly persisted user has no ID", retval.getId());
		}

		return retval;
	}

	// ======================================================================

	protected SupportedLanguage currentUserLanguage() {
		SupportedLanguage retval = supportedLanguageService.findByCode(LocaleContextHolder.getLocale().getISO3Language());
		assertNotNull("expected locale context to be set and match a supported language", retval);
		return retval;
	}

	/**
	 * Create a basic map as expected by the name command, consisting of generic
	 * eng and fra values.
	 */
	public Map<String, String> createNameMap() {
		Map<String, String> retval = new HashMap<String, String>();

		retval.put("eng", NAME_EN);
		retval.put("fra", NAME_FR);

		return retval;
	}

	// ======================================================================

	protected void persist(AbstractResourceModel entity) {
		getHibernateTemplate().persist(entity);
	}

	// ======================================================================

	protected <T extends Question> T getFirstQuestionOfType(Survey survey, Class<T> clazz) {
		assertNotNull(survey);
		assertNotNull(clazz);

		Question question = null;

		// TODO this is inefficient
		for (Question aQuestion : survey.getQuestions()) {
			if (clazz.isAssignableFrom(aQuestion.getClass())) {
				question = aQuestion;
				break;
			}
		}

		assertNotNull("no questions of specified type", question);

		@SuppressWarnings("unchecked")
		T retval = (T) question;
		return retval;
	}

	protected Question getSecondQuestion(Survey survey) {
		return getQuestionByOrder(survey, 1);
	}

	protected Question getFirstQuestion(Survey survey) {
		// TODO: refactor this to be more intelligent if performance suffers
		Collection<Question> list = survey.getQuestions();
		assertFalse("large dataset loaded due to programmer laziness: REFACTOR", list.size() > 20);

		Iterator<Question> iter = list.iterator();
		assertTrue(iter.hasNext());
		return iter.next();
	}

	protected Question getLastQuestion(Survey survey) {
		int size = survey.getQuestions().size();
		return survey.getQuestions().get(size - 1);
	}

	protected Question getQuestionByOrder(Survey survey, int order) {
		return survey.getQuestions().get(order);
	}

	// ======================================================================

	protected Response firstResponse(Survey survey) {
		return scenarioDataUtil.firstResponse(survey);
	}

	protected List<Response> allResponses(Survey survey) {
		return find("from Response where survey=?", survey);
	}

	// ======================================================================

	protected void validateAndExpect(Validator validator, Object command, int expectedErrors) {
		validateAndExpect(validator, command, expectedErrors, null);
	}

	protected void validateAndExpect(Validator validator, Object command, int expectedErrors, String[] errorCodes) {
		Errors errors = new BindException(command, "cmd");
		validator.validate(command, errors);
		assertEquals("did not have expected number of errors", expectedErrors, errors.getAllErrors().size());
		if (errorCodes != null) {
			for (String code : errorCodes) {
				assertHasError(errors, code);
			}
		}
	}

	// ======================================================================

	protected Errors getErrors(ModelAndView mav) {
		return (Errors) mav.getModel().get("errors");
	}

	// ======================================================================

	protected SupportedLanguage getEnglish() {
		return supportedLanguageService.findByCode("eng");
	}

	protected SupportedLanguage getFrench() {
		return supportedLanguageService.findByCode("fra");
	}

	// ======================================================================

	/**
	 * Determine how many BaseObject records exist.
	 */
	protected long countBaseObjects() {
		return (Long) getHibernateTemplate().find("select count(a) from BaseObject a").iterator().next();
	}

	// ======================================================================

	/**
	 * Assert that the string you specific is the exact view name.
	 * 
	 * @param mav
	 * @param partialViewName
	 */
	protected void assertHasViewName(ModelAndView mav, String viewName) {
		assertNotNull("expected a ModelAndView", mav);
		assertNotNull("expected mav to have view name but was null", mav.getViewName());
		assertEquals("unexpected view name returned", viewName, mav.getViewName());
	}

	/**
	 * Assert that the string you specify is part of the view name.
	 * 
	 * @param mav
	 * @param partialViewName
	 */
	protected void assertHasViewNamePart(ModelAndView mav, String partialViewName) {
		assertNotNull("expected a ModelAndView", mav);
		assertNotNull("expected mav to have viewName", mav.getViewName());
		assertTrue("unexpected view name returned: [expected: " + partialViewName + ", got: " + mav.getViewName() + "]", mav
				.getViewName().contains(partialViewName));
	}

	protected void assertModelHasAttribute(ModelAndView mav, String key) {
		assertNotNull("expected a ModelAndView", mav);
		ModelMap model = mav.getModelMap();
		assertNotNull("expected a model", model);

		assertNotNull("attribute not found in model: " + key, model.get(key));
	}

	protected void assertModelHasSurvey(ModelAndView mav, Survey survey) {
		assertModelHasSurvey("", mav, survey);
	}

	protected void assertModelHasSurvey(String message, ModelAndView mav, Survey survey) {

		if (message == null) {
			message = "";
		} else {
			message = message + ": ";
		}

		assertNotNull(message + "expected a ModelAndView", mav);
		ModelMap model = mav.getModelMap();
		assertNotNull(message + "expected a model", model);

		Survey surveyFromModel = (Survey) model.get("survey");
		assertNotNull(message + "expected a survey in the model", surveyFromModel);
		assertEquals(message + "expected correct survey in the model", survey.getId(), surveyFromModel.getId());
	}

	protected void assertNotRedirect(ModelAndView mav) {
		assertNotNull("expected ModelAndView data", mav);
		assertNotNull("expected view name", mav.getViewName());
		assertNull("expected NO View", mav.getView());
		assertTrue("expected view name", StringUtils.hasText(mav.getViewName()));
	}

	protected void assertIsRedirect(ModelAndView mav) {
		assertIsRedirect(null, mav);
	}

	protected void assertIsRedirect(String message, ModelAndView mav) {
		if (message == null) {
			message = "";
		}
		assertNotNull("expected ModelAndView data " + message, mav);
		assertNotNull("expected MAV to have an implementation of the View interface " + message, mav.getView());
		assertTrue(message, RedirectView.class.isAssignableFrom(mav.getView().getClass()));
	}

	protected void assertIsRedirect(String message, ModelAndView mav, String url) {
		assertNotNull("expected mav", mav);
		assertNotNull("expected real view", mav.getView());
		assertTrue(message, RedirectView.class.isAssignableFrom(mav.getView().getClass()));
		assertEquals("wrong redirect url; " + message, url, ((RedirectView) mav.getView()).getUrl());
	}

	protected void assertSecurityContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull("no security context exists", auth);
		assertNotNull("no security principal is in context", auth.getPrincipal());
	}

	/**
	 * Execute the callback and expect an AccessDeniedException.s
	 * 
	 * @param callback
	 */
	protected void assertFailsSecurityCheck(GenericControllerCallback callback) {
		try {
			callback.doCallback();
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	/**
	 * For each supported language in the Survey execute the callback.
	 * 
	 * @param survey
	 * @param callback
	 */
	protected void foreachLanguage(Survey survey, LanguageCallback callback) {
		for (SupportedLanguage language : survey.getSupportedLanguages()) {
			callback.doCallback(language);
		}
	}

	protected void assertEmpty(Collection<? extends Object> collection) {
		assertTrue("expected empty collection", collection == null || collection.isEmpty());
	}

	protected void assertNotEmpty(Collection<? extends Object> collection) {
		assertTrue("expected non-empty collection", collection != null && !collection.isEmpty());
	}

	protected void assertHasText(String subject) {
		assertHasText("expected non-empty text to exist", subject);
	}

	protected void assertHasText(String message, String subject) {
		assertTrue(message, StringUtils.hasText(subject));
	}

	protected void assertIsNull(Object subject) {
		assertIsNull("expected null value", subject);
	}

	protected void assertIsNull(String message, Object subject) {
		assertEquals(message, (String) null, subject);
	}

	protected void assertNoErrors(ModelAndView mav) {

		Errors errors = getErrors(mav);
		if (errors == null || !errors.hasErrors()) {
			return;
		}

		// has errors
		fail("errors object hasErrors(): " + errors);
	}

	protected void assertHasError(Errors errors, String code) {

		assertNotNull("null Errors", errors);
		assertHasText("no code to check", code);

		@SuppressWarnings("unchecked")
		List<ObjectError> list = errors.getAllErrors();

		for (ObjectError error : list) {
			if (error.getCode().equals(code)) {
				// no assertion
				return;
			}
		}

		// fail
		assertTrue("did not find error code: " + code, false);
	}

	protected void assertHasCommand(ModelAndView mav) {
		Object command = mav.getModel().get("command");
		assertTrue(command != null);
	}
}
