package com.improvedoverheads;

import static net.runelite.api.HitsplatID.DAMAGE_ME;
import static net.runelite.api.HitsplatID.DAMAGE_OTHER;
import static net.runelite.api.HitsplatID.HEAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.runelite.api.Hitsplat;
import org.junit.Test;

public class OverheadsMineFilterTest
{
	@Test
	public void treatsStandardMineHitsplatsAsMine()
	{
		assertTrue(OverheadsPlugin.shouldTreatAsMine(hitsplat(DAMAGE_ME)));
	}

	@Test
	public void treatsStandardOtherHitsplatsAsNotMine()
	{
		assertFalse(OverheadsPlugin.shouldTreatAsMine(hitsplat(DAMAGE_OTHER)));
	}

	@Test
	public void treatsUnassignedHitsplatsAsNotMine()
	{
		assertFalse(OverheadsPlugin.shouldTreatAsMine(hitsplat(HEAL)));
	}

	private static Hitsplat hitsplat(int hitsplatType)
	{
		return new Hitsplat()
		{
			@Override
			public int getHitsplatType()
			{
				return hitsplatType;
			}

			@Override
			public int getAmount()
			{
				return 1;
			}

			@Override
			public int getDisappearsOnGameCycle()
			{
				return 0;
			}
		};
	}
}
