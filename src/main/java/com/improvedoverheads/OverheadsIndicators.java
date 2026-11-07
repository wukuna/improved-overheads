package com.improvedoverheads;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.gameval.SpriteID;

final class OverheadsIndicators
{
    private final Map<Color, BufferedImage> tinted = new HashMap<>();
    private BufferedImage tintSource;
    private final Client client;
    private final OverheadsConfig config;
    private final OverheadsActorUiSpriteCache sprites;
    private final OverheadsPartyState party;
    private final OverheadsLocalTimers timers;

    OverheadsIndicators(Client client, OverheadsConfig config, OverheadsPartyState party, OverheadsLocalTimers timers)
    {
        this.client = client;
        this.config = config;
        this.party = party;
        this.timers = timers;
        sprites = new OverheadsActorUiSpriteCache(client);
    }

    boolean hasContent(Actor actor)
    {
        if (actor == client.getLocalPlayer())
        {
            return config.regenDisplay() != OverheadsBarDisplay.HIDDEN || config.poisonTimerDisplay() != OverheadsBarDisplay.HIDDEN;
        }
        return actor instanceof Player && config.partyDisplay() != OverheadsBarDisplay.HIDDEN && party.contains(actor.getName());
    }

    boolean replacesHealth(Actor actor)
    {
        return actor instanceof Player && config.partyShowHealth() && config.partyDisplay() != OverheadsBarDisplay.HIDDEN
            && party.value((Player) actor, 0) != null && config.indicatorOpacity() > 0;
    }

    void render(Graphics2D graphics, Actor actor, Point anchor, OverheadsCollisionLayout layout)
    {
        if (actor == client.getLocalPlayer())
        {
            timer(graphics, actor, anchor, layout, "HP", timers.regenRemaining(), timers.regenTicks(), config.regenDisplay(), new Color(80, 210, 110), 0);
            timer(graphics, actor, anchor, layout, "Psn", timers.poisonRemaining(), 30, config.poisonTimerDisplay(), new Color(155, 200, 65), 1);
        }
        else if (actor instanceof Player && config.partyDisplay() != OverheadsBarDisplay.HIDDEN)
        {
            String[] labels = {"HP", "Pray", "Run", "Spec"};
            Color[] colors = {new Color(80, 210, 110), config.prayerColor(), config.runColor(), config.specialColor()};
            boolean[] enabled = {config.partyShowHealth(), config.partyShowPrayer(), config.partyShowRun(), config.partyShowSpecial()};
            int slot = 0;
            for (int i = 0; i < 4; i++)
            {
                OverheadsPartyState.Value value = enabled[i] ? party.value((Player) actor, i) : null;
                if (value == null) { continue; }
                String label = labels[i] + " ~" + value.current + (i < 2 ? "/" + value.maximum : "%");
                draw(graphics, actor, anchor, layout, "party" + i, label, value.current / (double) value.maximum,
                    config.partyDisplay(), colors[i], slot++);
            }
        }
    }

    private void timer(Graphics2D graphics, Actor actor, Point anchor, OverheadsCollisionLayout layout,
        String label, double fraction, int ticks, OverheadsBarDisplay display, Color color, int slot)
    {
        if (fraction < 0 || display == OverheadsBarDisplay.HIDDEN) { return; }
        String text = label + " ~" + (int) Math.ceil(fraction * ticks * 0.6) + "s";
        draw(graphics, actor, anchor, layout, "timer" + label, text, fraction, display, color, slot);
    }

    private void draw(Graphics2D graphics, Actor actor, Point anchor, OverheadsCollisionLayout layout,
        String key, String text, double fraction, OverheadsBarDisplay display, Color color, int slot)
    {
        if (config.indicatorOpacity() <= 0) { return; }
        double zoom = OverheadsPlacement.zoom(client, actor, config.indicatorScaleWithZoom());
        boolean vertical = config.indicatorOrientation() == OverheadsOrientation.VERTICAL;
        int length = OverheadsHealthBarRenderer.barDimension(config.indicatorWidth(), 20, 200, zoom);
        int thickness = OverheadsHealthBarRenderer.barDimension(config.indicatorHeight(), 2, 30, zoom);
        OverheadsPosition position = config.indicatorPosition();
        Font baseFont = config.healthTextFont().getFont();
        Font font = baseFont.deriveFont((float) Math.max(6, Math.min(96, baseFont.getSize2D() * zoom)));
        FontMetrics metrics = graphics.getFontMetrics(font);
        int step = (vertical ? thickness + metrics.stringWidth("Pray ~255/126") + 10
            : Math.max(thickness + 4, metrics.getHeight() + 6)) + Math.max(0, Math.min(20, config.componentGap())) + 1;
        Rectangle bounds = OverheadsPlacement.bounds(actor, anchor, position, vertical ? thickness : length, vertical ? length : thickness,
            config.indicatorXOffset() + (vertical ? slot * step * (position == OverheadsPosition.LEFT ? -1 : 1) : 0),
            config.indicatorYOffset() + (vertical ? 0 : slot * step * (position == OverheadsPosition.ABOVE ? 1 : -1)));
        if (bounds == null) { return; }
        Rectangle painted = OverheadsCollisionLayout.barBounds(graphics, bounds, vertical, display, position, text,
            config.healthTextFont(), zoom, 1, config.indicatorOpacity(), 255);
        OverheadsCollisionLayout.draw(layout, graphics, key, position, painted, target ->
        {
            Graphics2D g = (Graphics2D) target.create();
            try
            {
                g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, Math.min(100, config.indicatorOpacity())) / 100f));
                double fill = Math.max(0, Math.min(1, fraction));
                OverheadsHealthBarFillDirection direction = vertical ? OverheadsHealthBarFillDirection.BOTTOM_TO_TOP : OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
                if (display != OverheadsBarDisplay.NUMBERS_ONLY)
                {
                    OverheadsSprite front = config.indicatorStyle() == OverheadsHealthBarStyle.NATIVE ? sprite(SpriteID.StandardHealth30.FRONT) : null;
                    OverheadsSprite back = config.indicatorStyle() == OverheadsHealthBarStyle.NATIVE ? sprite(SpriteID.StandardHealth30.BACK) : null;
                    if (config.indicatorStyle() == OverheadsHealthBarStyle.NATIVE && front != null && back != null)
                    {
                        OverheadsHealthBarRenderer.drawOsrsBar(g, bounds, vertical, fill, direction,
                            tint(front.getImage(), color), back.getImage());
                    }
                    else
                    {
                        Color background = new Color(20, 24, 30);
                        OverheadsHealthBarRenderer.drawCustomBar(g, bounds.x, bounds.y, bounds.width, bounds.height, length * fill,
                            direction, OverheadsHealthBarGradient.SOLID, color, color, OverheadsHealthBarGradient.SOLID,
                            background, background, fill, Color.BLACK, 1, 3);
                    }
                }
                if (display != OverheadsBarDisplay.BAR_ONLY)
                {
                    g.setFont(font);
                    int x = bounds.x + (bounds.width - metrics.stringWidth(text)) / 2;
                    if (vertical && display == OverheadsBarDisplay.BOTH)
                    {
                        x = position == OverheadsPosition.RIGHT ? bounds.x + bounds.width + 4 : bounds.x - metrics.stringWidth(text) - 4;
                    }
                    int y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
                    g.setColor(Color.BLACK);
                    g.drawString(text, x + 1, y + 1);
                    g.setColor(Color.WHITE);
                    g.drawString(text, x, y);
                }
            }
            finally { g.dispose(); }
        });
    }

    private OverheadsSprite sprite(int id)
    {
        OverheadsSprite source = sprites.getResourcePackOverride(id);
        return source == null ? sprites.get(id, 0) : source;
    }

    private BufferedImage tint(BufferedImage source, Color color)
    {
        if (source != tintSource) { tinted.clear(); tintSource = source; }
        return tinted.computeIfAbsent(color, key -> OverheadsHealthBarRenderer.tintSprite(source, key));
    }

    void clear() { sprites.clear(); tinted.clear(); tintSource = null; }
}
