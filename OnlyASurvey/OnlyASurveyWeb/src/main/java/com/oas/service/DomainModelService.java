package com.oas.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import ca.inforealm.core.service.AbstractServiceInterface;

import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.model.Attachment;
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
	 * Get the hostname to use for public purposes - invites, reminders,
	 * respondent links, password changes, etc.
	 * 
	 * @return URL prefix
	 */
	String getPublicHostname();

	/**
	 * Returns the prefix for the "short URL" feature, if config item
	 * "shortUrlPrefix" has a value.
	 * 
	 * @return URL prefix
	 */
	String getShortUrlPrefix();

	// ======================================================================

	/**
	 * Format a Date according to the HTTP spec.
	 */
	String formatHttpDate(Date date);

	// ======================================================================

	/**
	 * Add a "far-future-expires" header - ie, marking the given response as
	 * expiring in 10 years or so.
	 */
	void addFarFutureExpiresHeader(HttpServletResponse response);

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
	 * Add or update a user-supplied attachment to any object. May already
	 * exist. Asserts ownership of a containing Survey therefore only applies to
	 * BaseObject's that can resolve such a container (currently: Survey,
	 * Question, Choice).
	 * 
	 * @param object
	 *            The object to attach to.
	 * @param attachment
	 *            Complete attachment details.
	 */
	Attachment attachUpload(BaseObject object, Attachment attachment);

	/**
	 * Return all attachments to the specified object.
	 * 
	 * @param object
	 * @return List<Attachment>
	 */
	List<Attachment> findAttachments(BaseObject object);

	/**
	 * Return attachments to the specified object of the specified type.
	 * 
	 * @param object
	 * @return List<Attachment>
	 */
	List<Attachment> findAttachments(BaseObject object, Attachment.AttachmentType type);

	// /**
	// * Get attachment payto the specified object of the specified type.
	// *
	// * @param object
	// * @param type
	// *
	// * @return Map keyed by SupportedLanguage containing AttachmentPayload's
	// */
	// Map<SupportedLanguage, AttachmentPayload> countAttachments(BaseObject
	// object, AttachmentType type);

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
