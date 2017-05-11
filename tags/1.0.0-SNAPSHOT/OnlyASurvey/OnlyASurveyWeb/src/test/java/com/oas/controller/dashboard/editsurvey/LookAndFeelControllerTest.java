package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Survey;
import com.oas.model.SurveyTemplateOption;

public class LookAndFeelControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private LookAndFeelController controller;

	// ======================================================================

	@Test
	public void testShowTabView() {

		// authorized
		ModelAndView mav = controller.showTabView(new MockHttpServletRequest("GET", "/prefix/"
				+ scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true).getId() + ".html"));
		assertNotNull(mav);
		assertEquals("unexpected view name", "/dashboard/manage/lookAndFeel/lookAndFeelTab", mav.getViewName());
	}

	@Test
	public void testShowTabView_Security_Fail_NoUser() {

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
	public void testShowTabView_Security_Fail_WrongUser() {

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
	public void testCancel_Success() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.cancel(request);
		assertIsRedirect("expected redirect", mav, "/html/db/mgt/" + survey.getId() + ".html");
	}

	// ======================================================================

	@Test
	public void testImportTemplateForm_Success() {
		Survey survey = new Survey(createAndSetSecureUserWithRoleUser());
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
		ModelAndView mav = controller.importTemplateForm(request);
		assertHasViewName(mav, "/dashboard/manage/lookAndFeel/MTI/importTemplateForm");
	}

	@Test
	public void testImportTemplateForm_Security_FailNotOwner() {
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
		persist(survey);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + survey.getId() + ".html");
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

}
