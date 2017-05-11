package ca.inforealm.core.service.event;

import org.springframework.context.ApplicationEvent;

/**
 * Indicates that a configuration item has been changed. The source object is
 * the ConfigurationItem instance.
 * 
 * @author Jason Mroz
 */
public class ConfigurationItemChangedEvent extends ApplicationEvent {
	public ConfigurationItemChangedEvent(Object source) {
		super(source);
	}
}
