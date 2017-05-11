package ca.inforealm.core;

import static junit.framework.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.util.Assert;

import ca.inforealm.core.model.Actor;
import ca.inforealm.core.model.ActorGroup;
import ca.inforealm.core.model.ActorRole;
import ca.inforealm.core.model.Application;
import ca.inforealm.core.model.ConfigurationItem;
import ca.inforealm.core.model.PreferenceDefinition;
import ca.inforealm.core.model.PreferenceValue;
import ca.inforealm.core.model.ResourceString;
import ca.inforealm.core.model.RoleDefinition;
import ca.inforealm.core.model.UserAccount;
import ca.inforealm.core.persistence.DataAccessObject;
import ca.inforealm.core.security.RealUserWrapper;
import ca.inforealm.core.service.ConfigurationService;
import ca.inforealm.core.service.PreferenceService;
import ca.inforealm.core.service.ResourceStringService;
import ca.inforealm.core.service.RoleService;
import ca.inforealm.core.util.PasswordUtils;

@ContextConfiguration(inheritLocations = false, locations = { "classpath:/applicationContext-sane-core-test.xml",
		"classpath:/applicationContext-sane.xml" })
abstract public class AbstractBaseTest extends AbstractTransactionalJUnit4SpringContextTests implements DataAccessObject {

	protected Logger log = Logger.getLogger(this.getClass());

	private long mbun = 0;

	public static final String NAME_EN = "someNameEn";
	public static final String NAME_FR = "someNameFr";

	public static final String OFFLINE_FOR_MAINTENANCE = "offlineForMaintenance";

	public static final String ROLE_USER = "ROLE_USER";

	/**
	 * Determines if flushAndClear() is called when tearing down a transaction
	 * for a test. Useful if your test intentially causes a database error which
	 * would prevent anything from being flush()'d later.
	 * 
	 * Call expectInvalidTransactionalState() to turn this on.
	 */
	private boolean expectInvalidTransactionalState = false;

	@Autowired
	@Qualifier("saneContext")
	private SaneContext saneContext;

	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private PreferenceService preferenceService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired(required = false)
	private ResourceStringService resourceStringService;

	@Autowired
	@Qualifier("authenticationProvider")
	private AuthenticationProvider authenticationProvider;

	// ======================================================================

	protected void expectInvalidTransactionalState() {
		expectInvalidTransactionalState = true;
	}

	@Before
	public void onSetUpInTransaction() throws Exception {

		// set locale context
		setEnglish();
	}

	@After
	public void onTearDownInTransaction() throws Exception {

		// no persistence context
		if (expectInvalidTransactionalState) {
			getHibernateTemplate().clear();
		} else {
			flushAndClear();
		}

		// no security context
		clearSecurityContext();
	}

	protected void flushAndClear() {
		getHibernateTemplate().flush();
		getHibernateTemplate().clear();
	}

	protected void setEnglish() {
		LocaleContextHolder.setLocale(Locale.CANADA);
	}

	protected void setFrench() {
		LocaleContextHolder.setLocale(Locale.CANADA_FRENCH);
	}

	public SaneContext getSaneContext() {
		return saneContext;
	}

	protected long getMBUN() {
		return ++mbun;
	}

	// ======================================================================
	public DataAccessObject getDAO() {
		return dataAccessObject;
	}

	// ======================================================================

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	protected PreferenceService getPreferenceService() {
		return preferenceService;
	}

	protected RoleService getRoleService() {
		return roleService;
	}

	// ======================================================================
	public UserAccount createTestUser() {
		UserAccount retval = new UserAccount();

		retval.setUsername("testUser" + getMBUN());
		retval.setEmail("testEmailAddress@host" + getMBUN() + ".nonTld");
		retval.setMd5password(PasswordUtils.encode("password"));

		getHibernateTemplate().persist(retval);

		assertNotNull("newly persisted user has no ID", retval.getId());

		return retval;
	}

	public ActorGroup createTestActorGroup(Application application) {
		ActorGroup retval = new ActorGroup();

		retval.setApplication(application);
		retval.setNameEn("nameEn#" + getMBUN());
		retval.setNameFr("nameFr#" + getMBUN());

		getHibernateTemplate().persist(retval);

		assertNotNull("newly persisted actor group has no ID", retval.getId());

		return retval;
	}

	public Application createTestApplication(String identifier) {
		return createTestApplication(identifier, null, null);
	}

	public Application createTestApplication(String identifier, String nameEn, String nameFr) {
		Application retval = new Application();
		retval.setIdentifier(identifier);
		retval.setNameEn(nameEn);
		retval.setNameFr(nameFr);

		getHibernateTemplate().persist(retval);
		return retval;
	}

	// ----------------------------------------------------------------------

	/**
	 */
	protected void createSecureContext(String username) {

		try {
			// ApplicationContext context =
			// this.loadContextLocations(getConfigLocations());

			Authentication auth = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username,
					"password"));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	protected boolean isSecureContext() {
		return SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null;
	}

	protected void clearSecurityContext() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	protected void assignRole(Actor actor, String roleIdentifier) {
		assignRole(actor, roleIdentifier, getSaneContext().getApplicationModel());
	}

	protected void assignRole(Actor actor, String roleIdentifier, Application application) {
		RoleDefinition role = (RoleDefinition) getHibernateTemplate().find(
				"from RoleDefinition where identifier = ? and application = ?", new Object[] { roleIdentifier, application })
				.get(0);
		ActorRole ar = new ActorRole(actor, role);
		getHibernateTemplate().persist(ar);
	}

	protected UserAccount createAndSetSecureUser() {
		return createAndSetSecureUser((String[]) null, null);
	}

	protected UserAccount createAndSetSecureUserWithRoleUser() {
		Collection<RoleDefinition> roles = new ArrayList<RoleDefinition>();
		roles.add(getSaneContext().getRoleDefinition(ROLE_USER));
		return createAndSetSecureUser(roles, null);
	}

	protected UserAccount createAndSetSecureUser(String role) {
		return createAndSetSecureUser(new String[] { role }, null);
	}

	protected UserAccount createAndSetSecureUser(String[] roles) {
		return createAndSetSecureUser(roles, null);
	}

	protected UserAccount createAndSetSecureUser(String[] roles, Collection<PreferenceValue> preferences) {

		Collection<RoleDefinition> roleList = null;
		if (roles != null) {
			roleList = new ArrayList<RoleDefinition>(roles.length);
			for (String role : roles) {
				roleList.add(getSaneContext().getRoleDefinition(role));
			}
		}
		return createAndSetSecureUser(roleList, preferences);
	}

	protected UserAccount createAndSetSecureUser(Collection<RoleDefinition> roles, Collection<PreferenceValue> preferences) {
		UserAccount retval = createTestUser();

		// link new actor to roles
		if (roles != null) {
			for (RoleDefinition role : roles) {
				ActorRole ar = new ActorRole(retval, role);
				getHibernateTemplate().persist(ar);
			}
		}

		// update preferences to set the actor, as we just created it
		if (preferences != null) {
			for (PreferenceValue pref : preferences) {
				pref.setActor(retval);
				if (pref.getId() == null) {
					// not yet stored
					getHibernateTemplate().persist(pref);
				} else {
					// any changes will be persisted by flush()
				}
			}
		}

		getHibernateTemplate().flush();

		createSecureContext(retval.getUsername());

		return retval;
	}

	/**
	 * Get the current user from the security context; this returns the actual
	 * UserAccount in use.
	 * 
	 * @return
	 */
	public UserAccount getCurrentUser() {
		UserAccount retval = null;

		// get the auth object
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Assert.notNull(auth, "no security context exists");

		// extract the principal, which is our user type
		retval = ((RealUserWrapper) auth.getPrincipal()).getRealUser();
		Assert.notNull(retval, "no principal or real user in context");

		// done
		return retval;
	}

	// ----------------------------------------------------------------------

	protected PreferenceDefinition createPreferenceDefinition(String identifier) {
		PreferenceDefinition retval = new PreferenceDefinition();
		retval.setApplication(getSaneContext().getApplicationModel());
		retval.setNameEn(identifier + ".en");
		retval.setNameFr(identifier + ".fr");
		retval.setIdentifier(identifier);
		getHibernateTemplate().persist(retval);

		return retval;
	}

	protected RoleDefinition createRoleDefinition(String identifier) {
		return createRoleDefinition(identifier, getSaneContext().getApplicationModel());

	}

	protected RoleDefinition createRoleDefinition(String identifier, Application application) {
		RoleDefinition retval = new RoleDefinition();
		retval.setApplication(application);
		retval.setIdentifier(identifier);
		getHibernateTemplate().persist(retval);

		return retval;
	}

	protected void setPreference(String identifier, String value) {
		getPreferenceService().setPreference(identifier, value);
	}

	protected Application createModelScenario1() {

		Application application = new Application();
		application.setIdentifier("scenario1testApplication" + getMBUN() + 55);
		application.setStartUrl("http://localhost/someApp");

		//
		// configuration items
		//
		ConfigurationItem item1 = new ConfigurationItem(application, OFFLINE_FOR_MAINTENANCE, "boolean", "false");
		application.getConfigurationItems().add(item1);

		//
		// roles
		//
		RoleDefinition userRole = new RoleDefinition(application, "ROLE_USER");
		application.getRoleDefinitions().add(userRole);
		application.getRoleDefinitions().add(new RoleDefinition(application, "ROLE_APP_ADMIN"));

		//
		// preferences
		//
		PreferenceDefinition pd1 = new PreferenceDefinition();
		pd1.setApplication(application);
		pd1.setIdentifier("fancyAjax");
		pd1.setNameEn(NAME_EN);
		pd1.setNameFr(NAME_FR);
		application.getPreferenceDefinitions().add(pd1);

		//
		// actor group
		//
		ActorGroup actorGroup = new ActorGroup();
		actorGroup.setApplication(application);
		actorGroup.setNameEn("actorGroupEn");
		actorGroup.setNameFr("actorGroupFr");
		application.getActorGroups().add(actorGroup);

		// store it
		getHibernateTemplate().persist(application);

		// ===================

		// add an actor role mapping
		UserAccount account = createTestUser();
		ActorRole ar = new ActorRole();
		ar.setRole(userRole);
		ar.setActor(account);
		getHibernateTemplate().persist(ar);

		// add a preference value
		PreferenceValue pv = new PreferenceValue(pd1, account, "true");
		getHibernateTemplate().persist(pv);

		//
		// resource strings
		//
		getHibernateTemplate().persist(
				new ResourceString(application, "app.name", application.getNameEn(), application.getNameFr()));
		getHibernateTemplate().flush();

		return application;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * @return the resourceStringService
	 */
	public ResourceStringService getResourceStringService() {
		return resourceStringService;
	}

	// ======================================================================
	// ======================================================================
	// convenience access to the DAO
	// ======================================================================

	@Override
	public HibernateTemplate getHibernateTemplate() {
		return dataAccessObject.getHibernateTemplate();
	}

	@Override
	public void delete(Object entity) {

		dataAccessObject.delete(entity);
	}

	@Override
	public <E> List<E> find(String queryString) {

		return dataAccessObject.find(queryString);
	}

	@Override
	public <E> List<E> find(String queryString, Object value) {

		return dataAccessObject.find(queryString, value);
	}

	@Override
	public <E> List<E> find(String queryString, Object[] values) {

		return dataAccessObject.find(queryString, values);
	}

	@Override
	public <C> C get(Class<C> clazz, Serializable id) {

		return dataAccessObject.get(clazz, id);
	}

	@Override
	public <C> C load(Class<C> clazz, Serializable id) {

		return dataAccessObject.load(clazz, id);
	}

	@Override
	public void persist(Object entity) {

		dataAccessObject.persist(entity);
	}

	@Override
	public Object execute(HibernateCallback action) {

		return getHibernateTemplate().execute(action);
	}

	public <T> T unique(Collection<T> list) {

		Assert.notNull(list);

		switch (list.size()) {
		case 0:
			return null;
		case 1:
			return list.iterator().next();
		default:
			throw new IllegalArgumentException("passed collection has more than one element");
		}
	}

}
