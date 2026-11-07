package com.improvedoverheads;

public enum OverheadsPosition
{
    ABOVE("Above"),
    BELOW("Below"),
    LEFT("Left"),
    RIGHT("Right");

    private final String label;

    OverheadsPosition(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
