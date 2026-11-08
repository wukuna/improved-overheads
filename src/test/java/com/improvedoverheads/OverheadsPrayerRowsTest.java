package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import net.runelite.api.HeadIcon;
import net.runelite.api.Prayer;
import net.runelite.api.Point;
import net.runelite.api.gameval.SpriteID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPrayerRowsTest
{
    @Test
    public void centeredRowsSupportDifferentSizesBothOrdersAndSingleRow()
    {
        OverheadsSprite protect = sprite(Color.RED, 30);
        OverheadsSprite boost = sprite(Color.BLUE, 12);
        for (OverheadsPrayerRow row : OverheadsPrayerRow.values())
        {
            OverheadsPrayerRow other = row == OverheadsPrayerRow.ROW_1 ? OverheadsPrayerRow.ROW_2 : OverheadsPrayerRow.ROW_1;
            OverheadsPrayerIconLayout layout = new OverheadsPrayerIconLayout(Collections.singletonList(protect), row,
                Arrays.asList(boost, boost, boost), other, 100, 3);
            assertEquals(42, layout.width);
            assertEquals(45, layout.height);
            Rectangle red = layout.icons.stream().filter(icon -> icon.sprite == protect).findFirst().get().bounds;
            assertEquals(21, red.getCenterX(), 0.01);
            assertEquals(row == OverheadsPrayerRow.ROW_1 ? 0 : 15, red.y);
            Rectangle blue = layout.icons.stream().filter(icon -> icon.sprite == boost).findFirst().get().bounds;
            assertEquals(row == OverheadsPrayerRow.ROW_1 ? 33 : 0, blue.y);
        }
        OverheadsPrayerIconLayout same = new OverheadsPrayerIconLayout(Collections.singletonList(protect), OverheadsPrayerRow.ROW_2,
            Arrays.asList(boost, boost), OverheadsPrayerRow.ROW_2, 100, 3);
        assertEquals(30, same.height);
        assertEquals(60, same.width);
        assertEquals(9, same.icons.get(1).bounds.y);
        OverheadsPrayerIconLayout alone = new OverheadsPrayerIconLayout(Collections.emptyList(), OverheadsPrayerRow.ROW_1,
            Collections.singletonList(boost), OverheadsPrayerRow.ROW_2, 100, 3);
        assertEquals(12, alone.height);
        assertEquals(0, alone.icons.get(0).bounds.y);
    }

    @Test
    public void rendererKeepsRowsTogetherAndHonorsSeparatePositionsSizesAndVisibility()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        f.installSprite(SpriteID.HEADICONS_PRAYER, sprite(Color.RED, 20).getImage());
        f.installSprite(SpriteID.Prayeron.PIETY, sprite(Color.BLUE, 20).getImage());
        f.installSprite(SpriteID.Prayeron.PRESERVE, sprite(Color.BLUE, 20).getImage());
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
        when(f.client.isPrayerActive(Prayer.PRESERVE)).thenReturn(true);
        when(f.config.localBoostPrayerIcons()).thenReturn(true);
        when(f.config.separatePrayerPosition()).thenReturn(true);
        when(f.config.showPrayerIconBackground()).thenReturn(false);
        when(f.config.headIconScaleWithZoom()).thenReturn(false);
        when(f.config.prayerIconScalePercent()).thenReturn(150);
        when(f.config.prayerIconCombatScalePercent()).thenReturn(50);
        when(f.config.prayerIconPosition()).thenReturn(OverheadsPosition.ABOVE);
        when(f.config.prayerIconCombatPosition()).thenReturn(OverheadsPosition.ABOVE);
        OverheadsHeadIconRenderer renderer = f.icons;
        BufferedImage image = render(f, renderer);
        Rectangle red = pixels(image, Color.RED), blue = pixels(image, Color.BLUE);
        assertEquals(30, red.width);
        assertEquals(10, blue.height);
        assertEquals(red.getCenterX(), blue.getCenterX(), 1);
        assertTrue(red.getMaxY() < blue.y);
        when(f.config.prayerIconRow()).thenReturn(OverheadsPrayerRow.ROW_2);
        when(f.config.prayerIconCombatRow()).thenReturn(OverheadsPrayerRow.ROW_1);
        image = render(f, renderer);
        assertTrue(pixels(image, Color.BLUE).getMaxY() < pixels(image, Color.RED).y);
        when(f.config.prayerIconCombatPosition()).thenReturn(OverheadsPosition.BELOW);
        image = render(f, renderer);
        assertTrue(pixels(image, Color.RED).getMaxY() < 200);
        assertTrue(pixels(image, Color.BLUE).y > 300);
        when(f.client.getLocalPlayer()).thenReturn(mock(net.runelite.api.Player.class));
        image = render(f, renderer);
        assertNull(pixels(image, Color.BLUE));
        assertNotNull(pixels(image, Color.RED));
        verify(f.client, times(3)).isPrayerActive(Prayer.PIETY);
        when(f.player.getOverheadIcon()).thenReturn(null);
        assertFalse(renderer.prepare(f.player));
    }

    @Test
    public void miscellaneousOverheadIconsUseCombatGroupAndMigrationCopiesOnlyUnsetSettings()
    {
        assertTrue(OverheadsHeadIconRenderer.protection(0));
        assertTrue(OverheadsHeadIconRenderer.protection(2));
        assertTrue(OverheadsHeadIconRenderer.protection(6));
        for (int index : new int[]{-1, 3, 4, 5}) assertFalse(OverheadsHeadIconRenderer.protection(index));
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.put(OverheadsProfiles.Scope.NPCS, "prayerIconScalePercent", 175);
        f.put(OverheadsProfiles.Scope.NPCS, "prayerIconPosition", OverheadsPosition.LEFT);
        f.put(OverheadsProfiles.Scope.NPCS, "prayerIconCombatYOffset", 42);
        f.profiles.initialize();
        OverheadsConfig npc = f.profiles.config(OverheadsProfiles.Scope.NPCS);
        assertEquals(175, npc.prayerIconCombatScalePercent());
        assertEquals(OverheadsPosition.LEFT, npc.prayerIconCombatPosition());
        assertEquals(42, npc.prayerIconCombatYOffset());
        f.put(OverheadsProfiles.Scope.NPCS, "prayerIconCombatScalePercent", 70);
        f.profiles.initialize();
        assertEquals(70, npc.prayerIconCombatScalePercent());
    }

    private static BufferedImage render(OverheadsRenderPipelineTest.Fixture f, OverheadsHeadIconRenderer renderer)
    {
        BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(new OverheadsCollisionLayout.Memory(), 3))
        {
            assertTrue(renderer.prepare(f.player));
            renderer.renderPrepared(g, f.player, 100, new Point(300, 200), new Point(300, 200), 200, Integer.MAX_VALUE, layout);
            layout.render();
        }
        g.dispose();
        return image;
    }

    static Rectangle pixels(BufferedImage image, Color color)
    {
        Rectangle result = null;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
            if (image.getRGB(x, y) == color.getRGB())
                result = result == null ? new Rectangle(x, y, 1, 1) : result.union(new Rectangle(x, y, 1, 1));
        return result;
    }

    static OverheadsSprite sprite(Color color, int size)
    {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics(); g.setColor(color); g.fillRect(0, 0, size, size); g.dispose();
        return new OverheadsSprite(image, 0, 0);
    }
}
