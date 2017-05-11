package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.ChoiceCommand;
import com.oas.command.model.CreateQuestionCommand;
import com.oas.command.model.NameObjectCommand;
import com.oas.model.Choice;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.question.ChoiceQuestion;

public class EditChoicesControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private EditChoicesController controller;

	@Test
	public void testListChoices() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		ModelAndView mav = controller.listChoices(request);
		assertNotNull("expected mav", mav);
		assertTrue("unexpected view", mav.getViewName().endsWith("listChoices"));
		assertNotNull("unexpected model", mav.getModel());

		ChoiceQuestion loaded = (ChoiceQuestion) mav.getModel().get("question");
		assertNotNull("unexpected model to have question", loaded);

		assertEquals("wrong question", question.getId(), loaded.getId());
	}

	@Test
	public void testListChoices_Security_FailNotOwner() {
		// authenticate a user
		createAndSetSecureUserWithRoleUser();

		// create survey for some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		try {
			controller.listChoices(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	// ======================================================================

	@Test
	public void testAddChoiceForm() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");

		ModelAndView mav = controller.addChoice(request);
		assertNotNull("expected mav", mav);
		assertTrue("unexpected view", mav.getViewName().endsWith("addChoiceForm"));
		assertNotNull("unexpected model", mav.getModel());
		assertNotNull("unexpected model to have command", mav.getModel().get("command"));
		assertNotNull("unexpected model to have question", mav.getModel().get("question"));
	}

	@Test
	public void testAddChoiceForm_Security_FailNotOwner() {

		// authenticate one user
		createAndSetSecureUserWithRoleUser();

		// create as some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");

		try {
			controller.addChoice(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testAddChoiceSubmit_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		int initialChoiceCount = question.getChoices().size();
		assertNotNull("unable to get choice question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		NameObjectCommand command = new NameObjectCommand(survey.getSupportedLanguages());
		for (String key : command.getMap().keySet()) {
			command.getMap().put(key, "testValue");
		}

		ModelAndView mav = controller.addChoiceSubmit(request, command);
		assertNotNull("expected mav", mav);
		assertIsRedirect("expected persist and redirect", mav, "/html/db/mgt/qchs/" + question.getId() + ".html");

		// confirm db changes
		flushAndClear();

		ChoiceQuestion loaded = (ChoiceQuestion) unique(find("from Question where id=?", question.getId()));
		assertNotNull(loaded);
		assertEquals("expected 1 new choice", initialChoiceCount + 1, loaded.getChoices().size());
		Choice choice = loaded.getChoices().get(loaded.getChoices().size() - 1);
		assertEquals("unexpected displayTitle", "testValue", choice.getDisplayTitle());
	}

	@Test
	public void testAddChoiceSubmit_Fail_MissingLanguages() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		NameObjectCommand command = new NameObjectCommand(survey.getSupportedLanguages());
		boolean skippedOne = false;
		for (String key : command.getMap().keySet()) {
			if (!skippedOne) {
				skippedOne = true;
				continue;
			}
			command.getMap().put(key, "testValue");
		}
		assertTrue("defect in test", skippedOne);

		ModelAndView mav = controller.addChoiceSubmit(request, command);
		assertNotRedirect(mav);
		Map<String, Object> model = mav.getModel();
		assertNotNull("no model", model);
		Errors errors = (Errors) model.get("errors");
		assertNotNull("no errors object", errors);
		assertHasError(errors, "error.nameObject.missingInLanguage");
	}

	@Test
	public void testAddChoiceSubmit_Security_FailNotOwner() {

		// authenticate one user
		createAndSetSecureUserWithRoleUser();

		// create as some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");

		try {
			controller.addChoiceSubmit(request, new NameObjectCommand());
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testDeleteChoiceForm() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		Choice choice = question.getChoices().get(0);
		assertNotNull("defect in test data: no choice", choice);
		// execute
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + choice.getId() + ".html");
		ModelAndView mav = controller.deleteChoiceForm(request);
		assertNotRedirect(mav);
		assertHasViewName(mav, "/dashboard/manage/editChoices/deleteChoiceForm");
	}

	@Test
	public void testDeleteChoiceSubmit() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		scenarioDataUtil.createResponseData(survey, 10);

		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		Choice choice = question.getChoices().get(0);
		assertNotNull("defect in test data: no choice", choice);
		assertTrue("should ALWAYS contain choice", question.getChoices().contains(choice));

		// make lots of links specifically to the choice above, to ensure
		// response data is cascaded to
		for (Response response : allResponses(survey)) {

			persist(new ChoiceAnswer(response, question, choice));
		}
		flushAndClear();

		//
		int initialChoiceCount = question.getChoices().size();

		// execute
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + choice.getId() + ".html");
		ModelAndView mav = controller.deleteChoiceSubmit(request);
		assertNotNull("expected mav", mav);
		assertIsRedirect("expected persist and redirect", mav, "/html/db/mgt/qchs/" + question.getId() + ".html");

		// confirm db changes
		flushAndClear();

		ChoiceQuestion loaded = (ChoiceQuestion) unique(find("from Question where id=?", question.getId()));
		assertNotNull(loaded);
		assertEquals("expected 1 fewer choices", initialChoiceCount - 1, loaded.getChoices().size());
		assertFalse("should NOT contain choice", loaded.getChoices().contains(choice));
	}

	@Test
	public void testDeleteChoiceSubmit_Security_FailWrongUser() {

		// authenticated user
		createAndSetSecureUserWithRoleUser();

		// survey for some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		Choice choice = question.getChoices().get(0);
		assertNotNull("defect in test data: no choice", choice);

		// execute
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/prefix/" + choice.getId() + ".html");
		try {
			controller.deleteChoiceSubmit(request);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testMoveUp() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		// move #2 up
		// old new
		// 0 ... 1
		// 1 ... 0
		Choice choice1 = question.getChoices().get(1);
		Choice choice2 = question.getChoices().get(0);

		assertNotNull("defect in test data: no choice", choice1);
		assertNotNull("defect in test data: no choice", choice2);
		assertEquals("defect in test data: should be correct choice", 1, question.getChoices().indexOf(choice1));
		assertEquals("defect in test data: should be correct choice", 0, question.getChoices().indexOf(choice2));
		assertEquals("defect in test data: should be correct display order", Long.valueOf(1), choice1.getDisplayOrder());
		assertEquals("defect in test data: should be correct display order", Long.valueOf(0), choice2.getDisplayOrder());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + choice1.getId() + ".html");
		ModelAndView mav = controller.moveUp(request);
		assertNotNull("expected mav", mav);
		assertIsRedirect(mav);
		flushAndClear();

		ChoiceQuestion loaded = load(ChoiceQuestion.class, question.getId());

		assertEquals("should be correct choice", 0, loaded.getChoices().indexOf(choice1));
		assertEquals("should be correct choice", 1, loaded.getChoices().indexOf(choice2));
		assertEquals("should be correct display order", Long.valueOf(0), loaded.getChoices().get(0).getDisplayOrder());
		assertEquals("should be correct display order", Long.valueOf(1), loaded.getChoices().get(1).getDisplayOrder());
	}

	@Test
	public void testMoveDown() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);

		// move #0 down
		// old new
		// 0 ... 1
		// 1 ... 0
		// 2 ... 2
		Choice choice1 = question.getChoices().get(0);
		Choice choice2 = question.getChoices().get(1);

		assertNotNull("defect in test data: no choice", choice1);
		assertNotNull("defect in test data: no choice", choice2);
		assertEquals("defect in test data: should be correct choice", 0, question.getChoices().indexOf(choice1));
		assertEquals("defect in test data: should be correct choice", 1, question.getChoices().indexOf(choice2));
		assertEquals("defect in test data: should be correct display order", Long.valueOf(0), choice1.getDisplayOrder());
		assertEquals("defect in test data: should be correct display order", Long.valueOf(1), choice2.getDisplayOrder());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + choice1.getId() + ".html");
		ModelAndView mav = controller.moveDown(request);
		assertNotNull("expected mav", mav);
		assertIsRedirect(mav);
		flushAndClear();

		ChoiceQuestion loaded = load(ChoiceQuestion.class, question.getId());

		assertEquals("should be correct choice", 0, loaded.getChoices().indexOf(choice2));
		assertEquals("should be correct choice", 1, loaded.getChoices().indexOf(choice1));
		assertEquals("should be correct display order", Long.valueOf(0), loaded.getChoices().get(0).getDisplayOrder());
		assertEquals("should be correct display order", Long.valueOf(1), loaded.getChoices().get(1).getDisplayOrder());
	}

	@Test
	public void testCloneChoice() {
		//
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		Choice choice = question.getChoices().get(0);
		assertNotNull("defect in test data: no choice", choice);
		assertTrue("should ALWAYS contain choice", question.getChoices().contains(choice));

		//
		int initialChoiceCount = question.getChoices().size();

		// execute
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + choice.getId() + ".html");
		ModelAndView mav = controller.cloneChoice(request);
		assertNotNull("expected mav", mav);
		assertIsRedirect("expected persist and redirect", mav, "/html/db/mgt/qchs/" + question.getId() + ".html");

		// confirm db changes
		flushAndClear();

		ChoiceQuestion loaded = get(ChoiceQuestion.class, question.getId());
		// ChoiceQuestion loaded = (ChoiceQuestion)
		// unique(find("from Question where id=?", question.getId()));
		assertNotNull(loaded);
		assertEquals("expected 1 new choice", initialChoiceCount + 1, loaded.getChoices().size());
		assertTrue("should ALWAYS still contain choice", question.getChoices().contains(choice));

		Choice clone = loaded.getChoices().get(loaded.getChoices().size() - 1);
		assertEquals("expected cloned name", choice.getDisplayTitle(), clone.getDisplayTitle());
	}

	@Test
	public void testAddManyChoices() throws Exception {

		// how many to add and how many to expect
		final int addCount = 10;

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		Choice choice = question.getChoices().get(0);
		assertNotNull("defect in test data: no choice", choice);
		assertTrue("should ALWAYS contain choice", question.getChoices().contains(choice));

		//
		int initialChoiceCount = question.getChoices().size();

		// execute
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");

		// input to the controller
		CreateQuestionCommand command = new CreateQuestionCommand();
		for (int i = 0; i < addCount; i++) {
			command.getChoiceList().add(new ChoiceCommand(createNameMap()));
		}

		// .../{question.id}.html?_am
		ModelAndView mav = controller.addManyChoicesSubmit(request, question, command);

		assertNotNull("expected mav", mav);
		assertIsRedirect("expected persist and redirect", mav, "/html/db/mgt/qchs/" + question.getId() + ".html");

		// confirm db changes
		flushAndClear();

		ChoiceQuestion loaded = get(ChoiceQuestion.class, question.getId());
		assertNotNull(loaded);
		assertEquals("expected 1 new choice", initialChoiceCount + addCount, loaded.getChoices().size());
		assertTrue("should ALWAYS still contain choice", question.getChoices().contains(choice));
	}
}
