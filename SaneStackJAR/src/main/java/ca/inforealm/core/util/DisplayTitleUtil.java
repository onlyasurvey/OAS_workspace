package ca.inforealm.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import ca.inforealm.core.model.annotation.DisplayTitle;

/**
 * Implements utilities for the DisplayTitle annotation, specifically it
 * resolves the appropriate method for a DisplayTitle-annotated class and
 * returns it's value.
 * 
 * @author Jason Mroz
 * 
 */
abstract public class DisplayTitleUtil {

	private static final Logger log = Logger.getLogger(DisplayTitleUtil.class);

	/**
	 * Determine the "best" method to call to return a user-locale-specific
	 * (where available) display title for the specified resource.
	 * 
	 * @param subject
	 *            Any object, in particular those with DisplayTitle annotations
	 * @return String
	 */
	public static String getDisplayTitle(Object subject) {

		String retval = "bug";

		ArrayList<Method> candidateList = new ArrayList<Method>();
		Method[] list = subject.getClass().getMethods();

		// collect all methods that have the DisplayTitle annotation present
		for (Method method : list) {
			if (method.isAnnotationPresent(DisplayTitle.class)) {
				// this method is a candidate DisplayTitle
				candidateList.add(method);
			}
		}

		try {
			switch (candidateList.size()) {
			case 0:
				// very generic
				retval = getFallbackIdValue(subject);
				break;
			case 1:
				// there's only one, use it
				Method method = candidateList.get(0);
				retval = getFirstMethodValue(subject, method);
				break;
			default:
				// try to pick the best based on locale
				retval = getBestMethodMatch(subject, candidateList);
				break;
			}
		} catch (Exception e) {
			// TODO review: this uses a generic Exception to avoid untestability
			log.error("Exception getting @DisplayTitle from class: " + subject.getClass().getName());
			throw new RuntimeException(e);
		}
		return retval;
	}

	private static String getFirstMethodValue(Object subject, Method method) throws IllegalAccessException, InvocationTargetException {

		return (String) method.invoke(subject);
	}

	/**
	 * Determine the "best" method to use from the list of candidate methods,
	 * based on an exact match on the user's language.
	 * 
	 * TODO handle case where multiple candidate methods match on language
	 * 
	 * TODO change falling back to include all candidate methods, not just the
	 * first one (which may be the same as a null-returning exact match)
	 * 
	 * @param subject
	 * @param candidateList
	 * @return
	 */
	private static String getBestMethodMatch(Object subject, Collection<Method> candidateList) throws IllegalAccessException, InvocationTargetException {

		String retval = "bug";

		Assert.notEmpty(candidateList, "cannot be called with empty candidateList");

		Method firstMatch = null;
		Method exactMatch = null;

		// the preferred language
		// TODO locale specific language here
		String language = LocaleContextHolder.getLocale().getISO3Language();

		for (Method method : candidateList) {

			// record first matching item
			if (firstMatch == null) {
				firstMatch = method;
			}

			// get the matching annotation
			DisplayTitle annotation = method.getAnnotation(DisplayTitle.class);

			if (language.equals(annotation.language())) {
				// this is our preferred method
				exactMatch = method;
			}
		}

		// we have an exact match re: language
		if (exactMatch != null) {
			Object objectRetval = exactMatch.invoke(subject);
			if (objectRetval != null) {
				retval = objectRetval.toString();
			} else {
				// if there's an exactMatch then there will always be at least a
				// firstMatch
				objectRetval = firstMatch.invoke(subject);
				if (objectRetval != null) {
					retval = objectRetval.toString();
				} else {

					// TODO should recurse here to ignore candidate methods that
					// return null
					retval = "n/a";
				}
			}
		} else {
			// no exact match, but at least one match, because this method is
			// never called with fewer than 1 candidate methods
			return getFirstMethodValue(subject, firstMatch);
		}

		return retval;
	}

	/**
	 * Attempt to get a basic ID-based fallback value.
	 * 
	 * TODO review: this uses a generic Exception to avoid untestability
	 * 
	 * @param subject
	 * @return String
	 * @throws Exception
	 */
	private static String getFallbackIdValue(Object subject) throws Exception {

		String retval = subject.getClass().getSimpleName() + "#";

		Object objectRetval = subject.getClass().getMethod("getId").invoke(subject);
		// method exists and we were able to call it
		if (objectRetval != null) {
			// if the id is null then use it for display
			String id = objectRetval.toString();
			retval += id;
		} else {
			// otherwise, it likely means object has not yet been persisted
			retval += "new";
		}

		return retval;
	}
}
