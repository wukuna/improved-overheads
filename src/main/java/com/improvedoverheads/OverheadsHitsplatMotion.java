package com.improvedoverheads;

public enum OverheadsHitsplatMotion
{
    FIXED("Fixed layout"),
    SCROLL_UP("Scroll up"),
    SCROLL_DOWN("Scroll down");

    private final String name;

    OverheadsHitsplatMotion(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
