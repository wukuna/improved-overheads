package com.improvedoverheads;

import com.google.gson.JsonObject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.VarPlayerID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsReleaseFeaturesTest
{
    @Test public void undoRestoresOnlyAppliedSettingsAndNeverPartyConsent()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("nameXOffset", 33);
        document.getAsJsonObject("settings").addProperty("runHideWhenFull", true);
        document.getAsJsonObject("settings").addProperty("partyDisplay", "BOTH");
        OverheadsSetups.Preview applied = f.setups.preview(document.toString(), f.session);
        assertEquals(2, f.setups.apply(applied));
        OverheadsSetups.Preview undo = f.setups.reverse(applied);
        f.values.put("runOpacity", 45);
        assertEquals(2, f.setups.apply(undo));
        assertEquals(0, f.values.get("nameXOffset"));
        assertEquals(false, f.values.get("runHideWhenFull"));
        assertEquals(45, f.values.get("runOpacity"));
        assertFalse(f.raw.containsKey("partyDisplay"));
    }

    @Test public void undoRejectsConflictingEditsAndExpiredSessionBeforeWriting()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("nameXOffset", 33);
        OverheadsSetups.Preview applied = f.setups.preview(document.toString(), f.session);
        f.setups.apply(applied);
        OverheadsSetups.Preview undo = f.setups.reverse(applied);
        f.values.put("nameXOffset", 44);
        f.raw.clear();
        assertThrows(IllegalStateException.class, () -> f.setups.apply(undo));
        assertTrue(f.raw.isEmpty());
        f.values.put("nameXOffset", 33);
        f.setups.invalidate();
        assertThrows(IllegalStateException.class, () -> f.setups.apply(undo));
        assertTrue(f.raw.isEmpty());
    }

    @Test public void hitsplatThemeUndoLeavesOtherAppearanceAlone()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        OverheadsSetups.Preview applied = f.setups.preview(f.setups.themePreset(
            OverheadsHitsplatStyle.MODERN, OverheadsHitsplatMotion.SCROLL_UP, f.session), f.session, true);
        JsonObject before = f.setups.schema.snapshot();
        f.setups.apply(applied);
        OverheadsSetups.Preview undo = f.setups.reverse(applied);
        f.values.put("nameXOffset", 29);
        f.setups.apply(undo);
        assertEquals(before.get("hitsplatStyle"), f.setups.schema.snapshot().get("hitsplatStyle"));
        assertEquals(before.get("hitsplatMotion"), f.setups.schema.snapshot().get("hitsplatMotion"));
        assertEquals(29, f.values.get("nameXOffset"));
    }

    @Test public void fullResourceHidingIsIndependentAndUsesUnroundedEnergy()
    {
        Client client = mock(Client.class);
        Player player = mock(Player.class);
        when(client.getLocalPlayer()).thenReturn(player);
        OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        OverheadsExtrasRenderer renderer = new OverheadsExtrasRenderer(client, config);
        for (OverheadsBarDisplay display : OverheadsBarDisplay.values())
        {
            when(config.runDisplay()).thenReturn(display);
            when(config.specialDisplay()).thenReturn(display);
            when(client.getEnergy()).thenReturn(10000);
            when(client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(1000);
            when(config.runHideWhenFull()).thenReturn(false);
            when(config.specialHideWhenFull()).thenReturn(false);
            assertEquals(display != OverheadsBarDisplay.HIDDEN, renderer.resourceVisible(false));
            when(config.runHideWhenFull()).thenReturn(true);
            assertFalse(renderer.resourceVisible(false));
            assertEquals(display != OverheadsBarDisplay.HIDDEN, renderer.resourceVisible(true));
            when(config.specialHideWhenFull()).thenReturn(true);
            assertFalse(renderer.resourceVisible(true));
            when(client.getEnergy()).thenReturn(9999);
            when(client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(999);
            assertEquals(display != OverheadsBarDisplay.HIDDEN, renderer.resourceVisible(false));
            assertEquals(display != OverheadsBarDisplay.HIDDEN, renderer.resourceVisible(true));
        }
    }
}
