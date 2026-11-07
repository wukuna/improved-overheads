package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.runelite.client.config.FontType;
import org.junit.Test;

public class OverheadsSettingsTest
{
	@Test
	public void defaultGeneralSettingsAllowReplacementHits()
	{
		OverheadsConfig config = new OverheadsConfig()
		{
		};

		assertFalse(config.disableEnemyHitsplats());
		assertFalse(config.disableAllyHitsplats());
		assertFalse(config.disableMyHitsplats());
		assertEquals(16, config.maxHitsplats());
		assertFalse(config.reuseOldHitsplatSlots());
		assertEquals(550, config.hitsplatReuseInterval());
		assertEquals(100, config.opacityPercent());
		assertSame(OverheadsTargetDetection.EITHER, config.largeTargetDetection());
		assertEquals(2, config.largeTargetSize());
		assertEquals(100, config.largeTargetHealthScale());
		assertEquals(100, config.hitsplatScalePercent());
	}

	@Test
	public void settingsUseStoredDisplayValues()
	{
		MutableConfig config = new MutableConfig();
		config.onlyMine = true;
		config.maxRadius = 64;
		OverheadsSettings settings = new OverheadsSettings(config);

		assertTrue(settings.onlyDisplayMine());
		assertSame(FontType.SMALL, settings.hitsplatFont());
		assertEquals(64, settings.maxRadius());
		assertTrue(config.onlyMine);
		assertEquals(64, config.maxRadius);
	}


	private static final class MutableConfig implements OverheadsConfig
	{
		private boolean onlyMine;
		private int maxRadius;


		@Override
		public boolean onlyDisplayMine()
		{
			return onlyMine;
		}

		@Override
		public int maxRadius()
		{
			return maxRadius;
		}
	}
}
