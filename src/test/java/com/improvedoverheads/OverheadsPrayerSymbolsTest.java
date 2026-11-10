package com.improvedoverheads;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.HeadIcon;
import net.runelite.api.SpritePixels;
import net.runelite.api.gameval.SpriteID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPrayerSymbolsTest
{
    @Test public void symbolsOnlySuppressesBothRowsAndCornerBackingsWithoutChangingGlyphs()
    {
        for (OverheadsPrayerLayout layout : OverheadsPrayerLayout.values())
        {
            OverheadsRenderPipelineTest.Fixture f = OverheadsPrayerBadgesTest.fixture();
            when(f.config.prayerIconLayout()).thenReturn(layout);
            when(f.config.prayerIconShape()).thenReturn(OverheadsIconShape.ROUND);
            when(f.config.prayerIconRow2Shape()).thenReturn(OverheadsIconShape.SQUARE);
            when(f.config.prayerIconBackground()).thenReturn(Color.MAGENTA);
            when(f.config.prayerIconRow2Background()).thenReturn(Color.MAGENTA);
            when(f.config.prayerIconBadgeBackgroundColor()).thenReturn(Color.MAGENTA);
            when(f.config.prayerIconBorder()).thenReturn(Color.GREEN);
            when(f.config.prayerIconRow2Border()).thenReturn(Color.GREEN);
            when(f.config.prayerIconBadgeBorderColor()).thenReturn(Color.GREEN);
            BufferedImage expected = OverheadsPrayerBadgesTest.render(f);
            when(f.config.showPrayerIconBackground()).thenReturn(true);
            when(f.config.prayerIconRow2BackgroundEnabled()).thenReturn(true);
            when(f.config.prayerIconBadgeBackground()).thenReturn(true);
            f.icons.clearCache();
            BufferedImage withBackings = OverheadsPrayerBadgesTest.render(f);
            assertNotNull(OverheadsPrayerRowsTest.pixels(withBackings, Color.MAGENTA));
            when(f.config.prayerIconSymbolsOnly()).thenReturn(true);
            f.icons.clearCache();
            assertImageEquals(expected, OverheadsPrayerBadgesTest.render(f));
            when(f.player.getOverheadIcon()).thenReturn(null);
            BufferedImage noOverhead = OverheadsPrayerBadgesTest.render(f);
            assertNotNull(OverheadsPrayerRowsTest.pixels(noOverhead, Color.BLUE));
            assertNull(OverheadsPrayerRowsTest.pixels(noOverhead, Color.MAGENTA));
            when(f.player.getOverheadIcon()).thenReturn(HeadIcon.MELEE);
            assertImageEquals(expected, OverheadsPrayerBadgesTest.render(f));
            when(f.config.prayerIconSymbolsOnly()).thenReturn(false);
            f.icons.clearCache();
            assertImageEquals(withBackings, OverheadsPrayerBadgesTest.render(f));
            assertTrue(f.config.showPrayerIconBackground());
            assertTrue(f.config.prayerIconRow2BackgroundEnabled());
            assertTrue(f.config.prayerIconBadgeBackground());
        }
    }

    @Test public void resourcePackArtworkKeepsAllColorsAndRefreshesWhileSymbolsOnlyIsEnabled()
    {
        int spriteId = SpriteID.Prayeron.PROTECT_FROM_MELEE;
        Map<Integer, SpritePixels> overrides = new HashMap<>();
        BufferedImage original = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
        original.setRGB(10, 10, 0xFFB5A16D);
        original.setRGB(11, 10, 0xFF7449A7);
        SpritePixels first = mock(SpritePixels.class);
        when(first.toBufferedImage()).thenReturn(original);
        overrides.put(spriteId, first);
        OverheadsActorUiSpriteCache cache = new OverheadsActorUiSpriteCache(() -> 0,
            id -> { fail("Symbols-only resource packs do not load a native backing"); return null; }, () -> overrides);
        OverheadsSprite result = cache.getPrayerIcon(0, spriteId, true, true);
        assertImageEquals(original, result.getImage());
        assertSame(result, cache.getPrayerIcon(0, spriteId, true, true));
        SpritePixels replacement = mock(SpritePixels.class);
        BufferedImage next = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        next.setRGB(20, 20, Color.CYAN.getRGB());
        when(replacement.toBufferedImage()).thenReturn(next);
        overrides.put(spriteId, replacement);
        assertImageEquals(next, cache.getPrayerIcon(0, spriteId, true, true).getImage());
    }

    @Test public void unknownNativeArtworkAndUnsupportedSizesRemainUntouched()
    {
        for (int size : new int[]{20, 25, 30})
        {
            BufferedImage artwork = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            artwork.setRGB(10, 10, 0xFFB5A16D);
            artwork.setRGB(11, 10, Color.WHITE.getRGB());
            OverheadsSprite source = new OverheadsSprite(artwork, 2, 3);
            for (int index : new int[]{-1, 0, 5, 21, 22, 23, Integer.MAX_VALUE})
                assertSame(source, OverheadsNativePrayerSymbols.removeBacking(index, source));
            assertEquals(0xFFB5A16D, artwork.getRGB(10, 10));
        }
        assertNull(OverheadsNativePrayerSymbols.removeBacking(0, null));
    }

    @Test public void setupSharingPreservesIndependentActorChoicesAndSavedBackgrounds()
    {
        OverheadsProfilesTest.Fixture from = new OverheadsProfilesTest.Fixture();
        from.profiles.initialize();
        assertFalse(from.self.prayerIconSymbolsOnly());
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconSymbolsOnly", true);
        from.put(OverheadsProfiles.Scope.NPCS, "prayerIconSymbolsOnly", true);
        from.put(OverheadsProfiles.Scope.SELF, "prayerIconBadgeBackground", true);
        OverheadsSetups exporter = from.setups();
        OverheadsProfilesTest.Fixture to = new OverheadsProfilesTest.Fixture();
        to.profiles.initialize();
        OverheadsSetups importer = to.setups();
        importer.apply(importer.preview(exporter.exportCurrent("Symbols", exporter.session()), importer.session()));
        assertEquals(exporter.schema.snapshot(), importer.schema.snapshot());
        assertTrue(to.self.prayerIconSymbolsOnly());
        assertTrue(to.profiles.config(OverheadsProfiles.Scope.NPCS).prayerIconSymbolsOnly());
        assertFalse(to.profiles.config(OverheadsProfiles.Scope.PLAYERS).prayerIconSymbolsOnly());
        assertFalse(to.profiles.config(OverheadsProfiles.Scope.PARTY).prayerIconSymbolsOnly());
        assertTrue(to.self.prayerIconBadgeBackground());
        to.put(OverheadsProfiles.Scope.SELF, "prayerIconSymbolsOnly", false);
        assertFalse(to.self.prayerIconSymbolsOnly());
        assertTrue(to.self.prayerIconBadgeBackground());
    }

    private static void assertImageEquals(BufferedImage expected, BufferedImage actual)
    {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertArrayEquals(expected.getRGB(0, 0, expected.getWidth(), expected.getHeight(), null, 0, expected.getWidth()),
            actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0, actual.getWidth()));
    }
}
