package com.improvedoverheads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsCompatibilityTest
{
    @Test
    public void filteredBlankCannotResurrectAnEarlierMessage()
    {
        OverheadsLocalChatEffectTracker tracker = new OverheadsLocalChatEffectTracker();
        Object actor = new Object();
        tracker.recordOverhead(actor, actor, "blocked message", 100);
        for (String blank : new String[]{" ", "", null})
        {
            tracker.recordOverhead(actor, actor, "blocked message", 100);
            assertEquals(blank, tracker.messageStateFor(actor, blank, 150, 100, 150).getText());
        }
    }

    @Test
    public void censoredTextAndNativeExpiryWinOverCachedText()
    {
        OverheadsLocalChatEffectTracker tracker = new OverheadsLocalChatEffectTracker();
        Object actor = new Object();
        tracker.recordOverhead(actor, actor, "original", 100);
        assertEquals("censored", tracker.messageStateFor(actor, "censored", 100, 101, 150).getText());
        tracker.recordOverhead(actor, actor, "shortened", 100);
        assertEquals(0, tracker.messageStateFor(actor, "shortened", 0, 104, 150).getOverheadCycle());
    }

    @Test
    public void realEventBusRunsFilteringBeforeOverheadObservation() throws Exception
    {
        Client client = mock(Client.class);
        Player player = mock(Player.class);
        AtomicReference<String> live = new AtomicReference<>("blocked message");
        when(player.getOverheadText()).thenAnswer(call -> live.get());
        doAnswer(call -> { live.set(call.getArgument(0)); return null; }).when(player).setOverheadText(any());
        when(client.getLocalPlayer()).thenReturn(player);
        when(client.getGameCycle()).thenReturn(100);
        OverheadsPlugin plugin = new OverheadsPlugin();
        OverheadsLocalChatEffectTracker tracker = new OverheadsLocalChatEffectTracker();
        inject(plugin, "client", client);
        inject(plugin, "localChatEffectTracker", tracker);
        EventBus bus = new EventBus(error -> { throw new AssertionError(error); });
        bus.register(plugin);
        bus.register(new Filter());
        bus.post(new OverheadTextChanged(player, "blocked message"));
        assertEquals(" ", tracker.messageStateFor(player, player.getOverheadText(), 150, 100, 150).getText());
    }

    @Test
    public void entityHidersWinAndDisableRestoresTheNativePass()
    {
        Client client = mock(Client.class);
        when(client.getGameCycle()).thenReturn(30);
        Player player = mock(Player.class);
        RenderCallbackManager manager = new RenderCallbackManager();
        OverheadsActorUiVisibilityTracker tracker = new OverheadsActorUiVisibilityTracker(client, manager);
        RenderCallback hider = new RenderCallback()
        {
            @Override
            public boolean addEntity(Renderable renderable, boolean ui)
            {
                return !ui;
            }
        };
        manager.register(hider);
        tracker.enable();
        assertFalse(manager.addEntity(player, true));
        List<Actor> accepted = new ArrayList<>();
        tracker.copyAcceptedInto(accepted, 30);
        assertTrue(accepted.isEmpty());
        manager.unregister(hider);
        assertFalse(manager.addEntity(player, true));
        tracker.copyAcceptedInto(accepted, 30);
        assertEquals(List.of(player), accepted);
        assertTrue(manager.addEntity(player, false));
        tracker.disable();
        assertTrue(manager.addEntity(player, true));
        accepted.clear();
        tracker.copyAcceptedInto(accepted, 30);
        assertTrue(accepted.isEmpty());
        tracker.enable();
        manager.register(hider);
        tracker.moveToEnd();
        assertFalse(manager.addEntity(player, true));
        tracker.copyAcceptedInto(accepted, 30);
        assertTrue(accepted.isEmpty());
        tracker.disable();
        assertFalse(manager.addEntity(player, true));
    }

    @Test
    public void preservesNativeSpecialBarsAndDropsOldActorFrames()
    {
        Client client = mock(Client.class);
        when(client.getGameCycle()).thenReturn(40);
        RenderCallbackManager manager = new RenderCallbackManager();
        OverheadsActorUiVisibilityTracker tracker = new OverheadsActorUiVisibilityTracker(client, manager);
        tracker.enable();
        for (int id : new int[]{NpcID.DOM_BOSS, NpcID.DOM_BOSS_SHIELDED, NpcID.TOA_KEPHRI_BOSS_SHIELDED,
            NpcID.YAMA_VOIDFLARE, NpcID.MM_LAB_TABLE_BAR})
        {
            NPC npc = mock(NPC.class);
            when(npc.getId()).thenReturn(id);
            assertTrue(manager.addEntity(npc, true));
            assertTrue(tracker.retainedNativeUi(npc, 40));
        }
        Player player = mock(Player.class);
        assertFalse(manager.addEntity(player, true));
        List<Actor> accepted = new ArrayList<>();
        tracker.copyAcceptedInto(accepted, 41);
        assertTrue(accepted.isEmpty());
        tracker.remove(player);
        tracker.discardBefore(41);
        tracker.copyAcceptedInto(accepted, 40);
        assertTrue(accepted.isEmpty());
    }

    static void inject(Object object, String field, Object value) throws Exception
    {
        java.lang.reflect.Field target = object.getClass().getDeclaredField(field);
        target.setAccessible(true);
        target.set(object, value);
    }

    public static class Filter
    {
        @Subscribe
        public void onOverheadTextChanged(OverheadTextChanged event)
        {
            event.getActor().setOverheadText(" ");
        }
    }
}
