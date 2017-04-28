package com.oas.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.oas.command.processor.AnswerProcessor;
import com.oas.command.processor.BooleanAnswerProcessor;
import com.oas.command.processor.ChoiceAnswerProcessor;
import com.oas.command.processor.ScaleAnswerProcessor;
import com.oas.command.processor.TextAnswerProcessor;
import com.oas.model.Question;
import com.oas.model.question.BooleanQuestion;
import com.oas.model.question.ChoiceQuestion;
import com.oas.model.question.PageQuestion;
import com.oas.model.question.ScaleQuestion;
import com.oas.model.question.TextQuestion;

/**
 * Utilities relating to question types.
 * 
 * @author xhalliday
 * @since September 8, 2008
 */
abstract public class QuestionTypeUtil {

	private static final Map<Class<? extends Question>, QuestionTypeMapping> mappings;

	// @evil
	static {
		mappings = new HashMap<Class<? extends Question>, QuestionTypeMapping>();
		mappings.put(BooleanQuestion.class, new QuestionTypeMapping("boolean", new BooleanAnswerProcessor()));
		mappings.put(ChoiceQuestion.class, new QuestionTypeMapping("choice", new ChoiceAnswerProcessor()));
		mappings.put(TextQuestion.class, new QuestionTypeMapping("text", new TextAnswerProcessor()));
		mappings.put(ScaleQuestion.class, new QuestionTypeMapping("scale", new ScaleAnswerProcessor()));
		mappings.put(PageQuestion.class, new QuestionTypeMapping("page", new ScaleAnswerProcessor()));
	}

	public static String getViewNameForQuestion(Question question) {
		QuestionTypeMapping mapping = mappings.get(question.getClass());
		Assert.notNull(mapping);
		String variablePart = mapping.getViewName();
		Assert.notNull(variablePart);
		return "question/" + variablePart + "Question";
	}

	public static AnswerProcessor getAnswerProcessor(Question question) {

		QuestionTypeMapping mapping = null;

		// TODO must be a better way
		// parameter will always be Question class, we have to iterate to find
		// true type
		for (Class<? extends Question> clazz : mappings.keySet()) {
			// if (clazz.isAssignableFrom(question.getClass())) {
			if (question.getClass().isAssignableFrom(clazz)) {
				mapping = mappings.get(clazz);
				break;
			}
		}

		// new instance
		Assert.notNull(mapping);
		AnswerProcessor retval = mapping.getAnswerProcessor();
		Assert.notNull(retval);

		return retval;
	}

}
