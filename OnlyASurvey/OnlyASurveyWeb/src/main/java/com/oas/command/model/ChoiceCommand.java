package com.oas.command.model;

import java.util.Collection;
import java.util.Map;

import com.oas.model.SupportedLanguage;

/**
 * Command representing a Choice, currently it only consists of a language-keyed
 * map.
 * 
 * @author xhalliday
 */
public class ChoiceCommand extends NameObjectCommand {

	public ChoiceCommand() {
	}

	public ChoiceCommand(Collection<SupportedLanguage> supportedLanguages) {
		super(supportedLanguages);
	}

	public ChoiceCommand(Map<String, String> map) {
		super(map);
	}
}
