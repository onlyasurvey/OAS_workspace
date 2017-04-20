package com.oas.model.enterprise;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.oas.AbstractOASBaseTest;

public class QuickStatsTest extends AbstractOASBaseTest {

	@Test
	public void typical() {

		QuickStats stats = (QuickStats) unique(find("from QuickStats"));
		assertNotNull(stats);

		// TODO rest here

	}
}
