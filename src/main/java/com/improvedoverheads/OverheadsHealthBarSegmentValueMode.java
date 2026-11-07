package com.improvedoverheads;

public enum OverheadsHealthBarSegmentValueMode
{
	EXACT_HP_WITH_PUBLIC_FALLBACK("Exact HP; public fallback"),
	EXACT_HP_ONLY("Exact HP only"),
	PUBLIC_SCALE("Public scale units");

	private final String displayName;

	OverheadsHealthBarSegmentValueMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
