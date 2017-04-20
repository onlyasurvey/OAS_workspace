package com.oas.util;

import org.springframework.util.Assert;

import com.oas.model.BaseObject;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;

abstract public class BaseObjectUtil {

	public static boolean isSurvey(BaseObject object) {
		Assert.notNull(object);
		return Survey.class.isAssignableFrom(object.getClass());
	}

	public static boolean isQuestion(BaseObject object) {
		Assert.notNull(object);
		return Question.class.isAssignableFrom(object.getClass());
	}

	public static boolean isChoice(BaseObject object) {
		Assert.notNull(object);
		return Choice.class.isAssignableFrom(object.getClass());
	}
}
