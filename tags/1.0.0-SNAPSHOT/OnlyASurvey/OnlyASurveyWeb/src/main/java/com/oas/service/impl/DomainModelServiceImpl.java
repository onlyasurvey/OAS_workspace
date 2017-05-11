package com.oas.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.security.annotation.Unsecured;
import ca.inforealm.core.security.annotation.ValidUser;
import ca.inforealm.core.service.ConfigurationService;
import ca.inforealm.core.service.impl.AbstractServiceImpl;

import com.oas.command.model.NameObjectCommand;
import com.oas.command.model.ObjectTextCommand;
import com.oas.model.BaseObject;
import com.oas.model.Choice;
import com.oas.model.ObjectName;
import com.oas.model.ObjectResource;
import com.oas.model.Question;
import com.oas.model.SupportedLanguage;
import com.oas.model.Survey;
import com.oas.model.question.rules.EntryRule;
import com.oas.model.question.rules.ExitRule;
import com.oas.service.DomainModelService;
import com.oas.service.SupportedLanguageService;
import com.oas.util.Constants;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class DomainModelServiceImpl extends AbstractServiceImpl implements DomainModelService {

	/** i18n. */
	@Autowired
	private SupportedLanguageService supportedLanguageService;

	/**
	 * Maps class hashCode values to the classes themselves, for lookup later.
	 */
	private final Map<Integer, Class<? extends BaseObject>> typeHashCodeMap = new HashMap<Integer, Class<? extends BaseObject>>();

	@Autowired
	private ConfigurationService configurationService;

	// ======================================================================

	/**
	 * Initialize the member typeHashCodeMap, populating it's keys with various
	 * domain model class hashcodes and mapping them to the classes themselves.
	 * 
	 * TODO review this
	 * 
	 */
	@SuppressWarnings("unused")
	@PostConstruct
	private void initializeTypeHashCodeMap() {
		typeHashCodeMap.put(Survey.class.hashCode(), Survey.class);
		typeHashCodeMap.put(Question.class.hashCode(), Question.class);
		typeHashCodeMap.put(Choice.class.hashCode(), Choice.class);
	}

	// ======================================================================

	@Override
	@ValidUser
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void deleteObject(BaseObject object) {

		log.info("object " + object.getId() + " deleted by user#" + getCurrentUser().getId());

		object.setDateDeleted(new Date());
		object.setDeleted(true);
		persist(object);
	}

	// ======================================================================

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Class<? extends BaseObject> whatIs(BaseObject subject) {
		Class<? extends BaseObject> retval = null;

		Integer hashCode = subject.getClass().hashCode();
		retval = typeHashCodeMap.get(hashCode);

		return retval;
	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Class<? extends BaseObject> whatIs(Long subjectId) {
		Assert.notNull(subjectId);
		// TODO potentially inefficient
		BaseObject subject = load(BaseObject.class, subjectId);
		return whatIs(subject);
	}

	// ======================================================================

	@Override
	@Unsecured
	public boolean isSurvey(BaseObject subject) {
		Assert.notNull(subject);
		return (Survey.class.isAssignableFrom(subject.getClass()));
	}

	@Override
	@Unsecured
	public boolean isQuestion(BaseObject subject) {
		Assert.notNull(subject);
		return (Question.class.isAssignableFrom(subject.getClass()));
	}

	@Override
	@Unsecured
	public boolean isEntryRule(BaseObject subject) {
		Assert.notNull(subject);
		return (EntryRule.class.isAssignableFrom(subject.getClass()));
	}

	@Override
	@Unsecured
	public boolean isExitRule(BaseObject subject) {
		Assert.notNull(subject);
		return (ExitRule.class.isAssignableFrom(subject.getClass()));
	}

	// ======================================================================

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public ObjectTextCommand findObjectText(BaseObject subject, String key) {

		Collection<ObjectResource> list = find("from ObjectResource r where r.object = ? and r.key = ?", new Object[] { subject,
				key });
		ObjectTextCommand retval = null;

		for (ObjectResource resource : list) {
			// instantiate this lazily and only when there's at least one value,
			// otherwise we return the default (null) below indicating no such
			// text.
			if (retval == null) {
				retval = new ObjectTextCommand(key, new HashMap<String, String>(list.size()));
			}
			retval.addName(resource.getSupportedLanguage().getIso3Lang(), resource.getValue());
		}

		return retval;
	}

	/**
	 * TODO: optimize, this currently calls findObjectText(BaseObject, String)
	 * repeatedly rather than use an IN
	 */
	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Collection<ObjectTextCommand> findObjectText(BaseObject subject, String[] keys) {
		Collection<ObjectTextCommand> retval = new ArrayList<ObjectTextCommand>();
		for (String key : keys) {
			retval.add(findObjectText(subject, key));
		}
		return retval;
	}

	/**
	 * TODO: optimize, this currently deletes and re-adds
	 */
	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void setObjectText(BaseObject subject, ObjectTextCommand command) {

		Assert.notNull(subject);
		Assert.notNull(command);

		String key = command.getKey();
		Assert.hasText(key);

		for (String languageCode : command.getMap().keySet()) {

			// the language code
			Assert.notNull(languageCode);
			SupportedLanguage language = supportedLanguageService.findByCode(languageCode);

			// new value
			String value = command.getMap().get(languageCode);

			ObjectResource target = null;
			for (ObjectResource resource : subject.getObjectResources()) {
				if (key.equals(resource.getKey()) && resource.getSupportedLanguage().getId().equals(language.getId())) {
					target = resource;
					break;
				}
			}

			if (target == null) {
				// no existing resource
				if (value != null) {
					// only add non-null values
					ObjectResource newObjectResource = new ObjectResource(subject, language, key, value);
					subject.getObjectResources().add(newObjectResource);
					persist(newObjectResource);
				}
			} else {
				if (value != null) {
					// update existing resource
					target.setValue(value);
					persist(target);
				} else {
					// delete existing resource
					subject.getObjectResources().remove(target);
					delete(target);
				}
			}
		}

	}

	@Override
	@Unsecured
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void nameObject(BaseObject object, NameObjectCommand command) {

		for (String languageCode : command.getMap().keySet()) {
			String value = command.getMap().get(languageCode);
			SupportedLanguage language = supportedLanguageService.findByCode(languageCode);
			Assert.notNull(language);

			ObjectName target = null;
			for (ObjectName name : object.getObjectNames()) {
				if (name.getLanguage().getId().equals(language.getId())) {
					target = name;
					break;
				}
			}

			if (target == null) {
				// no existing name
				if (StringUtils.hasText(value)) {
					// only add non-null values

					ObjectName newObjectName = new ObjectName(object, language, value);

					// add to object but only persist name
					object.getObjectNames().add(newObjectName);

					// save it
					persist(newObjectName);
				}
			} else {
				if (StringUtils.hasText(value)) {
					// update existing name
					target.setValue(value);
					persist(target);
				} else {
					// delete existing name
					// remove from object but only delete object name itself
					object.getObjectNames().remove(target);
					delete(target);
					// target.setObject(null);
				}
			}

			// object.setObjectName(language, value);
		}

		// persist just the collection, otherwise half the database will get
		// loaded
		// persist(object.getObjectNames());
		// persist(object);
	}

	// ======================================================================

	// ======================================================================

	@Override
	@Unsecured
	public String getContentFromURL(String url) {
		String retval = "BUG";
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);

		ResponseHandler<String> handler = new BasicResponseHandler();

		try {
			// go
			retval = client.execute(get, handler);

		} catch (ClientProtocolException e) {
			log.error("unable to execute GET for URL", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("unable to execute GET for URL", e);
			throw new RuntimeException(e);
		}

		return retval;
	}

	// ======================================================================

	@Override
	@Unsecured
	public byte[] getPublicContent(String url) {

		String publicContentFilesystemPrefix = Constants.DEFAULT_CONTENT_FILESYSTEM_PREFIX;
		ConfigurationItem config = configurationService.getConfigurationItem("publicContentFilesystemPrefix");
		if (config != null && StringUtils.hasText(config.getValue())) {
			publicContentFilesystemPrefix = config.getValue();
		}

		Assert.hasText(publicContentFilesystemPrefix, "no prefix for loading public site content [1]");

		FileInputStream reader = null;

		String filename = determineAbsolutePathForContent(publicContentFilesystemPrefix, url);
		File file = new File(filename);

		try {

			// if it's not in English, try that language first, assuming the URL
			// is a .html URL
			String languageCode = LocaleContextHolder.getLocale().getISO3Language();
			if (!file.exists()) {
				if (url.endsWith(".html") && (!"eng".equals(languageCode))) {
					filename = filename.replace("." + languageCode + ".html", ".eng.html");
					file = new File(filename);
				}
			}
			if (!file.exists()) {
				return null;
			}
			reader = new FileInputStream(file);

			byte[] cbuf = new byte[(int) file.length()];

			reader.read(cbuf);
			return cbuf;

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException verboseExceptions) {
					// can't fail much more than this
				}
			}
		}

	}

	@Override
	@Unsecured
	public String determineAbsolutePathForContent(String publicContentFilesystemPrefix, String url) {

		// 1. normalize the URL to translate ".." and such
		// - does not allow "../.." to mess with our prefix
		// 2. normalize the entire string + "/" to avoid double slashes
		String retval = FilenameUtils.normalize(publicContentFilesystemPrefix + "/" + FilenameUtils.normalize(url));

		// extension
		String extension = FilenameUtils.getExtension(retval);

		// strip extension
		retval = retval.substring(0, retval.length() - extension.length());

		// re-normalize to use system's path separator
		retval = FilenameUtils.separatorsToSystem(retval);

		// add language (retval will still have trailing "." at this point
		if ("html".equals(extension)) {
			String languageCode = LocaleContextHolder.getLocale().getISO3Language();
			retval += languageCode + ".";
		}

		// finally re-add extension
		retval += extension;

		return retval;
	}

	// ======================================================================

	@Override
	@Unsecured
	public <C> List<C> getPagedList(final String hql, int page, int count) {
		return getPagedList(hql, page, count, null);
	}

	@Override
	@Unsecured
	public <C> List<C> getPagedList(final String hql, int page, int count, Object[] params) {

		Query query = (Query) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(hql);
			}
		});

		// offset and length
		query.setFirstResult(page * count);
		query.setMaxResults(count);

		// set all parameters, if any
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				query.setParameter(i, params[i]);
			}
		}

		//
		@SuppressWarnings("unchecked")
		List<C> retval = query.list();

		// can't possibly happen: at least the current user exists
		Assert.notNull(retval, "no return value");

		//
		return retval;
	}

}
