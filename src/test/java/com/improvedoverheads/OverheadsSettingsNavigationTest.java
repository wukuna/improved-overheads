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
import javax.swing.SwingUtilities;
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
