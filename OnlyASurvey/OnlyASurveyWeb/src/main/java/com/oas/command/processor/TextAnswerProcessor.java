package com.oas.command.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.TextAnswer;

public class TextAnswerProcessor extends AbstractAnswerProcessor {

	@Override
	public List<Answer> processAnswer(Survey survey, Response response, Question question, SimpleAnswerCommand command) {
		super.processAnswer(survey, response, question, command);

		// only ever 1 text answer
		ArrayList<Answer> retval = new ArrayList<Answer>();

		// never add blank answer
		String text = command.getAnswer();
		if (StringUtils.hasText(text)) {
			retval.add(new TextAnswer(response, question, text));
		}

		return retval;
	}
}
