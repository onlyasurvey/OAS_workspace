package ca.inforealm.core.support.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.service.event.ConfigurationItemChangedEvent;

@Component
public class ConfigurationMappingEventHandler implements ApplicationListener {

	private Map<String, Map<Method, Set<Object>>> targetObjects = new HashMap<String, Map<Method, Set<Object>>>();

	// private Map<Method, Set<Object>> targetObjects = new HashMap<Method,
	// Set<Object>>();

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ConfigurationItemChangedEvent) {

			// the source of the event
			ConfigurationItem item = (ConfigurationItem) event.getSource();
			Map<Method, Set<Object>> targetInvocations = targetObjects.get(item.getIdentifier());

			if (targetInvocations == null) {
				// no objects are interested in this key
				return;
			}

			for (Method method : targetInvocations.keySet()) {
				// for each method, invoke on instances
				for (Object targetObject : targetInvocations.get(method)) {
					try {
						// invoke the method, presumably a setter
						method.invoke(targetObject, item.getValue());
					} catch (Exception e) {
						// wrap in runtime exception
						throw new RuntimeException(e);
					}
				}
			}

		}
	}

	public void addTargetObject(String identifier, Method method, Object object) {
		// get an existing mapping, if any
		Map<Method, Set<Object>> mapping = targetObjects.get(identifier);

		// initialize the entry if not yet existing
		if (mapping == null) {
			mapping = new HashMap<Method, Set<Object>>();
			targetObjects.put(identifier, mapping);
		}

		// initialize the set of target objects if not yet existing
		Set<Object> objectSet = mapping.get(method);
		if (objectSet == null) {
			objectSet = new HashSet<Object>();
			mapping.put(method, objectSet);
		}

		// add to this set
		objectSet.add(object);
	}
}
