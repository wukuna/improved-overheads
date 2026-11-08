package com.improvedoverheads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Prayer;

final class OverheadsPrayerBadgeIcons
{
    private static final Color INK = new Color(20, 16, 14);
    private static final Color RED_EDGE = new Color(244, 51, 27);
    private static final Color PURPLE_EDGE = new Color(166, 35, 224);
    private static final Color BLUE_EDGE = new Color(126, 151, 217);

    private OverheadsPrayerBadgeIcons() { }

    static OverheadsSprite create(Prayer prayer, int requestedSize, boolean background, Color fill, Color border)
    {
        int size = Math.max(12, Math.min(28, requestedSize));
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (background)
            {
                Shape circle = new Ellipse2D.Double(.5, .5, size - 1, size - 1);
                g.setColor(fill);
                g.fill(circle);
                g.setColor(border);
                g.draw(circle);
            }
            g.translate(2, 2);
            g.scale((size - 4) / 16.0, (size - 4) / 16.0);
            draw(g, prayer);
        }
        finally { g.dispose(); }
        return new OverheadsSprite(image, 0, 0);
    }

    private static void draw(Graphics2D g, Prayer prayer)
    {
        Color silver = new Color(210, 224, 237), gold = new Color(255, 218, 104);
        Color red = new Color(245, 96, 99), green = new Color(126, 222, 126), blue = new Color(132, 198, 255);
        switch (prayer)
        {
            case THICK_SKIN: skin(g, INK); break;
            case ROCK_SKIN: skin(g, RED_EDGE); break;
            case STEEL_SKIN: skin(g, PURPLE_EDGE); break;
            case CLARITY_OF_THOUGHT: brain(g, INK); break;
            case IMPROVED_REFLEXES: brain(g, RED_EDGE); break;
            case INCREDIBLE_REFLEXES: brain(g, PURPLE_EDGE); break;
            case BURST_OF_STRENGTH: strength(g, INK); break;
            case SUPERHUMAN_STRENGTH: strength(g, RED_EDGE); break;
            case ULTIMATE_STRENGTH: strength(g, PURPLE_EDGE); break;
            case SHARP_EYE: ranged(g, new Color(201, 87, 5), INK); break;
            case HAWK_EYE: ranged(g, new Color(177, 181, 180), RED_EDGE); break;
            case EAGLE_EYE: ranged(g, new Color(206, 176, 20), PURPLE_EDGE); break;
            case DEADEYE: ranged(g, new Color(125, 132, 251), PURPLE_EDGE); break;
            case RIGOUR: ranged(g, new Color(105, 155, 42), BLUE_EDGE); break;
            case MYSTIC_WILL: crystal(g, new Color(70, 239, 36), INK); break;
            case MYSTIC_LORE: crystal(g, new Color(51, 158, 246), RED_EDGE); break;
            case MYSTIC_MIGHT: crystal(g, new Color(247, 72, 37), PURPLE_EDGE); break;
            case MYSTIC_VIGOUR: crystal(g, new Color(222, 189, 20), PURPLE_EDGE); break;
            case AUGURY: crystal(g, new Color(153, 82, 199), BLUE_EDGE); break;
            case RAPID_HEAL: heart(g, PURPLE_EDGE); break;
            case RAPID_RESTORE: heart(g, INK); break;
            case PROTECT_ITEM:
                outlined(g, new Color(130, 74, 20), PURPLE_EDGE,
                    path(2, 3, 10, 1, 14, 3, 14, 12, 9, 15, 2, 12));
                flat(g, new Color(84, 43, 14), path(2, 3, 6, 7, 6, 13, 2, 12));
                flat(g, new Color(180, 109, 31), path(3, 3, 10, 2, 13, 4, 10, 9, 8, 9));
                detail(g, new Color(61, 32, 12), 3, 3, 8, 9, 10, 9, 13, 4);
                flat(g, gold, path(10, 7, 12, 7, 12, 10, 10, 10));
                break;
            case PRESERVE:
                outlined(g, new Color(226, 173, 140), PURPLE_EDGE,
                    path(2, 4, 4, 4, 5, 10, 7, 12, 5, 15, 2, 12, 1, 8));
                outlined(g, new Color(226, 173, 140), PURPLE_EDGE,
                    path(14, 4, 12, 4, 11, 10, 9, 12, 11, 15, 14, 12, 15, 8));
                outlined(g, new Color(225, 47, 15), PURPLE_EDGE,
                    path(6.5, 1, 9.5, 1, 9.5, 5, 11, 7, 11, 10, 9, 12, 7, 12, 5, 10, 5, 7, 6.5, 5));
                flat(g, gold, path(7, 1, 9, 1, 9, 5, 7, 5));
                detail(g, new Color(255, 147, 66), 6.5, 7, 6.5, 9);
                break;
            case CHIVALRY:
                outlined(g, BLUE_EDGE, PURPLE_EDGE, path(3, 4, 8, 2, 13, 4, 12, 11, 8, 15, 4, 11));
                flat(g, new Color(86, 84, 150), path(8, 3, 12, 5, 11, 11, 8, 14));
                blade(g);
                break;
            case PIETY: wings(g, BLUE_EDGE); break;
            case RP_ANCIENT_SIGHT:
            case RP_ANNIHILATE:
                paint(g, new Color(204, 139, 242), path(4, 1, 10, 4, 13, 8, 10, 12, 4, 15, 7, 11, 8, 8, 7, 5));
                line(g, silver, 4, 1, 4, 15);
                line(g, Color.WHITE, 1, 8, 15, 8, 12, 5);
                marks(g, prayer == Prayer.RP_ANCIENT_SIGHT ? 1 : 3);
                break;
            case ZEAL:
                wings(g, gold);
                blade(g);
                break;
            case RP_ANCIENT_STRENGTH: arm(g, new Color(204, 139, 242), 1); break;
            case RP_DECIMATE: arm(g, new Color(204, 139, 242), 3); break;
            case RP_ANCIENT_WILL: star(g, new Color(204, 139, 242), 1); break;
            case RP_VAPORISE: star(g, new Color(204, 139, 242), 3); break;
            case RP_TRINITAS:
                paint(g, gold, path(8, 1, 15, 14, 1, 14));
                paint(g, new Color(204, 139, 242), path(8, 5, 11, 11, 5, 11));
                break;
            case RP_FUMUS_VOW:
                paint(g, green, path(2, 14, 2, 8, 5, 5, 4, 1, 9, 3, 8, 7, 12, 6, 15, 10, 13, 14));
                break;
            case RP_UMBRA_VOW:
                paint(g, new Color(204, 139, 242), path(12, 1, 6, 2, 2, 7, 3, 12, 7, 15, 13, 13, 8, 11, 6, 7, 8, 3));
                break;
            case RP_CRUORS_VOW:
                paint(g, red, path(8, 1, 14, 9, 13, 13, 8, 15, 3, 13, 2, 9));
                line(g, Color.WHITE, 5, 9, 5, 11);
                break;
            case RP_GLACIES_VOW:
                line(g, blue, 8, 1, 8, 15);
                line(g, blue, 2, 4, 14, 12);
                line(g, blue, 2, 12, 14, 4);
                paint(g, Color.WHITE, new Ellipse2D.Double(6, 6, 4, 4));
                break;
            case RP_INTENSIFY:
                paint(g, gold, path(2, 8, 8, 1, 14, 8, 10, 8, 10, 15, 6, 15, 6, 8));
                break;
            default: throw new IllegalArgumentException("Unsupported secondary prayer: " + prayer);
        }
    }

    private static void outlined(Graphics2D g, Color fill, Color edge, Shape shape)
    {
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(INK);
        g.draw(shape);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edge);
        g.draw(shape);
        flat(g, fill, shape);
    }

    private static void flat(Graphics2D g, Color color, Shape shape)
    {
        g.setColor(color);
        g.fill(shape);
    }

    private static void detail(Graphics2D g, Color color, double... points)
    {
        Path2D path = new Path2D.Double();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        g.draw(path);
    }

    private static void wings(Graphics2D g, Color edge)
    {
        outlined(g, new Color(192, 181, 220), edge,
            path(1, 5, 6, 5, 8, 7, 10, 5, 15, 5, 12, 11, 4, 11));
        Color shadow = new Color(110, 87, 157), feather = new Color(236, 229, 206);
        flat(g, shadow, path(2, 6, 6, 6, 8, 8, 10, 6, 14, 6, 11, 10, 5, 10));
        detail(g, feather, 2, 6, 5, 6, 7, 8, 7, 10);
        detail(g, feather, 14, 6, 11, 6, 9, 8, 9, 10);
        detail(g, feather, 4, 8, 5, 9);
        detail(g, feather, 12, 8, 11, 9);
    }

    private static void blade(Graphics2D g)
    {
        paint(g, new Color(231, 225, 195), path(7, 4, 9, 4, 9, 12, 8, 15, 7, 12));
        detail(g, Color.WHITE, 7.5, 5, 7.5, 12);
        detail(g, new Color(247, 180, 27), 8, 1, 8, 4);
        detail(g, new Color(255, 196, 35), 5, 4, 11, 4);
    }

    private static void ranged(Graphics2D g, Color color, Color edge)
    {
        outlined(g, color.darker(), edge,
            path(1, 8, 2, 4, 4, 5, 5, 2, 7, 4, 8, 1, 10, 4, 12, 3, 12, 6, 14, 5, 15, 9, 12, 13, 4, 13));
        flat(g, color, path(2, 9, 5, 6, 11, 6, 14, 9, 11, 12, 5, 12));
        detail(g, color.brighter(), 3, 9, 5, 7, 11, 7, 13, 9, 11, 11, 5, 11, 3, 9);
        flat(g, color.darker().darker(), new Ellipse2D.Double(6, 6.5, 4.5, 5));
        flat(g, color.brighter(), new Ellipse2D.Double(8.3, 7, 1.5, 1.5));
    }

    private static void crystal(Graphics2D g, Color color, Color edge)
    {
        outlined(g, color, edge, path(7, 1, 9, 5, 14, 11, 10, 11, 10, 13, 4, 15, 1, 12, 4, 7));
        flat(g, color.darker(), path(7, 2, 6, 12, 4, 14, 2, 12, 5, 7));
        flat(g, color.brighter(), path(7, 3, 8, 7, 12, 10, 8, 9, 6, 12));
        detail(g, color.darker(), 6, 12, 8, 9, 10, 11);
    }

    private static void heart(Graphics2D g, Color edge)
    {
        outlined(g, new Color(222, 81, 0), edge, path(8, 15, 2, 10, 1, 5, 3, 2, 6, 2, 8, 4, 10, 2, 13, 2, 15, 5, 14, 10));
        flat(g, new Color(167, 55, 0), path(12, 3, 14, 5, 13, 10, 8, 14, 7, 12, 11, 9, 12, 6));
    }

    private static void skin(Graphics2D g, Color edge)
    {
        outlined(g, new Color(172, 103, 77), edge,
            path(7, 1, 9, 1, 9, 4, 14, 4, 14, 7, 9, 7, 9, 15, 6, 15, 6, 7, 1, 7, 1, 4, 7, 4));
        detail(g, new Color(238, 166, 137), 8, 2, 8, 5, 13, 5);
        detail(g, new Color(235, 154, 121), 3, 5, 7, 5, 7, 14);
    }

    private static void brain(Graphics2D g, Color edge)
    {
        outlined(g, new Color(228, 188, 159), edge,
            path(1, 10, 2, 6, 5, 3, 10, 2, 13, 4, 15, 8, 15, 13, 12, 14, 5, 14, 2, 12));
        Color fold = new Color(113, 71, 53);
        detail(g, fold, 5, 4, 7, 6, 6, 9, 9, 12, 10, 14);
        detail(g, fold, 10, 4, 9, 7, 12, 8, 13, 6);
        detail(g, fold, 3, 9, 6, 9, 4, 12);
        detail(g, fold, 11, 10, 13, 12);
    }

    private static void strength(Graphics2D g, Color edge)
    {
        outlined(g, new Color(226, 174, 147), edge,
            path(2, 10, 4, 6, 7, 2, 10, 2, 10, 5, 7, 5, 6, 9, 9, 11, 12, 9, 14, 6, 14, 12, 12, 15, 8, 13, 4, 13));
        detail(g, new Color(255, 217, 184), 7, 3, 5, 7, 4, 10, 7, 11);
        detail(g, new Color(148, 98, 77), 9, 12, 12, 11);
    }

    private static void arm(Graphics2D g, Color color, int tier)
    {
        paint(g, color, path(1, 12, 4, 5, 6, 2, 11, 2, 12, 6, 8, 6, 8, 4, 6, 7, 8, 10, 12, 9, 15, 11, 14, 14, 5, 15));
        marks(g, tier);
    }

    private static void star(Graphics2D g, Color color, int tier)
    {
        paint(g, color, path(8, 0, 10, 5, 15, 7, 10, 9, 8, 14, 6, 9, 1, 7, 6, 5));
        marks(g, tier);
    }

    private static void marks(Graphics2D g, int count)
    {
        for (int i = 0; i < count; i++)
            paint(g, Color.WHITE, new java.awt.geom.Rectangle2D.Double(8 - count * 2 + i * 4, 13, 2, 2));
    }

    private static Path2D path(double... points)
    {
        Path2D path = new Path2D.Double();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        path.closePath();
        return path;
    }

    private static void paint(Graphics2D g, Color color, Shape shape)
    {
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(15, 19, 23, 230));
        g.draw(shape);
        g.setColor(color);
        g.fill(shape);
    }

    private static void line(Graphics2D g, Color color, double... points)
    {
        Path2D path = new Path2D.Double();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(15, 19, 23, 230));
        g.draw(path);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        g.draw(path);
    }
}
