package com.oas.service.maintenance.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.inforealm.core.persistence.DataAccessObject;

import com.oas.AbstractOASBaseTest;
import com.oas.model.Response;
import com.oas.model.Survey;

public class ResponseMaintenanceServiceImplTest extends AbstractOASBaseTest {

	/** Service under test. */
	private ResponseMaintenanceServiceImpl impl;

	@Autowired
	@Qualifier("dataAccessObject")
	private DataAccessObject dataAccessObject;

	@Before
	public void initialize() {
		impl = new ResponseMaintenanceServiceImpl();
		impl.setDataAccessObject(dataAccessObject);
	}

	@Test
	public void getNewestPartialResponseDateToDelete_Success() {

		Date now = new Date();
		Date target = impl.getNewestPartialResponseDateToDelete();

		Calendar thirty = GregorianCalendar.getInstance();
		thirty.setTime(new Date());
		thirty.add(Calendar.MONTH, -1);
		// sanity
		assertTrue("expected date in past", thirty.getTime().before(now));

		// the real test: expect that moderately-current partial responses won't
		// be deleted by the process
		assertTrue("expected date in past", target.before(now));

		// sanity
		thirty.add(Calendar.MONTH, 140);
		assertTrue("sanity check failed", thirty.getTime().after(now));
	}

	@Test
	public void getBatchOfPartialResponsesToDelete_Success() {

		Survey survey = scenarioDataUtil.createScenario2(createTestUser());
		flushAndClear();

		int initialCount = impl.getBatchOfPartialResponsesToDelete().size();
		assertFalse("test cannot execute in database with a lot of old, partial responses",
				initialCount >= ResponseMaintenanceServiceImpl.MAXIMUM_PARTIAL_RESPONSE_BATCH_SIZE);

		// should never return recent partial responses
		{
			persist(new Response(survey, new Date(), getEnglish(), LOCALHOST_IP));
			flushAndClear();

			int secondCount = impl.getBatchOfPartialResponsesToDelete().size();
			// it's a new Response, so it should not affect the list
			assertEquals("expected same count", initialCount, secondCount);
		}

		// real test: add a really old response and expect it to be returned
		{
			Response response = new Response(survey, new Date(0), getEnglish(), LOCALHOST_IP);
			persist(response);
			flushAndClear();

			List<Response> list = impl.getBatchOfPartialResponsesToDelete();
			int thirdCount = list.size();

			// it's an OLD Response, so expect it to affect the count
			assertEquals("expected different count", initialCount + 1, thirdCount);
			assertTrue("expected old response in list", list.contains(response));
		}
	}
}
