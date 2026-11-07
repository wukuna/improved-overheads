package com.improvedoverheads;

public enum OverheadsTargetDetection
{
	FOOTPRINT("Footprint"),
	HEALTH_SCALE("Public health scale"),
	EITHER("Either");

	private final String displayName;

	OverheadsTargetDetection(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
