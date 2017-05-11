package ca.inforealm.core.service.event;

import org.springframework.context.ApplicationEvent;

/**
 * Indicates that the SANE application model has changed, generally due to test
 * cases setting up scenarios but also if the context is manually reloaded.
 * 
 * @author Jason Mroz
 * 
 */
public class ApplicationModelChangedEvent extends ApplicationEvent {

	public ApplicationModelChangedEvent(Object source) {
		super(source);
	}

}
