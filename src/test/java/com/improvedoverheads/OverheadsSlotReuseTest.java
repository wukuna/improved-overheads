package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class OverheadsSlotReuseTest
{
	@Test
	public void reuseWindowRoundsUpToWholeGameCycles()
	{
		assertEquals(28, OverheadsPlugin.reuseWindowCycles(550));
		assertEquals(28, OverheadsPlugin.reuseWindowCycles(560));
		assertEquals(29, OverheadsPlugin.reuseWindowCycles(561));
		assertEquals(1, OverheadsPlugin.reuseWindowCycles(0));
	}

	@Test
	public void hitBecomesReusableAtExactBoundary()
	{
		OverheadsHitsplat old = hitsplat(4, 100, 0, false);

		assertNull(OverheadsPlugin.findTimedReuseVictim(
			Arrays.asList(old), 127, 28, false, false));
		assertSame(old, OverheadsPlugin.findTimedReuseVictim(
			Arrays.asList(old), 128, 28, false, false));
		assertEquals(4, old.getPosition());
	}

	@Test
	public void sameWindowHitsDoNotReplaceEachOther()
	{
		List<OverheadsHitsplat> hitsplats = new ArrayList<>();
		OverheadsHitsplat old = hitsplat(0, 100, 0, false);
		hitsplats.add(old);

		OverheadsHitsplat victim = OverheadsPlugin.findTimedReuseVictim(
			hitsplats, 128, 28, false, false);
		assertSame(old, victim);
		hitsplats.remove(victim);
		hitsplats.add(hitsplat(0, 128, 1, false));

		assertNull(OverheadsPlugin.findTimedReuseVictim(
			hitsplats, 128, 28, false, false));
	}

	@Test
	public void oldestVisibleAgeWinsThenSequenceBreaksTies()
	{
		OverheadsHitsplat firstSequence = hitsplat(0, 100, 0, false);
		OverheadsHitsplat olderDisplay = hitsplat(1, 90, 1, false);
		OverheadsHitsplat laterSequence = hitsplat(2, 100, 2, false);

		assertSame(olderDisplay, OverheadsPlugin.findTimedReuseVictim(
			Arrays.asList(firstSequence, olderDisplay, laterSequence), 130, 28, false, false));
		assertSame(firstSequence, OverheadsPlugin.findTimedReuseVictim(
			Arrays.asList(laterSequence, firstSequence), 130, 28, false, false));
	}

	@Test
	public void prioritizedMineChoosesEligibleNonMineFirst()
	{
		OverheadsHitsplat olderMine = hitsplat(0, 80, 0, true);
		OverheadsHitsplat nonMine = hitsplat(1, 90, 1, false);
		List<OverheadsHitsplat> hitsplats = Arrays.asList(olderMine, nonMine);

		assertSame(nonMine, OverheadsPlugin.findTimedReuseVictim(
			hitsplats, 130, 28, true, true));
		assertSame(olderMine, OverheadsPlugin.findTimedReuseVictim(
			hitsplats, 130, 28, true, false));
		assertSame(olderMine, OverheadsPlugin.findTimedReuseVictim(
			hitsplats, 130, 28, false, true));
	}

	@Test
	public void delayedAppearanceStartsReuseAgeLaterThanEvent()
	{
		assertEquals(140, OverheadsPlugin.reuseAgeStartsOnGameCycle(100, 140));
		assertEquals(140, OverheadsPlugin.reuseAgeStartsOnGameCycle(140, 100));
	}

	private static OverheadsHitsplat hitsplat(
		int position,
		int reuseAgeStartsOnGameCycle,
		long sequence,
		boolean mine)
	{
		return new OverheadsHitsplat(
			0,
			10,
			position,
			reuseAgeStartsOnGameCycle,
			reuseAgeStartsOnGameCycle,
			reuseAgeStartsOnGameCycle,
			reuseAgeStartsOnGameCycle + 100,
			reuseAgeStartsOnGameCycle + 200,
			sequence,
			mine);
	}
}
