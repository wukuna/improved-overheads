package com.improvedoverheads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import net.runelite.api.HitsplatID;

final class OverheadsModernHitsplatRenderer
{
    private OverheadsModernHitsplatRenderer()
    {
    }

    static boolean supports(int type)
    {
        return OverheadsHealthBarRenderer.isHealthDamageHitsplat(type)
            || type == HitsplatID.BLOCK_ME || type == HitsplatID.BLOCK_OTHER;
    }

    static Rectangle bounds(FontMetrics metrics, String text, int centerX, int centerY, int scale)
    {
        int padding = Math.max(2, OverheadsRenderLayout.scaleDimension(5, scale));
        int width = Math.max(OverheadsRenderLayout.scaleDimension(24, scale), metrics.stringWidth(text) + 2 * padding);
        int height = metrics.getAscent() + metrics.getDescent() + padding;
        return new Rectangle(centerX - width / 2, centerY - height / 2, width, height);
    }

    static Rectangle paintedBounds(Graphics2D graphics, String text, int scale,
        OverheadsHitsplatStyle style, OverheadsConfig config)
    {
        FontMetrics metrics = graphics.getFontMetrics();
        Rectangle textBounds = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), text)
            .getPixelBounds(graphics.getFontRenderContext(), -metrics.stringWidth(text) / 2f,
                (metrics.getAscent() - metrics.getDescent()) / 2f);
        if (config.hitsplatTextShadow())
        {
            textBounds.add(new Rectangle(textBounds.x + 1, textBounds.y + 1, textBounds.width, textBounds.height));
        }
        if (style == OverheadsHitsplatStyle.MODERN)
        {
            Rectangle badge = bounds(metrics, text, 0, 0, scale);
            int border = (int) Math.ceil(config.hitsplatBorderThickness() * scale / 200.0) + 1;
            badge.grow(border, border);
            textBounds.add(badge);
        }
        textBounds.grow(1, 1);
        return textBounds;
    }

    static void render(Graphics2D graphics, String text, int type, int centerX, int centerY,
        int scale, OverheadsHitsplatStyle style, OverheadsConfig config)
    {
        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            FontMetrics metrics = g.getFontMetrics();
            Rectangle bounds = bounds(metrics, text, centerX, centerY, scale);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (style == OverheadsHitsplatStyle.MODERN)
            {
                Color fill = color(type, config);
                int arc = OverheadsRenderLayout.scaleCoordinate(config.hitsplatCornerRadius(), scale);
                g.setColor(fill);
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, arc, arc);
                if (config.hitsplatBorderThickness() > 0)
                {
                    g.setColor(config.hitsplatBorderColor());
                    g.setStroke(new BasicStroke(Math.max(0.5f, config.hitsplatBorderThickness() * scale / 100f)));
                    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, arc, arc);
                }
            }
            int x = centerX - metrics.stringWidth(text) / 2;
            int y = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
            Color textColor = textColor(type, config);
            if (config.hitsplatTextShadow())
            {
                g.setColor(new Color(0, 0, 0, textColor.getAlpha()));
                g.drawString(text, x + 1, y + 1);
            }
            g.setColor(textColor);
            g.drawString(text, x, y);
        }
        finally
        {
            g.dispose();
        }
    }

    static Color textColor(int type, OverheadsConfig config)
    {
        switch (type)
        {
            case HitsplatID.BLOCK_ME:
            case HitsplatID.BLOCK_OTHER:
                return config.hitsplatMissNumberColor();
            case HitsplatID.POISON:
            case HitsplatID.VENOM:
            case HitsplatID.BURN:
                return config.hitsplatStatusNumberColor();
            default:
                return config.hitsplatNumberColor();
        }
    }

    private static Color color(int type, OverheadsConfig config)
    {
        switch (type)
        {
            case HitsplatID.BLOCK_ME:
            case HitsplatID.BLOCK_OTHER:
                return config.hitsplatMissColor();
            case HitsplatID.POISON:
                return config.hitsplatPoisonColor();
            case HitsplatID.VENOM:
                return config.hitsplatVenomColor();
            case HitsplatID.BURN:
                return config.hitsplatBurnColor();
            default:
                return config.hitsplatDamageColor();
        }
    }
}
