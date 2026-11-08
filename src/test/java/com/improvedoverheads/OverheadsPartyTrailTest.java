package com.improvedoverheads;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPartyTrailTest
{
    @Test
    public void receivedHealthHonorsHealthToggleAndVisibility()
    {
        OverheadsPartyTest.Fixture f = new OverheadsPartyTest.Fixture(true);
        net.runelite.client.party.messages.PartyData message = f.message();
        int[] base = new int[6], boost = new int[6];
        base[net.runelite.api.Skill.HITPOINTS.ordinal()] = 99;
        boost[net.runelite.api.Skill.HITPOINTS.ordinal()] = 50;
        message.setStatBase(base);
        message.setStatBoost(boost);
        f.accept(message);
        when(f.player.getCanvasImageLocation(any(), anyInt())).thenReturn(new net.runelite.api.Point(120, 180));
        OverheadsIndicators indicators = new OverheadsIndicators(f.client, f.config, f.state, null);
        assertTrue(indicators.replacesHealth(f.player));
        assertTrue(pixels(indicators, f.player) > 0);
        when(f.config.healthBarsEnabled()).thenReturn(false);
        assertFalse(indicators.replacesHealth(f.player));
        assertEquals(0, pixels(indicators, f.player));
        when(f.config.healthBarsEnabled()).thenReturn(true);
        when(f.config.otherHealthVisibility()).thenReturn(OverheadsVisibility.HIDDEN);
        assertFalse(indicators.replacesHealth(f.player));
        assertEquals(0, pixels(indicators, f.player));
    }

    private static int pixels(OverheadsIndicators indicators, net.runelite.api.Player actor)
    {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(240, 240, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        indicators.render(graphics, actor, new net.runelite.api.Point(120, 120), null);
        graphics.dispose();
        int count = 0;
        for (int y = 0; y < 240; y++) for (int x = 0; x < 240; x++) if ((image.getRGB(x, y) >>> 24) != 0) count++;
        return count;
    }

    @Test
    public void partyHealthTrailsHoldDrainAndResetAcrossSessions()
    {
        OverheadsPartyTest.Fixture f = new OverheadsPartyTest.Fixture(true);
        when(f.config.healthBarStyle()).thenReturn(OverheadsHealthBarStyle.SOLID);
        when(f.config.healthBarDamageTrailEnabled()).thenReturn(true);
        when(f.config.healthBarDamageTrailHold()).thenReturn(500);
        when(f.config.healthBarDamageTrailDrain()).thenReturn(1000);
        OverheadsIndicators indicators = new OverheadsIndicators(f.client, f.config, f.state, null);
        assertEquals(1, indicators.healthTrail(f.player, 1, 99, 0), .001);
        assertEquals(1, indicators.healthTrail(f.player, .5, 99, 100), .001);
        assertEquals(1, indicators.healthTrail(f.player, .5, 99, 500), .001);
        assertEquals(.75, indicators.healthTrail(f.player, .5, 99, 1100), .001);
        assertEquals(.5, indicators.healthTrail(f.player, .5, 99, 1600), .001);
        indicators.healthTrail(f.player, 1, 99, 1700);
        indicators.healthTrail(f.player, .3, 99, 1800);
        f.state.clear();
        assertEquals(.3, indicators.healthTrail(f.player, .3, 99, 1900), .001);
        when(f.config.healthBarDamageTrailEnabled()).thenReturn(false);
        assertEquals(.1, indicators.healthTrail(f.player, .1, 99, 2000), .001);
    }
}
