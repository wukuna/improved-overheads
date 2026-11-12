package com.improvedoverheads;

public enum OverheadsHealthPosition
{
    NATIVE(OverheadsPosition.ABOVE),
    ABOVE(OverheadsPosition.ABOVE),
    BELOW(OverheadsPosition.BELOW),
    LEFT(OverheadsPosition.LEFT),
    RIGHT(OverheadsPosition.RIGHT);

    private final OverheadsPosition side;

    OverheadsHealthPosition(OverheadsPosition side)
    {
        this.side = side;
    }

    OverheadsPosition side() { return side; }

    @Override public String toString() { return this == NATIVE ? "Native" : side.toString(); }
}
