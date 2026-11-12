package com.improvedoverheads;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.runelite.client.config.FontType;

final class OverheadsCollisionLayout implements AutoCloseable
{
    static final class Memory
    {
        private final Map<String, Slot> slots = new HashMap<>();
        private long sequence;
    }

    private static final class Slot
    {
        private final long order;
        private final OverheadsPosition side;
        private Point shift = new Point();

        private Slot(long order, OverheadsPosition side)
        {
            this.order = order;
            this.side = side;
        }
    }

    private static final class Command
    {
        private final Graphics2D graphics;
        private final String key;
        private final Slot slot;
        private final Rectangle bounds;
        private final Consumer<Graphics2D> painter;

        private Command(Graphics2D graphics, String key, Slot slot, Rectangle bounds, Consumer<Graphics2D> painter)
        {
            this.graphics = graphics;
            this.key = key;
            this.slot = slot;
            this.bounds = new Rectangle(bounds);
            this.painter = painter;
        }
    }

    private Rectangle viewport;
    private boolean preview;
    private boolean avoid = true;

    void configure(Rectangle viewport, boolean preview, boolean avoid)
    {
        this.viewport = viewport;
        this.preview = preview;
        this.avoid = avoid;
    }

    private final Memory memory;
    private final int gap;
    private final List<Rectangle> chatObstacles;
    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Rectangle> resolved = new HashMap<>();

    void compact(String key)
    {
        Slot slot = memory.slots.get(key);
        if (slot != null) slot.shift.setLocation(0, 0);
    }

    OverheadsCollisionLayout(Memory memory, int gap)
    {
        this(memory, gap, new ArrayList<>());
    }

    OverheadsCollisionLayout(Memory memory, int gap, List<Rectangle> chatObstacles)
    {
        this.memory = memory;
        this.gap = Math.max(0, Math.min(20, gap));
        this.chatObstacles = chatObstacles;
    }

    static void draw(OverheadsCollisionLayout layout, Graphics2D graphics, String key, OverheadsPosition side,
        Rectangle bounds, Consumer<Graphics2D> painter)
    {
        if (layout == null)
        {
            painter.accept(graphics);
            return;
        }
        if (bounds == null || bounds.isEmpty())
        {
            return;
        }
        Slot slot = layout.memory.slots.get(key);
        if (slot == null || slot.side != side)
        {
            slot = new Slot(layout.memory.sequence++, side);
            layout.memory.slots.put(key, slot);
        }
        layout.commands.add(new Command((Graphics2D) graphics.create(), key, slot, bounds, painter));
    }

    void render()
    {
        commands.sort(Comparator.<Command>comparingInt(command -> nativeHealth(command) ? 0 : 1)
            .thenComparingLong(command -> command.slot.order));
        List<Rectangle> occupied = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Command command : commands)
        {
            List<Rectangle> obstacles = occupied;
            if (command.key.equals("chat") && !chatObstacles.isEmpty())
            {
                obstacles = new ArrayList<>(occupied);
                obstacles.addAll(chatObstacles);
            }
            Point previous = nativeHealth(command) ? new Point() : command.slot.shift;
            Point shift = avoid ? resolve(command.bounds, command.slot.side, previous, obstacles, gap) : new Point();
            command.slot.shift = shift;
            Rectangle actual = new Rectangle(command.bounds);
            actual.translate(shift.x, shift.y);
            occupied.add(actual);
            resolved.put(command.key, actual);
            seen.add(command.key);
        }
        Rectangle union = null;
        for (Rectangle bounds : resolved.values()) { union = union == null ? new Rectangle(bounds) : union.union(bounds); }
        Point adjustment = fit(union, viewport);
        for (Command command : commands)
        {
            Rectangle actual = resolved.get(command.key);
            actual.translate(adjustment.x, adjustment.y);
            if (command.key.equals("chat")) { chatObstacles.add(new Rectangle(actual)); }
            if (viewport != null) { command.graphics.clip(viewport); }
            command.graphics.translate(command.slot.shift.x + adjustment.x, command.slot.shift.y + adjustment.y);
            command.painter.accept(command.graphics);
            if (preview)
            {
                command.graphics.setColor(new java.awt.Color(80, 220, 255, 180));
                command.graphics.draw(command.bounds);
            }
        }
        memory.slots.keySet().retainAll(seen);
    }

    private static boolean nativeHealth(Command command)
    {
        return command.key.equals("health") && command.slot.side == OverheadsPosition.ABOVE;
    }

    static Point fit(Rectangle bounds, Rectangle viewport)
    {
        if (bounds == null || viewport == null || viewport.isEmpty()) { return new Point(); }
        int x = Math.max(viewport.x, Math.min(bounds.x, viewport.x + viewport.width - bounds.width));
        int y = Math.max(viewport.y, Math.min(bounds.y, viewport.y + viewport.height - bounds.height));
        return new Point(x - bounds.x, y - bounds.y);
    }

    Map<String, Rectangle> resolvedBounds()
    {
        return new HashMap<>(resolved);
    }

    static Point resolve(Rectangle requested, OverheadsPosition side, Point previous,
        List<Rectangle> occupied, int gap)
    {
        Point shift = new Point(previous);
        Rectangle candidate = new Rectangle(requested);
        candidate.translate(shift.x, shift.y);
        for (int pass = 0; pass <= occupied.size(); pass++)
        {
            boolean moved = false;
            for (Rectangle obstacle : occupied)
            {
                Rectangle padded = new Rectangle(obstacle);
                padded.grow(gap, gap);
                if (!candidate.intersects(padded))
                {
                    continue;
                }
                switch (side)
                {
                    case LEFT:
                        shift.x = padded.x - requested.x - requested.width;
                        break;
                    case RIGHT:
                        shift.x = padded.x + padded.width - requested.x;
                        break;
                    case BELOW:
                        shift.y = padded.y + padded.height - requested.y;
                        break;
                    case ABOVE:
                    default:
                        shift.y = padded.y - requested.y - requested.height;
                        break;
                }
                candidate.setLocation(requested.x + shift.x, requested.y + shift.y);
                moved = true;
            }
            if (!moved)
            {
                break;
            }
        }
        return shift;
    }

    static Rectangle barBounds(Graphics2D graphics, Rectangle bar, boolean vertical, OverheadsBarDisplay display,
        OverheadsPosition side, String text, FontType fontType, double zoom, double border, int opacity, int textAlpha)
    {
        return barBounds(graphics, bar, vertical, display, side, text, fontType, zoom, border, opacity, textAlpha, OverheadsBarTextPosition.ON_BAR);
    }

    static Rectangle barBounds(Graphics2D graphics, Rectangle bar, boolean vertical, OverheadsBarDisplay display,
        OverheadsPosition side, String text, FontType fontType, double zoom, double border, int opacity, int textAlpha, OverheadsBarTextPosition textPosition)
    {
        Rectangle bounds = null;
        if (display != OverheadsBarDisplay.NUMBERS_ONLY && display != OverheadsBarDisplay.HIDDEN && opacity > 0)
        {
            bounds = new Rectangle(bar);
            int padding = (int) Math.ceil(Math.max(0, Math.min(10, border))) + 1;
            bounds.grow(padding, padding);
        }
        if (display != OverheadsBarDisplay.BAR_ONLY && display != OverheadsBarDisplay.HIDDEN && textAlpha > 0)
        {
            Font font = fontType.getFont();
            Font scaled = font.deriveFont((float) Math.max(6, Math.min(96, font.getSize2D() * zoom)));
            FontMetrics metrics = graphics.getFontMetrics(scaled);
            int width = metrics.stringWidth(text);
            Point baseline = textPosition.baseline(bar, metrics, text, vertical, display, side, border);
            int x = baseline.x;
            Rectangle numbers = new Rectangle(x - 1, baseline.y - metrics.getAscent() - 1,
                width + 3, metrics.getHeight() + 5);
            Rectangle ink = scaled.createGlyphVector(graphics.getFontRenderContext(), text).getPixelBounds(
                graphics.getFontRenderContext(), x, baseline.y);
            ink.grow(2, 2);
            numbers = numbers.union(ink);
            bounds = bounds == null ? numbers : bounds.union(numbers);
        }
        return bounds;
    }

    @Override
    public void close()
    {
        commands.forEach(command -> command.graphics.dispose());
        commands.clear();
    }
}
