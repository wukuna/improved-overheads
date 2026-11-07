package com.improvedoverheads;

public enum OverheadsLayoutBehavior
{
	INCREMENTAL("Incremental"),
	SYMMETRIC("Symmetrical"),
	RANDOM("Random");

	private final String name;

	OverheadsLayoutBehavior(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
