package com.improvedoverheads;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projection;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

final class OverheadsPlacement
{
    private static final BufferedImage PROJECTION = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private OverheadsPlacement()
    {
    }

    static double zoom(Client client, Actor actor, boolean enabled)
    {
        if (!enabled)
        {
            return 1.0;
        }
        WorldView view = actor.getWorldView();
        LocalPoint location = actor.getLocalLocation();
        Projection projection = view == null ? null : view.getCanvasProjection();
        if (location == null || projection == null)
        {
            return 1.0;
        }
        int z = Perspective.getFootprintTileHeight(client, location, view.getPlane(), actor.getFootprintSize())
            - actor.getAnimationHeightOffset() - actor.getLogicalHeight() / 2;
        float[] point = projection.project(location.getX(), z, location.getY(), new float[3]);
        return zoomFactor(client.getScale(), point == null || point.length < 3 ? Float.NaN : point[2]);
    }

    static double zoomFactor(int scale, float depth)
    {
        if (scale <= 0 || !Float.isFinite(depth) || depth < 50)
        {
            return 1.0;
        }
        return Math.max(0.25, Math.min(4.0, scale / (double) depth / 0.375));
    }

    static Rectangle bounds(Actor actor, Point top, OverheadsPosition position,
        int width, int height, int xOffset, int yOffset)
    {
        Point feet = actor.getCanvasImageLocation(PROJECTION, 0);
        Point middle = actor.getCanvasImageLocation(PROJECTION, actor.getLogicalHeight() / 2);
        if (top == null || feet == null || middle == null)
        {
            return null;
        }
        return bounds(top, middle, feet, position, width, height, xOffset, yOffset);
    }

    static Rectangle bounds(Point top, Point middle, Point feet, OverheadsPosition position,
        int width, int height, int xOffset, int yOffset)
    {
        int x;
        int y;
        switch (position)
        {
            case BELOW:
                x = feet.getX() - width / 2;
                y = feet.getY() + 6;
                break;
            case LEFT:
                x = middle.getX() - 18 - width;
                y = middle.getY() - height / 2;
                break;
            case RIGHT:
                x = middle.getX() + 18;
                y = middle.getY() - height / 2;
                break;
            case ABOVE:
            default:
                x = top.getX() - width / 2;
                y = top.getY() - height - 2;
                break;
        }
        return new Rectangle(x + xOffset, y - yOffset, width, height);
    }

    static Point bottomCenter(Rectangle rectangle)
    {
        return rectangle == null ? null
            : new Point(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height + 2);
    }
}
