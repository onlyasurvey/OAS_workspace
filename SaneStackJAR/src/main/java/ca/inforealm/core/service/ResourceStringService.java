package ca.inforealm.core.service;

import org.springframework.context.MessageSource;

public interface ResourceStringService extends AbstractServiceInterface, MessageSource {

	/**
	 * Reload all resource strings.
	 * 
	 */
	public void reloadResources();

	/**
	 * Load resource strings that have changed.
	 */
	public void performDifferentialReload();

	/**
	 * Set (or unset) a fallback MessageSource to try if the resource can't be
	 * loaded directly via this service.
	 * 
	 * TODO there is likely something for this already in Spring
	 * 
	 * @param fallbackSource
	 */
	public void setFallbackSource(MessageSource fallbackSource);
}
