package com.oas.command.processor;

import java.util.ArrayList;
import java.util.List;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.BooleanAnswer;

public class BooleanAnswerProcessor extends AbstractAnswerProcessor {

	@Override
	public List<Answer> processAnswer(Survey survey, Response response, Question question, SimpleAnswerCommand command) {
		super.processAnswer(survey, response, question, command);

		Boolean value = null;

		String textValue = command.getAnswer();
		if (textValue == null) {
			// value still null
			// TODO delete existing answer if null allowed and no answer
			// provided by user

		} else if ("1".equals(textValue)) {
			value = true;
		} else if ("0".equals(textValue)) {
			value = false;
		} else {
			throw new IllegalArgumentException("requires 0, 1 or null");
		}

		// only ever 1 text answer
		ArrayList<Answer> retval = new ArrayList<Answer>(1);

		retval.add(new BooleanAnswer(response, question, value));

		return retval;
	}
}
