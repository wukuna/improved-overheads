package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicLong;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.Menu;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.plugins.itemstats.StatsChanges;
import net.runelite.client.plugins.itemstats.stats.Stat;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsCompletionTest
{
    @Test
    public void previewUsesCalculatedRestorationAndClearsOnLeavingInventory()
    {
        PreviewFixture f = new PreviewFixture();
        f.state.clientTick();
        assertEquals(6, f.state.restore(false));
        assertEquals(25, f.state.restore(true));
        f.state.clientTick();
        verify(f.effect, times(1)).calculate(f.client);
        f.clock.addAndGet(110_000_000L);
        f.state.clientTick();
        verify(f.effect, times(2)).calculate(f.client);
        when(f.widget.getId()).thenReturn(123);
        f.state.clientTick();
        assertEquals(0, f.state.restore(false));
        assertEquals(0, f.state.restore(true));
        when(f.widget.getId()).thenReturn(InterfaceID.Inventory.ITEMS);
        f.state.clientTick();
        assertEquals(6, f.state.restore(false));
        when(f.client.isMenuOpen()).thenReturn(true);
        f.state.clientTick();
        assertEquals(0, f.state.restore(false));
    }

    @Test
    public void previewRefreshesOnGameTickAndNeverSurvivesProfileClear()
    {
        PreviewFixture f = new PreviewFixture();
        f.state.clientTick();
        f.state.gameTick();
        f.state.clientTick();
        verify(f.effect, times(2)).calculate(f.client);
        f.state.clear();
        assertEquals(0, f.state.restore(true));
        assertEquals(-1, f.state.tickProgress(), 0);
        when(f.config.restorePreview()).thenReturn(false);
        f.state.clientTick();
        verify(f.effect, times(2)).calculate(f.client);
    }

    @Test
    public void prayerTickTracksObservedTicksAndActualLoss()
    {
        PreviewFixture f = new PreviewFixture();
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(62);
        f.state.gameTick();
        assertFalse(f.state.prayerJustDrained());
        f.clock.addAndGet(300_000_000L);
        assertEquals(0.5, f.state.tickProgress(), 0.001);
        f.clock.addAndGet(300_000_000L);
        when(f.client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(61);
        f.state.gameTick();
        assertTrue(f.state.prayerJustDrained());
        f.clock.addAndGet(900_000_000L);
        assertEquals(1, f.state.tickProgress(), 0);
        assertFalse(f.state.prayerJustDrained());
    }

    @Test
    public void restoreSegmentHonorsEveryFillDirectionAndLeavesCurrentHealthUntouched()
    {
        for (OverheadsHealthBarFillDirection direction : OverheadsHealthBarFillDirection.values())
        {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            OverheadsHealthBarRenderer.drawRestorePreview(g, new Rectangle(10, 10, 80, 80), direction, 0.25, 0.5, Color.GREEN, 0);
            g.dispose();
            Rectangle pixels = OverheadsRenderPipelineTest.pixelBounds(image);
            assertNotNull(pixels);
            switch (direction)
            {
                case LEFT_TO_RIGHT: assertEquals(30, pixels.x); assertEquals(19, pixels.width); break;
                case RIGHT_TO_LEFT: assertEquals(50, pixels.x); assertEquals(19, pixels.width); break;
                case TOP_TO_BOTTOM: assertEquals(30, pixels.y); assertEquals(19, pixels.height); break;
                case BOTTOM_TO_TOP: assertEquals(50, pixels.y); assertEquals(19, pixels.height); break;
            }
        }
    }

    @Test
    public void independentPrayerIconMovesWithoutMovingTheSkull()
    {
        OverheadsRenderPipelineTest.Fixture f = bareFixture();
        when(f.config.headIconsEnabled()).thenReturn(true);
        when(f.player.getSkullIcon()).thenReturn(0);
        when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
        when(f.config.separatePrayerPosition()).thenReturn(true);
        when(f.config.prayerIconPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.prayerIconShape()).thenReturn(OverheadsIconShape.ORIGINAL);
        when(f.config.showPrayerIconBackground()).thenReturn(false);
        f.installSprite(SpriteID.HEADICONS_PK, solid(Color.RED));
        f.installSprite(SpriteID.HEADICONS_PRAYER, solid(Color.BLUE));
        BufferedImage image = f.render();
        Rectangle skull = coloredBounds(image, Color.RED);
        Rectangle prayer = coloredBounds(image, Color.BLUE);
        assertNotNull(skull);
        assertNotNull(prayer);
        assertTrue(skull.getMaxY() < f.feetY - 100);
        assertTrue(prayer.x > f.centerX);
        when(f.config.prayerIconPosition()).thenReturn(OverheadsPosition.LEFT);
        BufferedImage moved = f.render();
        assertEquals(skull, coloredBounds(moved, Color.RED));
        assertTrue(coloredBounds(moved, Color.BLUE).getMaxX() < f.centerX);
        when(f.config.showSkullIcons()).thenReturn(false);
        assertNotNull(coloredBounds(f.render(), Color.BLUE));
    }

    @Test
    public void namesAndResourceMetersHonorMasksAndEverySide()
    {
        OverheadsRenderPipelineTest.Fixture f = bareFixture();
        when(f.config.selfNameVisibility()).thenReturn(OverheadsVisibility.ALL);
        for (OverheadsPosition side : OverheadsPosition.values())
        {
            when(f.config.namePosition()).thenReturn(side);
            assertSide(OverheadsRenderPipelineTest.pixelBounds(f.render()), side, f);
        }
        when(f.config.selfNameVisibility()).thenReturn(OverheadsVisibility.HIDDEN);
        when(f.client.getEnergy()).thenReturn(7350);
        when(f.config.runDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        for (OverheadsPosition side : OverheadsPosition.values())
        {
            when(f.config.runPosition()).thenReturn(side);
            assertSide(OverheadsRenderPipelineTest.pixelBounds(f.render()), side, f);
        }
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_OVERHEAD_TEXT);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_ALL);
        f.tracker.disable();
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
    }

    @Test
    public void resourceMetersUseCorrectUnitsAndNumbersSurviveZeroBarOpacity()
    {
        OverheadsRenderPipelineTest.Fixture f = bareFixture();
        when(f.client.getEnergy()).thenReturn(5000);
        when(f.config.runDisplay()).thenReturn(OverheadsBarDisplay.BAR_ONLY);
        when(f.config.runColor()).thenReturn(Color.YELLOW);
        when(f.config.runBackColor()).thenReturn(Color.BLUE);
        BufferedImage image = f.render();
        Rectangle filled = coloredBounds(image, Color.YELLOW);
        Rectangle empty = coloredBounds(image, Color.BLUE);
        assertNotNull(filled);
        assertNotNull(empty);
        assertEquals(filled.width, empty.width, 2);
        when(f.config.runOpacity()).thenReturn(0);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.config.runDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        assertNotNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.config.runDisplay()).thenReturn(OverheadsBarDisplay.HIDDEN);
        when(f.config.specialDisplay()).thenReturn(OverheadsBarDisplay.BAR_ONLY);
        when(f.client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(500);
        when(f.config.specialColor()).thenReturn(Color.GREEN);
        when(f.config.specialBackColor()).thenReturn(Color.BLUE);
        image = f.render();
        assertEquals(coloredBounds(image, Color.GREEN).width, coloredBounds(image, Color.BLUE).width, 2);
    }

    @Test
    public void statusDoesNotInventOtherPlayersPoisonOrPredictNpcAggression()
    {
        OverheadsRenderPipelineTest.Fixture f = bareFixture();
        OverheadsExtrasRenderer renderer = new OverheadsExtrasRenderer(f.client, f.config);
        when(f.config.showStatusIcons()).thenReturn(true);
        when(f.config.showTargetingIcon()).thenReturn(true);
        when(f.client.getVarpValue(VarPlayerID.POISON)).thenReturn(1_000_020);
        assertEquals(2, renderer.statusMask(f.player));
        NPC npc = mock(NPC.class);
        assertEquals(0, renderer.statusMask(npc));
        when(npc.getInteracting()).thenReturn(f.player);
        assertEquals(0, renderer.statusMask(npc));
        when(f.player.getInteracting()).thenReturn(npc);
        assertEquals(8, renderer.statusMask(npc));
        when(f.player.getInteracting()).thenReturn(null);
        assertEquals(0, renderer.statusMask(npc));
        when(f.client.getVarpValue(VarPlayerID.POISON)).thenReturn(-30);
        assertEquals(0, renderer.statusMask(f.player));
    }

    @Test
    public void targetVisibilityAndNpcExclusionsUpdateWithoutRestart()
    {
        OverheadsRenderPipelineTest.Fixture f = bareFixture();
        when(f.config.healthBarsEnabled()).thenReturn(true);
        NPC first = mock(NPC.class);
        NPC second = mock(NPC.class);
        when(first.getId()).thenReturn(100);
        when(first.getHealthRatio()).thenReturn(20);
        when(first.getHealthScale()).thenReturn(30);
        when(second.getHealthRatio()).thenReturn(20);
        when(second.getHealthScale()).thenReturn(30);
        when(f.player.getInteracting()).thenReturn(first);
        when(f.config.npcHealthVisibility()).thenReturn(OverheadsVisibility.CURRENT_TARGET);
        assertTrue(f.health.hasRenderableHealthBar(first));
        assertFalse(f.health.hasRenderableHealthBar(second));
        when(f.config.hiddenNpcHealthIds()).thenReturn("100, invalid, 200");
        assertFalse(f.health.hasRenderableHealthBar(first));
        when(f.config.hiddenNpcHealthIds()).thenReturn("");
        assertTrue(f.health.hasRenderableHealthBar(first));
    }

    @Test
    public void addedEncounterBarsKeepTheirNativeInterface()
    {
        for (int id : new int[]{NpcID.WHISPERER, NpcID.WHISPERER_MELEE_QUEST,
            NpcID.VERZIK_PILLAR_NPC, NpcID.VERZIK_STORY_PILLAR_NPC, NpcID.VERZIK_HARD_PILLAR_NPC})
        {
            assertTrue(OverheadsActorUiVisibilityTracker.isSpecialBarCompatibilityNpc(id));
        }
    }

    private static OverheadsRenderPipelineTest.Fixture bareFixture()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.overheadChatEnabled()).thenReturn(false);
        return f;
    }

    private static void assertSide(Rectangle pixels, OverheadsPosition side, OverheadsRenderPipelineTest.Fixture f)
    {
        assertNotNull(pixels);
        switch (side)
        {
            case LEFT: assertTrue(pixels.getMaxX() < f.centerX); break;
            case RIGHT: assertTrue(pixels.x > f.centerX); break;
            case ABOVE: assertTrue(pixels.getMaxY() < f.feetY - 100); break;
            case BELOW: assertTrue(pixels.y > f.feetY); break;
        }
    }

    private static BufferedImage solid(Color color)
    {
        BufferedImage image = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 12, 12);
        g.dispose();
        return image;
    }

    private static Rectangle coloredBounds(BufferedImage image, Color color)
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

    private static final class PreviewFixture
    {
        final Client client = mock(Client.class);
        final OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        final Widget widget = mock(Widget.class);
        final Effect effect = mock(Effect.class);
        final AtomicLong clock = new AtomicLong(1_000_000_000L);
        final OverheadsVitalsState state;

        PreviewFixture()
        {
            ItemStatChangesService service = mock(ItemStatChangesService.class);
            Menu menu = mock(Menu.class);
            MenuEntry entry = mock(MenuEntry.class);
            when(client.getMenu()).thenReturn(menu);
            when(menu.getMenuEntries()).thenReturn(new MenuEntry[]{entry});
            when(entry.getWidget()).thenReturn(widget);
            when(widget.getId()).thenReturn(InterfaceID.Inventory.ITEMS);
            when(widget.getItemId()).thenReturn(100);
            when(service.getItemStatChanges(100)).thenReturn(effect);
            StatsChanges changes = new StatsChanges(2);
            changes.setStatChanges(new StatChange[]{change(Skill.HITPOINTS, 6, 22), change(Skill.PRAYER, 25, 25)});
            when(effect.calculate(client)).thenReturn(changes);
            state = new OverheadsVitalsState(client, config, service, clock::get);
        }

        private static StatChange change(Skill skill, int actual, int theoretical)
        {
            Stat stat = mock(Stat.class);
            when(stat.getName()).thenReturn(skill.getName());
            StatChange change = new StatChange();
            change.setStat(stat);
            change.setRelative(actual);
            change.setTheoretical(theoretical);
            return change;
        }
    }
}
