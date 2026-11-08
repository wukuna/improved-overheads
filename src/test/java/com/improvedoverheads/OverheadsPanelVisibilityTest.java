package com.improvedoverheads;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPanelVisibilityTest
{
    @Test
    public void actorSelectionWritesOnlyThatActorAndNumbersAreAvailableForEachResource() throws Exception
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.profiles.initialize();
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(f.self, f.manager, mock(ColorPickerManager.class));
            panel.configureProfiles(f.profiles);
            panel.rebuild();
            for (String key : new String[]{"selfHealthDisplay", "prayerDisplay", "runDisplay", "specialDisplay"})
            {
                javax.swing.JComboBox<?> display = (javax.swing.JComboBox<?>) find(panel, key);
                assertNotNull(key, display);
                display.setSelectedItem(OverheadsBarDisplay.NUMBERS_ONLY);
                assertEquals(OverheadsBarDisplay.NUMBERS_ONLY, f.get(OverheadsProfiles.Scope.SELF, key));
            }
            javax.swing.JComboBox<?> actors = actorPicker(panel);
            assertNotNull(actors);
            actors.setSelectedItem(OverheadsProfiles.Scope.PLAYERS);
            assertFalse(row(panel, "selfHealthDisplay").isVisible());
            assertTrue(row(panel, "otherHealthDisplay").isVisible());
            ((javax.swing.JSpinner) find(panel, "healthBarOpacity")).setValue(22);
            assertEquals(22, f.get(OverheadsProfiles.Scope.PLAYERS, "healthBarOpacity"));
            assertNotEquals(22, f.self.healthBarOpacity());
            actors.setSelectedItem(OverheadsProfiles.Scope.NPCS);
            assertTrue(row(panel, "npcHealthDisplay").isVisible());
            assertNull(find(panel, "runDisplay"));
        });
    }

    private static javax.swing.JComboBox<?> actorPicker(Container parent)
    {
        for (Component component : parent.getComponents())
        {
            if (component instanceof javax.swing.JComboBox)
            {
                javax.swing.JComboBox<?> box = (javax.swing.JComboBox<?>) component;
                if (box.getItemCount() > 0 && box.getItemAt(0) instanceof OverheadsProfiles.Scope) { return box; }
            }
            if (component instanceof Container)
            {
                javax.swing.JComboBox<?> found = actorPicker((Container) component);
                if (found != null) { return found; }
            }
        }
        return null;
    }

    @Test
    public void hitsplatControlsFollowStyleAndMotionWithoutConfigWrites() throws Exception
    {
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        ConfigManager manager = mock(ConfigManager.class);
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(config, manager, mock(ColorPickerManager.class));
            panel.rebuild();
            assertFalse(row(panel, "hitsplatBorderColor").isVisible());
            assertFalse(row(panel, "hitsplatScrollColumns").isVisible());
            when(config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.MODERN);
            when(config.hitsplatMotion()).thenReturn(OverheadsHitsplatMotion.SCROLL_UP);
            panel.refresh();
            assertTrue(row(panel, "hitsplatBorderColor").isVisible());
            assertTrue(row(panel, "hitsplatScrollColumns").isVisible());
            assertFalse(row(panel, OverheadsConfig.LAYOUT_SHAPE_KEY).isVisible());
            when(config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.NUMBERS);
            panel.refresh();
            assertFalse(row(panel, "hitsplatPoisonColor").isVisible());
            assertTrue(row(panel, "hitsplatStatusNumberColor").isVisible());
        });
        verify(manager, atLeastOnce()).getProfile();
        verifyNoMoreInteractions(manager);
    }

    @Test
    public void textEditsAcceptRefreshedProfileObjectsButRejectOldSessions() throws Exception
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(f.config, f.manager, mock(ColorPickerManager.class));
            panel.rebuild();
            javax.swing.JTextField field = (javax.swing.JTextField) find(panel, "hiddenNpcHealthIds");
            assertNotNull(field);
            net.runelite.client.config.ConfigProfile refreshed = mock(net.runelite.client.config.ConfigProfile.class);
            when(refreshed.getId()).thenReturn(1L);
            when(f.manager.getProfile()).thenReturn(refreshed);
            field.setText("123");
            field.postActionEvent();
            assertEquals("123", f.raw.get("hiddenNpcHealthIds"));
            panel.invalidateSetupSession();
            field.setText("456");
            field.postActionEvent();
            assertEquals("123", f.raw.get("hiddenNpcHealthIds"));
        });
    }

    @Test
    public void irrelevantControlsHideWithoutResettingStoredChoices() throws Exception
    {
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        ConfigManager manager = mock(ConfigManager.class);
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(config, manager, mock(ColorPickerManager.class));
            panel.rebuild();
            assertFalse(row(panel, "runColor").isVisible());
            assertFalse(row(panel, "prayerIconPosition").isVisible());
            when(config.runDisplay()).thenReturn(OverheadsBarDisplay.NUMBERS_ONLY);
            when(config.runColor()).thenReturn(java.awt.Color.MAGENTA);
            when(config.separatePrayerPosition()).thenReturn(true);
            panel.refresh();
            assertTrue(row(panel, "runFont").isVisible());
            assertFalse(row(panel, "runColor").isVisible());
            assertTrue(row(panel, "prayerIconPosition").isVisible());
            when(config.runDisplay()).thenReturn(OverheadsBarDisplay.BAR_ONLY);
            panel.refresh();
            assertTrue(row(panel, "runColor").isVisible());
            assertFalse(row(panel, "runFont").isVisible());
            assertEquals(java.awt.Color.MAGENTA, config.runColor());
            when(config.healthBarStyle()).thenReturn(OverheadsHealthBarStyle.NATIVE);
            panel.refresh();
            assertFalse(row(panel, "healthBarBorderColor").isVisible());
        });
        verify(manager, atLeastOnce()).getProfile();
        verifyNoMoreInteractions(manager);
    }

    private static Component row(Container parent, String key)
    {
        Component editor = find(parent, key);
        assertNotNull(key, editor);
        return editor.getParent();
    }

    private static Component find(Container parent, String key)
    {
        for (Component component : parent.getComponents())
        {
            if (component instanceof JComponent && key.equals(((JComponent) component).getClientProperty("settingKey")))
            {
                return component;
            }
            if (component instanceof Container)
            {
                Component found = find((Container) component, key);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }
}
