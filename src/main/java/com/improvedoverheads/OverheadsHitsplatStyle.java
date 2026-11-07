package com.improvedoverheads;

public enum OverheadsHitsplatStyle
{
    OSRS("OSRS"),
    MODERN("Modern"),
    NUMBERS("Numbers only");

    private final String label;

    OverheadsHitsplatStyle(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
