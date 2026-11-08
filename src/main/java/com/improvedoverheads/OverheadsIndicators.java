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
    private OverheadsActorRenderers<OverheadsIndicators> scoped;
    @javax.inject.Inject
    void configureProfiles(OverheadsProfiles profiles)
    {
        scoped = new OverheadsActorRenderers<>(this, profiles, selected -> childRenderer(selected));
    }

    private final Map<Color, BufferedImage> tinted = new HashMap<>();
    private BufferedImage tintSource;
    private final Client client;
    private final OverheadsConfig config;
    private OverheadsConfig sharing;
    private final OverheadsActorUiSpriteCache sprites;
    private final OverheadsPartyState party;
    private final OverheadsLocalTimers timers;
    private final OverheadsHealthBarRenderer healthStyle;
    private long partySession = Long.MIN_VALUE;

    OverheadsIndicators(Client client, OverheadsConfig config, OverheadsPartyState party, OverheadsLocalTimers timers)
    {
        this.client = client;
        this.config = config;
        this.sharing = config;
        this.party = party;
        this.timers = timers;
        healthStyle = new OverheadsHealthBarRenderer(client, config);
        sprites = new OverheadsActorUiSpriteCache(client);
    }

    private OverheadsIndicators childRenderer(OverheadsConfig selected)
    {
        OverheadsIndicators child = new OverheadsIndicators(client, selected, party, timers);
        child.sharing = sharing;
        return child;
    }

    boolean hasContent(Actor actor)
    {
        OverheadsIndicators selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.hasContent(actor); }

        if (actor == client.getLocalPlayer())
        {
            return config.regenDisplay() != OverheadsBarDisplay.HIDDEN || config.poisonTimerDisplay() != OverheadsBarDisplay.HIDDEN;
        }
        return actor instanceof Player && sharing.partyDisplay() != OverheadsBarDisplay.HIDDEN && party.contains(actor.getName());
    }

    boolean replacesHealth(Actor actor)
    {
        OverheadsIndicators selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.replacesHealth(actor); }

        return actor instanceof Player && showPartyHealth(actor) && sharing.partyDisplay() != OverheadsBarDisplay.HIDDEN
            && party.value((Player) actor, 0) != null && config.otherHealthDisplay() != OverheadsBarDisplay.HIDDEN
            && (config.healthBarOpacity() > 0 || config.otherHealthDisplay() != OverheadsBarDisplay.BAR_ONLY && config.healthTextColor().getAlpha() > 0);
    }

    void render(Graphics2D graphics, Actor actor, Point anchor, OverheadsCollisionLayout layout)
    {
        OverheadsIndicators selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { selected.render(graphics, actor, anchor, layout); return; }

        if (actor == client.getLocalPlayer())
        {
            timer(graphics, actor, anchor, layout, "HP", timers.regenRemaining(), timers.regenTicks(), config.regenDisplay(), new Color(80, 210, 110), 0);
            timer(graphics, actor, anchor, layout, "Psn", timers.poisonRemaining(), 30, config.poisonTimerDisplay(), new Color(155, 200, 65), 1);
        }
        else if (actor instanceof Player && sharing.partyDisplay() != OverheadsBarDisplay.HIDDEN)
        {
            String[] labels = {"HP", "Pray", "Run", "Spec"};
            Color[] colors = {config.healthBarFrontColor(), config.prayerColor(), config.runColor(), config.specialColor()};
            boolean[] enabled = {showPartyHealth(actor), config.partyShowPrayer(), config.partyShowRun(), config.partyShowSpecial()};
            int slot = 0;
            for (int i = 0; i < 4; i++)
            {
                OverheadsPartyState.Value value = enabled[i] ? party.value((Player) actor, i) : null;
                if (value == null) { if (i == 0) healthStyle.remove(actor); continue; }
                OverheadsBarDisplay display = i == 0 ? config.otherHealthDisplay() : i == 1 ? config.prayerDisplay() : i == 2 ? config.runDisplay() : config.specialDisplay();
                if (display == OverheadsBarDisplay.HIDDEN || value.current >= value.maximum
                    && (i == 2 && config.runHideWhenFull() || i == 3 && config.specialHideWhenFull())) { continue; }
                OverheadsNumberFormat format = i == 0 ? config.healthNumberFormat() : i == 1 ? config.prayerNumberFormat() : i == 2 ? config.runNumberFormat() : config.specialNumberFormat();
                String label = labels[i] + (i == 0 ? " " : " ~") + format.format(value.current, value.maximum);
                draw(graphics, actor, anchor, layout, "party" + i, label, value.current / (double) value.maximum,
                    display, colors[i], slot++, i, value.maximum);
            }
        }
    }

    private void timer(Graphics2D graphics, Actor actor, Point anchor, OverheadsCollisionLayout layout,
        String label, double fraction, int ticks, OverheadsBarDisplay display, Color color, int slot)
    {
        if (fraction < 0 || display == OverheadsBarDisplay.HIDDEN) { return; }
        String text = label + " ~" + (int) Math.ceil(fraction * ticks * 0.6) + "s";
        draw(graphics, actor, anchor, layout, "timer" + label, text, fraction, display, color, slot, -1, 0);
    }

    private boolean showPartyHealth(Actor actor)
    {
        return config.partyShowHealth() && config.healthBarsEnabled() && config.otherHealthVisibility().includes(client, actor);
    }

    private void draw(Graphics2D graphics, Actor actor, Point anchor, OverheadsCollisionLayout layout,
        String key, String text, double fraction, OverheadsBarDisplay display, Color color, int slot, int resource, int maximum)
    {
        int opacity = resource == 0 ? config.healthBarOpacity() : resource == 1 ? config.prayerOpacity() : resource == 2 ? config.runOpacity() : resource == 3 ? config.specialOpacity() : config.indicatorOpacity();
        net.runelite.client.config.FontType textFont = resource == 1 ? config.prayerTextFont() : resource == 2 ? config.runFont() : resource == 3 ? config.specialFont() : config.healthTextFont();
        Color textColor = resource == 0 ? config.healthTextColor() : resource == 1 ? config.prayerTextColor() : resource == 2 ? config.runTextColor() : resource == 3 ? config.specialTextColor() : Color.WHITE;
        Color background = resource == 0 ? config.healthBarBackColor() : resource == 1 ? config.prayerBackColor() : resource == 2 ? config.runBackColor() : resource == 3 ? config.specialBackColor() : new Color(20, 24, 30);
        Color border = resource == 0 ? config.healthBarBorderColor() : resource == 1 ? config.prayerBorderColor() : resource == 2 ? config.runBorderColor() : resource == 3 ? config.specialBorderColor() : Color.BLACK;
        double borderThickness = resource == 0 ? config.healthBarBorderThickness() : resource == 1 ? config.prayerBorderThickness() : 1;
        double cornerRadius = resource == 0 ? config.healthBarCornerRadius() : resource == 1 ? config.prayerCornerRadius() : resource == 2 ? config.runCornerRadius() : resource == 3 ? config.specialCornerRadius() : 3;
        OverheadsHealthBarStyle style = resource == 0 ? config.healthBarStyle() : resource == 1 ? config.prayerBarStyle() : resource == 2 ? config.runStyle() : resource == 3 ? config.specialStyle() : config.indicatorStyle();
        OverheadsOrientation orientation = resource == 0 ? config.healthOrientation() : resource == 1 ? config.prayerOrientation() : resource == 2 ? config.runOrientation() : resource == 3 ? config.specialOrientation() : config.indicatorOrientation();
        boolean scaleWithZoom = resource == 0 ? config.healthScaleWithZoom() : resource == 1 ? config.prayerScaleWithZoom() : resource == 2 ? config.runScaleWithZoom() : resource == 3 ? config.specialScaleWithZoom() : config.indicatorScaleWithZoom();
        int width = resource == 0 ? config.playerHealthWidth() : resource == 1 ? config.prayerWidth() : resource == 2 ? config.runWidth() : resource == 3 ? config.specialWidth() : config.indicatorWidth();
        int height = resource == 0 ? config.playerHealthHeight() : resource == 1 ? config.prayerHeight() : resource == 2 ? config.runHeight() : resource == 3 ? config.specialHeight() : config.indicatorHeight();
        int xOffset = resource == 0 ? config.healthBarXOffset() : resource == 1 ? config.prayerXOffset() : resource == 2 ? config.runXOffset() : resource == 3 ? config.specialXOffset() : config.indicatorXOffset();
        int yOffset = resource == 0 ? config.healthBarYOffset() : resource == 1 ? config.prayerYOffset() : resource == 2 ? config.runYOffset() : resource == 3 ? config.specialYOffset() : config.indicatorYOffset();

        if (opacity <= 0 && (display == OverheadsBarDisplay.BAR_ONLY || textColor.getAlpha() == 0)) { return; }
        double zoom = OverheadsPlacement.zoom(client, actor, scaleWithZoom);
        boolean vertical = orientation == OverheadsOrientation.VERTICAL;
        double widthScale = resource == 0 ? OverheadsHealthBarRenderer.effectiveScalePercent(config.healthBarScaleMode(),
            config.healthBarScalePercent(), config.healthBarLargeScalePercent(), config.healthBarScaleThreshold(), actor.getHealthScale()) / 100.0 : 1;
        double heightScale = resource == 0 ? OverheadsHealthBarRenderer.effectiveScalePercent(config.healthBarScaleMode(),
            config.healthBarScalePercent(), config.healthBarLargeHeightScalePercent(), config.healthBarScaleThreshold(), actor.getHealthScale()) / 100.0 : 1;
        int length = OverheadsHealthBarRenderer.barDimension(width, resource == 0 ? 10 : 20, 200, zoom * widthScale);
        int thickness = OverheadsHealthBarRenderer.barDimension(height, 2, 30, zoom * heightScale);
        OverheadsPosition position = resource == 0 ? config.healthPosition() : resource == 1 ? config.prayerPosition() : resource == 2 ? config.runPosition() : resource == 3 ? config.specialPosition() : config.indicatorPosition();
        Font baseFont = textFont.getFont();
        Font font = baseFont.deriveFont((float) Math.max(6, Math.min(96, baseFont.getSize2D() * zoom)));
        FontMetrics metrics = graphics.getFontMetrics(font);
        int step = (vertical ? thickness + metrics.stringWidth("Pray ~255/126") + 10
            : Math.max(thickness + 4, metrics.getHeight() + 6)) + Math.max(0, Math.min(20, config.componentGap())) + 1;
        Rectangle bounds = OverheadsPlacement.bounds(actor, anchor, position, vertical ? thickness : length, vertical ? length : thickness,
            xOffset + (vertical ? slot * step * (position == OverheadsPosition.LEFT ? -1 : 1) : 0),
            yOffset + (vertical ? 0 : slot * step * (position == OverheadsPosition.ABOVE ? 1 : -1)));
        if (bounds == null) { return; }
        OverheadsBarTextPosition textPosition = resource == 0 ? config.healthTextPosition() : resource == 1 ? config.prayerTextPosition()
            : resource == 2 ? config.runTextPosition() : resource == 3 ? config.specialTextPosition() : OverheadsBarTextPosition.ON_BAR;
        Rectangle painted = OverheadsCollisionLayout.barBounds(graphics, bounds, vertical, display, position, text,
            textFont, zoom, borderThickness, opacity, textColor.getAlpha(), textPosition);
        if (resource == 0)
        {
            double trail = healthTrail(actor, fraction, maximum, System.nanoTime() / 1_000_000L);
            OverheadsCollisionLayout.draw(layout, graphics, key, position, painted, target ->
                healthStyle.paintBar(target, bounds, vertical, display, position, fraction, trail, color,
                    config.healthBarFrontSecondaryColor(), opacity, text, textFont, textColor, zoom,
                    config.healthBarSegmentsEnabled() ? maximum : 0, false, 0, maximum));
            return;
        }
        OverheadsCollisionLayout.draw(layout, graphics, key, position, painted, target ->
        {
            Graphics2D g = (Graphics2D) target.create();
            try
            {
                g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, Math.min(100, opacity)) / 100f));
                double fill = Math.max(0, Math.min(1, fraction));
                OverheadsHealthBarFillDirection selectedDirection = resource == 1 ? config.prayerFillDirection()
                    : resource == 2 ? config.runFillDirection() : resource == 3 ? config.specialFillDirection() : OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
                OverheadsHealthBarFillDirection direction = vertical
                    ? (selectedDirection == OverheadsHealthBarFillDirection.TOP_TO_BOTTOM ? selectedDirection : OverheadsHealthBarFillDirection.BOTTOM_TO_TOP)
                    : (selectedDirection == OverheadsHealthBarFillDirection.RIGHT_TO_LEFT ? selectedDirection : OverheadsHealthBarFillDirection.LEFT_TO_RIGHT);
                if (display != OverheadsBarDisplay.NUMBERS_ONLY)
                {
                    OverheadsSprite front = style == OverheadsHealthBarStyle.NATIVE ? sprite(SpriteID.StandardHealth30.FRONT) : null;
                    OverheadsSprite back = style == OverheadsHealthBarStyle.NATIVE ? sprite(SpriteID.StandardHealth30.BACK) : null;
                    if (style == OverheadsHealthBarStyle.NATIVE && front != null && back != null)
                    {
                        OverheadsHealthBarRenderer.drawOsrsBar(g, bounds, vertical, fill, direction,
                            tint(front.getImage(), color), back.getImage());
                    }
                    else
                    {
                        OverheadsHealthBarRenderer.drawCustomBar(g, bounds.x, bounds.y, bounds.width, bounds.height, length * fill,
                            direction, OverheadsHealthBarGradient.SOLID, color, color, OverheadsHealthBarGradient.SOLID,
                            background, background, fill, border, borderThickness, cornerRadius);
                    }
                }
                if (display != OverheadsBarDisplay.BAR_ONLY)
                {
                    g.setComposite(AlphaComposite.SrcOver);
                    g.setFont(font);
                    java.awt.Point baseline = textPosition.baseline(bounds, metrics, text, vertical, display, position, borderThickness);
                    int x = baseline.x;
                    int y = baseline.y;
                    g.setColor(new Color(0, 0, 0, textColor.getAlpha()));
                    g.drawString(text, x + 1, y + 1);
                    g.setColor(textColor);
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

    double healthTrail(Actor actor, double fraction, int maximum, long nowMillis)
    {
        if (partySession != party.session())
        {
            healthStyle.clearCache();
            partySession = party.session();
        }
        if (!config.healthBarDamageTrailEnabled() || config.healthBarStyle() != OverheadsHealthBarStyle.SOLID)
        {
            healthStyle.remove(actor);
            return fraction;
        }
        return healthStyle.damageTrailState(actor, nowMillis).update(fraction, maximum, nowMillis,
            config.healthBarDamageTrailHold(), config.healthBarDamageTrailDrain());
    }

    private BufferedImage tint(BufferedImage source, Color color)
    {
        if (source != tintSource) { tinted.clear(); tintSource = source; }
        return tinted.computeIfAbsent(color, key -> OverheadsHealthBarRenderer.tintSprite(source, key));
    }

    void clear() {
        if (scoped != null) scoped.forEach(child -> child.clear());
        sprites.clear(); tinted.clear(); tintSource = null; healthStyle.clearCache(); partySession = Long.MIN_VALUE;
    }
}
