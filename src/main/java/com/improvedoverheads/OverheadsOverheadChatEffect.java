package com.improvedoverheads;

public enum OverheadsOverheadChatEffect
{
	STATIC("Static"),
	WAVE("Wave"),
	WAVE_2("Wave 2"),
	SHAKE("Shake"),
	SCROLL("Scroll"),
	SLIDE("Slide");

	private final String displayName;

	OverheadsOverheadChatEffect(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
