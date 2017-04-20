package com.oas.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.oas.command.processor.BooleanAnswerProcessor;
import com.oas.command.processor.ChoiceAnswerProcessor;
import com.oas.command.processor.ScaleAnswerProcessor;
import com.oas.command.processor.TextAnswerProcessor;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;

public class QuestionTypeUtilTest {

	@Test
	public void testGetAnswerProcessor_Success() {

		// cast as the expected type, ClastCastException will fail the test if
		// it's not correct
		BooleanAnswerProcessor bap = (BooleanAnswerProcessor) QuestionTypeUtil.getAnswerProcessor(new BooleanQuestion());
		assertNotNull(bap);
		ChoiceAnswerProcessor cap = (ChoiceAnswerProcessor) QuestionTypeUtil.getAnswerProcessor(new ChoiceQuestion());
		assertNotNull(cap);
		TextAnswerProcessor tap = (TextAnswerProcessor) QuestionTypeUtil.getAnswerProcessor(new TextQuestion());
		assertNotNull(tap);
		ScaleAnswerProcessor scap = (ScaleAnswerProcessor) QuestionTypeUtil.getAnswerProcessor(new ScaleQuestion());
		assertNotNull(scap);
	}

	@Test
	public void testGetAnswerProcessor_Fail_Boolean() {
		assertEquals("expected correct class", BooleanAnswerProcessor.class, QuestionTypeUtil.getAnswerProcessor(
				new BooleanQuestion()).getClass());
	}

	@Test
	public void testGetAnswerProcessor_Fail_Choice() {

		assertEquals("expected correct class", ChoiceAnswerProcessor.class, QuestionTypeUtil.getAnswerProcessor(
				new ChoiceQuestion()).getClass());
	}

	@Test
	public void testGetAnswerProcessor_Fail_Text() {

		assertEquals("expected correct class", TextAnswerProcessor.class, QuestionTypeUtil.getAnswerProcessor(new TextQuestion())
				.getClass());
	}

	// ======================================================================

}
