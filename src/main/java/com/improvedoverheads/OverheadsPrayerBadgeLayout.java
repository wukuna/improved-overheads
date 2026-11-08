package com.improvedoverheads;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class OverheadsPrayerBadgeLayout
{
    final List<OverheadsPrayerIconLayout.Icon> icons = new ArrayList<>();
    final int width;
    final int height;

    OverheadsPrayerBadgeLayout(OverheadsSprite protection, Map<Integer, OverheadsSprite> badges,
        int protectionSize, int badgeSize, int spacing, int percent)
    {
        int main = OverheadsHeadIconRenderer.scaled(Math.max(24, Math.min(64, protectionSize)), percent);
        int badge = OverheadsHeadIconRenderer.scaled(Math.max(12, Math.min(28, badgeSize)), percent);
        int gap = OverheadsHeadIconRenderer.scaledEdge(Math.max(0, Math.min(8, spacing)), percent);
        double distance = (main / 2.0 + badge / 2.0 - Math.max(1, percent / 100.0) + gap) / Math.sqrt(2);
        int margin = Math.max(0, (int) Math.ceil(distance + badge / 2.0 - main / 2.0));
        width = main + margin * 2;
        height = width;
        double ratio = main / (double) Math.max(protection.getWidth(), protection.getHeight());
        int w = Math.max(1, (int) Math.round(protection.getWidth() * ratio));
        int h = Math.max(1, (int) Math.round(protection.getHeight() * ratio));
        icons.add(new OverheadsPrayerIconLayout.Icon(protection, new Rectangle((width - w) / 2, (height - h) / 2, w, h)));
        for (Map.Entry<Integer, OverheadsSprite> entry : badges.entrySet())
        {
            int slot = entry.getKey();
            if (slot < 0 || slot > 3) continue;
            int x = (int) Math.round(width / 2.0 + (slot % 2 == 0 ? -distance : distance) - badge / 2.0);
            int y = (int) Math.round(height / 2.0 + (slot < 2 ? distance : -distance) - badge / 2.0);
            icons.add(new OverheadsPrayerIconLayout.Icon(entry.getValue(), new Rectangle(x, y, badge, badge)));
        }
    }
}
