package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import net.runelite.api.HeadIcon;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.SpritePixels;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPrayerBadgesTest
{
    @Test public void protectionOffImmediatelyRestoresExactRegularRowAppearanceAndPosition()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.player.getOverheadIcon()).thenReturn(null);
        BufferedImage fallback = render(f);
        Rectangle blue = pixels(fallback, Color.BLUE);
        assertNotNull(blue);
        assertTrue(blue.y > 300);
        assertEquals(12, blue.height);
        when(f.config.prayerIconLayout()).thenReturn(OverheadsPrayerLayout.ROWS);
        assertImageEquals(fallback, render(f));
        when(f.config.prayerIconLayout()).thenReturn(OverheadsPrayerLayout.CORNER_BADGES);
        for (int i = 0; i < 5; i++)
        {
            when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
            BufferedImage attached = render(f);
            assertNull(pixels(attached, Color.BLUE));
            assertEquals(34, pixels(attached, Color.RED).width);
            when(f.player.getOverheadIcon()).thenReturn(null);
            assertImageEquals(fallback, render(f));
        }
        assertEquals(OverheadsPosition.BELOW, f.config.prayerIconCombatPosition());
        assertEquals(60, f.config.prayerIconCombatScalePercent());
    }

    @Test public void missingOrHiddenOverheadUsesRowsAndOtherPlayersStayNative()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.config.showPrayerIcons()).thenReturn(false);
        assertNotNull(pixels(render(f), Color.BLUE));
        when(f.config.showPrayerIcons()).thenReturn(true);
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.client.getSprites(any(), eq(SpriteID.HEADICONS_PRAYER), eq(0))).thenReturn(null);
        f.icons.clearCache();
        assertNotNull(pixels(render(f), Color.BLUE));
        f.installSprite(SpriteID.HEADICONS_PRAYER, OverheadsPrayerRowsTest.sprite(Color.RED, 20).getImage());
        f.icons.clearCache();
        when(f.client.getLocalPlayer()).thenReturn(mock(Player.class));
        BufferedImage other = render(f);
        assertNull(pixels(other, Color.BLUE));
        assertEquals(20, pixels(other, Color.RED).width);
    }

    @Test public void backgroundsDefaultOffHaveTheirOwnToggleAndNeverAlterRows()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.config.prayerIconBadgeBackgroundColor()).thenReturn(Color.MAGENTA);
        when(f.config.prayerIconBadgeBorderColor()).thenReturn(Color.GREEN);
        assertFalse(new OverheadsConfig() {}.prayerIconBadgeBackground());
        BufferedImage off = render(f);
        assertNull(pixels(off, Color.MAGENTA));
        when(f.config.prayerIconBadgeBackground()).thenReturn(true);
        f.icons.clearCache();
        assertNotNull(pixels(render(f), Color.MAGENTA));
        when(f.player.getOverheadIcon()).thenReturn(null);
        BufferedImage rowWithToggle = render(f);
        when(f.config.prayerIconBadgeBackground()).thenReturn(false);
        f.icons.clearCache();
        assertImageEquals(rowWithToggle, render(f));
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        assertImageEquals(off, render(f));
    }

    @Test public void separateSizesModesAndZoomWorkWithoutEnablingSeparatePositions()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.config.separatePrayerPosition()).thenReturn(false);
        when(f.config.prayerIconBadgeProtectionSize()).thenReturn(48);
        when(f.config.prayerIconBadgeSize()).thenReturn(24);
        assertEquals(48, pixels(render(f), Color.RED).width);
        when(f.config.prayerIconBadgeProtectionSize()).thenReturn(32);
        assertEquals(32, pixels(render(f), Color.RED).width);
        when(f.config.headIconScaleWithZoom()).thenReturn(true);
        f.zoom = 2;
        assertEquals(64, pixels(render(f), Color.RED).width);
        when(f.config.headIconScaleWithZoom()).thenReturn(false);
        when(f.config.prayerIconLayout()).thenReturn(OverheadsPrayerLayout.ROWS);
        assertNotNull(pixels(render(f), Color.BLUE));
        assertEquals(20, pixels(render(f), Color.RED).width);
    }

    @Test public void moreThanFourSecondaryPrayersRemainVisibleInTheirRow()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        for (Prayer prayer : new Prayer[]{Prayer.THICK_SKIN, Prayer.RAPID_RESTORE, Prayer.RAPID_HEAL, Prayer.PROTECT_ITEM})
            when(f.client.isPrayerActive(prayer)).thenReturn(true);
        f.installSprite(SpriteID.Prayeron.RAPID_RESTORE, OverheadsPrayerRowsTest.sprite(Color.BLUE, 20).getImage());
        f.installSprite(SpriteID.Prayeron.RAPID_HEAL, OverheadsPrayerRowsTest.sprite(Color.BLUE, 20).getImage());
        f.installSprite(SpriteID.Prayeron.PROTECT_ITEM, OverheadsPrayerRowsTest.sprite(Color.BLUE, 20).getImage());
        f.installSprite(SpriteID.Prayeron.THICK_SKIN, OverheadsPrayerRowsTest.sprite(Color.BLUE, 20).getImage());
        Rectangle overflow = pixels(render(f), Color.BLUE);
        assertNotNull(overflow);
        assertTrue(overflow.y > 300);
        when(f.player.getOverheadIcon()).thenReturn(null);
        assertTrue(pixels(render(f), Color.BLUE).width > overflow.width);
    }

    @Test public void allOverheadPrayersAttachAndRowSizeDoesNotLeakIntoAttachedFrames()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.config.showPrayerIconBackground()).thenReturn(true);
        when(f.config.prayerIconShape()).thenReturn(OverheadsIconShape.ROUND);
        BufferedImage normal = render(f);
        when(f.config.prayerIconScalePercent()).thenReturn(25);
        f.icons.clearCache();
        assertImageEquals(normal, render(f));
        for (HeadIcon icon : new HeadIcon[]{HeadIcon.MELEE, HeadIcon.RANGED, HeadIcon.MAGIC,
            HeadIcon.SMITE, HeadIcon.REDEMPTION, HeadIcon.RETRIBUTION})
        {
            when(f.player.getOverheadIcon()).thenReturn(icon);
            assertImageEquals(normal, render(f));
            when(f.player.getOverheadIcon()).thenReturn(null);
            BufferedImage fallback = render(f);
            when(f.config.prayerIconLayout()).thenReturn(OverheadsPrayerLayout.ROWS);
            assertImageEquals(fallback, render(f));
            when(f.config.prayerIconLayout()).thenReturn(OverheadsPrayerLayout.CORNER_BADGES);
        }
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.config.prayerIconBadgeSize()).thenReturn(28);
        f.icons.clearCache();
        BufferedImage bigger = render(f);
        assertEquals(pixels(normal, Color.RED).width, pixels(bigger, Color.RED).width);
        assertTrue(OverheadsRenderPipelineTest.pixelBounds(bigger).width > OverheadsRenderPipelineTest.pixelBounds(normal).width);
    }

    @Test public void fourCornersFitAllSizesWithoutBadgeOverlapAndKeepProtectionCentered()
    {
        OverheadsSprite icon = OverheadsPrayerRowsTest.sprite(Color.RED, 20);
        for (int main : new int[]{24, 34, 64}) for (int small : new int[]{12, 18, 28})
            for (int zoom : new int[]{50, 100, 200, 400}) for (int count = 1; count <= 4; count++)
            {
                Map<Integer, OverheadsSprite> badges = new TreeMap<>();
                for (int i = 0; i < count; i++) badges.put(i, icon);
                OverheadsPrayerBadgeLayout layout = new OverheadsPrayerBadgeLayout(icon, badges, main, small, 0, zoom);
                assertEquals(count + 1, layout.icons.size());
                Rectangle bounds = new Rectangle(0, 0, layout.width, layout.height);
                Rectangle protection = layout.icons.get(0).bounds;
                assertEquals(layout.width / 2.0, protection.getCenterX(), .5);
                for (int i = 1; i < layout.icons.size(); i++)
                {
                    Rectangle badge = layout.icons.get(i).bounds;
                    assertTrue(bounds.contains(badge));
                    assertFalse(badge.contains(protection.getCenterX(), protection.getCenterY()));
                    for (int j = 1; j < i; j++) assertFalse(badge.intersects(layout.icons.get(j).bounds));
                }
            }
    }

    @Test public void remainingBadgesKeepSlotsWhenOthersToggleAndOverflowPromotesToFreeCorner()
    {
        OverheadsPrayerBadgeSlots slots = new OverheadsPrayerBadgeSlots();
        slots.update(Arrays.asList(Prayer.PIETY, Prayer.PRESERVE, Prayer.PROTECT_ITEM, Prayer.RAPID_HEAL, Prayer.RAPID_RESTORE));
        assertEquals(-1, slots.slot(Prayer.RAPID_RESTORE));
        slots.update(Arrays.asList(Prayer.PRESERVE, Prayer.PROTECT_ITEM, Prayer.RAPID_HEAL, Prayer.RAPID_RESTORE));
        assertEquals(1, slots.slot(Prayer.PRESERVE));
        assertEquals(2, slots.slot(Prayer.PROTECT_ITEM));
        assertEquals(3, slots.slot(Prayer.RAPID_HEAL));
        assertEquals(0, slots.slot(Prayer.RAPID_RESTORE));
        slots.update(Collections.emptyList());
        assertEquals(-1, slots.slot(Prayer.PRESERVE));
        slots.update(Collections.singletonList(Prayer.PRESERVE));
        slots.clear();
        assertEquals(-1, slots.slot(Prayer.PRESERVE));
    }

    @Test public void everySupportedSecondaryPrayerHasDistinctTransparentSmallArtwork() throws Exception
    {
        java.lang.reflect.Field field = OverheadsHeadIconRenderer.class.getDeclaredField("BOOST_PRAYERS");
        field.setAccessible(true);
        Map<Prayer, Integer> supported = (Map<Prayer, Integer>) field.get(null);
        java.util.Set<Integer> images = new java.util.HashSet<>();
        for (Prayer prayer : supported.keySet())
        {
            for (int size : new int[]{12, 16, 18, 20, 28})
            {
                BufferedImage image = OverheadsPrayerBadgeIcons.create(prayer, size, false, Color.MAGENTA, Color.GREEN).getImage();
                assertEquals(size, image.getWidth());
                assertEquals(0, image.getRGB(0, 0));
                assertNotNull(OverheadsRenderPipelineTest.pixelBounds(image));
                assertNull(prayer.name(), pixels(image, Color.MAGENTA));
                if (size == 18) assertTrue(prayer.name(), images.add(Arrays.hashCode(image.getRGB(0, 0, size, size, null, 0, size))));
            }
        }
    }

    @Test public void setupSharingAndActorSettingsKeepBadgeControlsIndependent()
    {
        OverheadsProfilesTest.Fixture from = new OverheadsProfilesTest.Fixture();
        from.profiles.initialize();
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconLayout", OverheadsPrayerLayout.CORNER_BADGES);
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconBadgeProtectionSize", 42);
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconBadgeSize", 20);
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconBadgeBackground", true);
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconBadgeBackgroundColor", new Color(1, 2, 3, 4));
        OverheadsSetups exporter = from.setups();
        OverheadsProfilesTest.Fixture to = new OverheadsProfilesTest.Fixture();
        to.profiles.initialize();
        OverheadsSetups importer = to.setups();
        importer.apply(importer.preview(exporter.exportCurrent("Corners", exporter.session()), importer.session()));
        assertEquals(exporter.schema.snapshot(), importer.schema.snapshot());
        assertEquals(OverheadsPrayerLayout.CORNER_BADGES, to.self.prayerIconLayout());
        assertEquals(20, to.self.prayerIconBadgeSize());
        assertEquals(OverheadsPrayerLayout.ROWS, to.profiles.config(OverheadsProfiles.Scope.NPCS).prayerIconLayout());
        assertFalse(to.profiles.config(OverheadsProfiles.Scope.NPCS).prayerIconBadgeBackground());
    }

    @Test public void sidebarProvidesLayoutSeparateSizesAndBackgroundToggle() throws Exception
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.profiles.initialize();
        SwingUtilities.invokeAndWait(() -> {
            OverheadsPanel panel = new OverheadsPanel(f.self, f.manager, mock(ColorPickerManager.class));
            panel.configureProfiles(f.profiles);
            panel.rebuild();
            ((JComboBox<?>) find(panel, "prayerIconLayout")).setSelectedItem(OverheadsPrayerLayout.CORNER_BADGES);
            ((JSpinner) find(panel, "prayerIconBadgeSize")).setValue(22);
            assertEquals(22, f.self.prayerIconBadgeSize());
            assertEquals(34, f.self.prayerIconBadgeProtectionSize());
            assertEquals(100, f.self.prayerIconCombatScalePercent());
            ((JCheckBox) find(panel, "prayerIconBadgeBackground")).doClick();
            assertTrue(f.self.prayerIconBadgeBackground());
        });
    }

    static OverheadsRenderPipelineTest.Fixture fixture()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        SpritePixels sprite = mock(SpritePixels.class);
        when(sprite.toBufferedImage()).thenReturn(OverheadsPrayerRowsTest.sprite(Color.RED, 20).getImage());
        when(sprite.getMaxWidth()).thenReturn(20); when(sprite.getMaxHeight()).thenReturn(20);
        when(f.client.getSprites(any(), eq(SpriteID.HEADICONS_PRAYER), eq(0)))
            .thenReturn(new SpritePixels[]{sprite, sprite, sprite, sprite, sprite, sprite});
        f.installSprite(SpriteID.Prayeron.PIETY, OverheadsPrayerRowsTest.sprite(Color.BLUE, 20).getImage());
        f.installSprite(SpriteID.Prayeron.PRESERVE, OverheadsPrayerRowsTest.sprite(Color.BLUE, 20).getImage());
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
        when(f.client.isPrayerActive(Prayer.PRESERVE)).thenReturn(true);
        when(f.config.prayerIconLayout()).thenReturn(OverheadsPrayerLayout.CORNER_BADGES);
        when(f.config.localBoostPrayerIcons()).thenReturn(true);
        when(f.config.separatePrayerPosition()).thenReturn(true);
        when(f.config.showPrayerIconBackground()).thenReturn(false);
        when(f.config.prayerIconRow2BackgroundEnabled()).thenReturn(false);
        when(f.config.headIconScaleWithZoom()).thenReturn(false);
        when(f.config.prayerIconPosition()).thenReturn(OverheadsPosition.ABOVE);
        when(f.config.prayerIconCombatPosition()).thenReturn(OverheadsPosition.BELOW);
        when(f.config.prayerIconCombatScalePercent()).thenReturn(60);
        return f;
    }

    static BufferedImage render(OverheadsRenderPipelineTest.Fixture f)
    {
        BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(new OverheadsCollisionLayout.Memory(), 3))
        {
            assertTrue(f.icons.prepare(f.player));
            f.icons.renderPrepared(g, f.player, 100, new Point(300, 200), new Point(300, 200), 200, Integer.MAX_VALUE, layout);
            layout.render();
        }
        g.dispose();
        return image;
    }

    private static Rectangle pixels(BufferedImage image, Color color) { return OverheadsPrayerRowsTest.pixels(image, color); }

    private static javax.swing.JComponent find(java.awt.Component component, String key)
    {
        if (component instanceof javax.swing.JComponent && key.equals(((javax.swing.JComponent) component).getClientProperty("settingKey")))
            return (javax.swing.JComponent) component;
        if (component instanceof java.awt.Container)
            for (java.awt.Component child : ((java.awt.Container) component).getComponents())
            {
                javax.swing.JComponent found = find(child, key);
                if (found != null) return found;
            }
        return null;
    }

    private static void assertImageEquals(BufferedImage expected, BufferedImage actual)
    {
        assertArrayEquals(expected.getRGB(0, 0, 600, 440, null, 0, 600), actual.getRGB(0, 0, 600, 440, null, 0, 600));
    }
}
