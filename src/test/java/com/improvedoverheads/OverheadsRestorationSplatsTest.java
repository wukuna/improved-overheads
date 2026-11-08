package com.improvedoverheads;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsRestorationSplatsTest
{
    @Test
    public void eventBusAndCompleteOverlayRespectVisibilityAndLifecycle() throws Exception
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.healingSplatEnabled()).thenReturn(true);
        when(f.config.healingSplatDisplay()).thenReturn(OverheadsRestoreDisplay.NUMBER_ONLY);
        f.overlay.gameTick();
        OverheadsPlugin plugin = new OverheadsPlugin();
        OverheadsCompatibilityTest.inject(plugin, "overlay", f.overlay);
        OverheadsCompatibilityTest.inject(plugin, "running", true);
        net.runelite.client.eventbus.EventBus bus = new net.runelite.client.eventbus.EventBus();
        bus.register(plugin);
        bus.post(new StatChanged(Skill.HITPOINTS, 0, 99, 93));
        assertNotNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_OVERHEAD_TEXT);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_ALL);
        f.overlay.clearVitals();
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        f.overlay.gameTick();
        OverheadsCompatibilityTest.inject(plugin, "running", false);
        bus.post(new StatChanged(Skill.HITPOINTS, 0, 99, 99));
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        OverheadsCompatibilityTest.inject(plugin, "running", true);
        bus.post(new StatChanged(Skill.HITPOINTS, 0, 99, 99));
        f.tracker.disable();
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        bus.unregister(plugin);
    }

    @Test
    public void observesActualPositiveChangesWithoutHoverOrAttemptedClicks()
    {
        Fixture f = new Fixture();
        f.state.tick();
        f.change(Skill.HITPOINTS, 70);
        f.change(Skill.PRAYER, 52);
        List<OverheadsRestorationSplats.Splat> splats = f.state.snapshot();
        assertEquals(2, splats.size());
        assertEquals(20, splats.get(0).amount);
        assertFalse(splats.get(0).prayer);
        assertEquals(22, splats.get(1).amount);
        assertTrue(splats.get(1).prayer);
        f.change(Skill.PRAYER, 52);
        f.change(Skill.HITPOINTS, 40);
        f.state.changed(new StatChanged(Skill.ATTACK, 0, 99, 120));
        assertEquals(2, f.state.snapshot().size());
        f.cycle.incrementAndGet();
        f.change(Skill.HITPOINTS, 41);
        assertEquals(1, f.state.snapshot().get(2).amount);
        verify(f.client, never()).getMenu();
    }

    @Test
    public void loginHopDeathProfileAndActorReplacementDoNotCreateRestoration()
    {
        Fixture f = new Fixture();
        f.change(Skill.HITPOINTS, 99);
        f.state.tick();
        assertTrue(f.state.snapshot().isEmpty());
        f.change(Skill.HITPOINTS, 0);
        f.change(Skill.HITPOINTS, 99);
        assertFalse(f.state.hasContent());
        f.state.tick();
        when(f.client.getGameState()).thenReturn(GameState.HOPPING);
        f.change(Skill.PRAYER, 99);
        assertFalse(f.state.hasContent());
        when(f.client.getGameState()).thenReturn(GameState.LOGGED_IN);
        f.state.tick();
        f.change(Skill.PRAYER, 20);
        f.change(Skill.PRAYER, 40);
        assertTrue(f.state.hasContent());
        f.state.clear();
        f.change(Skill.PRAYER, 99);
        assertFalse(f.state.hasContent());
        f.state.tick();
        when(f.client.getLocalPlayer()).thenReturn(mock(Player.class));
        f.change(Skill.HITPOINTS, 120);
        assertFalse(f.state.hasContent());
    }

    @Test
    public void ignoresLevelUpsAndMeasuresOnlyActualRestoreNearFull()
    {
        Fixture f = new Fixture();
        f.hp.set(98);
        f.state.tick();
        f.change(Skill.HITPOINTS, 99);
        assertEquals(1, f.state.snapshot().get(0).amount);
        f.state.clearSplats();
        f.state.changed(new StatChanged(Skill.HITPOINTS, 1, 100, 100));
        assertFalse(f.state.hasContent());
    }

    @Test
    public void independentTogglesKeepBaselinesCurrentAndNeverReplayDisabledGains()
    {
        Fixture f = new Fixture();
        when(f.config.healingSplatEnabled()).thenReturn(false);
        f.state.tick();
        f.change(Skill.HITPOINTS, 80);
        f.change(Skill.PRAYER, 50);
        assertEquals(1, f.state.snapshot().size());
        assertTrue(f.state.snapshot().get(0).prayer);
        when(f.config.healingSplatEnabled()).thenReturn(true);
        when(f.config.prayerSplatEnabled()).thenReturn(false);
        f.change(Skill.HITPOINTS, 90);
        assertEquals(1, f.state.snapshot().size());
        assertEquals(10, f.state.snapshot().get(0).amount);
        assertFalse(f.state.snapshot().get(0).prayer);
        when(f.config.healingSplatEnabled()).thenReturn(false);
        assertFalse(f.state.hasContent());
    }

    @Test
    public void batchesSameCycleGainsAndBoundsHistoryDuringLongSessions()
    {
        Fixture f = new Fixture();
        f.state.tick();
        f.change(Skill.HITPOINTS, 60);
        f.change(Skill.HITPOINTS, 70);
        assertEquals(1, f.state.snapshot().size());
        assertEquals(20, f.state.snapshot().get(0).amount);
        for (int i = 0; i < 1000; i++)
        {
            f.cycle.incrementAndGet();
            f.change(Skill.HITPOINTS, 50);
            f.change(Skill.HITPOINTS, 55);
            f.change(Skill.PRAYER, 30);
            f.change(Skill.PRAYER, 35);
        }
        assertEquals(8, f.state.snapshot().size());
        when(f.config.healingSplatDuration()).thenReturn(200);
        f.cycle.addAndGet(10);
        assertTrue(f.state.hasContent());
        assertTrue(f.state.snapshot().stream().allMatch(s -> s.prayer));
        f.cycle.addAndGet(60);
        assertFalse(f.state.hasContent());
    }

    @Test
    public void independentOpacityDisplayAndLifetimeApplyToRenderedIcons()
    {
        Fixture f = new Fixture();
        f.state.tick();
        f.change(Skill.HITPOINTS, 70);
        f.change(Skill.PRAYER, 52);
        when(f.config.healingSplatDisplay()).thenReturn(OverheadsRestoreDisplay.ICON_ONLY);
        when(f.config.prayerSplatDisplay()).thenReturn(OverheadsRestoreDisplay.ICON_ONLY);
        when(f.config.healingSplatOpacity()).thenReturn(0);
        BufferedImage image = f.render();
        assertEquals(0, colored(image, Color.RED));
        assertTrue(colored(image, Color.BLUE) > 0);
        when(f.config.healingSplatOpacity()).thenReturn(100);
        when(f.config.prayerSplatOpacity()).thenReturn(0);
        image = f.render();
        assertTrue(colored(image, Color.RED) > 0);
        assertEquals(0, colored(image, Color.BLUE));
        f.cycle.addAndGet(60);
        assertEquals(0, colored(f.render(), Color.RED));
    }

    @Test
    public void numbersOnlyRespectsItsOwnFontColorPositionAndScale()
    {
        Fixture f = new Fixture();
        when(f.config.prayerSplatEnabled()).thenReturn(false);
        when(f.config.healingSplatDisplay()).thenReturn(OverheadsRestoreDisplay.NUMBER_ONLY);
        when(f.config.healingSplatColor()).thenReturn(Color.GREEN);
        f.state.tick();
        f.change(Skill.HITPOINTS, 70);
        int original = colored(f.render(), Color.GREEN);
        assertTrue(original > 0);
        when(f.config.healingSplatScalePercent()).thenReturn(200);
        assertTrue(colored(f.render(), Color.GREEN) > original);
        for (OverheadsHitsplatPosition position : OverheadsHitsplatPosition.values())
        {
            when(f.config.healingSplatPosition()).thenReturn(position);
            assertTrue(position.toString(), colored(f.render(), Color.GREEN) > 0);
        }
        verifyNoInteractions(f.icons);
    }

    @Test
    public void restorationSettingsRoundTripIndependentlyAndPreserveHoverPreviews()
    {
        OverheadsProfilesTest.Fixture from = new OverheadsProfilesTest.Fixture();
        from.profiles.initialize();
        from.put(OverheadsProfiles.Scope.SELF, "healingSplatEnabled", true);
        from.put(OverheadsProfiles.Scope.SELF, "prayerSplatEnabled", false);
        from.put(OverheadsProfiles.Scope.SELF, "healingSplatColor", new Color(12, 34, 56, 78));
        from.put(OverheadsProfiles.Scope.SELF, "prayerSplatPosition", OverheadsHitsplatPosition.BELOW);
        from.put(OverheadsProfiles.Scope.SELF, "healingSplatDuration", 800);
        from.put(OverheadsProfiles.Scope.SELF, "prayerSplatDuration", 2500);
        OverheadsSetups exporter = from.setups();
        String json = exporter.exportCurrent("Restoration", exporter.session());
        OverheadsProfilesTest.Fixture to = new OverheadsProfilesTest.Fixture();
        to.profiles.initialize();
        OverheadsSetups importer = to.setups();
        importer.apply(importer.preview(json, importer.session()));
        assertTrue(to.self.healingSplatEnabled());
        assertFalse(to.self.prayerSplatEnabled());
        assertEquals(new Color(12, 34, 56, 78), to.self.healingSplatColor());
        assertEquals(OverheadsHitsplatPosition.BELOW, to.self.prayerSplatPosition());
        assertEquals(800, to.self.healingSplatDuration());
        assertEquals(2500, to.self.prayerSplatDuration());
        assertTrue(to.self.restorePreview());
        assertTrue(to.self.prayerRestorePreview());
    }

    @Test
    public void sidebarExposesSeparateControlsOnlyForMyPlayer() throws Exception
    {
        OverheadsProfilesTest.Fixture f = new OverheadsProfilesTest.Fixture();
        f.profiles.initialize();
        SwingUtilities.invokeAndWait(() ->
        {
            OverheadsPanel panel = new OverheadsPanel(f.self, f.manager, mock(ColorPickerManager.class));
            panel.configureProfiles(f.profiles);
            panel.rebuild();
            JCheckBox healing = (JCheckBox) find(panel, "healingSplatEnabled");
            JCheckBox prayer = (JCheckBox) find(panel, "prayerSplatEnabled");
            assertNotNull(healing);
            assertNotNull(prayer);
            assertNotSame(healing.getParent(), prayer.getParent());
            healing.doClick();
            panel.refresh();
            assertTrue(f.self.healingSplatEnabled());
            assertFalse(f.self.prayerSplatEnabled());
            assertTrue(find(panel, "healingSplatColor").getParent().isVisible());
            assertFalse(find(panel, "prayerSplatColor").getParent().isVisible());
            JComboBox<?> actors = actorPicker(panel);
            for (OverheadsProfiles.Scope scope : new OverheadsProfiles.Scope[]{OverheadsProfiles.Scope.PARTY, OverheadsProfiles.Scope.PLAYERS, OverheadsProfiles.Scope.NPCS})
            {
                actors.setSelectedItem(scope);
                assertNull(find(panel, "healingSplatEnabled"));
                assertNull(find(panel, "prayerSplatEnabled"));
            }
        });
    }

    private static Component find(Container parent, String key)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JComponent && key.equals(((JComponent) child).getClientProperty("settingKey"))) return child;
            if (child instanceof Container)
            {
                Component found = find((Container) child, key);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JComboBox<?> actorPicker(Container parent)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JComboBox && ((JComboBox<?>) child).getItemAt(0) instanceof OverheadsProfiles.Scope) return (JComboBox<?>) child;
            if (child instanceof Container)
            {
                JComboBox<?> found = actorPicker((Container) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static int colored(BufferedImage image, Color color)
    {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
        {
            int pixel = image.getRGB(x, y);
            if ((pixel >>> 24) != 0 && (pixel & 0xFFFFFF) == (color.getRGB() & 0xFFFFFF)) count++;
        }
        return count;
    }

    static final class Fixture
    {
        final Client client = mock(Client.class);
        final Player player = mock(Player.class);
        final OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        final SkillIconManager icons = mock(SkillIconManager.class);
        final AtomicInteger cycle = new AtomicInteger(100);
        final AtomicInteger hp = new AtomicInteger(50);
        final AtomicInteger prayer = new AtomicInteger(30);
        final OverheadsRestorationSplats state = new OverheadsRestorationSplats(client, config);

        Fixture()
        {
            when(client.getLocalPlayer()).thenReturn(player);
            when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
            when(client.getGameCycle()).thenAnswer(call -> cycle.get());
            when(client.getRealSkillLevel(any())).thenReturn(99);
            when(client.getBoostedSkillLevel(Skill.HITPOINTS)).thenAnswer(call -> hp.get());
            when(client.getBoostedSkillLevel(Skill.PRAYER)).thenAnswer(call -> prayer.get());
            when(player.getLogicalHeight()).thenReturn(120);
            when(player.getCanvasImageLocation(any(), anyInt())).thenAnswer(call -> new Point(160, 180 - (int) call.getArgument(1) / 2));
            when(config.healingSplatEnabled()).thenReturn(true);
            when(config.prayerSplatEnabled()).thenReturn(true);
            for (Skill skill : new Skill[]{Skill.HITPOINTS, Skill.PRAYER})
            {
                BufferedImage icon = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = icon.createGraphics();
                g.setColor(skill == Skill.HITPOINTS ? Color.RED : Color.BLUE);
                g.fillRect(0, 0, 18, 18);
                g.dispose();
                when(icons.getSkillImage(skill)).thenReturn(icon);
            }
            state.setIcons(icons);
        }

        void change(Skill skill, int value)
        {
            (skill == Skill.HITPOINTS ? hp : prayer).set(value);
            state.changed(new StatChanged(skill, 0, 99, value));
        }

        BufferedImage render()
        {
            BufferedImage image = new BufferedImage(320, 260, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            state.render(graphics, player, new Point(160, 110), null);
            graphics.dispose();
            return image;
        }
    }
}
