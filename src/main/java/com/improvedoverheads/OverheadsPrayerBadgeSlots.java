package com.improvedoverheads;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Prayer;

final class OverheadsPrayerBadgeSlots
{
    private final Map<Prayer, Integer> slots = new EnumMap<>(Prayer.class);

    void update(List<Prayer> active)
    {
        slots.keySet().retainAll(active);
        for (Prayer prayer : active)
        {
            if (slots.containsKey(prayer)) continue;
            for (int slot = 0; slot < 4; slot++)
            {
                if (!slots.containsValue(slot))
                {
                    slots.put(prayer, slot);
                    break;
                }
            }
        }
    }

    int slot(Prayer prayer) { return slots.getOrDefault(prayer, -1); }

    void clear() { slots.clear(); }
}
