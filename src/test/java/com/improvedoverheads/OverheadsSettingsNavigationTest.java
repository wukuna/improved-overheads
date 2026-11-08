package com.improvedoverheads;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsSettingsNavigationTest
{
    @Test
    public void regularSettingsExposeOnlyFourOwnPlayerToggles()
    {
        ConfigManager manager = mock(ConfigManager.class);
        when(manager.getConfigDescriptor(any())).thenCallRealMethod();
        ConfigDescriptor descriptor = manager.getConfigDescriptor(new OverheadsConfig() {});
        Set<String> visible = descriptor.getItems().stream().filter(item -> !item.getItem().hidden())
            .map(item -> item.getItem().keyName()).collect(Collectors.toSet());
        assertEquals(new HashSet<>(Arrays.asList("healthBarsEnabled", "overheadChatEnabled", "headIconsEnabled", "disableMyHitsplats")), visible);
        assertEquals(1, descriptor.getSections().size());
        assertEquals("My player", descriptor.getSections().iterator().next().getSection().name());
        for (ConfigItemDescriptor item : descriptor.getItems())
        {
            if (!item.getItem().hidden())
            {
                assertEquals(boolean.class, item.getType());
                assertEquals(OverheadsConfig.QUICK_SECTION, item.getItem().section());
            }
        }
    }

    @Test
    public void everyConfigSettingRemainsReachableInActorSidePanels() throws Exception
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.profiles.initialize();
        when(f.manager.getConfigDescriptor(any())).thenCallRealMethod();
        Set<String> expected = f.manager.getConfigDescriptor(new OverheadsConfig() {}).getItems().stream()
            .map(item -> item.getItem().keyName()).collect(Collectors.toSet());
        Set<String> found = new HashSet<>();
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(f.self, f.manager, mock(ColorPickerManager.class));
            panel.configureProfiles(f.profiles);
            panel.rebuild();
            JComboBox<?> actors = actorPicker(panel);
            for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
            {
                actors.setSelectedItem(scope);
                collectKeys(panel, found);
                assertFalse(hasToggle(panel, "More settings"));
                assertTrue(hasToggle(panel, "Display"));
                assertTrue(hasToggle(panel, "Styling"));
                assertTrue(hasToggle(panel, "Position and size"));
            }
        });
        assertEquals(expected, found);
    }

    @Test
    public void navigationFitsNarrowSidebarWithEitherClientLookAndFeel() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            javax.swing.LookAndFeel previous = UIManager.getLookAndFeel();
            try
            {
                for (javax.swing.LookAndFeel look : new javax.swing.LookAndFeel[]{new javax.swing.plaf.metal.MetalLookAndFeel(), new net.runelite.client.ui.laf.RuneLiteLAF()})
                {
                    UIManager.setLookAndFeel(look);
                    OverheadsPanel panel = new OverheadsPanel(new OverheadsConfig() {}, mock(ConfigManager.class), mock(ColorPickerManager.class));
                    panel.rebuild();
                    panel.setSize(225, 650);
                    layout(panel);
                    JTabbedPane tabs = find(panel, JTabbedPane.class);
                    assertEquals(5, tabs.getTabCount());
                    for (int i = 0; i < tabs.getTabCount(); i++)
                    {
                        java.awt.Rectangle bounds = tabs.getBoundsAt(i);
                        assertEquals(tabs.getBoundsAt(0).y, bounds.y);
                        assertTrue(bounds.x + bounds.width <= tabs.getWidth());
                    }
                    assertEquals(OverheadsPanelStyle.CONTROL, actorPicker(panel).getBackground());
                    assertEquals(12, actorPicker(panel).getFont().getSize());
                }
            }
            catch (Exception error) { throw new RuntimeException(error); }
            finally
            {
                try { UIManager.setLookAndFeel(previous); }
                catch (Exception error) { throw new RuntimeException(error); }
            }
        });
    }

    @Test
    public void rebuildingPreservesSelectedPageAndExpandedSection() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(new OverheadsConfig() {}, mock(ConfigManager.class), mock(ColorPickerManager.class));
            panel.rebuild();
            find(panel, JTabbedPane.class).setSelectedIndex(2);
            JToggleButton styling = findToggle(panel, "Styling");
            styling.doClick();
            panel.rebuild();
            assertEquals(2, find(panel, JTabbedPane.class).getSelectedIndex());
            assertTrue(findToggle(panel, "Styling").isSelected());
        });
    }

    private static void layout(Container parent)
    {
        parent.doLayout();
        for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
    }

    private static <T> T find(Container parent, Class<T> type)
    {
        for (Component child : parent.getComponents())
        {
            if (type.isInstance(child)) return type.cast(child);
            if (child instanceof Container)
            {
                T found = find((Container) child, type);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JToggleButton findToggle(Container parent, String text)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JToggleButton && text.equals(((JToggleButton) child).getText())) return (JToggleButton) child;
            if (child instanceof Container)
            {
                JToggleButton found = findToggle((Container) child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void collectKeys(Container parent, Set<String> found)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JComponent)
            {
                Object key = ((JComponent) child).getClientProperty("settingKey");
                if (key instanceof String) found.add((String) key);
            }
            if (child instanceof Container) collectKeys((Container) child, found);
        }
    }

    private static boolean hasToggle(Container parent, String text)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JToggleButton && text.equals(((JToggleButton) child).getText())) return true;
            if (child instanceof Container && hasToggle((Container) child, text)) return true;
        }
        return false;
    }

    private static JComboBox<?> actorPicker(Container parent)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JComboBox && ((JComboBox<?>) child).getItemCount() > 0
                && ((JComboBox<?>) child).getItemAt(0) instanceof OverheadsProfiles.Scope) return (JComboBox<?>) child;
            if (child instanceof Container)
            {
                JComboBox<?> found = actorPicker((Container) child);
                if (found != null) return found;
            }
        }
        return null;
    }
}
