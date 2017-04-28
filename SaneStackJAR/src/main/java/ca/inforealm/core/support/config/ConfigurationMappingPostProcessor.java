package ca.inforealm.core.support.config;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.model.annotation.SetByConfiguration;

/**
 * Processes any fields or methods that have a SetByConfiguration annotation.
 * 
 * When configuration is (re)loaded any such elements are set to the new value
 * based on the {@link ConfigurationItem#identifier}.
 * 
 * @author Jason Mroz
 * 
 */
@Component
public class ConfigurationMappingPostProcessor implements BeanPostProcessor {

	/** Logger. */
	protected Logger log = Logger.getLogger(this.getClass());

	/**
	 * The actual event handler that this class delegates to.
	 */
	@Autowired
	private ConfigurationMappingEventHandler eventHandler;

	/**
	 * Default constructor.
	 */
	public ConfigurationMappingPostProcessor() {
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(SetByConfiguration.class)) {

				SetByConfiguration annotation = method.getAnnotation(SetByConfiguration.class);
				// 
				Class<? extends Object>[] paramTypes = method.getParameterTypes();

				// method must take one parameter, and it must be a string
				// ie., setSomeValue(String value);
				final String validationError = "methods having @SetByConfiguration must take exactly one parameter and it must be a string";
				Assert.isTrue(paramTypes.length == 1, validationError);
				Assert.isTrue(String.class.equals(paramTypes[0]), validationError);

				eventHandler.addTargetObject(annotation.value(), method, bean);
			}
		}

		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * @param eventHandler
	 *            the eventHandler to set
	 */
	public void setEventHandler(ConfigurationMappingEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}
}
