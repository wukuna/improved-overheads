package com.improvedoverheads;

public enum OverheadsRestoreDisplay
{
    ICON_AND_NUMBER("Icon and number"), NUMBER_ONLY("Number only"), ICON_ONLY("Icon only");

    private final String label;

    OverheadsRestoreDisplay(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
