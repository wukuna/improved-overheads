package com.improvedoverheads;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.api.HitsplatID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsHitsplatAppearanceTest
{
    @Test
    public void independentStatusAndMissBackgroundsReachTheRenderer()
    {
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        when(config.hitsplatDamageColor()).thenReturn(Color.RED);
        when(config.hitsplatMissColor()).thenReturn(Color.BLUE);
        when(config.hitsplatPoisonColor()).thenReturn(Color.GREEN);
        when(config.hitsplatVenomColor()).thenReturn(Color.CYAN);
        when(config.hitsplatBurnColor()).thenReturn(Color.ORANGE);
        when(config.hitsplatBorderThickness()).thenReturn(0);
        when(config.hitsplatCornerRadius()).thenReturn(0);
        int[] types = {HitsplatID.DAMAGE_ME, HitsplatID.BLOCK_ME, HitsplatID.POISON, HitsplatID.VENOM, HitsplatID.BURN};
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.ORANGE};
        for (int i = 0; i < types.length; i++)
        {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setFont(new Font("Dialog", Font.BOLD, 14));
            Rectangle badge = OverheadsModernHitsplatRenderer.bounds(g.getFontMetrics(), "42", 50, 50, 100);
            OverheadsModernHitsplatRenderer.render(g, "42", types[i], 50, 50, 100, OverheadsHitsplatStyle.MODERN, config);
            g.dispose();
            assertEquals(colors[i].getRGB(), image.getRGB(badge.x + 2, 50));
        }
    }

    @Test
    public void paintedBoundsContainThickBordersItalicNumbersAndShadows()
    {
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        when(config.hitsplatBorderThickness()).thenReturn(5);
        for (OverheadsHitsplatStyle style : new OverheadsHitsplatStyle[]{OverheadsHitsplatStyle.MODERN, OverheadsHitsplatStyle.NUMBERS})
        {
            for (int scale : new int[]{50, 100, 200})
            {
                BufferedImage image = new BufferedImage(500, 300, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 36 * scale / 100));
                Rectangle bounds = OverheadsModernHitsplatRenderer.paintedBounds(g, "9999", scale, style, config);
                bounds.translate(250, 150);
                OverheadsModernHitsplatRenderer.render(g, "9999", HitsplatID.DAMAGE_ME, 250, 150, scale, style, config);
                g.dispose();
                Rectangle pixels = OverheadsRenderPipelineTest.pixelBounds(image);
                assertNotNull(pixels);
                assertTrue(style + " " + scale + " " + bounds + " " + pixels, bounds.contains(pixels));
            }
        }
    }

    @Test
    public void numbersUseDistinctDamageMissAndStatusTextColors()
    {
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        when(config.hitsplatNumberColor()).thenReturn(Color.RED);
        when(config.hitsplatMissNumberColor()).thenReturn(Color.BLUE);
        when(config.hitsplatStatusNumberColor()).thenReturn(Color.GREEN);
        assertEquals(Color.RED, OverheadsModernHitsplatRenderer.textColor(HitsplatID.DAMAGE_OTHER, config));
        assertEquals(Color.BLUE, OverheadsModernHitsplatRenderer.textColor(HitsplatID.BLOCK_OTHER, config));
        assertEquals(Color.GREEN, OverheadsModernHitsplatRenderer.textColor(HitsplatID.POISON, config));
        assertEquals(Color.GREEN, OverheadsModernHitsplatRenderer.textColor(HitsplatID.VENOM, config));
        assertEquals(Color.GREEN, OverheadsModernHitsplatRenderer.textColor(HitsplatID.BURN, config));
    }
}
