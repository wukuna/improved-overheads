package com.improvedoverheads;

public enum OverheadsHealthBarFillDirection
{
	LEFT_TO_RIGHT("Left to right"),
	RIGHT_TO_LEFT("Right to left"),
	TOP_TO_BOTTOM("Top to bottom"),
	BOTTOM_TO_TOP("Bottom to top");

	private final String displayName;

	OverheadsHealthBarFillDirection(String displayName)
	{
		this.displayName = displayName;
	}

	boolean isVertical()
	{
		return this == TOP_TO_BOTTOM || this == BOTTOM_TO_TOP;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
