package com.improvedoverheads;

public enum OverheadsHealthScaleMode
{
	FIXED("Fixed"),
	THRESHOLD("Threshold"),
	DYNAMIC("Dynamic");

	private final String displayName;

	OverheadsHealthScaleMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
