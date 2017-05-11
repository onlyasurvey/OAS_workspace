package com.oas.util;

import junit.framework.TestCase;

import org.junit.Test;

import com.oas.model.Choice;
import com.oas.model.Question;
import com.oas.model.Survey;

public class BaseObjectUtilTest extends TestCase {

	@Test
	public void testIsSurvey_Pass() {
		Survey object = new Survey();
		assertTrue(BaseObjectUtil.isSurvey(object));
		assertFalse(BaseObjectUtil.isQuestion(object));
		assertFalse(BaseObjectUtil.isChoice(object));
	}

	@Test
	public void testIsQuestion_Pass() {
		Question object = new Question();
		assertFalse(BaseObjectUtil.isSurvey(object));
		assertTrue(BaseObjectUtil.isQuestion(object));
		assertFalse(BaseObjectUtil.isChoice(object));
	}

	@Test
	public void testIsChoice_Pass() {
		Choice object = new Choice();
		assertFalse(BaseObjectUtil.isSurvey(object));
		assertFalse(BaseObjectUtil.isQuestion(object));
		assertTrue(BaseObjectUtil.isChoice(object));
	}

}
