package com.improvedoverheads;

import javax.inject.Inject;
import net.runelite.client.config.FontType;

final class OverheadsSettings
{
	private final OverheadsConfig config;

	@Inject
	OverheadsSettings(OverheadsConfig config)
	{
		this.config = config;
	}

	boolean onlyDisplayMine()
	{
		return config.onlyDisplayMine();
	}

	boolean prioritizeMine()
	{
		return config.prioritizeMine();
	}

	boolean hideZeroHitsplats()
	{
		return config.hideZeroHitsplats();
	}

	OverheadsLayoutShape layoutShape()
	{
		return config.layoutShape();
	}

	OverheadsLayoutDirection layoutDirection()
	{
		return config.layoutDirection();
	}

	OverheadsLayoutBehavior layoutBehavior()
	{
		return config.layoutBehavior();
	}

	int minRadius()
	{
		return config.minRadius();
	}

	int maxRadius()
	{
		return config.maxRadius();
	}

	int xSpacing()
	{
		return config.xSpacing();
	}

	int ySpacing()
	{
		return config.ySpacing();
	}

	FontType hitsplatFont()
	{
		return config.hitsplatFont();
	}

	int hitsplatScalePercent()
	{
		return config.hitsplatScalePercent();
	}

	int fadeInDuration()
	{
		return config.fadeInDuration();
	}

	int fullOpacityDuration()
	{
		return config.fullOpacityDuration();
	}

	int fadeOutDuration()
	{
		return config.fadeOutDuration();
	}
}
