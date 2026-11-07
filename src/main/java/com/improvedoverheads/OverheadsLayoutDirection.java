package com.improvedoverheads;

public enum OverheadsLayoutDirection
{
	CLOCKWISE("Clockwise"),
	COUNTERCLOCKWISE("Counterclockwise");

	private final String name;

	OverheadsLayoutDirection(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
