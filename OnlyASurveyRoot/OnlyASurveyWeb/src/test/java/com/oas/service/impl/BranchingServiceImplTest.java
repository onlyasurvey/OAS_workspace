package com.oas.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.inforealm.core.persistence.DataAccessObject;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Question;
import com.oas.model.Survey;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.ExitRule;
import com.oas.service.BranchingService;

/**
 * Test for BranchingService interface.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
public class BranchingServiceImplTest extends AbstractOASBaseTest {

	@Autowired
	private BranchingService service;

	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	private BranchingServiceImpl serviceImpl;

	@Before
	public void initializeImpl() {
		serviceImpl = new BranchingServiceImpl();
		serviceImpl.setDataAccessObject(dataAccessObject);
		assertNotNull(serviceImpl);
	}

	@Test
	public void confirmTestScenarioAssumptions() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		assertEquals(1, service.findEntryRules(survey.getQuestions().get(0)).size());
		assertEquals(2, service.findEntryRules(survey.getQuestions().get(1)).size());

		assertEquals(1, service.findExitRules(survey.getQuestions().get(4)).size());

	}

	// ======================================================================

	@Test
	public void getNextRuleApplyOrder_EntryRule_NoRules() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true, true);
		// fifth question has no entry rules
		Question question = survey.getQuestions().get(4);
		assertEquals("expected no existing rule", 0, service.findEntryRules(question).size());

		int newOrder = serviceImpl.getNextRuleApplyOrder(EntryRule.class, question);
		assertEquals("unexpected new order", 0, newOrder);
	}

	@Test
	public void getNextRuleApplyOrder_ExitRule_NoRules() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true, true);

		// second question has no exit rules
		Question question = survey.getQuestions().get(1);
		assertEquals("expected no existing rule", 0, service.findExitRules(question).size());

		int newOrder = serviceImpl.getNextRuleApplyOrder(ExitRule.class, question);
		assertEquals("unexpected new order", 0, newOrder);
	}

	@Test
	public void getNextRuleApplyOrder_EntryRule_HasRules() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true, true);
		Question question = survey.getQuestions().get(0);
		assertEquals("expected 1 existing rule", 1, service.findEntryRules(question).size());

		int newOrder = serviceImpl.getNextRuleApplyOrder(EntryRule.class, question);
		assertEquals("unexpected new order", 1, newOrder);
	}

	@Test
	public void getNextRuleApplyOrder_ExitRule_HasRules() {

		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true, true);
		Question question = survey.getQuestions().get(4);
		assertEquals("expected 1 existing rule", 1, service.findExitRules(question).size());

		int newOrder = serviceImpl.getNextRuleApplyOrder(ExitRule.class, question);
		assertEquals("unexpected new order", 1, newOrder);
	}
}
