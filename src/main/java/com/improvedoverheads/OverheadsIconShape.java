package com.improvedoverheads;

public enum OverheadsIconShape
{
    ORIGINAL("OSRS"),
    ROUND("Round"),
    SQUARE("Square");

    private final String label;

    OverheadsIconShape(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
