package com.improvedoverheads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

final class OverheadsPrayerIconStyle
{
    private OverheadsPrayerIconStyle()
    {
    }

    static OverheadsSprite create(OverheadsSprite source, OverheadsIconShape shape,
        Color background, Color border, int scalePercent)
    {
        return create(source, shape, background, border, scalePercent, 1, background);
    }

    static OverheadsSprite create(OverheadsSprite source, OverheadsIconShape shape,
        Color background, Color border, int scalePercent, double fraction, Color emptyBackground)
    {
        int percent = Math.max(25, Math.min(300, scalePercent));
        if (shape == OverheadsIconShape.ORIGINAL && percent == 100)
        {
            return source;
        }
        int width = Math.max(1, source.getWidth() * percent / 100);
        int height = Math.max(1, source.getHeight() * percent / 100);
        int padding = shape == OverheadsIconShape.ORIGINAL ? 0 : Math.max(3, percent * 3 / 100);
        int size = Math.max(width, height) + padding * 2;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (shape != OverheadsIconShape.ORIGINAL)
            {
                Shape frame = shape == OverheadsIconShape.ROUND
                    ? new Ellipse2D.Double(0.5, 0.5, size - 1, size - 1)
                    : new Rectangle2D.Double(0.5, 0.5, size - 1, size - 1);
                double level = Double.isFinite(fraction) ? Math.max(0, Math.min(1, fraction)) : 1;
                Shape clip = graphics.getClip();
                int filled = (int) Math.round(size * level);
                graphics.clipRect(0, 0, size, size - filled);
                graphics.setColor(emptyBackground);
                graphics.fill(frame);
                graphics.setClip(clip);
                graphics.clipRect(0, size - filled, size, filled);
                graphics.setColor(background);
                graphics.fill(frame);
                graphics.setClip(clip);
                graphics.setColor(border);
                graphics.setStroke(new BasicStroke(1));
                graphics.draw(frame);
            }
            graphics.drawImage(source.getImage(), (size - width) / 2, (size - height) / 2, width, height, null);
        }
        finally
        {
            graphics.dispose();
        }
        return new OverheadsSprite(image, 0, 0);
    }
}
