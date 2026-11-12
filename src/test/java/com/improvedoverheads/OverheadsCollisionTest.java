package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.HeadIcon;
import net.runelite.api.HitsplatID;
import net.runelite.api.gameval.SpriteID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsCollisionTest
{
    @Test
    public void componentsSeparateOutwardOnAllSidesAndKeepManualOffsets()
    {
        for (OverheadsPosition side : OverheadsPosition.values())
        {
            OverheadsCollisionLayout.Memory memory = new OverheadsCollisionLayout.Memory();
            Rectangle first = new Rectangle(100, 100, 70, 30);
            Rectangle offset = new Rectangle(500, 450, 20, 20);
            Map<String, Rectangle> result = frame(memory, side, first, first, offset);
            assertEquals(first, result.get("first"));
            assertEquals(offset, result.get("offset"));
            Rectangle second = result.get("second");
            switch (side)
            {
                case LEFT: assertEquals(first.x - 4, second.x + second.width); break;
                case RIGHT: assertEquals(first.x + first.width + 4, second.x); break;
                case ABOVE: assertEquals(first.y - 4, second.y + second.height); break;
                case BELOW: assertEquals(first.y + first.height + 4, second.y); break;
            }
            assertSeparated(result, 4);
        }
    }

    @Test
    public void continuouslyVisibleComponentsStayPutWhenOthersAppearAndExpire()
    {
        OverheadsCollisionLayout.Memory memory = new OverheadsCollisionLayout.Memory();
        Rectangle base = new Rectangle(100, 100, 70, 30);
        Map<String, Rectangle> first = frame(memory, OverheadsPosition.ABOVE, base, base, null);
        Map<String, Rectangle> withoutFirst = frame(memory, OverheadsPosition.ABOVE, null, base, null);
        assertEquals(first.get("second"), withoutFirst.get("second"));
        Map<String, Rectangle> returning = frame(memory, OverheadsPosition.ABOVE, base, base, null);
        assertEquals(first.get("second"), returning.get("second"));
        assertSeparated(returning, 4);
        Rectangle translated = new Rectangle(base);
        translated.translate(20, 30);
        Map<String, Rectangle> cameraMoved = frame(memory, OverheadsPosition.ABOVE, translated, translated, null);
        Rectangle expected = new Rectangle(returning.get("second"));
        expected.translate(20, 30);
        assertEquals(expected, cameraMoved.get("second"));
    }

    @Test
    public void changingSizeAndCrossingSidesRechecksFullBounds()
    {
        OverheadsCollisionLayout.Memory memory = new OverheadsCollisionLayout.Memory();
        Rectangle small = new Rectangle(100, 100, 30, 10);
        frame(memory, OverheadsPosition.RIGHT, small, small, null);
        Map<String, Rectangle> enlarged = frame(memory, OverheadsPosition.RIGHT,
            new Rectangle(95, 95, 100, 80), new Rectangle(100, 100, 80, 60), null);
        assertSeparated(enlarged, 4);
        Map<String, Rectangle> changed = frame(memory, OverheadsPosition.LEFT, small, small, null);
        assertEquals(small, changed.get("first"));
        assertTrue(changed.get("second").x < small.x);
    }

    @Test
    public void fullPipelineSeparatesBarsNamesChatIconsAndHitsIncludingVerticalNumbers()
    {
        for (OverheadsPosition side : OverheadsPosition.values())
        {
            OverheadsRenderPipelineTest.Fixture f = fixture(side);
            for (OverheadsOrientation orientation : OverheadsOrientation.values())
            {
                when(f.config.healthOrientation()).thenReturn(orientation);
                when(f.config.prayerOrientation()).thenReturn(orientation);
                f.overlay.reflow();
                BufferedImage image = renderLarge(f);
                Color[] colors = {Color.RED, Color.CYAN, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.PINK};
                Map<String, Rectangle> components = new HashMap<>();
                for (Color color : colors)
                {
                    Rectangle bounds = colorBounds(image, color);
                    assertNotNull(side + "/" + orientation + "/" + color, bounds);
                    components.put(color.toString(), bounds);
                }
                assertSeparated(components, 1);
            }
        }
    }

    @Test
    public void healthAppearingAfterPrayerStaysUnderItAcrossRepeatedTransitions()
    {
        for (OverheadsHealthPosition placement : new OverheadsHealthPosition[]{OverheadsHealthPosition.NATIVE, OverheadsHealthPosition.ABOVE})
        {
            for (OverheadsPrayerLayout prayerLayout : OverheadsPrayerLayout.values())
            {
                OverheadsRenderPipelineTest.Fixture f = fixture(OverheadsPosition.ABOVE);
                when(f.config.healthPosition()).thenReturn(placement);
                when(f.config.prayerIconLayout()).thenReturn(prayerLayout);
                when(f.config.prayerIconPosition()).thenReturn(OverheadsPosition.ABOVE);
                when(f.config.prayerIconCombatPosition()).thenReturn(OverheadsPosition.ABOVE);
                when(f.player.getOverheadText()).thenReturn(null);
                when(f.config.healthBarsEnabled()).thenReturn(false);
                assertNotNull(colorBounds(renderLarge(f), Color.BLUE));
                when(f.config.healthBarsEnabled()).thenReturn(true);
                BufferedImage first = renderLarge(f);
                Rectangle health = colorBounds(first, Color.RED);
                Rectangle prayer = colorBounds(first, Color.BLUE);
                assertNotNull(health); assertNotNull(prayer);
                assertTrue(placement + "/" + prayerLayout, prayer.y + prayer.height < health.y);
                when(f.config.healthBarsEnabled()).thenReturn(false); renderLarge(f);
                when(f.config.healthBarsEnabled()).thenReturn(true);
                assertEquals(health, colorBounds(renderLarge(f), Color.RED));
                when(f.player.getOverheadIcon()).thenReturn(null); renderLarge(f);
                when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
                BufferedImage returning = renderLarge(f);
                assertEquals(health, colorBounds(returning, Color.RED));
                prayer = colorBounds(returning, Color.BLUE);
                assertTrue(prayer.y + prayer.height < health.y);
            }
        }
    }

    @Test
    public void nativeHealthRemainsAtItsAnchorWhenChatAppearsWithoutOverlapHandling()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture(OverheadsPosition.ABOVE);
        when(f.config.healthPosition()).thenReturn(OverheadsHealthPosition.NATIVE);
        when(f.config.avoidOverlap()).thenReturn(false);
        when(f.config.keepOnScreen()).thenReturn(false);
        when(f.config.layoutPreview()).thenReturn(false);
        when(f.player.getOverheadText()).thenReturn(null);
        Rectangle health = colorBounds(renderLarge(f), Color.RED);
        when(f.player.getOverheadText()).thenReturn("Native HP stays put");
        Rectangle withChat = colorBounds(renderLarge(f), Color.RED);
        assertEquals(health.getLocation(), withChat.getLocation());
        assertEquals(health.width, withChat.width);
    }

    @Test
    public void chatAppearanceDoesNotMoveExistingHealthAndManualModeRemainsAvailable()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture(OverheadsPosition.ABOVE);
        when(f.player.getOverheadText()).thenReturn(null);
        Rectangle healthBefore = colorBounds(renderLarge(f), Color.RED);
        when(f.player.getOverheadText()).thenReturn("A message appears");
        Rectangle healthAfter = colorBounds(renderLarge(f), Color.RED);
        assertEquals(healthBefore, healthAfter);
        when(f.config.avoidOverlap()).thenReturn(false);
        when(f.config.healthPosition()).thenReturn(OverheadsHealthPosition.RIGHT);
        when(f.config.prayerPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.healthBarOpacity()).thenReturn(50);
        assertNotNull(OverheadsRenderPipelineTest.pixelBounds(renderLarge(f)));
    }

    private static Map<String, Rectangle> frame(OverheadsCollisionLayout.Memory memory, OverheadsPosition side,
        Rectangle first, Rectangle second, Rectangle offset)
    {
        BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(memory, 4))
        {
            if (first != null) { add(layout, g, "first", side, first); }
            if (second != null) { add(layout, g, "second", side, second); }
            if (offset != null) { add(layout, g, "offset", side, offset); }
            layout.render();
            return layout.resolvedBounds();
        }
        finally
        {
            g.dispose();
        }
    }

    @Test
    public void transparentNamesAndHitsDoNotReserveInvisibleSpace()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture(OverheadsPosition.ABOVE);
        when(f.config.nameColor()).thenReturn(new Color(0, 0, 0, 0));
        when(f.config.nameBackground()).thenReturn(new Color(0, 0, 0, 0));
        when(f.config.opacityPercent()).thenReturn(0);
        f.overlay.reflow();
        BufferedImage transparent = renderLarge(f);
        when(f.config.selfNameVisibility()).thenReturn(OverheadsVisibility.HIDDEN);
        f.hits.clear();
        f.overlay.reflow();
        BufferedImage hidden = renderLarge(f);
        for (Color color : new Color[]{Color.RED, Color.CYAN, Color.GREEN, Color.BLUE, Color.ORANGE})
        {
            assertEquals(colorBounds(transparent, color), colorBounds(hidden, color));
        }
    }

    private static void add(OverheadsCollisionLayout layout, Graphics2D graphics, String name, OverheadsPosition side, Rectangle bounds)
    {
        OverheadsCollisionLayout.draw(layout, graphics, name, side, bounds, g -> g.fill(bounds));
    }

    private static void assertSeparated(Map<String, Rectangle> bounds, int gap)
    {
        ArrayList<Map.Entry<String, Rectangle>> list = new ArrayList<>(bounds.entrySet());
        for (int i = 0; i < list.size(); i++)
        {
            Rectangle padded = new Rectangle(list.get(i).getValue());
            padded.grow(gap, gap);
            for (int j = i + 1; j < list.size(); j++)
            {
                assertFalse(list.get(i).getKey() + " overlaps " + list.get(j).getKey(), padded.intersects(list.get(j).getValue()));
            }
        }
    }

    private static OverheadsRenderPipelineTest.Fixture fixture(OverheadsPosition side)
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        f.centerX = 500;
        f.feetY = 500;
        when(f.config.healthPosition()).thenReturn(OverheadsHealthPosition.valueOf(side.name()));
        when(f.config.prayerPosition()).thenReturn(side);
        when(f.config.runPosition()).thenReturn(side);
        when(f.config.iconsPosition()).thenReturn(side);
        when(f.config.namePosition()).thenReturn(side);
        when(f.config.chatPosition()).thenReturn(side);
        when(f.config.hitsplatPosition()).thenReturn(OverheadsHitsplatPosition.valueOf(side.name()));
        when(f.config.healthBarFrontColor()).thenReturn(Color.RED);
        when(f.config.healthBarBackColor()).thenReturn(Color.RED);
        when(f.config.healthTextColor()).thenReturn(Color.RED);
        when(f.config.selfHealthDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        when(f.config.healthNumberFormat()).thenReturn(OverheadsNumberFormat.CURRENT_MAXIMUM);
        when(f.config.prayerVisibility()).thenReturn(OverheadsPrayerVisibility.ALWAYS);
        when(f.config.prayerColor()).thenReturn(Color.CYAN);
        when(f.config.prayerBackColor()).thenReturn(Color.CYAN);
        when(f.config.prayerTextColor()).thenReturn(Color.CYAN);
        when(f.config.prayerDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        when(f.config.runDisplay()).thenReturn(OverheadsBarDisplay.BAR_ONLY);
        when(f.config.runColor()).thenReturn(Color.GREEN);
        when(f.config.runBackColor()).thenReturn(Color.GREEN);
        when(f.config.selfNameVisibility()).thenReturn(OverheadsVisibility.ALL);
        when(f.config.nameBackground()).thenReturn(Color.MAGENTA);
        when(f.config.nameColor()).thenReturn(Color.MAGENTA);
        when(f.config.overheadChatColor()).thenReturn(Color.ORANGE);
        when(f.config.overheadChatEffect()).thenReturn(OverheadsOverheadChatEffect.WAVE_2);
        when(f.player.getOverheadText()).thenReturn("Spacing test");
        when(f.player.getOverheadCycle()).thenReturn(140);
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.player.getSkullIcon()).thenReturn(-1);
        BufferedImage icon = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 18, 18);
        g.dispose();
        f.installSprite(SpriteID.HEADICONS_PRAYER, icon);
        when(f.config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.MODERN);
        when(f.config.hitsplatDamageColor()).thenReturn(Color.PINK);
        f.hits.add(new OverheadsHitsplat(HitsplatID.DAMAGE_ME, 42, 0, 90, 90, 90, 200, 220, 1, true));
        return f;
    }

    private static BufferedImage renderLarge(OverheadsRenderPipelineTest.Fixture f)
    {
        BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        f.callbacks.addEntity(f.player, true);
        f.overlay.render(g);
        g.dispose();
        return image;
    }

    private static Rectangle colorBounds(BufferedImage image, Color color)
    {
        Rectangle bounds = null;
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                if (image.getRGB(x, y) == color.getRGB())
                {
                    if (bounds == null) { bounds = new Rectangle(x, y, 1, 1); }
                    else { bounds.add(x, y); }
                }
            }
        }
        return bounds;
    }
}
