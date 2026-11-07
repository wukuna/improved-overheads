package com.improvedoverheads;

public enum OverheadsPrayerVisibility
{
    NEVER("Hidden"),
    ACTIVE("While praying"),
    ALWAYS("Always");

    private final String label;

    OverheadsPrayerVisibility(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
