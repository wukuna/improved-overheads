package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Point;
import net.runelite.api.Projection;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.callback.RenderCallbackManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsRenderPipelineTest
{
    @Test
    public void completeRenderHonorsDrawMasksAndLiveFilteredText()
    {
        Fixture f = new Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.player.getOverheadText()).thenReturn("Visible message");
        when(f.player.getOverheadCycle()).thenReturn(150);
        assertNotNull(pixelBounds(f.render()));
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_OTHERS);
        assertNull(pixelBounds(f.render()));
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_OVERHEAD_TEXT);
        assertNotNull(pixelBounds(f.render()));
        when(f.player.getOverheadText()).thenReturn(" ");
        assertNull(pixelBounds(f.render()));
        f.tracker.disable();
        when(f.player.getOverheadText()).thenReturn("Visible message");
        assertNull(pixelBounds(f.render()));
    }

    @Test
    public void modernHitsplatGroupsStayOnTheirSelectedSide()
    {
        Fixture f = new Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.MODERN);
        f.hits.add(new OverheadsHitsplat(HitsplatID.DAMAGE_ME, 42, 0, 90, 90, 90, 200, 220, 1, true));
        f.hits.add(new OverheadsHitsplat(HitsplatID.BLOCK_ME, 0, 1, 90, 90, 90, 200, 220, 2, true));
        for (OverheadsPosition side : OverheadsPosition.values())
        {
            when(f.config.hitsplatPosition()).thenReturn(OverheadsHitsplatPosition.valueOf(side.name()));
            Rectangle pixels = pixelBounds(f.render());
            assertNotNull(pixels);
            switch (side)
            {
                case LEFT: assertTrue(pixels.getMaxX() < f.centerX); break;
                case RIGHT: assertTrue(pixels.x > f.centerX); break;
                case ABOVE: assertTrue(pixels.getMaxY() < f.feetY - 100); break;
                case BELOW: assertTrue(pixels.y > f.feetY); break;
            }
        }
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_OVERHEAD_TEXT);
        assertNull(pixelBounds(f.render()));
        when(f.client.getDraw2DMask()).thenReturn(Client.DRAW_2D_ALL);
        f.cycle = 230;
        assertNull(pixelBounds(f.render()));
    }

    @Test
    public void nativeHitsplatPositionStaysCenteredOnTargetAndHonorsOffsets()
    {
        Fixture f = new Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.MODERN);
        when(f.config.hitsplatPosition()).thenReturn(OverheadsHitsplatPosition.NATIVE);
        f.hits.add(new OverheadsHitsplat(HitsplatID.DAMAGE_ME, 42, 0, 90, 90, 90, 200, 220, 1, true));
        Rectangle nativeBounds = pixelBounds(f.render());
        assertNotNull(nativeBounds);
        assertEquals(f.centerX, nativeBounds.getCenterX(), 2);
        assertEquals(f.feetY - 50, nativeBounds.getCenterY(), 2);
        when(f.config.avoidOverlap()).thenReturn(false);
        assertEquals(nativeBounds, pixelBounds(f.render()));
        when(f.config.hitsplatXOffset()).thenReturn(17);
        when(f.config.hitsplatYOffset()).thenReturn(12);
        Rectangle shifted = pixelBounds(f.render());
        assertEquals(nativeBounds.x + 17, shifted.x);
        assertEquals(nativeBounds.y - 12, shifted.y);
        when(f.player.getCanvasImageLocation(any(), eq(50))).thenReturn(null);
        assertNull(pixelBounds(f.render()));
    }

    @Test
    public void zoomScalesBarsOnlyWhenEnabled()
    {
        Fixture f = new Fixture();
        when(f.config.selfHealthDisplay()).thenReturn(OverheadsBarDisplay.BAR_ONLY);
        Rectangle fixed = pixelBounds(f.render());
        f.zoom = 2;
        Rectangle stillFixed = pixelBounds(f.render());
        assertEquals(fixed.width, stillFixed.width);
        assertEquals(fixed.height, stillFixed.height);
        when(f.config.healthScaleWithZoom()).thenReturn(true);
        Rectangle scaled = pixelBounds(f.render());
        assertTrue(scaled.width >= fixed.width * 1.8);
        assertTrue(scaled.height >= fixed.height * 1.7);
    }

    @Test
    public void healthVisibilityIsIndependentForEachActorGroup()
    {
        Fixture f = new Fixture();
        Player other = mock(Player.class);
        NPC npc = mock(NPC.class);
        when(other.getHealthRatio()).thenReturn(20);
        when(other.getHealthScale()).thenReturn(30);
        when(npc.getHealthRatio()).thenReturn(20);
        when(npc.getHealthScale()).thenReturn(30);
        when(f.config.selfHealthDisplay()).thenReturn(OverheadsBarDisplay.HIDDEN);
        when(f.config.otherHealthDisplay()).thenReturn(OverheadsBarDisplay.HIDDEN);
        assertFalse(f.health.hasRenderableHealthBar(f.player));
        assertFalse(f.health.hasRenderableHealthBar(other));
        assertTrue(f.health.hasRenderableHealthBar(npc));
        when(f.config.prayerVisibility()).thenReturn(OverheadsPrayerVisibility.ALWAYS);
        assertTrue(f.health.hasRenderableHealthBar(f.player));
    }

    @Test
    public void prayerAppearanceDoesNotInheritHealthColorsOrDirection()
    {
        Fixture f = new Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.healthBarStyle()).thenReturn(OverheadsHealthBarStyle.NATIVE);
        when(f.config.prayerBarStyle()).thenReturn(OverheadsHealthBarStyle.SOLID);
        when(f.config.prayerVisibility()).thenReturn(OverheadsPrayerVisibility.ALWAYS);
        when(f.config.prayerDisplay()).thenReturn(OverheadsBarDisplay.BAR_ONLY);
        when(f.config.prayerColor()).thenReturn(Color.CYAN);
        when(f.config.prayerBackColor()).thenReturn(Color.BLUE);
        when(f.config.healthBarBackColor()).thenReturn(Color.MAGENTA);
        when(f.config.prayerFillDirection()).thenReturn(OverheadsHealthBarFillDirection.RIGHT_TO_LEFT);
        BufferedImage image = f.render();
        Rectangle bounds = pixelBounds(image);
        assertEquals(Color.BLUE.getRGB(), image.getRGB(bounds.x + 7, bounds.y + bounds.height / 2));
        assertEquals(Color.CYAN.getRGB(), image.getRGB(bounds.x + bounds.width - 7, bounds.y + bounds.height / 2));
    }

    @Test
    public void numberFormatsPreserveOverhealAndHandleUnknownMaximum()
    {
        assertEquals("73", OverheadsNumberFormat.CURRENT.format(73, 99));
        assertEquals("73/99", OverheadsNumberFormat.CURRENT_MAXIMUM.format(73, 99));
        assertEquals("74%", OverheadsNumberFormat.PERCENTAGE.format(73, 99));
        assertEquals("121%", OverheadsNumberFormat.PERCENTAGE.format(120, 99));
        assertEquals("0/99", OverheadsNumberFormat.CURRENT_MAXIMUM.format(-1, 99));
        assertEquals("73/?", OverheadsNumberFormat.CURRENT_MAXIMUM.format(73, 0));
        assertEquals("?", OverheadsNumberFormat.PERCENTAGE.format(73, 0));
    }

    @Test
    public void specialEncounterHitsplatsKeepTheirOriginalArtwork()
    {
        assertTrue(OverheadsModernHitsplatRenderer.supports(HitsplatID.DAMAGE_ME));
        assertTrue(OverheadsModernHitsplatRenderer.supports(HitsplatID.BLOCK_OTHER));
        assertFalse(OverheadsModernHitsplatRenderer.supports(HitsplatID.DOOM));
        assertFalse(OverheadsModernHitsplatRenderer.supports(Integer.MAX_VALUE));
    }

    static Rectangle pixelBounds(BufferedImage image)
    {
        Rectangle bounds = null;
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                if ((image.getRGB(x, y) >>> 24) != 0)
                {
                    if (bounds == null) { bounds = new Rectangle(x, y, 1, 1); }
                    else { bounds.add(x, y); }
                }
            }
        }
        return bounds;
    }

    static class Fixture
    {
        final Client client = mock(Client.class);
        final Player player = mock(Player.class);
        final OverheadsConfig config = mock(OverheadsConfig.class, CALLS_REAL_METHODS);
        final RenderCallbackManager callbacks = new RenderCallbackManager();
        final OverheadsActorUiVisibilityTracker tracker = new OverheadsActorUiVisibilityTracker(client, callbacks);
        final OverheadsHealthBarRenderer health = new OverheadsHealthBarRenderer(client, config);
        final OverheadsActorUiSpriteCache sprites = new OverheadsActorUiSpriteCache(client);
        final OverheadsHeadIconRenderer icons = new OverheadsHeadIconRenderer(client, config, sprites);
        final OverheadsPlugin plugin = mock(OverheadsPlugin.class);
        final CopyOnWriteArrayList<OverheadsHitsplat> hits = new CopyOnWriteArrayList<>();
        final Map<Actor, CopyOnWriteArrayList<OverheadsHitsplat>> hitMap = new HashMap<>();
        final OverheadsOverlay overlay;
        int cycle = 100;
        int centerX = 300;
        int feetY = 300;
        double zoom = 1;

        Fixture()
        {
            when(client.getLocalPlayer()).thenReturn(player);
            when(client.getGameCycle()).thenAnswer(call -> cycle);
            when(client.getDraw2DMask()).thenReturn(Client.DRAW_2D_ALL);
            when(client.getRealSkillLevel(Skill.HITPOINTS)).thenReturn(99);
            when(client.getBoostedSkillLevel(Skill.HITPOINTS)).thenReturn(73);
            when(client.getRealSkillLevel(Skill.PRAYER)).thenReturn(99);
            when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(62);
            when(client.getScale()).thenReturn(750);
            when(player.getName()).thenReturn("Preview player");
            when(player.getPlayerComposition()).thenReturn(mock(PlayerComposition.class));
            when(player.getHealthRatio()).thenReturn(22);
            when(player.getHealthScale()).thenReturn(30);
            when(player.getLogicalHeight()).thenReturn(100);
            when(player.getCanvasImageLocation(any(), anyInt())).thenAnswer(call ->
                new Point(centerX, feetY - (int) Math.round((Integer) call.getArgument(1) * zoom)));
            WorldView view = mock(WorldView.class);
            Projection projection = mock(Projection.class);
            when(player.getWorldView()).thenReturn(view);
            LocalPoint location = new LocalPoint(3200, 3200, view);
            when(player.getLocalLocation()).thenReturn(location);
            when(client.getWorldView(anyInt())).thenReturn(view);
            when(view.getTileSettings()).thenReturn(new byte[4][104][104]);
            when(view.getCanvasProjection()).thenReturn(projection);
            when(projection.project(anyFloat(), anyFloat(), anyFloat(), any())).thenAnswer(call ->
                new float[]{centerX, feetY, (float) (2000 / zoom)});
            hitMap.put(player, hits);
            when(plugin.getHitsplats()).thenReturn(hitMap);
            overlay = new OverheadsOverlay(client, plugin, config, new OverheadsSettings(config), tracker,
                health, icons, new OverheadsOverheadChatRenderer(), new OverheadsLocalChatEffectTracker());
            tracker.enable();
        }

        BufferedImage render()
        {
            BufferedImage image = new BufferedImage(600, 440, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            callbacks.addEntity(player, true);
            overlay.render(g);
            g.dispose();
            return image;
        }

        void installSprite(int id, BufferedImage image)
        {
            SpritePixels sprite = mock(SpritePixels.class);
            when(sprite.toBufferedImage()).thenReturn(image);
            when(sprite.getMaxWidth()).thenReturn(image.getWidth());
            when(sprite.getMaxHeight()).thenReturn(image.getHeight());
            when(client.getSprites(any(), eq(id), eq(0))).thenReturn(new SpritePixels[]{sprite});
        }
    }
}
