package com.oas.service;

import java.util.Collection;
import java.util.List;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.model.BaseObject;

/**
 * General services relating to the domain model.
 * 
 * @author xhalliday
 * @since November 7, 2008
 */
public interface DomainModelService extends AbstractServiceInterface {

	/**
	 * Determine the actual class of a BaseObject.
	 * 
	 * @param subject
	 * @return
	 */
	Class<? extends BaseObject> whatIs(Long subjectId);

	/**
	 * Determine the actual class of a BaseObject.
	 * 
	 * @param subject
	 * @return
	 */
	Class<? extends BaseObject> whatIs(BaseObject subject);

	// ======================================================================

	/**
	 * Set the multilingual name of the given object by the command.
	 * 
	 * @param object
	 * @param command
	 */
	void nameObject(BaseObject object, NameObjectCommand command);

	// ======================================================================

	/**
	 * Mark a domain object as deleted.
	 * 
	 * @param object
	 *            Object derived from BaseObject.
	 */
	void deleteObject(BaseObject object);

	// ======================================================================

	/**
	 * Determine if the passed object is a survey.
	 * 
	 * @param subject
	 * @return
	 */
	boolean isSurvey(BaseObject subject);

	/**
	 * Determine if the passed object is a question.
	 * 
	 * @param subject
	 * @return
	 */
	boolean isQuestion(BaseObject subject);

	/**
	 * Detemrine if the passed object is an Entry Rule.
	 * 
	 * @param subject
	 * @return
	 */
	boolean isEntryRule(BaseObject subject);

	/**
	 * Detemrine if the passed object is an Entry Rule.
	 * 
	 * @param subject
	 * @return
	 */
	boolean isExitRule(BaseObject subject);

	// ======================================================================

	/**
	 * Find any associated Object Text for the given object under the given key.
	 * May return null.
	 */
	ObjectTextCommand findObjectText(BaseObject subject, String key);

	/**
	 * Find any associated Object Text for the given object under the given key
	 * set. May return null.
	 */
	Collection<ObjectTextCommand> findObjectText(BaseObject subject, String keys[]);

	/**
	 * Set the Object Text for the given object using the given command.
	 * 
	 * @param subject
	 * @param command
	 */
	void setObjectText(BaseObject subject, ObjectTextCommand command);

	// ======================================================================

	/**
	 * Load the content from the specified URL and return it as a String. Does
	 * not handle redirects.
	 * 
	 * @param url
	 *            The absolute URL of the content to be retrieved.
	 * 
	 */
	String getContentFromURL(String url);

	// ======================================================================

	/**
	 * Load the Content for the relative URL, in the current user's language.
	 * 
	 * @param url
	 *            The relative URL to retrieve by.
	 * @return Markup or null
	 */
	byte[] getPublicContent(String url);

	/**
	 * Determine the absolute filesystem path to load a given relative URL's
	 * content, including language code and extension.
	 * 
	 * @param publicContentFilesystemPrefix
	 *            Filesystem prefix
	 * @param url
	 *            Relative URL
	 * 
	 * @return String in the form publicContentFilesystemPrefix +
	 *         FilenameUtils.normalize(url)
	 */
	String determineAbsolutePathForContent(String publicContentFilesystemPrefix, String url);

	// ======================================================================

	/**
	 * Execute the query, with results starting at
	 * <code>page</count> and having up to <code>count</code> objects.
	 * 
	 * @param hql
	 *            Hibernate query to run
	 * @param page
	 *            The page to start on.
	 * @param count
	 *            The maximum number of objects to return.
	 * 
	 * @return Collection<T> of the type clazz
	 */
	<C> List<C> getPagedList(String hql, int page, int count);

	/**
	 * Execute the query, with results starting at
	 * <code>page</count> and having up to <code>count</code> objects.
	 * 
	 * @param hql
	 *            Hibernate query to run
	 * @param page
	 *            The page to start on.
	 * @param count
	 *            The maximum number of objects to return.
	 * @param params
	 *            (Optional) Parameter list for the HQL string.
	 * 
	 * @return Collection<T> of the type clazz
	 */
	<C> List<C> getPagedList(String hql, int page, int count, Object[] params);

	// ======================================================================

	// void attachContent(Survey survey);
	//
	// Attachment getAttachment(Survey survey, String key);

	// ======================================================================

}
