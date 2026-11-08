package com.improvedoverheads;

import com.google.gson.JsonObject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.party.PartyDataService;
import net.runelite.client.party.PartyDataType;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.messages.PartyData;
import net.runelite.client.plugins.Plugin;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPartyTest
{
    @Test
    public void subscriptionIsExplicitMinimalAndReversible()
    {
        Fixture f = new Fixture(false);
        verifyNoInteractions(f.data);
        when(f.config.partyDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        f.state.refresh(f.owner, true);
        verify(f.data).subscribe(f.owner, PartyDataType.CHARACTER_NAME, PartyDataType.ENERGY, PartyDataType.SPEC);
        verify(f.data).subscribe(f.owner, Skill.HITPOINTS, Skill.PRAYER);
        f.state.refresh(f.owner, false);
        verify(f.data).unsubscribe(f.owner, PartyDataType.CHARACTER_NAME, PartyDataType.ENERGY, PartyDataType.SPEC);
        verify(f.data).unsubscribe(f.owner, Skill.HITPOINTS, Skill.PRAYER);
        verifyNoMoreInteractions(f.data);
        verify(f.party, never()).changeParty(anyString());
    }

    @Test
    public void unknownValuesStayHiddenAndReceivedValuesExpireIndependently()
    {
        Fixture f = new Fixture(true);
        assertNull(f.state.value(f.player, 0));
        assertNull(f.state.value(f.player, 2));
        PartyData message = f.message();
        message.setRunEnergy(60);
        f.accept(message);
        assertEquals(60, f.state.value(f.player, 2).current);
        assertNull(f.state.value(f.player, 0));
        message = f.message();
        int[] base = new int[6];
        int[] boost = new int[6];
        base[Skill.HITPOINTS.ordinal()] = 99;
        boost[Skill.HITPOINTS.ordinal()] = 47;
        base[Skill.PRAYER.ordinal()] = 77;
        boost[Skill.PRAYER.ordinal()] = 0;
        message.setStatBase(base);
        message.setStatBoost(boost);
        when(f.client.getTickCount()).thenReturn(150);
        f.accept(message);
        assertEquals(47, f.state.value(f.player, 0).current);
        assertEquals(99, f.state.value(f.player, 0).maximum);
        assertEquals(0, f.state.value(f.player, 1).current);
        when(f.client.getTickCount()).thenReturn(201);
        assertNull(f.state.value(f.player, 2));
        assertNotNull(f.state.value(f.player, 0));
        when(f.client.getTickCount()).thenReturn(251);
        assertNull(f.state.value(f.player, 0));
    }

    @Test
    public void staleSessionsLeaveAndMemberReplacementCannotReviveValues()
    {
        Fixture f = new Fixture(true);
        PartyData message = f.message();
        message.setSpecEnergy(100);
        long oldSession = f.state.session();
        f.state.clear();
        f.state.accept(message, 44, oldSession);
        assertNull(f.state.value(f.player, 3));
        f.accept(message);
        assertNotNull(f.state.value(f.player, 3));
        when(f.member.isLoggedIn()).thenReturn(false);
        assertNull(f.state.value(f.player, 3));
        when(f.member.isLoggedIn()).thenReturn(true);
        when(f.party.getPartyId()).thenReturn(45L);
        assertNull(f.state.value(f.player, 3));
        f.state.accept(message, 44, f.state.session());
        assertNull(f.state.value(f.player, 3));
        f.accept(message);
        PartyMember replacement = mock(PartyMember.class);
        when(replacement.getMemberId()).thenReturn(5L);
        when(replacement.isLoggedIn()).thenReturn(true);
        when(f.party.getMemberByDisplayName("Friend")).thenReturn(replacement);
        assertNull(f.state.value(f.player, 3));
    }

    @Test
    public void setupsCannotTurnOnOrOffPartySharing()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        f.values.put("partyDisplay", OverheadsBarDisplay.BOTH);
        JsonObject document = f.document();
        assertFalse(document.getAsJsonObject("settings").has("partyDisplay"));
        document.getAsJsonObject("settings").addProperty("partyDisplay", "HIDDEN");
        document.getAsJsonObject("settings").addProperty("indicatorWidth", 120);
        OverheadsSetups.Preview preview = f.setups.preview(document.toString(), f.session);
        assertEquals(1, preview.ignored);
        assertEquals(1, f.setups.apply(preview));
        assertEquals(OverheadsBarDisplay.BOTH, f.values.get("partyDisplay"));
        assertFalse(f.raw.containsKey("partyDisplay"));
    }

    @Test
    public void pendingBaselineWaitsForLoginThenSendsOnlyOnce()
    {
        Fixture f = new Fixture(false);
        when(f.client.getGameState()).thenReturn(net.runelite.api.GameState.LOGIN_SCREEN);
        when(f.party.getLocalMember()).thenReturn(mock(PartyMember.class));
        when(f.config.partyDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        f.state.refresh(f.owner, true);
        verify(f.party, never()).send(any(net.runelite.client.party.messages.UserSync.class));
        when(f.client.getGameState()).thenReturn(net.runelite.api.GameState.LOGGED_IN);
        for (int i = 0; i < 10; i++) { f.state.refresh(f.owner, true); }
        verify(f.party, times(1)).send(any(net.runelite.client.party.messages.UserSync.class));
    }

    @Test
    public void manualRefreshIsRateLimitedAndNeverJoinsAParty()
    {
        Fixture f = new Fixture(true);
        when(f.party.getLocalMember()).thenReturn(mock(PartyMember.class));
        f.state.requestRefresh();
        f.state.requestRefresh();
        verify(f.party, times(1)).send(any(net.runelite.client.party.messages.UserSync.class));
        verify(f.party, never()).changeParty(anyString());
        when(f.config.partyDisplay()).thenReturn(OverheadsBarDisplay.HIDDEN);
        f.state.refresh(f.owner, true);
        f.state.requestRefresh();
        verify(f.party, times(1)).send(any(net.runelite.client.party.messages.UserSync.class));
    }

    @Test
    public void partyRowsStayAlignedAndNativeHpReturnsWhenDataExpires()
    {
        Fixture f = new Fixture(true);
        OverheadsRenderPipelineTest.Fixture render = new OverheadsRenderPipelineTest.Fixture();
        when(render.player.getName()).thenReturn("Friend");
        when(render.client.getLocalPlayer()).thenReturn(mock(Player.class));
        when(f.config.healthPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.prayerPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.runPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.specialPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(f.config.otherHealthDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        when(f.config.prayerDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        when(f.config.runDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        when(f.config.specialDisplay()).thenReturn(OverheadsBarDisplay.BOTH);
        PartyData message = f.message();
        int[] base = new int[6], boost = new int[6];
        base[Skill.HITPOINTS.ordinal()] = 99; boost[Skill.HITPOINTS.ordinal()] = 73;
        base[Skill.PRAYER.ordinal()] = 77; boost[Skill.PRAYER.ordinal()] = 50;
        message.setStatBase(base); message.setStatBoost(boost);
        message.setRunEnergy(80); message.setSpecEnergy(50); f.accept(message);
        OverheadsIndicators renderer = new OverheadsIndicators(render.client, f.config, f.state,
            new OverheadsLocalTimers(render.client, mock(net.runelite.client.plugins.PluginManager.class),
                mock(net.runelite.client.game.ItemManager.class), f.config));
        assertTrue(renderer.replacesHealth(render.player));
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(600, 440, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(new OverheadsCollisionLayout.Memory(), f.config.componentGap()))
        {
            renderer.render(graphics, render.player, new net.runelite.api.Point(300, 200), layout);
            layout.render();
            java.util.Map<String, java.awt.Rectangle> bounds = layout.resolvedBounds();
            assertEquals(4, bounds.size());
            for (int i = 1; i < 4; i++)
            {
                assertTrue(bounds.get("party" + i).getMinX() > 300);
                assertFalse(bounds.get("party" + (i - 1)).intersects(bounds.get("party" + i)));
            }
        }
        finally { graphics.dispose(); }
        when(f.config.healthPosition()).thenReturn(OverheadsPosition.LEFT);
        when(f.config.prayerPosition()).thenReturn(OverheadsPosition.LEFT);
        when(f.config.runPosition()).thenReturn(OverheadsPosition.LEFT);
        when(f.config.specialPosition()).thenReturn(OverheadsPosition.LEFT);
        when(f.config.healthOrientation()).thenReturn(OverheadsOrientation.VERTICAL);
        when(f.config.prayerOrientation()).thenReturn(OverheadsOrientation.VERTICAL);
        when(f.config.runOrientation()).thenReturn(OverheadsOrientation.VERTICAL);
        when(f.config.specialOrientation()).thenReturn(OverheadsOrientation.VERTICAL);
        graphics = image.createGraphics();
        try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(new OverheadsCollisionLayout.Memory(), f.config.componentGap()))
        {
            renderer.render(graphics, render.player, new net.runelite.api.Point(300, 200), layout);
            layout.render();
            for (java.awt.Rectangle bounds : layout.resolvedBounds().values()) { assertTrue(bounds.getMaxX() < 300); }
        }
        finally { graphics.dispose(); }
        when(f.client.getTickCount()).thenReturn(201);
        assertFalse(renderer.replacesHealth(render.player));
    }

    private static final class Fixture
    {
        final Client client = mock(Client.class);
        final OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        final PartyService party = mock(PartyService.class);
        final PartyDataService data = mock(PartyDataService.class);
        final Plugin owner = mock(Plugin.class);
        final PartyMember member = mock(PartyMember.class);
        final Player player = mock(Player.class);
        final OverheadsPartyState state = new OverheadsPartyState(client, config, party, data);

        Fixture(boolean enabled)
        {
            when(client.getGameState()).thenReturn(net.runelite.api.GameState.LOGGED_IN);
            when(party.getPartyId()).thenReturn(44L);
            when(party.isInParty()).thenReturn(true);
            when(party.getMemberById(5L)).thenReturn(member);
            when(party.getMemberByDisplayName("Friend")).thenReturn(member);
            when(member.getMemberId()).thenReturn(5L);
            when(member.isLoggedIn()).thenReturn(true);
            when(player.getName()).thenReturn("Friend");
            when(client.getTickCount()).thenReturn(100);
            when(config.partyDisplay()).thenReturn(enabled ? OverheadsBarDisplay.BOTH : OverheadsBarDisplay.HIDDEN);
            state.refresh(owner, true);
        }

        PartyData message()
        {
            PartyData message = new PartyData();
            message.setMemberId(5L);
            return message;
        }

        void accept(PartyData message) { state.accept(message, state.id(), state.session()); }
    }
}
