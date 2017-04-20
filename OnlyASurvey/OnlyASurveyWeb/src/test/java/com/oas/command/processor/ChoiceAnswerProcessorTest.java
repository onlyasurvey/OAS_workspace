package com.oas.command.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.ChoiceAnswer;
import com.oas.model.answer.TextAnswer;
import com.oas.model.question.ChoiceQuestion;
import com.oas.util.Constants;

public class ChoiceAnswerProcessorTest extends AbstractOASBaseTest {

	@Test
	public void testProcessAnswer_Success_WithSingleValue() {
		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerForChoice(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command, 1, true, false, null);

		assertNotNull("should return a populated answer object", answer);
		assertEquals("should have 1 selected choice", 1, answer.size());
	}

	@Test
	public void testProcessAnswer_Success_WithMultipleValues() {
		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		Collection<Answer> answer = AnswerProcessorTestUtil.getAnswerForChoice(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command, 2, true, false, null);

		assertNotNull("should return a populated answer object", answer);
		assertEquals("should have 2 selected choices", 2, answer.size());
	}

	@Test
	public void testProcessAnswer_Success_WithOnlyOtherTextValue() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);

		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setChoiceIdList(new long[] { Constants.OTHER_TEXT_ID });
		command.setOtherText("some other text");

		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("expected a choice question in scenario data", question);

		// make nice
		question.setAllowOtherText(true);

		// process
		Collection<Answer> list = proc.processAnswer(survey, response, question, command);

		assertEquals("should have one answer", 1, list.size());
		Answer answer = list.iterator().next();

		// Other Text is always stored as a TextAnswer
		assertTrue("expected Text answer", answer.isTextAnswer());
		assertEquals("expected same text", command.getOtherText(), ((TextAnswer) answer).getSimpleValue());
	}

	@Test
	public void testProcessAnswer_Success_WithOnlyNoneOfTheAboveValue() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);

		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setChoiceIdList(new long[] { Constants.NONE_OF_THE_ABOVE_ID });

		ChoiceQuestion question = getFirstQuestionOfType(survey, ChoiceQuestion.class);
		assertNotNull("expected a choice question in scenario data", question);

		// make nice
		question.setRequired(false);

		// process
		Collection<Answer> list = proc.processAnswer(survey, response, question, command);

		// "none of the above" means no answer
		assertEquals("should have no answers", 0, list.size());
	}

	@Test
	public void testProcessAnswer_Fail_NotUnlimitedWithChoiceAndOtherTextValue() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);

		ChoiceQuestion question = null;
		for (Question theQuestion : survey.getQuestions()) {
			if (theQuestion.isChoiceQuestion()) {
				question = (ChoiceQuestion) theQuestion;
				break;
			}
		}
		assertNotNull("expected a choice question in scenario data", question);

		// make it only accept 1 answer, so a choice + other text is invalid
		question.setUnlimited(false);

		//
		question.setAllowOtherText(true);
		Choice choice = question.getChoices().iterator().next();
		assertNotNull("test data fault: no choice for multi-choice question", choice);

		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setChoiceIdList(new long[] { choice.getId(), Constants.OTHER_TEXT_ID });
		command.setOtherText("some other text");

		// process
		try {
			proc.processAnswer(survey, response, question, command);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testProcessAnswer_Success_UnlimitedWithChoiceAndOtherTextValue() {
		Survey survey = scenarioDataUtil.createTypicalScenario1(createAndSetSecureUserWithRoleUser(), true);
		Response response = scenarioDataUtil.addDefaultScenario1Response(survey);

		ChoiceQuestion question = null;
		for (Question theQuestion : survey.getQuestions()) {
			if (theQuestion.isChoiceQuestion()) {
				question = (ChoiceQuestion) theQuestion;
				break;
			}
		}
		assertNotNull("expected a choice question in scenario data", question);

		// ensure question is unlimited, which allows choice(s) + Other Text
		question.setUnlimited(true);

		//
		question.setAllowOtherText(true);
		Choice choice = question.getChoices().iterator().next();
		assertNotNull("test data fault: no choice for multi-choice question", choice);

		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setChoiceIdList(new long[] { choice.getId(), Constants.OTHER_TEXT_ID });
		command.setOtherText("some other text");

		// process
		Collection<Answer> list = proc.processAnswer(survey, response, question, command);

		assertEquals("unexpected # answers", 2, list.size());

		boolean foundTextAnswer = false;
		boolean foundChoiceAnswer = false;

		for (Answer answer : list) {
			if (answer.isChoiceAnswer()) {
				foundChoiceAnswer = true;
			}
			if (answer.isTextAnswer()) {
				foundTextAnswer = true;
				// Other Text is always stored as a TextAnswer
				assertEquals("expected same text", command.getOtherText(), ((TextAnswer) answer).getSimpleValue());
			}
		}

		assertTrue("did not find choice answer", foundChoiceAnswer);
		assertTrue("did not find text answer", foundTextAnswer);
	}

	@Test
	public void testProcessAnswer_ThrowsWithChoiceFromOtherQuestion() {
		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setChoiceIdList(new long[] { 0L });
		try {
			AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc, createAndSetSecureUserWithRoleUser(), command);
			fail("should have thrown exception");
		} catch (RuntimeException e) {
			// expected
		}
	}

	@Test
	public void testProcessAnswer_FailOnIllegalValueArgument() {
		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		command.setChoiceIdList(null);

		try {
			AnswerProcessorTestUtil.getAnswerFor(scenarioDataUtil, proc, createAndSetSecureUserWithRoleUser(), command);
			fail("should have thrown exception");
		} catch (RuntimeException e) {
			// expected
		}
	}

	@Test
	public void testProcessAnswer_SummingQuestion() {
		ChoiceAnswerProcessor proc = new ChoiceAnswerProcessor();
		SimpleAnswerCommand command = new SimpleAnswerCommand();
		List<? extends Answer> answerList = AnswerProcessorTestUtil.getAnswerForChoice(scenarioDataUtil, proc,
				createAndSetSecureUserWithRoleUser(), command, 3, true, true, new int[] { 60, 0, 40 });

		assertNotNull("should return a populated answer object", answerList);
		assertEquals("should have 2 selected choices", 2, answerList.size());

		@SuppressWarnings("unchecked")
		List<ChoiceAnswer> choiceAnswerList = (List<ChoiceAnswer>) answerList;
		assertEquals("answer is supposed to have a value", Integer.valueOf(60), choiceAnswerList.get(0).getSumValue());
		assertEquals("answer is supposed to have a value", Integer.valueOf(40), choiceAnswerList.get(1).getSumValue());

	}
}
