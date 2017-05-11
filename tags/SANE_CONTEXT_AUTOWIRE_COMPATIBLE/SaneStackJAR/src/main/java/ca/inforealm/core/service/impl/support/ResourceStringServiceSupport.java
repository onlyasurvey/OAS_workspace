package ca.inforealm.core.service.impl.support;

import java.util.Collection;
import java.util.Date;

import org.springframework.util.Assert;

import ca.inforealm.core.model.ResourceString;

public abstract class ResourceStringServiceSupport {

	/**
	 * Search the collection for the newest last modified date.
	 * 
	 * @param list
	 * @return
	 */
	public static final Date findNewestResourceString(Collection<ResourceString> list) {

		Assert.notNull(list);
		Date newestDate = null;

		if (list.size() > 0) {
			for (ResourceString string : list) {

				if (newestDate == null || string.getLastModifiedDate().after(newestDate)) {
					newestDate = string.getLastModifiedDate();
				}
			}
		} // else: default

		return newestDate;
	}
}
