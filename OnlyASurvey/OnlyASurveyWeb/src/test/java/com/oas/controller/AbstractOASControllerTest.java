package com.oas.controller;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.oas.AbstractOASBaseTest;
import com.oas.service.DomainModelService;

public class AbstractOASControllerTest extends AbstractOASBaseTest {

	@Autowired
	private DomainModelService domainModelService;

	private class TestController extends AbstractOASController {

	}

	/**
	 * The parent class implements referenceData specifically so that tests in
	 * the same package can invoke and test the method on subclasses of it.
	 * Since it's required, it's purpose is documented and tested here.
	 * 
	 * NOTE the test throws Exception because referenceData()'s contract
	 * requires it.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReferenceDataVisibility() throws Exception {

		TestController controller = new TestController();

		// this should never fail but the contract requires Exception to be
		// caught
		Map map = controller.referenceData(new MockHttpServletRequest());

		assertNull("default referenceData should never have data", map);
	}

	@Test
	public void testDetermineAbsoluteUrlPrefix() {

		String publicHostname = domainModelService.getPublicHostname();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContextPath("/oas");

		TestController controller = new TestController();
		controller.domainModelService = domainModelService;

		String url = controller.determineAbsoluteUrlPrefix(request);
		assertEquals("wrong URL generated", "http://" + publicHostname + "/oas", url);
	}
}
