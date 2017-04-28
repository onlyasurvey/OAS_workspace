package com.oas.model.enterprise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class ResponseStatSetTest {

	@Test
	public void subtract() {

		ResponseStatSet set1 = new ResponseStatSet(10l, 15l, 20l, 50l, 30l);
		ResponseStatSet set2 = new ResponseStatSet(5l, 10l, 15l, 25l, 5l);
		ResponseStatSet expected = new ResponseStatSet(5l, 5l, 5l, 25l, 25l);

		assertEquals("subtract returned unexpected value", expected, set1.subtract(set2));
	}

	@Test
	public void testEquals() {

		ResponseStatSet set1 = new ResponseStatSet(10l, 15l, 20l, 50l, 30l);
		ResponseStatSet set2 = new ResponseStatSet(10l, 15l, 20l, 50l, 30l);
		ResponseStatSet set9 = new ResponseStatSet(05l, 05l, 05l, 50l, 05l);
		ResponseStatSet emptySet = new ResponseStatSet();

		assertEquals("equals failed", set1, set2);
		assertFalse("equals failed", set1.equals(set9));
		assertFalse("equals failed", set1.equals(emptySet));
		assertFalse("equals failed", set1.equals("I'm a happy penguin!"));
		assertFalse("equals failed", set1.equals(null));
	}

	@Test
	public void testHashCode() {

		ResponseStatSet set1 = new ResponseStatSet(10l, 15l, 20l, 50l, 30l);
		ResponseStatSet set2 = new ResponseStatSet(10l, 15l, 20l, 50l, 30l);
		ResponseStatSet set9 = new ResponseStatSet(5l, 5l, 5l, 50l, 5l);

		assertEquals("hashCode failed", set1.hashCode(), set2.hashCode());
		assertFalse("hashCode failed", set1.hashCode() == set9.hashCode());
		assertFalse("hashCode failed", set1.hashCode() == "I'm a happy penguin!".hashCode());
	}

}
