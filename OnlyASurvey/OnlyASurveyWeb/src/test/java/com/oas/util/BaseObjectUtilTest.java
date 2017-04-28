package com.oas.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;

public class BaseObjectUtilTest extends AbstractOASBaseTest {

	@Test
	public void isSurvey_Pass() {
		Survey object = new Survey();
		assertTrue(BaseObjectUtil.isSurvey(object));
		assertFalse(BaseObjectUtil.isQuestion(object));
		assertFalse(BaseObjectUtil.isChoice(object));
	}

	@Test
	public void isQuestion_Pass() {
		Question object = new Question();
		assertFalse(BaseObjectUtil.isSurvey(object));
		assertTrue(BaseObjectUtil.isQuestion(object));
		assertFalse(BaseObjectUtil.isChoice(object));
	}

	@Test
	public void isChoice_Pass() {
		Choice object = new Choice();
		assertFalse(BaseObjectUtil.isSurvey(object));
		assertFalse(BaseObjectUtil.isQuestion(object));
		assertTrue(BaseObjectUtil.isChoice(object));
	}

}
