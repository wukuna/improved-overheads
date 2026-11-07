package com.improvedoverheads;

public enum OverheadsOrientation
{
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical");

    private final String label;

    OverheadsOrientation(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
