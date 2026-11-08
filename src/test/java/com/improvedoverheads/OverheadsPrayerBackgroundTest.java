package com.improvedoverheads;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Map;
import net.runelite.api.HeadIcon;
import net.runelite.api.Player;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.gameval.SpriteID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPrayerBackgroundTest
{
    @Test
    public void backgroundDrainsFromTopAndRefillsWithoutChangingIconOrBorder()
    {
        BufferedImage icon = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        icon.setRGB(6, 6, Color.WHITE.getRGB());
        OverheadsSprite source = new OverheadsSprite(icon, 0, 0);
        Color full = new Color(30, 150, 220, 180), empty = new Color(20, 20, 20, 90);
        for (OverheadsIconShape shape : new OverheadsIconShape[]{OverheadsIconShape.ROUND, OverheadsIconShape.SQUARE})
        {
            BufferedImage filled = style(source, shape, full, empty, 1);
            BufferedImage half = style(source, shape, full, empty, .5);
            BufferedImage drained = style(source, shape, full, empty, 0);
            assertEquals(empty.getRGB(), half.getRGB(9, 3));
            assertEquals(full.getRGB(), half.getRGB(9, 14));
            assertEquals(filled.getRGB(9, 9), half.getRGB(9, 9));
            assertEquals(filled.getRGB(9, 9), drained.getRGB(9, 9));
            assertEquals(filled.getRGB(9, 0), half.getRGB(9, 0));
            assertEquals(filled.getRGB(9, 0), drained.getRGB(9, 0));
            assertEquals(full.getRGB(), filled.getRGB(9, 3));
            assertEquals(empty.getRGB(), drained.getRGB(9, 14));
            BufferedImage restored = style(source, shape, full, empty, 1);
            assertArrayEquals(filled.getRGB(0, 0, 18, 18, null, 0, 18), restored.getRGB(0, 0, 18, 18, null, 0, 18));
        }
    }

    private static BufferedImage style(OverheadsSprite source, OverheadsIconShape shape, Color full, Color empty, double level)
    {
        return OverheadsPrayerIconStyle.create(source, shape, full, Color.YELLOW, 100, level, empty).getImage();
    }

    @Test
    public void renderedBackgroundUpdatesWithoutHoverAndReusesCachedLevels() throws Exception
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.prayerIconShape()).thenReturn(OverheadsIconShape.SQUARE);
        when(f.config.prayerIconBackgroundTracksPoints()).thenReturn(true);
        when(f.config.prayerIconBackground()).thenReturn(Color.CYAN);
        when(f.config.prayerIconEmptyBackground()).thenReturn(Color.RED);
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        net.runelite.api.SpritePixels sprite = mock(net.runelite.api.SpritePixels.class);
        when(sprite.toBufferedImage()).thenReturn(new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB));
        when(sprite.getMaxWidth()).thenReturn(12);
        when(sprite.getMaxHeight()).thenReturn(12);
        when(f.client.getSprites(any(), eq(SpriteID.HEADICONS_PRAYER), eq(0)))
            .thenReturn(new net.runelite.api.SpritePixels[]{sprite, sprite, sprite});
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(99);
        BufferedImage full = f.render();
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(0);
        BufferedImage empty = f.render();
        assertTrue(count(full, Color.CYAN) > count(empty, Color.CYAN));
        assertTrue(count(empty, Color.RED) > count(full, Color.RED));
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(99);
        BufferedImage restored = f.render();
        assertArrayEquals(full.getRGB(0, 0, 600, 440, null, 0, 600), restored.getRGB(0, 0, 600, 440, null, 0, 600));
        Field cache = OverheadsHeadIconRenderer.class.getDeclaredField("styledIcons");
        cache.setAccessible(true);
        Map<?, ?> cached = (Map<?, ?>) cache.get(f.icons);
        assertEquals(2, cached.size());
        for (HeadIcon icon : new HeadIcon[]{HeadIcon.MELEE, HeadIcon.RANGED, HeadIcon.MAGIC})
        {
            when(f.player.getOverheadIcon()).thenReturn(icon);
            for (int level = 0; level <= 99; level++)
            {
                when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(level);
                f.icons.prepare(f.player);
            }
        }
        assertEquals(128, cached.size());
        f.icons.clearCache();
        assertTrue(cached.isEmpty());
    }

    @Test
    public void unknownPrayerStaysStaticAndPartyDataIsOptional() throws Exception
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.prayerIconShape()).thenReturn(OverheadsIconShape.ROUND);
        when(f.config.prayerIconBackgroundTracksPoints()).thenReturn(true);
        OverheadsPartyTest.Fixture shared = new OverheadsPartyTest.Fixture(true);
        OverheadsPartyState party = shared.state;
        Field field = OverheadsHeadIconRenderer.class.getDeclaredField("party");
        field.setAccessible(true);
        field.set(f.icons, party);
        Player other = shared.player;
        assertEquals(-1, f.icons.prayerLevel(other));
        assertEquals(-1, f.icons.prayerLevel(mock(NPC.class)));
        net.runelite.client.party.messages.PartyData message = shared.message();
        int[] base = new int[6], boost = new int[6];
        base[Skill.PRAYER.ordinal()] = 50;
        boost[Skill.PRAYER.ordinal()] = 25;
        message.setStatBase(base);
        message.setStatBoost(boost);
        shared.accept(message);
        assertEquals(50, f.icons.prayerLevel(other));
        when(shared.client.getTickCount()).thenReturn(1000);
        assertEquals(-1, f.icons.prayerLevel(other));
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(120);
        assertEquals(100, f.icons.prayerLevel(f.player));
        when(f.client.getRealSkillLevel(Skill.PRAYER)).thenReturn(0);
        assertEquals(-1, f.icons.prayerLevel(f.player));
        when(f.config.prayerIconBackgroundTracksPoints()).thenReturn(false);
        assertEquals(-1, f.icons.prayerLevel(f.player));
    }

    private static int count(BufferedImage image, Color color)
    {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
            if (image.getRGB(x, y) == color.getRGB()) count++;
        return count;
    }
}
