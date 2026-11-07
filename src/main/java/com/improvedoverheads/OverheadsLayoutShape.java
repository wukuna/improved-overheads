package com.improvedoverheads;

public enum OverheadsLayoutShape
{
	HEXAGONAL("Hexagonal"),
	DIAMOND("Diamond"),
	GRID("Grid"),
	X("X");

	private final String name;

	OverheadsLayoutShape(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
