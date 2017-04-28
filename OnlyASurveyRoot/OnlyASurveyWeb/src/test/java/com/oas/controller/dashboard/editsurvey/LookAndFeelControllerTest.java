package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.model.Survey;
import com.oas.model.SurveyLanguage;
import com.oas.model.SurveyTemplateOption;
import com.oas.model.templating.Template;
import com.oas.model.templating.TemplateType;

public class LookAndFeelControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private LookAndFeelController controller;

	// ======================================================================

	@Test
	public void showTabView() {

		// authorized
		ModelAndView mav = controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
				+ scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true).getId() + ".html"));
		assertNotNull(mav);
		assertEquals("unexpected view name", "/dashboard/manage/lookAndFeel/lookAndFeelTab", mav.getViewName());
	}

	@Test
	public void showTabView_Security_Fail_NoUser() {

		try {
			// no current user
			controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
					+ scenarioDataUtil.createTypicalScenario1(createTestUser(), true).getId() + ".html"));
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void showTabView_Security_Fail_WrongUser() {

		try {
			// owner is not the same as the current user
			createAndSetSecureUserWithRoleUser();
			controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
					+ scenarioDataUtil.createTypicalScenario1(createTestUser(), true).getId() + ".html"));
			fail("expected security exception");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void cancel_Success() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.cancel(request);
		assertIsRedirect("expected redirect", mav, "/html/db/mgt/" + survey.getId() + ".html");
	}

	// ======================================================================

	@Test
	public void importTemplateForm_Success() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		// test with no pre-existing data
		{
			ModelAndView mav = controller.importTemplateForm(request);
			assertHasViewName(mav, "/dashboard/manage/lookAndFeel/MTI/importTemplateForm");
		}

		// if we re-invoke when data exists, the command should have the URLs
		{
			Template templateEng = new Template(survey, getEnglish(), TemplateType.OAS_COMMENTS);
			templateEng.setImportedFromUrl("http://eng.com");

			Template templateFra = new Template(survey, getFrench(), TemplateType.OAS_COMMENTS);
			templateFra.setImportedFromUrl("http://fra.com");

			survey.getTemplates().put(getEnglish(), templateEng);
			survey.getTemplates().put(getFrench(), templateFra);

			persist(survey);
			flushAndClear();

			ModelAndView mav = controller.importTemplateForm(request);
			assertHasViewName(mav, "/dashboard/manage/lookAndFeel/MTI/importTemplateForm");
			assertHasCommand(mav);

			NameObjectCommand command = (NameObjectCommand) mav.getModel().get("command");
			assertNotNull(command);

			String eng = command.getMap().get("eng");
			String fra = command.getMap().get("fra");

			assertEquals(templateEng.getImportedFromUrl(), eng);
			assertEquals(templateFra.getImportedFromUrl(), fra);
		}
	}

	@Test
	public void importTemplateForm_Security_FailNotOwner() {
		// the current user
		createAndSetSecureUserWithRoleUser();

		// some other user
		Survey survey = new Survey(createTestUser());
		persist(survey);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		// expect security check to fail
		assertFailsSecurityCheck(new GenericControllerCallback() {
			@Override
			public ModelAndView doCallback() {
				return controller.importTemplateForm(request);
			}
		});
	}

	// ======================================================================

	@Test
	public void uploadLogosForm_Success() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.getSurveyLanguages().add(new SurveyLanguage(survey, getFrench()));
		persist(survey);

		// note URL
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/r/fra/" + survey.getId() + ".html");
		ModelAndView mav = controller.uploadLogosForm(request);

		assertHasViewName(mav, "/dashboard/manage/lookAndFeel/uploadLogos/uploadLogosForm");
	}

	@Test
	public void uploadLogosSubmit_Success_Cancel() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		survey.setTemplateOption(SurveyTemplateOption.DEFAULT);
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		request.setParameter("_lecnac", "");
		ModelAndView mav = controller.uploadLogosSubmit(request, null, null);
		flushAndClear();

		// must always redirect out
		assertIsRedirect(mav);

		// should not have changed on submit
		Survey loaded = get(Survey.class, survey.getId());
		assertEquals("should not have changed", SurveyTemplateOption.DEFAULT, loaded.getTemplateOption());
	}

	@Test
	public void uploadLogosForm_Security_FailNotOwner() {
		// the current user
		createAndSetSecureUserWithRoleUser();

		// some other user
		Survey survey = new Survey(createTestUser());
		persist(survey);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		// expect security check to fail
		assertFailsSecurityCheck(new GenericControllerCallback() {
			@Override
			public ModelAndView doCallback() {
				return controller.uploadLogosForm(request);
			}
		});
	}

	@Test
	public void uploadLogosSubmit_Security_FailNotOwner() {
		// the current user
		createAndSetSecureUserWithRoleUser();

		// some other user
		Survey survey = new Survey(createTestUser());
		persist(survey);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		// expect security check to fail
		assertFailsSecurityCheck(new GenericControllerCallback() {
			@Override
			public ModelAndView doCallback() {
				return controller.uploadLogosSubmit(request, null, null);
			}
		});
	}

	// ======================================================================

	@Test
	public void changeTemplateType_Success_TypeDefault() {

		Long surveyId = null;
		{
			Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
			// set to non-default value
			survey.setTemplateOption(SurveyTemplateOption.UPLOADED_LOGOS);
			persist(survey);
			flushAndClear();

			assertFalse("test data defect", survey.getTemplateOption().equals(SurveyTemplateOption.DEFAULT));

			surveyId = survey.getId();
		}
		assertNotNull(surveyId);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + surveyId + ".html");

		// set new value to default
		IdListCommand command = new IdListCommand(new Long[] { (long) LookAndFeelController.TYPE_DEFAULT });
		ModelAndView mav = controller.changeTemplateType(request, command);
		assertIsRedirect(mav);
		flushAndClear();

		// reload and test
		{
			Survey loaded = load(Survey.class, surveyId);
			assertNotNull(loaded);

			assertEquals("expected new template option", SurveyTemplateOption.DEFAULT, loaded.getTemplateOption());
		}
	}

	@Test
	public void changeTemplateType_Success_TypeUploadLogos() {

		Long surveyId = null;
		{
			Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
			// set to non-default value
			survey.setTemplateOption(SurveyTemplateOption.MAGIC);
			persist(survey);
			flushAndClear();

			assertFalse("test data defect", survey.getTemplateOption().equals(SurveyTemplateOption.UPLOADED_LOGOS));

			surveyId = survey.getId();
		}
		assertNotNull(surveyId);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + surveyId + ".html");

		// set new value to default
		IdListCommand command = new IdListCommand(new Long[] { (long) LookAndFeelController.TYPE_UPLOAD_LOGOS });
		ModelAndView mav = controller.changeTemplateType(request, command);
		assertIsRedirect(mav);
		flushAndClear();

		{
			Survey loaded = load(Survey.class, surveyId);
			assertNotNull(loaded);

			assertEquals("expected new template option", SurveyTemplateOption.UPLOADED_LOGOS, loaded.getTemplateOption());
		}
	}

	@Test
	public void changeTemplateType_Success_TypeMagic() {

		Long surveyId = null;
		{
			Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
			// set to non-default value
			survey.setTemplateOption(SurveyTemplateOption.UPLOADED_LOGOS);
			persist(survey);
			flushAndClear();

			assertFalse("test data defect", survey.getTemplateOption().equals(SurveyTemplateOption.MAGIC));

			surveyId = survey.getId();
		}
		assertNotNull(surveyId);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + surveyId + ".html");

		// set new value to default
		IdListCommand command = new IdListCommand(new Long[] { (long) LookAndFeelController.TYPE_MAGIC });
		ModelAndView mav = controller.changeTemplateType(request, command);
		assertIsRedirect(mav);
		flushAndClear();

		// this type does NOT save the survey - it redirects to a page that
		// allows the user to continue the flow, and only after that is the
		// survey changed - assert that no change has yet happened
		{
			Survey loaded = load(Survey.class, surveyId);
			assertNotNull(loaded);

			assertEquals("expected new template option", SurveyTemplateOption.UPLOADED_LOGOS, loaded.getTemplateOption());
		}
	}

	@Test
	public void changeTemplateType_Security_FailNotOwner() {
		// the current user
		createAndSetSecureUserWithRoleUser();

		// some other user
		Survey survey = new Survey(createTestUser());
		persist(survey);

		final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");

		// expect security check to fail
		assertFailsSecurityCheck(new GenericControllerCallback() {
			@Override
			public ModelAndView doCallback() {
				IdListCommand command = new IdListCommand(new Long[] { (long) LookAndFeelController.TYPE_DEFAULT });
				return controller.changeTemplateType(request, command);
			}
		});
	}

	// ======================================================================

}
