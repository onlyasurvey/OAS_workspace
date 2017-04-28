package ca.inforealm.core.service.impl.support;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import ca.inforealm.core.AbstractBaseTest;
import ca.inforealm.core.model.ResourceString;

/**
 * Test for AbstractServiceImpl
 * 
 * @author Jason Mroz
 */
public class ResourceStringServiceSupportTest extends AbstractBaseTest {

	// ======================================================================

	// ======================================================================

	@Test
	public void testFindNewestResourceString_Pass() {
		Collection<ResourceString> list = new ArrayList<ResourceString>();
		Date expectedDate = new Date();
		Date olderDate = new Date(expectedDate.getTime() - 1L);
		{
			ResourceString str = new ResourceString(null, "str1", "en", "fr");
			str.setLastModifiedDate(expectedDate);
			list.add(str);
		}
		{
			ResourceString str = new ResourceString(null, "str2", "en", "fr");
			str.setLastModifiedDate(olderDate);
			list.add(str);
		}
		Date returnedDate = ResourceStringServiceSupport.findNewestResourceString(list);
		assertNotNull("should not return null", returnedDate);
		assertEquals("should have expected date", expectedDate, returnedDate);
	}

	@Test
	public void testFindNewestResourceString_ReturnsNullOnEmptyInput() {
		Collection<ResourceString> list = new ArrayList<ResourceString>();
		Date date = ResourceStringServiceSupport.findNewestResourceString(list);
		assertNull("should return null on empty imput", date);
	}

	@Test
	public void testFindNewestResourceString_ThrowsOnNullInput() {
		try {
			ResourceStringServiceSupport.findNewestResourceString(null);
			fail("should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	// ======================================================================
}
