package com.oas.command.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.oas.command.model.SimpleAnswerCommand;
import com.oas.model.Answer;
import com.oas.model.Question;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.answer.ScaleAnswer;

/**
 * Processes answers to Scale questions.
 * 
 * @author xhalliday
 * @since December 11, 2008
 */
public class ScaleAnswerProcessor extends AbstractAnswerProcessor {

	@Override
	public List<Answer> processAnswer(Survey survey, Response response, Question question, SimpleAnswerCommand command) {
		super.processAnswer(survey, response, question, command);

		// only ever 1 text answer
		ArrayList<Answer> retval = new ArrayList<Answer>();

		// never add blank answer
		if (StringUtils.hasText(command.getAnswer())) {
			Long answerValue;
			try {
				answerValue = new Long(command.getAnswer());
				// TODO assert value in allowed range
				retval.add(new ScaleAnswer(response, question, answerValue));
			} catch (NumberFormatException e) {
				// validator was supposed to handle this
				throw new IllegalArgumentException(e);
			}
		} else {
			Assert.isTrue(!question.isRequired(), "validator failed to prevent execution.");
		}

		// never add blank answer
		// UI is supposed to prevent this
		Assert.isTrue(!StringUtils.hasText(command.getOtherText()), "Scale Questions do not support Other Text");
		// if (StringUtils.hasText(command.getOtherText())) {
		// // UI should never allow this
		// Assert.isTrue(question.isAllowOtherText(),
		// "other text not allowed here");
		// retval.add(new TextAnswer(response, question,
		// command.getOtherText()));
		// }

		return retval;
	}
}
