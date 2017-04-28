package com.oas.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.inforealm.core.model.Actor;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.TextQuestion;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.EntryRuleAction;
import com.oas.model.question.rules.EntryRuleType;
import com.oas.model.question.rules.ExitRule;
import com.oas.model.question.rules.ExitRuleAction;
import com.oas.model.question.rules.ExitRuleType;

/**
 * Test for BranchingService interface.
 * 
 * @author xhalliday
 * @since March 9, 2009
 */
public class BranchingServiceTest extends AbstractOASBaseTest {

	@Autowired
	private BranchingService service;

	public interface RuleManipulationCallback {
		public void doCallback(Survey survey);
	}

	public interface RuleTestCallback {
		public void doCallback(Survey survey);
	}

	@Test
	public void confirmTestScenarioAssumptions() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);

		assertEquals(1, service.findEntryRules(survey.getQuestions().get(0)).size());
		assertEquals(2, service.findEntryRules(survey.getQuestions().get(1)).size());

		assertEquals(1, service.findExitRules(survey.getQuestions().get(4)).size());

	}

	@Test
	public void clearRules() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);

		int initialCount = countEntryRules(question);
		assertTrue("expected scenario data to have existing rules", initialCount > 0);

		// do the deed
		service.clearRules(question);

		int newCount = countEntryRules(question);
		assertEquals("expected zero rules", 0, newCount);
	}

	@Test
	public void findEntryRules_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		Question other = survey.getQuestions().get(1);

		int initialCount = countEntryRules(question);
		assertTrue("expected scenario data to have existing rules", initialCount > 0);

		// 
		persist(new EntryRule(question, null, EntryRuleType.DEFAULT, EntryRuleAction.SKIP_QUESTION, 0));
		persist(new EntryRule(question, other, EntryRuleType.OTHER_EMPTY, EntryRuleAction.SHOW_QUESTION, 1));

		persist(survey);
		flushAndClear();

		List<EntryRule> list = service.findEntryRules(question);
		assertNotNull(list);
		assertNotEmpty(list);

		assertEquals("expected two rules", initialCount + 2, list.size());

	}

	@Test
	public void findExitRules_Success() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Question question = survey.getQuestions().get(0);
		Question other = survey.getQuestions().get(1);

		int initialCount = countExitRules(question);

		// 
		persist(new ExitRule(question, null, null, ExitRuleType.DEFAULT, ExitRuleAction.FORCE_FINISH, 0));
		persist(new ExitRule(question, null, other, ExitRuleType.HAS_ANSWER, ExitRuleAction.JUMP_TO_QUESTION, 1));

		persist(survey);
		flushAndClear();

		List<ExitRule> list = service.findExitRules(question);
		assertNotNull(list);
		assertNotEmpty(list);

		assertEquals("expected two rules", initialCount + 2, list.size());

	}

	// ======================================================================

	private void doNextQuestionTest(RuleManipulationCallback maniuplationCallback, RuleTestCallback testCallback) {

		Actor user = createAndSetSecureUserWithRoleUser();

		Long surveyId = null;
		{
			Survey survey = new Survey(user);
			// persist(survey);

			// 3 simple text questions
			Question q1 = new TextQuestion(survey, 10, 10, 10, 0L);
			Question q2 = new TextQuestion(survey, 10, 10, 10, 1L);
			Question q3 = new TextQuestion(survey, 10, 10, 10, 2L);

			// multiple-choice question with 3 choices
			ChoiceQuestion q4 = new ChoiceQuestion(survey, 3L);
			q4.getChoices().addAll(Arrays.asList(new Choice[] { new Choice(q4, 0L), new Choice(q4, 1L), new Choice(q4, 2L) }));

			ChoiceQuestion q5 = new ChoiceQuestion(survey, 4L);
			q5.getChoices().addAll(Arrays.asList(new Choice[] { new Choice(q5, 0L), new Choice(q5, 1L), new Choice(q5, 2L) }));

			ChoiceQuestion q6 = new ChoiceQuestion(survey, 4L);
			q6.getChoices().addAll(Arrays.asList(new Choice[] { new Choice(q6, 0L), new Choice(q6, 1L), new Choice(q6, 2L) }));

			survey.addQuestion(q1);
			survey.addQuestion(q2);
			survey.addQuestion(q3);
			survey.addQuestion(q4);
			survey.addQuestion(q5);
			survey.addQuestion(q6);

			persist(survey);

			flushAndClear();

			surveyId = survey.getId();
		}

		// allow caller to manipulate rules
		if (maniuplationCallback != null) {
			maniuplationCallback.doCallback(load(Survey.class, surveyId));
		}

		flushAndClear();

		// no rules exist

		// allow caller to test the current persistent state
		if (testCallback != null) {
			testCallback.doCallback(load(Survey.class, surveyId));
		}

	}

	/**
	 * Question 1 should always have Question 2 as "next" when no rules exist.
	 */
	@Test
	public void nextQuestion_Success_NoRules() {

		doNextQuestionTest(null, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Question q1 = arg0.getQuestions().get(0);
				Question q2 = arg0.getQuestions().get(1);

				// we're checking against the first question
				// no answers, which doesn't matter, because no rules apply
				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);
				Question next = service.nextQuestion(q1, response);
				assertNotNull(next);

				// should have returned the second, since there are no branching
				// rules
				assertEquals(q2.getId(), next.getId());
			}
		});
	}

	@Test
	public void nextQuestion_ExitRule_Success_JumpToQuestion_HasAnswer() {

		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Question q1 = arg0.getQuestions().get(0);
				Question jumpTo = arg0.getQuestions().get(4);

				// set first question to jump to third when an answer is
				// provided
				persist(new ExitRule(q1, null, jumpTo, ExitRuleType.HAS_ANSWER, ExitRuleAction.JUMP_TO_QUESTION, 0));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);

				persist(response);

				List<Question> allQuestions = arg0.getQuestions();

				Question q1 = allQuestions.get(0);
				Question jumpTo = allQuestions.get(4);

				// add an answer to match
				List<Answer> answers = new ArrayList<Answer>();
				answers.add(new TextAnswer(response, q1, "some answer"));
				getHibernateTemplate().saveOrUpdateAll(answers);

				{
					Question next = service.nextQuestion(q1, response);
					assertNotNull(next);

					//
					assertEquals(jumpTo.getId(), next.getId());
				}

				//
				// add a skip rule to jumpTo and ensure it's respected
				//
				{
					// simple default rule
					persist(new EntryRule(jumpTo, null, EntryRuleType.DEFAULT, EntryRuleAction.SKIP_QUESTION, 0));

					//
					Question next = service.nextQuestion(q1, response);
					assertNotNull(next);

					//
					int index = allQuestions.indexOf(jumpTo) + 1;
					Question expectedQuestion = allQuestions.get(index);

					assertEquals(expectedQuestion.getId(), next.getId());
				}

			}
		});
	}

	@Test
	public void nextQuestion_ExitRule_Success_JumpToQuestion_NoAnswer() {
		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Question q1 = arg0.getQuestions().get(0);
				Question jumpTo = arg0.getQuestions().get(5);

				// set first question to jump to third when an answer is
				// provided
				persist(new ExitRule(q1, null, jumpTo, ExitRuleType.NO_ANSWER, ExitRuleAction.JUMP_TO_QUESTION, 0));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);

				Question q1 = arg0.getQuestions().get(0);
				Question jumpTo = arg0.getQuestions().get(5);

				// add no answers
				Question next = service.nextQuestion(q1, response);
				assertNotNull(next);

				// should have returned the second, since there are no branching
				// rules
				assertEquals(jumpTo.getId(), next.getId());
			}
		});
	}

	@Test
	public void nextQuestion_EntryRule_Success_OtherEmpty() {
		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				// Question q1 = arg0.getQuestions().get(0);
				Question q2 = arg0.getQuestions().get(1);
				Question otherAnswer = arg0.getQuestions().get(5);

				// default rule - skip when other is empty
				persist(new EntryRule(q2, otherAnswer, EntryRuleType.OTHER_EMPTY, EntryRuleAction.SKIP_QUESTION, 0));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);

				Question q1 = arg0.getQuestions().get(0);

				Question otherAnswer = arg0.getQuestions().get(5);

				{
					Question nextAfterSkippingQuestion2 = arg0.getQuestions().get(2);
					clearAnswers(otherAnswer);

					//
					Question next = service.nextQuestion(q1, response);
					assertNotNull(next);
					assertEquals(nextAfterSkippingQuestion2.getId(), next.getId());
				}

				//

				// add answer - rule should no longer match and we go to (1)
				// instead of (2)
				{
					Question sequentiallyNext = arg0.getQuestions().get(1);
					List<Answer> answers = new ArrayList<Answer>();
					answers.add(new TextAnswer(response, otherAnswer, "other text"));

					//
					getHibernateTemplate().saveOrUpdateAll(answers);
					Question next = service.nextQuestion(q1, response);
					assertNotNull(next);

					//
					assertEquals(sequentiallyNext.getId(), next.getId());
				}
			}
		});
	}

	@Test
	public void nextQuestion_ExitRule_Success_JumpToQuestion_ChoiceOn() {

		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				ChoiceQuestion q4 = (ChoiceQuestion) arg0.getQuestions().get(3);
				Choice c1 = q4.getChoices().get(0);

				Question jumpTo = arg0.getQuestions().get(5);

				// set first question to jump to third when an answer is
				// provided
				persist(new ExitRule(q4, c1, jumpTo, ExitRuleType.CHOICE_ON, ExitRuleAction.JUMP_TO_QUESTION, 0));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);

				ChoiceQuestion q4 = (ChoiceQuestion) arg0.getQuestions().get(3);
				ChoiceQuestion sequentiallyNext = (ChoiceQuestion) arg0.getQuestions().get(4);

				// just "other text" - no match on choice
				{
					clearAnswers(q4);

					List<Answer> answers = new ArrayList<Answer>();
					answers.add(new TextAnswer(response, q4, "other text"));

					//
					getHibernateTemplate().saveOrUpdateAll(answers);
					Question next = service.nextQuestion(q4, response);
					assertNotNull(next);

					//
					assertEquals(sequentiallyNext.getId(), next.getId());
				}

				// user select's the choice, the rule now matches
				{
					clearAnswers(q4);

					Choice c1 = q4.getChoices().get(0);
					Question jumpTo = arg0.getQuestions().get(5);

					List<Answer> answers = new ArrayList<Answer>();
					answers.add(new ChoiceAnswer(response, q4, c1));
					getHibernateTemplate().saveOrUpdateAll(answers);

					//
					Question next = service.nextQuestion(q4, response);
					assertNotNull(next);

					//
					assertEquals(jumpTo.getId(), next.getId());
				}

				// user select's some different choice, the rule does not match
				{
					clearAnswers(q4);

					Choice c2 = q4.getChoices().get(1);

					List<Answer> answers = new ArrayList<Answer>();
					answers.add(new ChoiceAnswer(response, q4, c2));
					getHibernateTemplate().saveOrUpdateAll(answers);

					//
					Question next = service.nextQuestion(q4, response);
					assertNotNull(next);

					//
					assertEquals(sequentiallyNext.getId(), next.getId());
				}
			}
		});
	}

	@Test
	public void nextQuestion_ExitRule_Success_JumpToQuestion_ChoiceOff() {
		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				ChoiceQuestion q4 = (ChoiceQuestion) arg0.getQuestions().get(3);
				Choice c1 = q4.getChoices().get(0);

				Question jumpTo = arg0.getQuestions().get(5);

				// set first question to jump to third when an answer is
				// provided
				persist(new ExitRule(q4, c1, jumpTo, ExitRuleType.CHOICE_OFF, ExitRuleAction.JUMP_TO_QUESTION, 0));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);

				ChoiceQuestion q4 = (ChoiceQuestion) arg0.getQuestions().get(3);
				ChoiceQuestion sequentiallyNext = (ChoiceQuestion) arg0.getQuestions().get(4);
				Choice c1 = q4.getChoices().get(0);
				Choice c2 = q4.getChoices().get(1);

				Question jumpTo = arg0.getQuestions().get(5);

				// add answer to choice #2 - the rule is on choice #1, so this
				// should not match
				List<Answer> answers = new ArrayList<Answer>();
				answers.add(new ChoiceAnswer(response, q4, c2));
				// an "other" text answer
				answers.add(new TextAnswer(response, q4, "other text"));
				getHibernateTemplate().saveOrUpdateAll(answers);

				{
					// we're checking against the first question
					// no answers, which doesn't matter, because no rules apply
					Question next = service.nextQuestion(q4, response);
					assertNotNull(next);

					//
					assertEquals(jumpTo.getId(), next.getId());
				}

				// the user has selected the choice - rule doesn't match
				{

					answers.add(new ChoiceAnswer(response, q4, c1));
					getHibernateTemplate().saveOrUpdateAll(answers);
					Question next = service.nextQuestion(q4, response);
					assertNotNull(next);

					// q5 is sequentially next, whereas the rule says to skip to
					// q6

					assertEquals(sequentiallyNext.getId(), next.getId());
				}
			}
		});
	}

	@Test
	public void nextQuestion_ExitRule_Success_Default() {
		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Question q1 = arg0.getQuestions().get(0);
				Question jumpTo = arg0.getQuestions().get(5);

				// first rule is a jump out
				persist(new ExitRule(q1, null, jumpTo, ExitRuleType.HAS_ANSWER, ExitRuleAction.JUMP_TO_QUESTION, 0));

				// the default is to end
				persist(new ExitRule(q1, null, null, ExitRuleType.DEFAULT, ExitRuleAction.FORCE_FINISH, 1));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);

				Question q1 = arg0.getQuestions().get(0);

				// add no answers: default case should be hit
				{
					clearAnswers(q1);

					Question next = service.nextQuestion(q1, response);
					assertIsNull("default rule should have ended the survey", next);
				}

				// ensure default is not always run
				// add an answer to match
				{
					clearAnswers(q1);

					Question jumpTo = arg0.getQuestions().get(5);
					List<Answer> answers = new ArrayList<Answer>();
					answers.add(new TextAnswer(response, q1, "some answer"));
					getHibernateTemplate().saveOrUpdateAll(answers);

					Question next = service.nextQuestion(q1, response);
					assertEquals(jumpTo.getId(), next.getId());
				}
			}
		});
	}

	/**
	 * Exercise entry (skip) rules
	 */
	@Test
	public void nextQuestion_Skipping_Success_DefaultSkip() {

		doNextQuestionTest(new RuleManipulationCallback() {
			@Override
			public void doCallback(Survey arg0) {

				//
				Question q1 = arg0.getQuestions().get(0);
				// Question q2 = arg0.getQuestions().get(1);
				Question q3 = arg0.getQuestions().get(2);

				persist(new EntryRule(q3, null, EntryRuleType.DEFAULT, EntryRuleAction.SHOW_QUESTION, 0));
				persist(new EntryRule(q3, q1, EntryRuleType.OTHER_ANSWERED, EntryRuleAction.SKIP_QUESTION, 1));

			}
		}, new RuleTestCallback() {
			@Override
			public void doCallback(Survey arg0) {

				/*
				 * First, with no rules then questionJustAnswered should forward
				 * to defaultNext.
				 * 
				 * Second, with an OTHER_ANSWERED on q2, pointing to q1 (did q1
				 * have an answer?) and where we add an answer for q1, then
				 * questionJustAnswered should forward to skipNext
				 */

				Question otherRuleChecks = arg0.getQuestions().get(0);
				Question questionJustAnswered = arg0.getQuestions().get(1);
				Question defaultNext = arg0.getQuestions().get(2);
				Question skipNext = arg0.getQuestions().get(3);

				Response response = new Response(arg0, new Date(), supportedLanguageService.findByCode("eng"), LOCALHOST_IP);
				persist(response);

				// we're checking against the first question
				// no answers, which doesn't matter, because no rules apply
				{
					Question next = service.nextQuestion(questionJustAnswered, response);
					assertNotNull(next);

					// should have returned the second, since there are no
					// branching
					// rules
					assertEquals("unexpected Next Question from question #" + questionJustAnswered.getId(), defaultNext.getId(),
							next.getId());
				}

				{

					// add answer to question 1, which will match an
					// OTHER_ANSWERED rule
					persist(new TextAnswer(response, otherRuleChecks, "text"));

					Question next = service.nextQuestion(questionJustAnswered, response);
					assertNotNull(next);

					// should return q3 because q2's OTHER_ANSWERED rule
					assertEquals(skipNext.getId(), next.getId());
				}
			}
		});
	}

	// ======================================================================

	@Test
	public void deleteRule_EntryRule() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true, true);
		Question question = survey.getQuestions().get(0);
		EntryRule rule = null;

		{
			List<EntryRule> list = service.findEntryRules(question);
			assertEquals("expected 1 existing rule", 1, list.size());
			rule = list.get(0);
		}

		assertNotNull(rule);

		service.deleteRule(rule);
		flushAndClear();

		{
			List<EntryRule> list = service.findEntryRules(question);
			assertEquals("expected no existing rule", 0, list.size());
		}
	}

	@Test
	public void deleteRule_ExitRule() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true, true);
		Question question = survey.getQuestions().get(4);
		ExitRule rule = null;

		{
			List<ExitRule> list = service.findExitRules(question);
			assertEquals("expected 1 existing rule", 1, list.size());
			rule = list.get(0);
		}

		assertNotNull(rule);

		service.deleteRule(rule);
		flushAndClear();

		{
			List<ExitRule> list = service.findExitRules(question);
			assertEquals("expected no existing rule", 0, list.size());
		}
	}

	// ======================================================================

	private void clearAnswers(Question question) {
		List<Answer> list = find("from Answer where question = ?", question);
		for (Answer answer : list) {
			delete(answer);
		}

		getHibernateTemplate().flush();
	}

	private int countEntryRules(Question question) {
		return ((Long) getHibernateTemplate().find("select count(a) from EntryRule a where question = ?", question).iterator()
				.next()).intValue();
	}

	private int countExitRules(Question question) {
		return ((Long) getHibernateTemplate().find("select count(a) from ExitRule a where question = ?", question).iterator()
				.next()).intValue();
	}

}
