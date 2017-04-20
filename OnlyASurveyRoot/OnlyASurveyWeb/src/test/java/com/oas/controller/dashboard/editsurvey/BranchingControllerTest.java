package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.AccessDeniedException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.controller.dashboard.editsurvey.BranchingController.WHEN;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.rules.BranchingRule;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.ExitRule;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;
import com.oas.service.BranchingService;

/**
 * Branching controller test.
 * 
 * @author xhalliday
 * @since March 10, 2009
 */
public class BranchingControllerTest extends AbstractOASBaseTest {

	/** Controller under test. */
	@Autowired
	private BranchingController controller;

	@Autowired
	private BranchingService branchingService;

	// ======================================================================

	/**
	 * These tests make certain assumptions about the shape of test data. Verify
	 * them here.
	 */
	@Test
	public void confirmTestScenarioAssumptions() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		assertEquals(1, branchingService.findEntryRules(survey.getQuestions().get(0)).size());
		assertEquals(2, branchingService.findEntryRules(survey.getQuestions().get(1)).size());

		assertEquals(1, branchingService.findExitRules(survey.getQuestions().get(4)).size());
	}

	// ======================================================================

	@Test
	public void getRule_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		flushAndClear();

		List<EntryRule> entryRules = branchingService.findEntryRules(survey.getQuestions().get(0));
		List<ExitRule> exitRules = branchingService.findExitRules(survey.getQuestions().get(4));
		assertEquals("expected 1 entry rule", 1, entryRules.size());
		assertEquals("expected 1 exit rule", 1, exitRules.size());

		BranchingRule entry = controller.getRule(entryRules.get(0).getId(), null);
		BranchingRule exit = controller.getRule(null, exitRules.get(0).getId());

		assertNotNull(entry);
		assertNotNull(exit);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRule_IllegalArgument() {
		controller.getRule(null, null);
	}

	// ======================================================================

	@Test
	public void showRules() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		ModelAndView mav = controller.showRules(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
		assertNotNull(mav);
		assertModelHasSurvey(mav, survey);
		assertModelHasAttribute(mav, "entryRules");
		assertModelHasAttribute(mav, "exitRules");
	}

	// ======================================================================

	@Test
	public void addEntryRuleForm_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		ModelAndView mav = controller.addEntryRule(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
		assertNotNull(mav);
		assertModelHasSurvey(mav, survey);
	}

	@Test(expected = AccessDeniedException.class)
	public void addEntryRuleSubmit_Security_FailWrongUser() {

		// some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		// authenticate a different user
		createAndSetSecureUserWithRoleUser();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		controller.addEntryRuleSubmit(request, 0, 0, null, null);
	}

	@Test
	public void addEntryRuleSubmit_Success_Cancel() {

		// some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		int initialCount = countRowsInTable("oas.exit_rule");

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		request.setParameter("_lecnac", "");
		ModelAndView mav = controller.addEntryRuleSubmit(request, 0, 0, null, null);
		assertIsRedirect(mav);
		flushAndClear();

		int newCount = countRowsInTable("oas.exit_rule");
		assertEquals("expected no new rules", initialCount, newCount);
	}

	/**
	 * The method checks to see if any of the numeric parameters are zero,
	 * indicating a missing/default value, which is an error. Does not apply to
	 * "wnChOp".
	 */
	@Test
	public void addEntryRuleSubmit_Fail_AnyOptionIsZero() {
		doAddEntryRuleSubmit_Fail_AnyOptionIsZero(0, 1);
		doAddEntryRuleSubmit_Fail_AnyOptionIsZero(1, 0);
	}

	private void doAddEntryRuleSubmit_Fail_AnyOptionIsZero(int wn, int wt) {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		int initialCount = countRowsInTable("oas.exit_rule");

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		ModelAndView mav = controller.addEntryRuleSubmit(request, wn, wt, 0L, 0L);
		flushAndClear();

		assertHasError(getErrors(mav), "branching.addEntryRule.error");

		int newCount = countRowsInTable("oas.exit_rule");
		assertEquals("expected no new rules", initialCount, newCount);
	}

	@Test
	public void addEntryRuleSubmit_Fail_InvalidEnum() {
		doAddEntryRuleSubmit_Fail_InvalidEnum(1, 999);
		doAddEntryRuleSubmit_Fail_InvalidEnum(999, 1);
	}

	private void doAddEntryRuleSubmit_Fail_InvalidEnum(int wn, int wt) {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		try {
			MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
			controller.addEntryRuleSubmit(request, wn, wt, 0L, 0L);

			fail("expected exception");

		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void addEntryRuleSubmit_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		flushAndClear();

		int count = 0;
		for (BranchingController.WHEN when : BranchingController.WHEN.values()) {
			for (BranchingController.WHAT what : BranchingController.WHAT.values()) {

				long wnOp = 0;
				long wnChOp = 0;

				if (WHEN.OTHER_ANSWER.equals(when) || WHEN.OTHER_EMPTY.equals(when)) {
					// need to set an "other question"
					// just take the last question
					wnOp = getLastQuestion(question.getSurvey()).getId();
				}

				if (WHEN.CHOICE_ON.equals(when) || WHEN.CHOICE_OFF.equals(when)) {
					// need to set an "other choice"
					wnChOp = question.getChoices().get(0).getId();
				}

				final String permutation = "count=" + count + ", when=" + when + ", what=" + what;

				// save case
				{
					MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");

					int initialCount = countRowsInTable("oas.entry_rule");
					ModelAndView mav = controller.addEntryRuleSubmit(request, when.value, what.value, wnOp, wnChOp);
					assertNotNull("no MAV: " + permutation, mav);
					assertNoErrors(mav);
					assertIsRedirect("should be a redirect: " + permutation, mav);
					//
					flushAndClear();

					int newCount = countRowsInTable("oas.entry_rule");
					assertEquals("should have added 1 rule to db: " + permutation, initialCount + 1, newCount);
				}

				// cancel case
				{
					int initialCount = countRowsInTable("oas.entry_rule");
					MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId()
							+ ".html?_lecnac");
					// signal a cancel
					request.setParameter("_lecnac", "");

					ModelAndView mav = controller.addEntryRuleSubmit(request, when.value, what.value, wnOp, wnChOp);
					assertNotNull("no MAV: " + permutation, mav);
					assertNoErrors(mav);
					assertIsRedirect("should be a redirect: " + permutation, mav);
					//
					flushAndClear();

					int newCount = countRowsInTable("oas.entry_rule");
					assertEquals("should have added NO rules to db: " + permutation, initialCount, newCount);
				}

				//
				count++;
			}
		}
		log.info("tested " + count + " permutations for BranchingController.addEntryRuleSubmit");
		assertTrue("expected many permutations but actually tested zero or to few: " + count, count >= 10);
	}

	// ======================================================================

	@Test
	public void addExitRuleForm_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		ModelAndView mav = controller.addExitRule(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
		assertNotNull(mav);
		assertModelHasSurvey(mav, survey);
	}

	@Test
	public void addExitRuleSubmit_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		flushAndClear();

		int count = 0;
		for (ExitRuleType type : ExitRuleType.values()) {
			for (ExitRuleAction action : ExitRuleAction.values()) {

				Long jumpToQuestionId = null;
				Long choiceId = null;

				if (ExitRuleAction.JUMP_TO_QUESTION.equals(action)) {
					// need to set a Jump To
					// just take the last question
					jumpToQuestionId = getLastQuestion(question.getSurvey()).getId();
				}

				if (ExitRuleType.CHOICE_OFF.equals(type) || ExitRuleType.CHOICE_ON.equals(type)) {
					// need to set a Choice Id
				}

				final String permutation = "count=" + count + ", type=" + type + ", action=" + action;

				// save case
				{
					int initialCount = countRowsInTable("oas.exit_rule");
					ModelAndView mav = controller.addExitRuleSubmit(new MockHttpServletRequest("GET", "/prefix/"
							+ question.getId() + ".html"), type.toString(), action.toString(), jumpToQuestionId, choiceId);
					assertNotNull("no MAV: " + permutation, mav);
					assertIsRedirect(mav);
					//
					flushAndClear();

					int newCount = countRowsInTable("oas.exit_rule");
					assertEquals("should have added 1 rule to db", initialCount + 1, newCount);
				}

				// cancel case
				{
					int initialCount = countRowsInTable("oas.exit_rule");
					MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId()
							+ ".html?_lecnac");
					// signal a cancel
					request.setParameter("_lecnac", "");

					ModelAndView mav = controller.addExitRuleSubmit(request, type.toString(), action.toString(),
							jumpToQuestionId, choiceId);
					assertNotNull("no MAV: " + permutation, mav);
					assertIsRedirect(mav);
					//
					flushAndClear();

					int newCount = countRowsInTable("oas.exit_rule");
					assertEquals("should have added NO rules to db", initialCount, newCount);
				}

				//
				count++;
			}
		}
		log.info("tested " + count + " permutations for BranchingController.addExitRuleSubmit");
		assertTrue("expected many permutations but actually tested zero or to few: " + count, count >= 10);
	}

	@Test(expected = AccessDeniedException.class)
	public void addExitRuleSubmit_Security_FailWrongUser() {
		// some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Question question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		flushAndClear();

		// authenticate a user that is not the owner above
		createAndSetSecureUserWithRoleUser();

		controller.addExitRuleSubmit(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"),
				ExitRuleType.CHOICE_OFF.toString(), ExitRuleAction.FORCE_FINISH.toString(), null, null);
	}

	@Test
	public void addExitRuleSubmit_Fail_NoType() {
		// some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		flushAndClear();

		ModelAndView mav = controller.addExitRuleSubmit(
				new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"), "", ExitRuleAction.FORCE_FINISH
						.toString(), null, null);
		Errors errors = getErrors(mav);
		assertHasError(errors, "branching.addExitRule.error");
	}

	@Test
	public void addExitRuleSubmit_Fail_NoAction() {
		// some other user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("unable to get choice question", question);
		flushAndClear();

		ModelAndView mav = controller.addExitRuleSubmit(
				new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"), ExitRuleType.CHOICE_OFF.toString(),
				"", null, null);
		Errors errors = getErrors(mav);
		assertHasError(errors, "branching.addExitRule.error");
	}

	// ======================================================================

	@Test
	public void deleteRule_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		List<EntryRule> list = branchingService.findEntryRules(question);
		assertNotNull(list);
		assertTrue("expected 1 entry rule in test data", list.size() == 1);
		EntryRule rule = list.get(0);

		flushAndClear();

		ModelAndView mav = controller.deleteRule(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"), rule
				.getId(), null);
		assertNotNull(mav);
		assertNotRedirect(mav);
		assertModelHasSurvey(mav, survey);
		EntryRule modelRule = (EntryRule) mav.getModel().get("subject");
		assertNotNull("rule missing from model", modelRule);
		assertEquals("wrong rule", rule.getId(), modelRule.getId());
		flushAndClear();

		List<EntryRule> newList = branchingService.findEntryRules(question);
		assertNotNull(newList);
		assertTrue("expected empty rule list", newList.size() == 1);
	}

	@Test
	public void deleteRuleSubmit_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		List<EntryRule> list = branchingService.findEntryRules(question);
		assertNotNull(list);
		assertTrue("expected 1 entry rule in test data", list.size() == 1);
		EntryRule rule = list.get(0);

		flushAndClear();

		ModelAndView mav = controller.deleteRuleSubmit(
				new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"), rule.getId(), null);
		assertNotNull(mav);
		assertIsRedirect(mav);
		flushAndClear();

		List<EntryRule> newList = branchingService.findEntryRules(question);
		assertNotNull(newList);
		assertTrue("expected empty rule list", newList.size() == 0);
	}

	@Test
	public void deleteRuleSubmit_Success_Cancel() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		List<EntryRule> list = branchingService.findEntryRules(question);
		assertNotNull(list);
		assertTrue("expected 1 entry rule in test data", list.size() == 1);
		EntryRule rule = list.get(0);

		flushAndClear();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html");
		request.setParameter("_lecnac", "");
		ModelAndView mav = controller.deleteRuleSubmit(request, rule.getId(), null);
		assertNotNull(mav);
		assertIsRedirect(mav);
		flushAndClear();

		List<EntryRule> newList = branchingService.findEntryRules(question);
		assertNotNull(newList);
		assertTrue("expected empty rule to not be deleted", newList.size() == 1);
	}

	@Test(expected = AccessDeniedException.class)
	public void deleteRuleSubmit_Security_FailNotOwner() {
		// current user
		createAndSetSecureUserWithRoleUser();

		// owner is another user
		Survey survey = scenarioDataUtil.createTypicalScenario1(createTestUser(), true);
		Question question = survey.getQuestions().get(0);
		List<EntryRule> list = branchingService.findEntryRules(question);
		assertNotNull(list);
		assertTrue("expected 1 entry rule in test data", list.size() == 1);
		EntryRule rule = list.get(0);

		flushAndClear();

		controller.deleteRuleSubmit(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"), rule.getId(),
				null);
	}
}
