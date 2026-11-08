package com.improvedoverheads;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import net.runelite.api.HeadIcon;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPrayerRowBackgroundTest
{
    @Test public void migrationCopiesEachActorsOldAppearanceOnceAndPreservesRowChoices()
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        {
            f.put(scope, "prayerIconBackground", new Color(20 + scope.ordinal(), 40, 60, 80));
            f.put(scope, "prayerIconShape", OverheadsIconShape.ROUND);
            f.put(scope, "showPrayerIconBackground", false);
            f.put(scope, "prayerIconBackgroundTracksPoints", true);
        }
        f.put(OverheadsProfiles.Scope.NPCS, "prayerIconRow2Shape", OverheadsIconShape.SQUARE);
        f.profiles.initialize();
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        {
            OverheadsConfig config = f.profiles.config(scope);
            assertEquals(new Color(20 + scope.ordinal(), 40, 60, 80), config.prayerIconRow2Background());
            assertEquals(scope == OverheadsProfiles.Scope.NPCS ? OverheadsIconShape.SQUARE : OverheadsIconShape.ROUND, config.prayerIconRow2Shape());
            assertFalse(config.prayerIconRow2BackgroundEnabled());
            assertTrue(config.prayerIconRow2TracksPoints());
            f.put(scope, "prayerIconBackground", Color.WHITE);
        }
        f.profiles.initialize();
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
            assertEquals(new Color(20 + scope.ordinal(), 40, 60, 80), f.profiles.config(scope).prayerIconRow2Background());
    }

    @Test public void setupSharingKeepsBothRowsIndependentForEveryActor()
    {
        OverheadsProfilesTest.Fixture from = new OverheadsProfilesTest.Fixture();
        from.profiles.initialize();
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        {
            from.put(scope, "prayerIconBackground", Color.RED);
            from.put(scope, "prayerIconRow2Background", new Color(20, 30, 40, 50 + scope.ordinal()));
            from.put(scope, "prayerIconRow2Border", Color.GREEN);
            from.put(scope, "prayerIconRow2Shape", OverheadsIconShape.SQUARE);
            from.put(scope, "prayerIconRow2TracksPoints", true);
            from.put(scope, "prayerIconRow2BackgroundEnabled", false);
        }
        OverheadsSetups exporter = from.setups();
        OverheadsProfilesTest.Fixture to = new OverheadsProfilesTest.Fixture();
        to.profiles.initialize();
        OverheadsSetups importer = to.setups();
        importer.apply(importer.preview(exporter.exportCurrent("Rows", exporter.session()), importer.session()));
        assertEquals(exporter.schema.snapshot(), importer.schema.snapshot());
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        {
            assertEquals(Color.RED, to.profiles.config(scope).prayerIconBackground());
            assertEquals(50 + scope.ordinal(), to.profiles.config(scope).prayerIconRow2Background().getAlpha());
        }
    }

    @Test public void eachRowUsesItsOwnFillBorderShapeAndPrayerDrain()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.config.prayerIconBackgroundTracksPoints()).thenReturn(true);
        when(f.config.prayerIconRow2TracksPoints()).thenReturn(false);
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(99);
        BufferedImage full = render(f);
        assertNotNull(pixels(full, Color.RED)); assertNotNull(pixels(full, Color.BLUE));
        assertNotNull(pixels(full, Color.YELLOW)); assertNotNull(pixels(full, Color.MAGENTA));
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(0);
        BufferedImage drained = render(f);
        assertNull(pixels(drained, Color.RED)); assertNotNull(pixels(drained, Color.GREEN));
        assertEquals(pixels(full, Color.BLUE), pixels(drained, Color.BLUE));
        when(f.config.prayerIconBackgroundTracksPoints()).thenReturn(false);
        when(f.config.prayerIconRow2TracksPoints()).thenReturn(true);
        f.icons.clearCache();
        drained = render(f);
        assertNotNull(pixels(drained, Color.RED)); assertNull(pixels(drained, Color.BLUE));
        assertNotNull(pixels(drained, Color.CYAN));
    }

    @Test public void everyNativeOverheadUsesTheOverheadGroupsBackgroundAndDrain()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        net.runelite.api.SpritePixels sprite = f.client.getSprites(null, SpriteID.HEADICONS_PRAYER, 0)[0];
        when(f.client.getSprites(any(), eq(SpriteID.HEADICONS_PRAYER), eq(0)))
            .thenReturn(new net.runelite.api.SpritePixels[]{sprite, sprite, sprite, sprite, sprite, sprite});
        when(f.config.prayerIconBackgroundTracksPoints()).thenReturn(true);
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(0);
        BufferedImage expected = render(f);
        assertNotNull(pixels(expected, Color.GREEN));
        assertNull(pixels(expected, Color.RED));
        for (HeadIcon icon : new HeadIcon[]{HeadIcon.RANGED, HeadIcon.MAGIC, HeadIcon.REDEMPTION,
            HeadIcon.SMITE, HeadIcon.RETRIBUTION})
        {
            when(f.player.getOverheadIcon()).thenReturn(icon);
            BufferedImage actual = render(f);
            assertArrayEquals(icon.name(), expected.getRGB(0, 0, 600, 440, null, 0, 600),
                actual.getRGB(0, 0, 600, 440, null, 0, 600));
        }
    }

    @Test public void rowAssignmentMovesStylingWithTheRowAndBackgroundTogglesAreSeparate()
    {
        OverheadsRenderPipelineTest.Fixture f = fixture();
        when(f.config.prayerIconScalePercent()).thenReturn(150);
        when(f.config.prayerIconCombatScalePercent()).thenReturn(50);
        BufferedImage first = render(f);
        assertTrue(pixels(first, Color.RED).height > pixels(first, Color.BLUE).height);
        when(f.config.prayerIconRow()).thenReturn(OverheadsPrayerRow.ROW_2);
        when(f.config.prayerIconCombatRow()).thenReturn(OverheadsPrayerRow.ROW_1);
        BufferedImage swapped = render(f);
        assertTrue(pixels(swapped, Color.RED).height < pixels(swapped, Color.BLUE).height);
        when(f.config.prayerIconRow2BackgroundEnabled()).thenReturn(false);
        f.icons.clearCache();
        BufferedImage hidden = render(f);
        assertNotNull(pixels(hidden, Color.RED)); assertNull(pixels(hidden, Color.BLUE));
        assertNull(pixels(hidden, Color.MAGENTA));
        when(f.config.prayerIconRow2BackgroundEnabled()).thenReturn(true);
        when(f.config.showPrayerIconBackground()).thenReturn(false);
        f.icons.clearCache();
        hidden = render(f);
        assertNull(pixels(hidden, Color.RED)); assertNotNull(pixels(hidden, Color.BLUE));
    }

    @Test public void backgroundsAreInSeparateSidebarSectionsAndEditsStayInTheSelectedScope() throws Exception
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.put(OverheadsProfiles.Scope.SELF, "separatePrayerPosition", true);
        f.put(OverheadsProfiles.Scope.SELF, "prayerIconShape", OverheadsIconShape.SQUARE);
        f.profiles.initialize();
        SwingUtilities.invokeAndWait(() -> {
            OverheadsPanel panel = new OverheadsPanel(f.self, f.manager, mock(ColorPickerManager.class));
            panel.configureProfiles(f.profiles); panel.rebuild();
            assertNotSame(row(panel, "prayerIconBackground").getParent(), row(panel, "prayerIconRow2Background").getParent());
            ((JComboBox<?>) find(panel, "prayerIconRow2Shape")).setSelectedItem(OverheadsIconShape.ROUND);
            assertEquals(OverheadsIconShape.SQUARE, f.self.prayerIconShape());
            assertEquals(OverheadsIconShape.ROUND, f.self.prayerIconRow2Shape());
            assertEquals(OverheadsIconShape.SQUARE, f.profiles.config(OverheadsProfiles.Scope.NPCS).prayerIconRow2Shape());
        });
    }

    private static OverheadsRenderPipelineTest.Fixture fixture()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        BufferedImage icon = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        icon.setRGB(6, 6, Color.WHITE.getRGB());
        f.installSprite(SpriteID.HEADICONS_PRAYER, icon);
        f.installSprite(SpriteID.Prayeron.PIETY, icon);
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
        when(f.config.localBoostPrayerIcons()).thenReturn(true);
        when(f.config.separatePrayerPosition()).thenReturn(true);
        when(f.config.headIconScaleWithZoom()).thenReturn(false);
        when(f.config.prayerIconPosition()).thenReturn(OverheadsPosition.ABOVE);
        when(f.config.prayerIconCombatPosition()).thenReturn(OverheadsPosition.ABOVE);
        when(f.config.prayerIconShape()).thenReturn(OverheadsIconShape.SQUARE);
        when(f.config.prayerIconBackground()).thenReturn(Color.RED);
        when(f.config.prayerIconBorder()).thenReturn(Color.YELLOW);
        when(f.config.prayerIconEmptyBackground()).thenReturn(Color.GREEN);
        when(f.config.prayerIconRow2Shape()).thenReturn(OverheadsIconShape.ROUND);
        when(f.config.prayerIconRow2Background()).thenReturn(Color.BLUE);
        when(f.config.prayerIconRow2Border()).thenReturn(Color.MAGENTA);
        when(f.config.prayerIconRow2EmptyBackground()).thenReturn(Color.CYAN);
        return f;
    }

    private static BufferedImage render(OverheadsRenderPipelineTest.Fixture f)
    {
        BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        assertTrue(f.icons.prepare(f.player));
        f.icons.renderPrepared(graphics, f.player, 100, new Point(300, 200), new Point(300, 200), 200, Integer.MAX_VALUE);
        graphics.dispose();
        return image;
    }

    private static Rectangle pixels(BufferedImage image, Color color) { return OverheadsPrayerRowsTest.pixels(image, color); }

    private static JComponent find(Component component, String key)
    {
        if (component instanceof JComponent && key.equals(((JComponent) component).getClientProperty("settingKey"))) return (JComponent) component;
        if (component instanceof Container) for (Component child : ((Container) component).getComponents()) {
            JComponent found = find(child, key); if (found != null) return found;
        }
        return null;
    }

    private static Component row(Component root, String key)
    {
        return find(root, key).getParent();
    }
}
