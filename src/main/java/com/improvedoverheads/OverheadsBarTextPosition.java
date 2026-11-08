package com.improvedoverheads;

import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;

public enum OverheadsBarTextPosition
{
    ON_BAR("On bar"), ABOVE("Above bar"), BELOW("Below bar");

    private final String label;
    OverheadsBarTextPosition(String label) { this.label = label; }
    @Override public String toString() { return label; }

    Point baseline(Rectangle bar, FontMetrics metrics, String text, boolean vertical, OverheadsBarDisplay display, OverheadsPosition side, double border)
    {
        int x = bar.x + (bar.width - metrics.stringWidth(text)) / 2;
        int y = bar.y + (bar.height - metrics.getHeight()) / 2 + metrics.getAscent();
        if (display == OverheadsBarDisplay.BOTH)
        {
            int gap = 3 + (int) Math.ceil(Math.max(0, Math.min(10, border)));
            if (this == ABOVE) y = bar.y - gap - metrics.getDescent();
            else if (this == BELOW) y = bar.y + bar.height + gap + metrics.getAscent();
            else if (vertical) x = side == OverheadsPosition.RIGHT ? bar.x + bar.width + 4 : bar.x - metrics.stringWidth(text) - 4;
        }
        return new Point(x, y);
    }
}
