package com.improvedoverheads;

public enum OverheadsPrayerLayout
{
    ROWS("Regular rows"),
    CORNER_BADGES("Corner badges");

    private final String label;

    OverheadsPrayerLayout(String label) { this.label = label; }

    @Override public String toString() { return label; }
}
