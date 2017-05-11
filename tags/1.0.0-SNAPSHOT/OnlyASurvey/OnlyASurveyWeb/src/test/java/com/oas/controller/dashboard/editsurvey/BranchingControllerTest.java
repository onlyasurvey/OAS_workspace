package com.oas.controller.dashboard.editsurvey;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Survey;
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

	@Test
	public void confirmTestScenarioAssumptions() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		assertEquals(1, branchingService.findEntryRules(survey.getQuestions().get(0)).size());
		assertEquals(2, branchingService.findEntryRules(survey.getQuestions().get(1)).size());

		assertEquals(1, branchingService.findExitRules(survey.getQuestions().get(4)).size());
	}

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

	@Test
	public void addEntryRuleForm_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		ModelAndView mav = controller.addEntryRule(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
		assertNotNull(mav);
		assertModelHasSurvey(mav, survey);
	}

	@Test
	public void addExitRuleForm_Success() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		flushAndClear();

		ModelAndView mav = controller.addExitRule(new MockHttpServletRequest("GET", "/prefix/" + question.getId() + ".html"));
		assertNotNull(mav);
		assertModelHasSurvey(mav, survey);
	}
}
