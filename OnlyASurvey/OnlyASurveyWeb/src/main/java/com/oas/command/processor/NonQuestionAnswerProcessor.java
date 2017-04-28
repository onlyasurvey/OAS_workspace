package com.oas.command.processor;

import java.util.Collections;
import java.util.List;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;

/**
 * AnswerProcessor for non-Question-questions, e.g., the Page question type.
 * 
 * @author xhalliday
 * @since May 25, 2009
 */
public class NonQuestionAnswerProcessor extends AbstractAnswerProcessor {

	/** {@inheritDoc} */
	@Override
	public List<Answer> processAnswer(Survey survey, Response response, Question question, SimpleAnswerCommand command) {

		// validation, etc
		super.processAnswer(survey, response, question, command);

		// no real op here
		return Collections.emptyList();
	}
}
