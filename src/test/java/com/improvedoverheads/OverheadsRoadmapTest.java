package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.regenmeter.RegenMeterPlugin;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsRoadmapTest
{
    @Test
    public void nativeUiFollowsMorphsAndExplicitExceptions()
    {
        NPC npc = mock(NPC.class);
        NPCComposition composition = mock(NPCComposition.class);
        when(npc.getId()).thenReturn(123);
        when(npc.getTransformedComposition()).thenReturn(composition);
        when(composition.getId()).thenReturn(NpcID.WHISPERER);
        assertTrue(OverheadsActorUiVisibilityTracker.requiresNativeUi(npc));
        when(composition.getId()).thenReturn(456);
        assertFalse(OverheadsActorUiVisibilityTracker.requiresNativeUi(npc));
        OverheadsActorUiVisibilityTracker tracker = new OverheadsActorUiVisibilityTracker(mock(Client.class), new RenderCallbackManager());
        tracker.setNativeExceptions("456, bad, -1");
        assertTrue(tracker.nativeException(npc));
        when(composition.getId()).thenReturn(789);
        assertFalse(tracker.nativeException(npc));
        tracker.setNativeExceptions("123");
        assertTrue(tracker.nativeException(npc));
        tracker.setNativeExceptions("");
        assertFalse(tracker.nativeException(npc));
        when(npc.getTransformedComposition()).thenReturn(null);
        assertFalse(OverheadsActorUiVisibilityTracker.requiresNativeUi(npc));
    }

    @Test
    public void relationshipPaletteUsesObservablePriorityAndMorphActions()
    {
        Client client = mock(Client.class);
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        when(config.relationshipNames()).thenReturn(true);
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Party player");
        OverheadsExtrasRenderer renderer = new OverheadsExtrasRenderer(client, config);
        renderer.setPartyMember(name -> name.equals("Party player"));
        assertEquals(config.partyNameColor(), renderer.nameColor(player));
        when(player.isClanMember()).thenReturn(true);
        assertEquals(config.clanNameColor(), renderer.nameColor(player));
        when(player.isFriend()).thenReturn(true);
        assertEquals(config.friendNameColor(), renderer.nameColor(player));
        when(client.getLocalPlayer()).thenReturn(player);
        assertEquals(config.selfNameColor(), renderer.nameColor(player));
        NPC npc = mock(NPC.class);
        NPCComposition composition = mock(NPCComposition.class);
        when(npc.getTransformedComposition()).thenReturn(composition);
        when(composition.getActions()).thenReturn(new String[]{null, "Trade"});
        assertEquals(config.shopNameColor(), renderer.nameColor(npc));
        when(composition.isFollower()).thenReturn(true);
        assertEquals(config.followerNameColor(), renderer.nameColor(npc));
        when(npc.getTransformedComposition()).thenReturn(null);
        assertEquals(config.npcNameColor(), renderer.nameColor(npc));
        when(config.relationshipNames()).thenReturn(false);
        assertEquals(config.nameColor(), renderer.nameColor(npc));
    }

    @Test
    public void viewportShiftPreservesSpacingAndDoesNotAccumulate()
    {
        OverheadsCollisionLayout.Memory memory = new OverheadsCollisionLayout.Memory();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            Rectangle first = null;
            for (int i = 0; i < 3; i++)
            {
                try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(memory, 4))
                {
                    layout.configure(new Rectangle(0, 0, 100, 100), false, true);
                    for (String key : new String[]{"health", "prayer"})
                    {
                        OverheadsCollisionLayout.draw(layout, graphics, key, OverheadsPosition.ABOVE,
                            new Rectangle(20, -5, 30, 8), g -> { g.setColor(Color.WHITE); g.fillRect(20, -5, 30, 8); });
                    }
                    layout.render();
                    Rectangle health = layout.resolvedBounds().get("health");
                    Rectangle prayer = layout.resolvedBounds().get("prayer");
                    assertFalse(health.intersects(prayer));
                    assertTrue(health.y >= 0 && prayer.y >= 0);
                    if (first != null) { assertEquals(first, health); }
                    first = health;
                }
            }
        }
        finally { graphics.dispose(); }
    }

    @Test
    public void poisonTimerRequiresObservedDamageAndStopsAtCureOrMissedTick()
    {
        TimerFixture f = new TimerFixture();
        when(f.client.getVarpValue(VarPlayerID.POISON)).thenReturn(50);
        when(f.client.getTickCount()).thenReturn(100);
        assertEquals(-1, f.timers.poisonRemaining(), 0);
        f.timers.observeHit(HitsplatID.POISON);
        assertEquals(1, f.timers.poisonRemaining(), 0);
        when(f.client.getTickCount()).thenReturn(115);
        assertEquals(0.5, f.timers.poisonRemaining(), 0);
        when(f.client.getTickCount()).thenReturn(130);
        assertEquals(-1, f.timers.poisonRemaining(), 0);
        f.timers.observeHit(HitsplatID.VENOM);
        assertEquals(1, f.timers.poisonRemaining(), 0);
        when(f.client.getVarpValue(VarPlayerID.POISON)).thenReturn(-30);
        f.timers.tick();
        when(f.client.getVarpValue(VarPlayerID.POISON)).thenReturn(50);
        assertEquals(-1, f.timers.poisonRemaining(), 0);
        f.timers.observeHit(HitsplatID.POISON);
        f.timers.clear();
        assertEquals(-1, f.timers.poisonRemaining(), 0);
    }

    @Test
    public void regenUsesActiveCoreServiceAndHidesUnknownCapePhase()
    {
        TimerFixture f = new TimerFixture();
        when(f.config.regenDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        RegenMeterPlugin core = mock(RegenMeterPlugin.class);
        when(f.plugins.getPlugins()).thenReturn(Collections.singletonList(core));
        when(f.client.getBoostedSkillLevel(Skill.HITPOINTS)).thenReturn(50);
        when(f.client.getRealSkillLevel(Skill.HITPOINTS)).thenReturn(99);
        when(core.getHitpointsPercentage()).thenReturn(0.25);
        f.timers.tick();
        assertEquals(-1, f.timers.regenRemaining(), 0);
        when(f.plugins.isPluginActive(core)).thenReturn(true);
        f.timers.tick();
        assertEquals(0.75, f.timers.regenRemaining(), 0);
        assertEquals(100, f.timers.regenTicks());
        ItemContainer equipment = mock(ItemContainer.class);
        ItemComposition cape = mock(ItemComposition.class);
        when(f.client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment);
        when(equipment.getItem(1)).thenReturn(new Item(123, 1));
        when(f.items.getItemComposition(123)).thenReturn(cape);
        when(cape.getName()).thenReturn("Hitpoints cape(t)");
        f.timers.tick();
        assertEquals(-1, f.timers.regenRemaining(), 0);
        when(f.client.isPrayerActive(Prayer.RAPID_HEAL)).thenReturn(true);
        f.timers.tick();
        assertEquals(0.75, f.timers.regenRemaining(), 0);
        assertEquals(50, f.timers.regenTicks());
        when(f.client.getBoostedSkillLevel(Skill.HITPOINTS)).thenReturn(99);
        f.timers.tick();
        assertEquals(-1, f.timers.regenRemaining(), 0);
    }

    @Test
    public void activePrayerBadgesAreLocalOnlyAndDisappearWhenPrayerStops()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.localBoostPrayerIcons()).thenReturn(true);
        when(f.config.showSkullIcons()).thenReturn(false);
        when(f.config.showPrayerIcons()).thenReturn(false);
        BufferedImage sprite = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = sprite.createGraphics();
        graphics.setColor(Color.GREEN);
        graphics.fillOval(0, 0, 20, 20);
        graphics.dispose();
        f.installSprite(net.runelite.api.gameval.SpriteID.Prayeron.PIETY, sprite);
        when(f.client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
        assertTrue(f.icons.prepare(f.player));
        Player other = mock(Player.class);
        assertFalse(f.icons.prepare(other));
        when(f.client.isPrayerActive(Prayer.PIETY)).thenReturn(false);
        assertFalse(f.icons.prepare(f.player));
        when(f.client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
        when(f.config.headIconsEnabled()).thenReturn(false);
        assertFalse(f.icons.prepare(f.player));
    }

    @Test
    public void numberOnlyTimerRendersWithHealthHiddenAndRespectsViewportAndMask()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.prayerDisplay()).thenReturn(OverheadsBarDisplay.HIDDEN);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.overheadChatEnabled()).thenReturn(false);
        when(f.config.poisonTimerDisplay()).thenReturn(OverheadsBarDisplay.NUMBERS_ONLY);
        when(f.config.indicatorPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.keepOnScreen()).thenReturn(true);
        when(f.client.getViewportWidth()).thenReturn(600);
        when(f.client.getViewportHeight()).thenReturn(440);
        when(f.client.getTickCount()).thenReturn(100);
        when(f.client.getVarpValue(VarPlayerID.POISON)).thenReturn(50);
        OverheadsLocalTimers timers = new OverheadsLocalTimers(f.client, mock(PluginManager.class), mock(ItemManager.class), f.config);
        OverheadsPartyState party = new OverheadsPartyState(f.client, f.config,
            mock(net.runelite.client.party.PartyService.class), mock(net.runelite.client.party.PartyDataService.class));
        f.overlay.configureIndicators(party, timers);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        timers.observeHit(HitsplatID.POISON);
        Rectangle bounds = OverheadsRenderPipelineTest.pixelBounds(f.render());
        assertNotNull(bounds);
        assertTrue(bounds.x > f.centerX);
        f.centerX = 595;
        bounds = OverheadsRenderPipelineTest.pixelBounds(f.render());
        assertNotNull(bounds);
        assertTrue(bounds.getMaxX() < 600);
        when(f.client.getDraw2DMask()).thenReturn(0);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
    }

    private static final class TimerFixture
    {
        final Client client = mock(Client.class);
        final OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        final PluginManager plugins = mock(PluginManager.class);
        final ItemManager items = mock(ItemManager.class);
        final OverheadsLocalTimers timers = new OverheadsLocalTimers(client, plugins, items, config);
    }
}
