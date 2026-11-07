package com.improvedoverheads;

public enum OverheadsBarDisplay
{
    BAR_ONLY("Bar only"),
    NUMBERS_ONLY("Numbers only"),
    BOTH("Both"),
    HIDDEN("Hidden");

    private final String label;

    OverheadsBarDisplay(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
