package com.oas.service;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.command.model.IdListCommand;
import com.oas.model.SupportedLanguage;

public class SupportedLanguageServiceTest extends AbstractOASBaseTest {

	@Autowired
	private SupportedLanguageService service;

	@Override
	@Before
	public void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();

		// the service depends on SupportedLanguages existing already
		Collection<Long> languageList = service.getSupportedLanguageIds();
		assertNotNull("should have some supported languages in the system", languageList);
		assertTrue("should have some supported languages in the system", languageList.size() > 0);
	}

	@Test
	public void isValidIdList_Success_EmptyList() {

		// Collection<Long> languageList = service.getSupportedLanguageIds();

		IdListCommand command = new IdListCommand();
		command.setIds(new ArrayList<Long>());

		// add no IDs, it's just an empty list
		assertTrue("should be valid", service.isValidIdList(command));
	}

	@Test
	public void isValidIdList_Success_SpecificIds() {

		Collection<Long> languageList = service.getSupportedLanguageIds();

		IdListCommand command = new IdListCommand();
		command.setIds(new ArrayList<Long>());

		for (Long languageId : languageList) {
			command.addId(languageId);
		}

		// list now contains each of the supported languages
		assertTrue("should be valid", service.isValidIdList(command));
	}

	@Test
	public void isValidIdList_FailOnNullCommand() {
		assertFalse("should be invalid", service.isValidIdList(null));
	}

	@Test
	public void isValidIdList_FailOnNullCommandIdList() {
		// 
		IdListCommand command = new IdListCommand();
		// set internal state to null, since by default it's initialized
		command.setIds(null);
		assertFalse("should be invalid", service.isValidIdList(command));
	}

	@Test
	public void isValidIdList_FailOnInvalidId() {
		IdListCommand command = new IdListCommand();
		command.setIds(new ArrayList<Long>());
		command.addId(-2938223L);

		assertFalse("should be invalid", service.isValidIdList(command));
	}

	@Test
	public void getSupportedLanguageMap_Success() {
		Map<String, SupportedLanguage> map = service.getSupportedLanguageMap();
		assertNotNull(map);
	}
}
