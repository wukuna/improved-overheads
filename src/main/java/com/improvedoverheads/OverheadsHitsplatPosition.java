package com.improvedoverheads;

public enum OverheadsHitsplatPosition
{
    NATIVE(null),
    ABOVE(OverheadsPosition.ABOVE),
    BELOW(OverheadsPosition.BELOW),
    LEFT(OverheadsPosition.LEFT),
    RIGHT(OverheadsPosition.RIGHT);

    private final OverheadsPosition side;

    OverheadsHitsplatPosition(OverheadsPosition side)
    {
        this.side = side;
    }

    OverheadsPosition side() { return side; }

    @Override public String toString() { return side == null ? "Native" : side.toString(); }
}
