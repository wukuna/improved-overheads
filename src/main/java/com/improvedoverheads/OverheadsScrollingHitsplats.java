package com.improvedoverheads;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class OverheadsScrollingHitsplats
{
    private final Map<Long, Double> displacements = new HashMap<>();

    Map<Long, OverheadsOffset> layout(List<OverheadsHitsplat> hits, int cycle, int width, int height,
        int scale, int columns, int xGap, int yGap, int travel, OverheadsHitsplatMotion motion)
    {
        columns = Math.max(1, Math.min(4, columns));
        double factor = Math.max(1, scale) / 100.0;
        double step = height / factor + Math.max(0, yGap) + 2;
        int xStep = width + OverheadsRenderLayout.scaleCoordinate(Math.max(0, xGap) + 2, scale);
        List<OverheadsHitsplat> ordered = new ArrayList<>(hits);
        ordered.sort(Comparator.comparingLong(OverheadsHitsplat::getSequence).reversed());
        double[] next = new double[columns];
        Map<Long, OverheadsOffset> offsets = new HashMap<>();
        for (OverheadsHitsplat hit : ordered)
        {
            if (hit.isExpired(cycle))
            {
                continue;
            }
            int column = Math.floorMod(hit.getPosition(), columns);
            double progress = hit.getProgress(cycle) * Math.max(0, Math.min(256, travel));
            double distance = Math.max(next[column], progress + displacements.getOrDefault(hit.getSequence(), 0.0));
            displacements.put(hit.getSequence(), distance - progress);
            next[column] = distance + step;
            int y = (int) Math.round(distance * factor);
            offsets.put(hit.getSequence(), new OverheadsOffset((2 * column - columns + 1) * xStep / 2,
                motion == OverheadsHitsplatMotion.SCROLL_DOWN ? y : -y));
        }
        displacements.keySet().retainAll(offsets.keySet());
        return offsets;
    }
}
