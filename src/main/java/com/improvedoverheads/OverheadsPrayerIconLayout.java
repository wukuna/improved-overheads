package com.improvedoverheads;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

final class OverheadsPrayerIconLayout
{
    static final class Icon
    {
        final OverheadsSprite sprite;
        final Rectangle bounds;
        Icon(OverheadsSprite sprite, Rectangle bounds) { this.sprite = sprite; this.bounds = bounds; }
    }

    final List<Icon> icons = new ArrayList<>();
    final int width;
    final int height;

    OverheadsPrayerIconLayout(List<OverheadsSprite> protection, OverheadsPrayerRow protectionRow,
        List<OverheadsSprite> combat, OverheadsPrayerRow combatRow, int percent, int spacing)
    {
        List<List<OverheadsSprite>> rows = new ArrayList<>();
        rows.add(new ArrayList<>());
        rows.add(new ArrayList<>());
        rows.get(protectionRow.ordinal()).addAll(protection);
        rows.get(combatRow.ordinal()).addAll(combat);
        int[] widths = new int[2], heights = new int[2];
        int gap = Math.max(0, Math.min(20, spacing));
        for (int row = 0; row < 2; row++)
        {
            for (OverheadsSprite sprite : rows.get(row))
            {
                if (widths[row] > 0) widths[row] += gap;
                widths[row] += OverheadsHeadIconRenderer.scaled(sprite.getMaxWidth(), percent);
                heights[row] = Math.max(heights[row], OverheadsHeadIconRenderer.scaled(sprite.getMaxHeight(), percent));
            }
        }
        width = Math.max(widths[0], widths[1]);
        height = heights[0] + heights[1] + (heights[0] > 0 && heights[1] > 0 ? gap : 0);
        int y = 0;
        for (int row = 0; row < 2; row++)
        {
            int x = (width - widths[row]) / 2;
            for (OverheadsSprite sprite : rows.get(row))
            {
                int canvasWidth = OverheadsHeadIconRenderer.scaled(sprite.getMaxWidth(), percent);
                int canvasHeight = OverheadsHeadIconRenderer.scaled(sprite.getMaxHeight(), percent);
                icons.add(new Icon(sprite, new Rectangle(x + OverheadsHeadIconRenderer.scaledEdge(sprite.getOffsetX(), percent),
                    y + (heights[row] - canvasHeight) / 2 + OverheadsHeadIconRenderer.scaledEdge(sprite.getOffsetY(), percent),
                    OverheadsHeadIconRenderer.scaledSpan(sprite.getOffsetX(), sprite.getWidth(), percent),
                    OverheadsHeadIconRenderer.scaledSpan(sprite.getOffsetY(), sprite.getHeight(), percent))));
                x += canvasWidth + gap;
            }
            if (heights[row] > 0) y += heights[row] + gap;
        }
    }
}
