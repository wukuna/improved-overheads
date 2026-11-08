package com.improvedoverheads;

public enum OverheadsPrayerRow
{
    ROW_1("Row 1"), ROW_2("Row 2");
    private final String label;
    OverheadsPrayerRow(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
