package com.improvedoverheads;

public enum OverheadsHealthBarStyle
{
	NATIVE("OSRS"),
	SOLID("Modern");

	private final String displayName;

	OverheadsHealthBarStyle(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
