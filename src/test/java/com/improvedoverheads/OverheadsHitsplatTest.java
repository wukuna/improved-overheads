package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OverheadsHitsplatTest
{
	@Test
	public void fadesInThenHoldsThenFadesOut()
	{
		OverheadsHitsplat hitsplat = new OverheadsHitsplat(0, 10, 0, 100, 100, 120, 160, 180, 0, true);

		assertEquals(0.0f, hitsplat.getAlpha(100), 0.001f);
		assertEquals(0.5f, hitsplat.getAlpha(110), 0.001f);
		assertEquals(1.0f, hitsplat.getAlpha(120), 0.001f);
		assertEquals(1.0f, hitsplat.getAlpha(150), 0.001f);
		assertEquals(0.5f, hitsplat.getAlpha(170), 0.001f);
		assertEquals(0.0f, hitsplat.getAlpha(180), 0.001f);
		assertFalse(hitsplat.isExpired(179));
		assertTrue(hitsplat.isExpired(180));
		assertFalse(hitsplat.isOutsidePositionLimit(0));
		assertFalse(hitsplat.isOutsidePositionLimit(1));

		OverheadsHitsplat secondPosition =
			new OverheadsHitsplat(0, 10, 1, 100, 100, 120, 160, 180, 1, true);
		assertTrue(secondPosition.isOutsidePositionLimit(1));
	}
}
