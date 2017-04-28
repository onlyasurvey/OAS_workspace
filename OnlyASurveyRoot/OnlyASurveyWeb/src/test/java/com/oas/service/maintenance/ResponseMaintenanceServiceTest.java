package com.oas.service.maintenance;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Response;
import com.oas.model.Survey;

public class ResponseMaintenanceServiceTest extends AbstractOASBaseTest {

	/** Service under test. */
	@Autowired
	private ResponseMaintenanceService service;

	/**
	 * Add a really old response and expect it to be deleted.
	 */
	@Test
	public void getBatchOfPartialResponsesToDelete_Success() {

		// this is a bit of a cheat, but it's possible that other responses in
		// the test database would affect this test
		getHibernateTemplate().bulkUpdate("update Response r set r.closed = true");

		Survey survey = scenarioDataUtil.createScenario2(createTestUser());

		Response response = new Response(survey, new Date(0), getEnglish(), LOCALHOST_IP);
		persist(response);
		flushAndClear();

		long initialCount = countBaseObjects();

		// invoke the service
		service.cleanUpPartialResponses();

		long newCount = countBaseObjects();

		//
		assertEquals("expected one less base object", initialCount - 1, newCount);
	}
}
