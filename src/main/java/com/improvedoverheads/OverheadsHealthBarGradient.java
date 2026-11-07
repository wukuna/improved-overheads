package com.improvedoverheads;

public enum OverheadsHealthBarGradient
{
	SOLID("Solid"),
	HORIZONTAL("Horizontal"),
	VERTICAL("Vertical"),
	HEALTH_BASED("Health based");

	private final String displayName;

	OverheadsHealthBarGradient(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
