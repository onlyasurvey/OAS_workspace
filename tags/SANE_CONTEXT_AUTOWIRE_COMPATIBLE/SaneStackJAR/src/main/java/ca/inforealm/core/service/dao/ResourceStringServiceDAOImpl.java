package ca.inforealm.core.service.dao;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import ca.inforealm.core.SaneContext;
import ca.inforealm.core.model.ResourceString;
import ca.inforealm.core.persistence.impl.DataAccessObjectImpl;

@Repository
public class ResourceStringServiceDAOImpl extends DataAccessObjectImpl implements ResourceStringServiceDAO {

	@Autowired
	private SaneContext saneContext;

	// ======================================================================

	/**
	 * Query to retrieve all resource strings. This uses the
	 * application.identifier property to allow the query to execute even if the
	 * SANE context is not fully initialized, ie., during Spring's
	 * ApplicationContext startup.
	 */
	private static final String QUERY_ALL = "from ResourceString where application.identifier = ?";

	/**
	 * Query to retrieve all resource strings that have changed since the date
	 * specified.
	 */
	private static final String QUERY_BASELINE_DATE = "from ResourceString where application.identifier = ? AND lastModifiedDate > ?";

	/* (non-Javadoc)
	 * @see ca.inforealm.core.service.dao.ResourceStringServiceDAO#loadAllResources()
	 */
	public Collection<ResourceString> loadAllResources() {

		Assert.notNull(saneContext, "no SANE context is set");
		Assert.notNull(saneContext.getApplicationIdentifier(), "no application identifier is set in SANE context");

		// get all data for the application
		return find(QUERY_ALL, saneContext.getApplicationIdentifier());
	}

	/* (non-Javadoc)
	 * @see ca.inforealm.core.service.dao.ResourceStringServiceDAO#loadNewerResources(java.util.Date)
	 */
	public Collection<ResourceString> loadNewerResources(Date baselineDate) {

		Assert.notNull(saneContext, "no SANE context is set");
		Assert.notNull(saneContext.getApplicationIdentifier(), "no application identifier is set in SANE context");
		Assert.notNull(baselineDate, "no baseline date specified");

		// 
		Object[] params = new Object[] { saneContext.getApplicationIdentifier(), baselineDate };
		return find(QUERY_BASELINE_DATE, params);
	}

	// ======================================================================

	// /**
	// * @param saneContext
	// * the saneContext to set
	// */
	// @Autowired
	// public void setSaneContext(SaneContext saneContext) {
	// this.saneContext = saneContext;
	// }
}
