package com.improvedoverheads;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.FontType;
import net.runelite.client.game.SkillIconManager;

final class OverheadsRestorationSplats
{
    private static final int MAX_PER_KIND = 4;
    private static final BufferedImage ANCHOR = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private final Client client;
    private final OverheadsConfig config;
    private final int[] previous = {-1, -1};
    private final int[] levels = {-1, -1};
    private final List<Splat> splats = new ArrayList<>();
    private Player owner;
    private SkillIconManager icons;

    static final class Splat
    {
        final boolean prayer;
        final int cycle;
        int amount;

        Splat(boolean prayer, int cycle, int amount)
        {
            this.prayer = prayer;
            this.cycle = cycle;
            this.amount = amount;
        }
    }

    OverheadsRestorationSplats(Client client, OverheadsConfig config)
    {
        this.client = client;
        this.config = config;
    }

    void setIcons(SkillIconManager icons)
    {
        this.icons = icons;
    }

    void tick()
    {
        Player player = client.getLocalPlayer();
        if (client.getGameState() != GameState.LOGGED_IN || player == null || player.isDead()
            || client.getBoostedSkillLevel(Skill.HITPOINTS) <= 0)
        {
            clear();
            return;
        }
        if (owner != player)
        {
            clear();
            owner = player;
            previous[0] = client.getBoostedSkillLevel(Skill.HITPOINTS);
            previous[1] = client.getBoostedSkillLevel(Skill.PRAYER);
            levels[0] = client.getRealSkillLevel(Skill.HITPOINTS);
            levels[1] = client.getRealSkillLevel(Skill.PRAYER);
        }
        prune();
    }

    void changed(StatChanged event)
    {
        Skill skill = event.getSkill();
        if (skill != Skill.HITPOINTS && skill != Skill.PRAYER)
        {
            return;
        }
        if (client.getGameState() != GameState.LOGGED_IN || owner == null || owner != client.getLocalPlayer()
            || owner.isDead() || skill == Skill.HITPOINTS && event.getBoostedLevel() <= 0)
        {
            clear();
            return;
        }
        boolean prayer = skill == Skill.PRAYER;
        int index = prayer ? 1 : 0;
        int current = event.getBoostedLevel();
        int old = previous[index];
        int oldLevel = levels[index];
        previous[index] = current;
        levels[index] = event.getLevel();
        if (old < 0 || current <= old || current > 255 || event.getLevel() != oldLevel || !enabled(prayer))
        {
            return;
        }
        prune();
        int cycle = client.getGameCycle();
        for (Splat splat : splats)
        {
            if (splat.prayer == prayer && splat.cycle == cycle)
            {
                splat.amount = Math.min(255, splat.amount + current - old);
                return;
            }
        }
        int count = 0;
        Splat oldest = null;
        for (Splat splat : splats)
        {
            if (splat.prayer == prayer)
            {
                if (oldest == null) oldest = splat;
                count++;
            }
        }
        if (count >= MAX_PER_KIND) splats.remove(oldest);
        splats.add(new Splat(prayer, cycle, current - old));
    }

    private boolean enabled(boolean prayer)
    {
        return prayer ? config.prayerSplatEnabled() : config.healingSplatEnabled();
    }

    private int duration(boolean prayer)
    {
        return Math.max(200, Math.min(5000, prayer ? config.prayerSplatDuration() : config.healingSplatDuration()));
    }

    private void prune()
    {
        int cycle = client.getGameCycle();
        splats.removeIf(splat -> !enabled(splat.prayer) || cycle < splat.cycle
            || (long) (cycle - splat.cycle) * 20 >= duration(splat.prayer));
    }

    boolean hasContent()
    {
        prune();
        return !splats.isEmpty();
    }

    List<Splat> snapshot()
    {
        return new ArrayList<>(splats);
    }

    void clearSplats()
    {
        splats.clear();
    }

    void clear()
    {
        clearSplats();
        owner = null;
        previous[0] = previous[1] = levels[0] = levels[1] = -1;
    }

    void render(Graphics2D graphics, Player player, Point top, OverheadsCollisionLayout collision)
    {
        if (player != owner || !hasContent()) return;
        renderKind(graphics, player, top, collision, false);
        renderKind(graphics, player, top, collision, true);
    }

    private void renderKind(Graphics2D graphics, Player player, Point top, OverheadsCollisionLayout collision, boolean prayer)
    {
        if (!enabled(prayer)) return;
        List<Splat> active = new ArrayList<>();
        for (Splat splat : splats) if (splat.prayer == prayer) active.add(splat);
        if (active.isEmpty()) return;
        OverheadsRestoreDisplay display = prayer ? config.prayerSplatDisplay() : config.healingSplatDisplay();
        FontType fontType = prayer ? config.prayerSplatFont() : config.healingSplatFont();
        Font base = (fontType == null ? FontType.SMALL : fontType).getFont();
        double scale = Math.max(25, Math.min(300, prayer ? config.prayerSplatScalePercent() : config.healingSplatScalePercent())) / 100.0
            * OverheadsPlacement.zoom(client, player, prayer ? config.prayerSplatScaleWithZoom() : config.healingSplatScaleWithZoom());
        Font font = base.deriveFont((float) Math.max(6, Math.min(96, base.getSize2D() * scale)));
        FontMetrics metrics = graphics.getFontMetrics(font);
        boolean number = display != OverheadsRestoreDisplay.ICON_ONLY;
        BufferedImage icon = display == OverheadsRestoreDisplay.NUMBER_ONLY || icons == null ? null
            : icons.getSkillImage(prayer ? Skill.PRAYER : Skill.HITPOINTS);
        int iconSize = icon == null ? 0 : Math.max(6, Math.min(96, (int) Math.round(18 * scale)));
        int gap = icon != null && number ? 3 : 0;
        int width = iconSize;
        for (Splat splat : active) width = Math.max(width, iconSize + gap + (number ? metrics.stringWidth("+" + splat.amount) : 0));
        if (width == 0) return;
        int lineHeight = Math.max(iconSize, number ? metrics.getHeight() : 0);
        int travel = Math.max(1, (int) Math.round(18 * Math.min(3, scale)));
        int height = (lineHeight + 3) * active.size() - 3 + travel;
        OverheadsHitsplatPosition position = prayer ? config.prayerSplatPosition() : config.healingSplatPosition();
        int xOffset = Math.max(-300, Math.min(300, prayer ? config.prayerSplatXOffset() : config.healingSplatXOffset()));
        int yOffset = Math.max(-300, Math.min(300, prayer ? config.prayerSplatYOffset() : config.healingSplatYOffset()));
        Rectangle bounds;
        if (position == null || position == OverheadsHitsplatPosition.NATIVE)
        {
            Point middle = player.getCanvasImageLocation(ANCHOR, player.getLogicalHeight() / 2);
            if (middle == null) return;
            bounds = new Rectangle(middle.getX() - width / 2 + xOffset,
                middle.getY() - lineHeight / 2 - height + lineHeight - yOffset, width, height);
        }
        else
        {
            bounds = OverheadsPlacement.bounds(player, top, position.side(), width, height, xOffset, yOffset);
        }
        if (bounds == null) return;
        Color color = prayer ? config.prayerSplatColor() : config.healingSplatColor();
        Color textColor = color == null ? Color.WHITE : color;
        float opacity = Math.max(0, Math.min(100, prayer ? config.prayerSplatOpacity() : config.healingSplatOpacity())) / 100f;
        int lifetime = duration(prayer);
        int cycle = client.getGameCycle();
        OverheadsCollisionLayout.draw(position == null || position.side() == null ? null : collision, graphics,
            prayer ? "prayer-restoration" : "healing", position == null || position.side() == null ? OverheadsPosition.ABOVE : position.side(), bounds,
            target ->
            {
                Graphics2D draw = (Graphics2D) target.create();
                try
                {
                    draw.setFont(font);
                    draw.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    for (int i = 0; i < active.size(); i++)
                    {
                        Splat splat = active.get(i);
                        double age = Math.max(0, (cycle - splat.cycle) * 20.0 / lifetime);
                        float fade = (float) Math.max(0, Math.min(1, (1 - age) * 4));
                        draw.setComposite(AlphaComposite.SrcOver.derive(opacity * fade));
                        int y = bounds.y + bounds.height - lineHeight - (active.size() - i - 1) * (lineHeight + 3)
                            - (int) Math.round(travel * age);
                        String text = "+" + splat.amount;
                        int contentWidth = iconSize + gap + (number ? metrics.stringWidth(text) : 0);
                        int x = bounds.x + (bounds.width - contentWidth) / 2;
                        if (icon != null) draw.drawImage(icon, x, y + (lineHeight - iconSize) / 2, iconSize, iconSize, null);
                        if (number)
                        {
                            int baseline = y + (lineHeight - metrics.getHeight()) / 2 + metrics.getAscent();
                            draw.setColor(new Color(0, 0, 0, textColor.getAlpha()));
                            draw.drawString(text, x + iconSize + gap + 1, baseline + 1);
                            draw.setColor(textColor);
                            draw.drawString(text, x + iconSize + gap, baseline);
                        }
                    }
                }
                finally
                {
                    draw.dispose();
                }
            });
    }
}
