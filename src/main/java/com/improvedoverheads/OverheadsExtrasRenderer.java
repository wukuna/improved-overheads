package com.improvedoverheads;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import java.util.function.Predicate;
import net.runelite.api.Point;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.FontType;
import net.runelite.client.util.Text;

final class OverheadsExtrasRenderer
{
    private OverheadsActorRenderers<OverheadsExtrasRenderer> scoped;
    @javax.inject.Inject
    void configureProfiles(OverheadsProfiles profiles)
    {
        scoped = new OverheadsActorRenderers<>(this, profiles, selected -> childRenderer(selected));
    }

    private final Client client;
    private final OverheadsConfig config;
    private final OverheadsActorUiSpriteCache sprites;
    private final BufferedImage[] tinted = new BufferedImage[2];
    private final BufferedImage[] sources = new BufferedImage[2];
    private final Color[] tints = new Color[2];

    OverheadsExtrasRenderer(Client client, OverheadsConfig config)
    {
        this.client = client;
        this.config = config;
        sprites = new OverheadsActorUiSpriteCache(client);
    }

    private OverheadsExtrasRenderer childRenderer(OverheadsConfig selected)
    {
        OverheadsExtrasRenderer child = new OverheadsExtrasRenderer(client, selected);
        child.setPartyMember(partyMember);
        return child;
    }

    private Predicate<String> partyMember = name -> false;

    void setPartyMember(Predicate<String> membership)
    {
        if (scoped != null) scoped.forEach(child -> child.setPartyMember(membership));

        partyMember = membership;
    }

    Color nameColor(Actor actor)
    {
        OverheadsExtrasRenderer selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.nameColor(actor); }

        if (!config.relationshipNames()) { return config.nameColor(); }
        if (actor == client.getLocalPlayer()) { return config.selfNameColor(); }
        if (actor instanceof Player)
        {
            Player player = (Player) actor;
            if (player.isFriend()) { return config.friendNameColor(); }
            if (player.isClanMember() || player.isFriendsChatMember()) { return config.clanNameColor(); }
            if (player.getName() != null && partyMember.test(player.getName())) { return config.partyNameColor(); }
            return config.playerNameColor();
        }
        if (actor instanceof NPC)
        {
            NPCComposition composition = ((NPC) actor).getTransformedComposition();
            if (composition != null)
            {
                if (composition.isFollower()) { return config.followerNameColor(); }
                String[] actions = composition.getActions();
                if (actions != null)
                {
                    for (String action : actions)
                    {
                        if ("Trade".equalsIgnoreCase(action) || "Trade-with".equalsIgnoreCase(action))
                        {
                            return config.shopNameColor();
                        }
                    }
                }
            }
        }
        return config.npcNameColor();
    }

    private boolean showName(Actor actor)
    {
        OverheadsVisibility visibility = actor == client.getLocalPlayer() ? config.selfNameVisibility()
            : actor instanceof NPC ? config.npcNameVisibility() : config.playerNameVisibility();
        return actor.getName() != null && visibility.includes(client, actor)
            && (nameColor(actor).getAlpha() > 0 || config.nameBackground().getAlpha() > 0);
    }

    boolean hasContent(Actor actor)
    {
        OverheadsExtrasRenderer selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.hasContent(actor); }

        return showName(actor) || statusMask(actor) != 0 || actor == client.getLocalPlayer()
            && (resourceVisible(false) || resourceVisible(true));
    }

    int render(Graphics2D graphics, Actor actor, Point anchor, int occupied)
    {
        return render(graphics, actor, anchor, occupied, null);
    }

    int render(Graphics2D graphics, Actor actor, Point anchor, int occupied, OverheadsCollisionLayout layout)
    {
        OverheadsExtrasRenderer selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.render(graphics, actor, anchor, occupied, layout); }

        if (actor == client.getLocalPlayer())
        {
            occupied = renderResource(graphics, actor, anchor, layout == null ? occupied : Integer.MAX_VALUE, false, layout);
            occupied = renderResource(graphics, actor, anchor, layout == null ? occupied : Integer.MAX_VALUE, true, layout);
        }
        int mask = statusMask(actor);
        if (mask != 0)
        {
            occupied = renderStatus(graphics, actor, anchor, layout == null ? occupied : Integer.MAX_VALUE, mask, layout);
        }
        if (showName(actor))
        {
            occupied = renderName(graphics, actor, anchor, layout == null ? occupied : Integer.MAX_VALUE, layout);
        }
        return occupied;
    }

    int statusMask(Actor actor)
    {
        OverheadsExtrasRenderer selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.statusMask(actor); }

        int mask = 0;
        if (config.showStatusIcons() && actor == client.getLocalPlayer())
        {
            int poison = client.getVarpValue(VarPlayerID.POISON);
            mask = poison >= 1_000_000 ? 2 : poison > 0 ? 1 : 0;
            if (client.getVarpValue(VarPlayerID.DISEASE) > 0)
            {
                mask |= 4;
            }
        }
        if (config.showTargetingIcon() && actor instanceof NPC && client.getLocalPlayer() != null
            && client.getLocalPlayer().getInteracting() == actor)
        {
            mask |= 8;
        }
        return mask;
    }

    private int renderStatus(Graphics2D graphics, Actor actor, Point anchor, int occupied, int mask, OverheadsCollisionLayout layout)
    {
        double zoom = OverheadsPlacement.zoom(client, actor, config.statusScaleWithZoom());
        int size = OverheadsHealthBarRenderer.barDimension(config.statusIconSize(), 10, 40, zoom);
        int count = Integer.bitCount(mask);
        Rectangle bounds = placed(actor, anchor, occupied, config.statusPosition(), count * (size + 3) - 3, size,
            config.statusXOffset(), config.statusYOffset());
        if (bounds == null)
        {
            return occupied;
        }
        Rectangle painted = new Rectangle(bounds);
        painted.grow(2, 2);
        OverheadsCollisionLayout.draw(layout, graphics, "status", config.statusPosition(), painted, target ->
        {
        Graphics2D g = (Graphics2D) target.create();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(8, size * 2 / 3)));
            String[] labels = {"P", "V", "D", "+"};
            Color[] colors = {new Color(90, 160, 55), new Color(30, 115, 75), new Color(185, 133, 40), new Color(205, 76, 55)};
            int x = bounds.x;
            for (int i = 0; i < labels.length; i++)
            {
                if ((mask & 1 << i) == 0)
                {
                    continue;
                }
                g.setColor(new Color(12, 18, 24, 230));
                g.fillOval(x - 1, bounds.y - 1, size + 2, size + 2);
                g.setColor(colors[i]);
                g.fillOval(x, bounds.y, size, size);
                FontMetrics metrics = g.getFontMetrics();
                g.setColor(Color.WHITE);
                g.drawString(labels[i], x + (size - metrics.stringWidth(labels[i])) / 2,
                    bounds.y + (size - metrics.getHeight()) / 2 + metrics.getAscent());
                x += size + 3;
            }
        }
        finally
        {
            g.dispose();
        }
        });
        return top(occupied, bounds, config.statusPosition());
    }

    private int renderName(Graphics2D graphics, Actor actor, Point anchor, int occupied, OverheadsCollisionLayout layout)
    {
        String name = Text.removeTags(actor.getName()).replace('\n', ' ');
        if (name.length() > 80)
        {
            name = name.substring(0, 80);
        }
        double zoom = OverheadsPlacement.zoom(client, actor, config.nameScaleWithZoom());
        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setFont(scaledFont(config.nameFont(), zoom));
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics metrics = g.getFontMetrics();
            Rectangle bounds = placed(actor, anchor, occupied, config.namePosition(), metrics.stringWidth(name) + 8,
                metrics.getHeight() + 4, config.nameXOffset(), config.nameYOffset());
            if (bounds == null)
            {
                return occupied;
            }
            String displayName = name;
            OverheadsCollisionLayout.draw(layout, g, "name", config.namePosition(), bounds, target ->
            {
            target.setColor(config.nameBackground());
            target.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 5, 5);
            target.setColor(new Color(0, 0, 0, nameColor(actor).getAlpha()));
            target.drawString(displayName, bounds.x + 5, bounds.y + metrics.getAscent() + 3);
            target.setColor(nameColor(actor));
            target.drawString(displayName, bounds.x + 4, bounds.y + metrics.getAscent() + 2);
            });
            return top(occupied, bounds, config.namePosition());
        }
        finally
        {
            g.dispose();
        }
    }

    boolean resourceVisible(boolean special)
    {
        OverheadsBarDisplay display = special ? config.specialDisplay() : config.runDisplay();
        if (display == OverheadsBarDisplay.HIDDEN) { return false; }
        boolean hideFull = special ? config.specialHideWhenFull() : config.runHideWhenFull();
        return !hideFull || (special ? client.getVarpValue(VarPlayerID.SA_ENERGY) < 1000 : client.getEnergy() < 10000);
    }

    private int renderResource(Graphics2D graphics, Actor actor, Point anchor, int occupied, boolean special, OverheadsCollisionLayout layout)
    {
        OverheadsBarDisplay display = special ? config.specialDisplay() : config.runDisplay();
        if (!resourceVisible(special))
        {
            return occupied;
        }
        double zoom = OverheadsPlacement.zoom(client, actor, special ? config.specialScaleWithZoom() : config.runScaleWithZoom());
        boolean vertical = (special ? config.specialOrientation() : config.runOrientation()) == OverheadsOrientation.VERTICAL;
        int length = OverheadsHealthBarRenderer.barDimension(special ? config.specialWidth() : config.runWidth(), 10, 200, zoom);
        int thickness = OverheadsHealthBarRenderer.barDimension(special ? config.specialHeight() : config.runHeight(), 2, 30, zoom);
        OverheadsPosition position = special ? config.specialPosition() : config.runPosition();
        Rectangle bounds = placed(actor, anchor, occupied, position, vertical ? thickness : length, vertical ? length : thickness,
            special ? config.specialXOffset() : config.runXOffset(), special ? config.specialYOffset() : config.runYOffset());
        if (bounds == null)
        {
            return occupied;
        }
        double value = Math.max(0, Math.min(100, special ? client.getVarpValue(VarPlayerID.SA_ENERGY) / 10.0 : client.getEnergy() / 100.0));
        Color fill = special ? config.specialColor() : config.runColor();
        OverheadsHealthBarStyle style = special ? config.specialStyle() : config.runStyle();
        OverheadsHealthBarFillDirection selected = special ? config.specialFillDirection() : config.runFillDirection();
        OverheadsHealthBarFillDirection direction = vertical
            ? selected == OverheadsHealthBarFillDirection.TOP_TO_BOTTOM ? selected : OverheadsHealthBarFillDirection.BOTTOM_TO_TOP
            : selected == OverheadsHealthBarFillDirection.RIGHT_TO_LEFT ? selected : OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
        String displayed = (special ? config.specialNumberFormat() : config.runNumberFormat()).format((int) value, 100);
        Rectangle painted = OverheadsCollisionLayout.barBounds(graphics, bounds, vertical, display, position, displayed,
            special ? config.specialFont() : config.runFont(), zoom, 1,
            special ? config.specialOpacity() : config.runOpacity(),
            (special ? config.specialTextColor() : config.runTextColor()).getAlpha());
        OverheadsCollisionLayout.draw(layout, graphics, special ? "special" : "run", position, painted, target ->
        {
        Graphics2D g = (Graphics2D) target.create();
        try
        {
            if (display != OverheadsBarDisplay.NUMBERS_ONLY)
            {
                int opacity = special ? config.specialOpacity() : config.runOpacity();
                g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, Math.min(100, opacity)) / 100f));
                boolean nativeDrawn = style == OverheadsHealthBarStyle.NATIVE && nativeBar(g, bounds, vertical, value / 100, direction, fill, special);
                if (!nativeDrawn)
                {
                    Color back = special ? config.specialBackColor() : config.runBackColor();
                    OverheadsHealthBarRenderer.drawCustomBar(g, bounds.x, bounds.y, bounds.width, bounds.height,
                        (vertical ? bounds.height : bounds.width) * value / 100, direction,
                        OverheadsHealthBarGradient.SOLID, fill, fill, OverheadsHealthBarGradient.SOLID, back, back, value / 100,
                        special ? config.specialBorderColor() : config.runBorderColor(), 1,
                        special ? config.specialCornerRadius() : config.runCornerRadius());
                }
            }
            if (display != OverheadsBarDisplay.BAR_ONLY)
            {
                g.setComposite(AlphaComposite.SrcOver);
                g.setFont(scaledFont(special ? config.specialFont() : config.runFont(), zoom));
                FontMetrics metrics = g.getFontMetrics();
                String text = (special ? config.specialNumberFormat() : config.runNumberFormat()).format((int) value, 100);
                int x = bounds.x + (bounds.width - metrics.stringWidth(text)) / 2;
                if (vertical && display == OverheadsBarDisplay.BOTH)
                {
                    x = position == OverheadsPosition.RIGHT ? bounds.x + bounds.width + 4 : bounds.x - metrics.stringWidth(text) - 4;
                }
                int y = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
                Color textColor = special ? config.specialTextColor() : config.runTextColor();
                g.setColor(new Color(0, 0, 0, textColor.getAlpha()));
                g.drawString(text, x + 1, y + 1);
                g.setColor(textColor);
                g.drawString(text, x, y);
            }
        }
        finally
        {
            g.dispose();
        }
        });
        return top(occupied, painted == null ? bounds : painted, position);
    }

    private boolean nativeBar(Graphics2D graphics, Rectangle bounds, boolean vertical, double fraction,
        OverheadsHealthBarFillDirection direction, Color fill, boolean special)
    {
        OverheadsSprite front = sprite(SpriteID.StandardHealth30.FRONT);
        OverheadsSprite back = sprite(SpriteID.StandardHealth30.BACK);
        if (front == null || back == null)
        {
            return false;
        }
        int index = special ? 1 : 0;
        if (sources[index] != front.getImage() || !fill.equals(tints[index]))
        {
            sources[index] = front.getImage();
            tints[index] = fill;
            tinted[index] = OverheadsHealthBarRenderer.tintSprite(front.getImage(), fill);
        }
        OverheadsHealthBarRenderer.drawOsrsBar(graphics, bounds, vertical, fraction, direction, tinted[index], back.getImage());
        return true;
    }

    private OverheadsSprite sprite(int id)
    {
        OverheadsSprite override = sprites.getResourcePackOverride(id);
        return override == null ? sprites.get(id, 0) : override;
    }

    private static Font scaledFont(FontType type, double zoom)
    {
        Font font = type.getFont();
        return font.deriveFont((float) Math.max(6, Math.min(96, font.getSize2D() * zoom)));
    }

    private static Rectangle placed(Actor actor, Point anchor, int occupied, OverheadsPosition position,
        int width, int height, int xOffset, int yOffset)
    {
        Point top = position == OverheadsPosition.ABOVE && occupied != OverheadsHealthBarRenderer.NO_OCCUPIED_TOP
            ? new Point(anchor.getX(), Math.min(anchor.getY(), occupied) - 3) : anchor;
        return OverheadsPlacement.bounds(actor, top, position, width, height, xOffset, yOffset);
    }

    private static int top(int occupied, Rectangle bounds, OverheadsPosition position)
    {
        return position == OverheadsPosition.ABOVE ? Math.min(occupied, bounds.y - 1) : occupied;
    }

    void clear()
    {
        if (scoped != null) scoped.forEach(child -> child.clear());

        sprites.clear();
        java.util.Arrays.fill(sources, null);
        java.util.Arrays.fill(tinted, null);
        java.util.Arrays.fill(tints, null);
    }
}
