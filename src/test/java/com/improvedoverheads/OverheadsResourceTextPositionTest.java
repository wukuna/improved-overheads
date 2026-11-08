package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.party.messages.PartyData;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsResourceTextPositionTest
{
    @Test
    public void localResourceTextUsesItsOwnPositionAndCollisionBounds()
    {
        for (int resource = 1; resource <= 3; resource++)
            for (OverheadsBarTextPosition position : new OverheadsBarTextPosition[]{OverheadsBarTextPosition.ABOVE, OverheadsBarTextPosition.BELOW})
                for (OverheadsOrientation orientation : OverheadsOrientation.values())
                {
                    OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
                    configure(f.config, resource, position, orientation);
                    when(f.client.getEnergy()).thenReturn(5000);
                    when(f.client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(500);
                    BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = image.createGraphics();
                    try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(new OverheadsCollisionLayout.Memory(), 3))
                    {
                        if (resource == 1) f.health.render(g, f.player, new Point(300, 200), 200, layout);
                        else new OverheadsExtrasRenderer(f.client, f.config).render(g, f.player, new Point(300, 200), 200, layout);
                        layout.render();
                        assertPlacement(image, position);
                        Rectangle all = OverheadsRenderPipelineTest.pixelBounds(image);
                        assertTrue(layout.resolvedBounds().values().stream().anyMatch(bounds -> bounds.contains(all)));
                    }
                    g.dispose();
                }
    }

    @Test
    public void sharedResourcesHonorTheSameIndependentSettings()
    {
        for (int resource = 1; resource <= 3; resource++)
            for (OverheadsBarTextPosition position : new OverheadsBarTextPosition[]{OverheadsBarTextPosition.ABOVE, OverheadsBarTextPosition.BELOW})
            {
                OverheadsPartyTest.Fixture party = new OverheadsPartyTest.Fixture(true);
                OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
                when(f.player.getName()).thenReturn("Friend");
                when(f.client.getLocalPlayer()).thenReturn(mock(Player.class));
                configure(party.config, resource, position, OverheadsOrientation.HORIZONTAL);
                PartyData data = party.message();
                int[] base = new int[6], boost = new int[6];
                base[Skill.PRAYER.ordinal()] = 99; boost[Skill.PRAYER.ordinal()] = 50;
                data.setStatBase(base); data.setStatBoost(boost); data.setRunEnergy(50); data.setSpecEnergy(50); party.accept(data);
                BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                try (OverheadsCollisionLayout layout = new OverheadsCollisionLayout(new OverheadsCollisionLayout.Memory(), 3))
                {
                    new OverheadsIndicators(f.client, party.config, party.state, null).render(g, f.player, new Point(300, 200), layout);
                    layout.render();
                    assertPlacement(image, position);
                    assertTrue(layout.resolvedBounds().get("party" + resource).contains(OverheadsRenderPipelineTest.pixelBounds(image)));
                }
                g.dispose();
            }
    }

    private static void configure(OverheadsConfig c, int resource, OverheadsBarTextPosition position, OverheadsOrientation orientation)
    {
        when(c.healthBarsEnabled()).thenReturn(false);
        when(c.partyShowHealth()).thenReturn(false);
        when(c.prayerVisibility()).thenReturn(OverheadsPrayerVisibility.ALWAYS);
        when(c.prayerDisplay()).thenReturn(resource == 1 ? OverheadsBarDisplay.BOTH : OverheadsBarDisplay.HIDDEN);
        when(c.runDisplay()).thenReturn(resource == 2 ? OverheadsBarDisplay.BOTH : OverheadsBarDisplay.HIDDEN);
        when(c.specialDisplay()).thenReturn(resource == 3 ? OverheadsBarDisplay.BOTH : OverheadsBarDisplay.HIDDEN);
        when(c.prayerTextPosition()).thenReturn(resource == 1 ? position : OverheadsBarTextPosition.ON_BAR);
        when(c.runTextPosition()).thenReturn(resource == 2 ? position : OverheadsBarTextPosition.ON_BAR);
        when(c.specialTextPosition()).thenReturn(resource == 3 ? position : OverheadsBarTextPosition.ON_BAR);
        when(c.prayerTextColor()).thenReturn(Color.MAGENTA);
        when(c.runTextColor()).thenReturn(Color.MAGENTA);
        when(c.specialTextColor()).thenReturn(Color.MAGENTA);
        when(c.prayerColor()).thenReturn(Color.GREEN);
        when(c.runColor()).thenReturn(Color.GREEN);
        when(c.specialColor()).thenReturn(Color.GREEN);
        when(c.prayerOpacity()).thenReturn(100);
        when(c.runOpacity()).thenReturn(100);
        when(c.specialOpacity()).thenReturn(100);
        when(c.prayerOrientation()).thenReturn(orientation);
        when(c.runOrientation()).thenReturn(orientation);
        when(c.specialOrientation()).thenReturn(orientation);
        when(c.prayerPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(c.runPosition()).thenReturn(OverheadsPosition.RIGHT);
        when(c.specialPosition()).thenReturn(OverheadsPosition.RIGHT);
    }

    private static void assertPlacement(BufferedImage image, OverheadsBarTextPosition position)
    {
        Rectangle text = OverheadsPrayerRowsTest.pixels(image, Color.MAGENTA);
        Rectangle bar = OverheadsPrayerRowsTest.pixels(image, Color.GREEN);
        assertNotNull(text); assertNotNull(bar);
        if (position == OverheadsBarTextPosition.ABOVE) assertTrue(text.getMaxY() < bar.y);
        else assertTrue(text.y > bar.getMaxY());
    }
}
