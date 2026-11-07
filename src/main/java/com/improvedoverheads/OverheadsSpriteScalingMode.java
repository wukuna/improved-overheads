package com.improvedoverheads;

import java.awt.RenderingHints;

public enum OverheadsSpriteScalingMode
{
	NEAREST("Nearest", RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR),
	BILINEAR("Bilinear", RenderingHints.VALUE_INTERPOLATION_BILINEAR),
	BICUBIC("Bicubic", RenderingHints.VALUE_INTERPOLATION_BICUBIC),
	XBR("xBR", RenderingHints.VALUE_INTERPOLATION_BICUBIC);

	private final String displayName;
	private final Object interpolationHint;

	OverheadsSpriteScalingMode(String displayName, Object interpolationHint)
	{
		this.displayName = displayName;
		this.interpolationHint = interpolationHint;
	}

	Object getInterpolationHint()
	{
		return interpolationHint;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
