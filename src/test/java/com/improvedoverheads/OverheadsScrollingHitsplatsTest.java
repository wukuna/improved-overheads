package com.improvedoverheads;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.api.HitsplatID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsScrollingHitsplatsTest
{
    @Test
    public void simultaneousHitsHaveSeparateLanesAndRowsAtEveryScale()
    {
        List<OverheadsHitsplat> hits = new ArrayList<>();
        for (int i = 0; i < 128; i++)
        {
            hits.add(hit(i, i, 0, 100));
        }
        for (int scale : new int[]{50, 100, 200})
        {
            for (int columns = 1; columns <= 4; columns++)
            {
                for (OverheadsHitsplatMotion motion : new OverheadsHitsplatMotion[]{OverheadsHitsplatMotion.SCROLL_UP, OverheadsHitsplatMotion.SCROLL_DOWN})
                {
                    int width = 30 * scale / 100;
                    int height = 20 * scale / 100;
                    Map<Long, OverheadsOffset> offsets = new OverheadsScrollingHitsplats()
                        .layout(hits, 25, width, height, scale, columns, -20, -20, 48, motion);
                    List<Rectangle> bounds = new ArrayList<>();
                    for (OverheadsOffset offset : offsets.values())
                    {
                        Rectangle next = new Rectangle(offset.getX(), offset.getY(), width, height);
                        for (Rectangle previous : bounds)
                        {
                            assertFalse(previous.intersects(next));
                        }
                        bounds.add(next);
                    }
                    assertEquals(128, bounds.size());
                }
            }
        }
    }

    @Test
    public void arrivingHitsPushOlderHitsOutwardAndBothKeepScrolling()
    {
        OverheadsScrollingHitsplats layout = new OverheadsScrollingHitsplats();
        OverheadsHitsplat older = hit(0, 1, 0, 100);
        OverheadsHitsplat newer = hit(1, 2, 20, 120);
        int before = offsets(layout, Collections.singletonList(older), 20).get(1L).getY();
        Map<Long, OverheadsOffset> withNew = offsets(layout, Arrays.asList(older, newer), 20);
        assertTrue(withNew.get(1L).getY() < before);
        assertTrue(withNew.get(2L).getY() - withNew.get(1L).getY() >= 24);
        Map<Long, OverheadsOffset> later = offsets(layout, Arrays.asList(older, newer), 30);
        assertTrue(later.get(1L).getY() < withNew.get(1L).getY());
        assertTrue(later.get(2L).getY() < withNew.get(2L).getY());
        Map<Long, OverheadsOffset> removed = offsets(layout, Collections.singletonList(older), 30);
        assertEquals(later.get(1L).getY(), removed.get(1L).getY());
    }

    @Test
    public void expirationAndReuseDoNotTransferMotionToANewHit()
    {
        OverheadsScrollingHitsplats layout = new OverheadsScrollingHitsplats();
        OverheadsHitsplat older = hit(0, 1, 0, 100);
        OverheadsHitsplat newer = hit(1, 2, 0, 100);
        offsets(layout, Arrays.asList(older, newer), 90);
        assertTrue(offsets(layout, Arrays.asList(older, newer), 100).isEmpty());
        OverheadsHitsplat replacement = hit(0, 3, 100, 200);
        assertEquals(0, offsets(layout, Collections.singletonList(replacement), 100).get(3L).getY());
        assertEquals(0f, replacement.getProgress(90), 0f);
        assertEquals(1f, replacement.getProgress(250), 0f);
    }

    @Test
    public void cameraScaleChangesSizeAndTravelTogether()
    {
        OverheadsScrollingHitsplats layout = new OverheadsScrollingHitsplats();
        List<OverheadsHitsplat> hits = Arrays.asList(hit(0, 1, 0, 100), hit(1, 2, 0, 100));
        Map<Long, OverheadsOffset> normal = layout.layout(hits, 50, 30, 20, 100, 2, 4, 4, 48, OverheadsHitsplatMotion.SCROLL_UP);
        Map<Long, OverheadsOffset> large = layout.layout(hits, 50, 60, 40, 200, 2, 4, 4, 48, OverheadsHitsplatMotion.SCROLL_UP);
        assertEquals(normal.get(1L).getY() * 2, large.get(1L).getY());
        assertEquals(normal.get(2L).getX() * 2, large.get(2L).getX());
    }

    @Test
    public void productionRendererMovesHitsAndHonorsExpiryOpacityAndOwnership()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.MODERN);
        when(f.config.hitsplatMotion()).thenReturn(OverheadsHitsplatMotion.SCROLL_UP);
        f.hits.add(new OverheadsHitsplat(HitsplatID.DAMAGE_OTHER, 42, 0, 100, 100, 100, 180, 200, 1, false));
        Rectangle before = OverheadsRenderPipelineTest.pixelBounds(f.render());
        assertNotNull(before);
        f.cycle = 130;
        Rectangle later = OverheadsRenderPipelineTest.pixelBounds(f.render());
        assertTrue(later.y < before.y - 10);
        when(f.config.opacityPercent()).thenReturn(0);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.config.opacityPercent()).thenReturn(100);
        when(f.config.onlyDisplayMine()).thenReturn(true);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
        when(f.config.onlyDisplayMine()).thenReturn(false);
        f.cycle = 200;
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
    }

    @Test
    public void scrollingZeroHitsRespectHideZeroAndDownwardMovement()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.NUMBERS);
        when(f.config.hitsplatMotion()).thenReturn(OverheadsHitsplatMotion.SCROLL_DOWN);
        f.hits.add(new OverheadsHitsplat(HitsplatID.BLOCK_ME, 0, 0, 100, 100, 100, 180, 200, 1, true));
        Rectangle before = OverheadsRenderPipelineTest.pixelBounds(f.render());
        f.cycle = 130;
        assertTrue(OverheadsRenderPipelineTest.pixelBounds(f.render()).y > before.y + 10);
        when(f.config.hideZeroHitsplats()).thenReturn(true);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(f.render()));
    }

    @Test
    public void zoomingBackDoesNotLeaveScrollingColumnsAtTheirEnlargedSpacing()
    {
        OverheadsRenderPipelineTest.Fixture f = new OverheadsRenderPipelineTest.Fixture();
        when(f.config.healthBarsEnabled()).thenReturn(false);
        when(f.config.headIconsEnabled()).thenReturn(false);
        when(f.config.avoidOverlap()).thenReturn(false);
        when(f.config.hitsplatStyle()).thenReturn(OverheadsHitsplatStyle.MODERN);
        when(f.config.hitsplatMotion()).thenReturn(OverheadsHitsplatMotion.SCROLL_UP);
        when(f.config.hitsplatScaleWithZoom()).thenReturn(true);
        f.hits.add(new OverheadsHitsplat(HitsplatID.DAMAGE_ME, 42, 0, 90, 90, 90, 200, 220, 1, true));
        f.hits.add(new OverheadsHitsplat(HitsplatID.DAMAGE_ME, 42, 1, 90, 90, 90, 200, 220, 2, true));
        Rectangle original = OverheadsRenderPipelineTest.pixelBounds(f.render());
        f.zoom = 2;
        f.render();
        f.zoom = 1;
        Rectangle restored = OverheadsRenderPipelineTest.pixelBounds(f.render());
        assertEquals(original, restored);
    }

    private static Map<Long, OverheadsOffset> offsets(OverheadsScrollingHitsplats layout, List<OverheadsHitsplat> hits, int cycle)
    {
        return layout.layout(hits, cycle, 30, 20, 100, 1, 2, 2, 48, OverheadsHitsplatMotion.SCROLL_UP);
    }

    private static OverheadsHitsplat hit(int position, long sequence, int appears, int expires)
    {
        return new OverheadsHitsplat(HitsplatID.DAMAGE_ME, 42, position, appears, appears, appears, expires - 10, expires, sequence, true);
    }
}
