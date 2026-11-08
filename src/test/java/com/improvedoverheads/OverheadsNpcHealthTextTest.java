package com.improvedoverheads;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.*;
import net.runelite.api.NPC;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsNpcHealthTextTest
{
    @Test
    public void sidebarSizeAndColorEditsUpdateNpcPixelsWithoutChangingMyPlayer() throws Exception
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.profiles.initialize();
        f.put(OverheadsProfiles.Scope.NPCS, "npcHealthDisplay", OverheadsBarDisplay.NUMBERS_ONLY);
        f.put(OverheadsProfiles.Scope.NPCS, "healthTextFont", new FontType().withFamily("Dialog").withSize(12));
        f.put(OverheadsProfiles.Scope.NPCS, "healthTextColor", Color.RED);
        NPC npc = mock(NPC.class);
        when(npc.getHealthRatio()).thenReturn(15);
        when(npc.getHealthScale()).thenReturn(30);
        when(npc.getCanvasImageLocation(any(), anyInt())).thenReturn(new net.runelite.api.Point(120, 160));
        OverheadsHealthBarRenderer renderer = new OverheadsHealthBarRenderer(f.client, f.self);
        renderer.configureProfiles(f.profiles);
        BufferedImage before = render(renderer, npc);
        FontType ownFont = f.self.healthTextFont();
        Color ownColor = f.self.healthTextColor();
        SwingUtilities.invokeAndWait(() ->
        {
            ColorPickerManager pickers = mock(ColorPickerManager.class);
            RuneliteColorPicker picker = mock(RuneliteColorPicker.class);
            when(picker.isDisplayable()).thenReturn(true);
            when(pickers.create(any(Component.class), any(), anyString(), eq(true))).thenReturn(picker);
            OverheadsPanel panel = new OverheadsPanel(f.self, f.manager, pickers);
            panel.configureProfiles(f.profiles);
            panel.rebuild();
            JComboBox<?> actors = find(panel, JComboBox.class);
            assertEquals(4, actors.getItemCount());
            actors.setSelectedItem(OverheadsProfiles.Scope.NPCS);
            find(setting(panel, "healthTextFont"), JSpinner.class).setValue(32);
            ((ColorJButton) setting(panel, "healthTextColor")).doClick();
            ArgumentCaptor<Consumer> callback = ArgumentCaptor.forClass(Consumer.class);
            verify(picker).setOnColorChange(callback.capture());
            callback.getValue().accept(Color.BLUE);
            panel.closeEditors();
        });
        assertEquals(32, f.profiles.config(OverheadsProfiles.Scope.NPCS).healthTextFont().getSize());
        assertEquals(Color.BLUE, f.profiles.config(OverheadsProfiles.Scope.NPCS).healthTextColor());
        BufferedImage after = render(renderer, npc);
        assertTrue(OverheadsRenderPipelineTest.pixelBounds(after).height > OverheadsRenderPipelineTest.pixelBounds(before).height);
        assertTrue(colored(after, Color.BLUE) > 0);
        assertEquals(0, colored(after, Color.RED));
        assertEquals(ownFont, f.self.healthTextFont());
        assertEquals(ownColor, f.self.healthTextColor());
    }

    @Test
    public void aboveAndBelowTextStayClearOfBarAndInsideCollisionBounds()
    {
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        OverheadsHealthBarRenderer renderer = new OverheadsHealthBarRenderer(mock(net.runelite.api.Client.class), config);
        FontType font = new FontType().withFamily("Dialog").withSize(24);
        Rectangle bar = new Rectangle(100, 100, 80, 10);
        for (OverheadsBarTextPosition position : new OverheadsBarTextPosition[]{OverheadsBarTextPosition.ABOVE, OverheadsBarTextPosition.BELOW})
        {
            when(config.healthTextPosition()).thenReturn(position);
            BufferedImage image = new BufferedImage(300, 250, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            renderer.paintBar(g, bar, false, OverheadsBarDisplay.BOTH, OverheadsPosition.ABOVE,
                0.5, 0.5, Color.GREEN, Color.GREEN, 0, "50%", font, Color.BLUE, 1, 0, false, 0, 0);
            Rectangle ink = OverheadsRenderPipelineTest.pixelBounds(image);
            if (position == OverheadsBarTextPosition.ABOVE) assertTrue(ink.getMaxY() < bar.y);
            else assertTrue(ink.y > bar.getMaxY());
            Rectangle occupied = OverheadsCollisionLayout.barBounds(g, bar, false, OverheadsBarDisplay.BOTH,
                OverheadsPosition.ABOVE, "50%", font, 1, config.healthBarBorderThickness(), 0, 255, position);
            assertTrue(occupied.contains(ink));
            g.dispose();
        }
    }

    @Test
    public void observedNpcNumbersUseSelectedFormatWithoutPretendingUnknownHpIsExact()
    {
        assertEquals("~50/100", OverheadsHealthBarRenderer.observedHealthText(0.5, 100, OverheadsNumberFormat.CURRENT_MAXIMUM));
        assertEquals("~50", OverheadsHealthBarRenderer.observedHealthText(0.5, 100, OverheadsNumberFormat.CURRENT));
        assertEquals("50%", OverheadsHealthBarRenderer.observedHealthText(0.5, 100, OverheadsNumberFormat.PERCENTAGE));
        assertEquals("50%", OverheadsHealthBarRenderer.observedHealthText(0.5, null, OverheadsNumberFormat.CURRENT));
    }

    private static BufferedImage render(OverheadsHealthBarRenderer renderer, NPC npc)
    {
        BufferedImage image = new BufferedImage(300, 250, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        renderer.render(g, npc, new net.runelite.api.Point(120, 150), 150);
        g.dispose();
        return image;
    }

    private static int colored(BufferedImage image, Color color)
    {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
            if ((image.getRGB(x, y) >>> 24) != 0 && (image.getRGB(x, y) & 0xffffff) == (color.getRGB() & 0xffffff)) count++;
        return count;
    }

    private static JComponent setting(Container parent, String key)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JComponent && key.equals(((JComponent) child).getClientProperty("settingKey"))) return (JComponent) child;
            if (child instanceof Container) { JComponent value = setting((Container) child, key); if (value != null) return value; }
        }
        return null;
    }

    private static <T> T find(Container parent, Class<T> type)
    {
        for (Component child : parent.getComponents())
        {
            if (type.isInstance(child)) return type.cast(child);
            if (child instanceof Container) { T value = find((Container) child, type); if (value != null) return value; }
        }
        return null;
    }
}
