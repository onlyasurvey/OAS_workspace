package ca.inforealm.core.service.event;

import org.springframework.context.ApplicationEvent;

public class ResourceStringDataChangedEvent extends ApplicationEvent {
	public ResourceStringDataChangedEvent(Object source) {
		super(source);
	}
}
