package com.improvedoverheads;

public enum OverheadsHealthBarGradientCoordinates
{
	SEGMENT("Relative to each segment"),
	FULL_BAR("Absolute across full bar");

	private final String displayName;

	OverheadsHealthBarGradientCoordinates(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
