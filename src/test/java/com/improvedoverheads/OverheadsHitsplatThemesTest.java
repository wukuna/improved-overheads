package com.improvedoverheads;

import com.google.gson.JsonObject;
import java.awt.Color;
import net.runelite.client.config.ConfigProfile;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsHitsplatThemesTest
{
    @Test
    public void themesTransferAppearanceWithoutTouchingFiltersOrOtherComponents()
    {
        OverheadsSetupsTest.Fixture from = new OverheadsSetupsTest.Fixture();
        from.values.put("hitsplatStyle", OverheadsHitsplatStyle.MODERN);
        from.values.put("hitsplatPoisonColor", new Color(1, 2, 3, 40));
        from.values.put("hitsplatMotion", OverheadsHitsplatMotion.SCROLL_DOWN);
        from.values.put("hitsplatBorderThickness", 4);
        from.values.put("hitsplatXOffset", 80);
        from.values.put("hideZeroHitsplats", true);
        from.values.put("nameXOffset", 88);
        String json = from.setups.exportCurrent("Green", from.session, true);
        JsonObject settings = OverheadsSetups.readObject(json, OverheadsSetups.MAX_JSON).getAsJsonObject("settings");
        assertFalse(settings.has("hideZeroHitsplats"));
        assertFalse(settings.has("nameXOffset"));
        assertFalse(settings.has("maxHitsplats"));
        assertFalse(settings.has("savedHitsplatThemes"));
        OverheadsSetupsTest.Fixture to = new OverheadsSetupsTest.Fixture();
        to.values.put("onlyDisplayMine", true);
        assertEquals(5, to.setups.apply(to.setups.preview(json, to.session, true)));
        assertEquals(from.config.hitsplatPoisonColor(), to.config.hitsplatPoisonColor());
        assertEquals(OverheadsHitsplatMotion.SCROLL_DOWN, to.config.hitsplatMotion());
        assertTrue(to.config.onlyDisplayMine());
        assertFalse(to.config.hideZeroHitsplats());
        assertEquals(0, to.config.nameXOffset());
    }

    @Test
    public void themeImportCannotApplyUnrelatedKeysEvenFromFullSetup()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("nameXOffset", 55);
        document.getAsJsonObject("settings").addProperty("disableMyHitsplats", true);
        document.getAsJsonObject("settings").addProperty("hitsplatScrollColumns", 4);
        OverheadsSetups.Preview preview = f.setups.preview(document.toString(), f.session, true);
        assertEquals(1, preview.changes.size());
        assertTrue(preview.ignored > 100);
        assertEquals(1, f.setups.apply(preview));
        assertFalse(f.config.disableMyHitsplats());
        assertEquals(0, f.config.nameXOffset());
    }

    @Test
    public void presetsResetAppearanceAndKeepSavedLibrariesSeparate()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        f.values.put("hitsplatBorderThickness", 5);
        f.values.put("onlyDisplayMine", true);
        f.setups.saveCurrent("Custom", false, f.session, true);
        f.setups.saveCurrent("Full setup", false, f.session);
        String preset = f.setups.themePreset(OverheadsHitsplatStyle.NUMBERS, OverheadsHitsplatMotion.SCROLL_UP, f.session);
        f.setups.apply(f.setups.preview(preset, f.session, true));
        assertEquals(1, f.config.hitsplatBorderThickness());
        assertEquals(OverheadsHitsplatStyle.NUMBERS, f.config.hitsplatStyle());
        assertEquals(OverheadsHitsplatMotion.SCROLL_UP, f.config.hitsplatMotion());
        assertTrue(f.config.onlyDisplayMine());
        assertEquals(1, f.setups.saved(f.session, true).size());
        assertThrows(IllegalArgumentException.class, () -> f.setups.saveCurrent("Custom", false, f.session, true));
        f.setups.saveCurrent("Custom", true, f.session, true);
        for (int i = 0; i < 11; i++)
        {
            f.setups.saveCurrent("Theme " + i, false, f.session, true);
        }
        assertThrows(IllegalArgumentException.class, () -> f.setups.saveCurrent("Overflow", false, f.session, true));
        f.setups.delete("Custom", f.session, true);
        assertEquals(11, f.setups.saved(f.session, true).size());
        assertEquals(1, f.setups.saved(f.session).size());
    }

    @Test
    public void invalidThemeValuesRejectBeforeWritesAndStaleThemeRejectsApply()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("hitsplatBorderThickness", 6);
        assertThrows(IllegalArgumentException.class, () -> f.setups.preview(document.toString(), f.session, true));
        assertTrue(f.raw.isEmpty());
        document.getAsJsonObject("settings").addProperty("hitsplatBorderThickness", 4);
        OverheadsSetups.Preview preview = f.setups.preview(document.toString(), f.session, true);
        f.setups.invalidate();
        assertThrows(IllegalStateException.class, () -> f.setups.apply(preview));
        assertTrue(f.raw.isEmpty());
    }

    @Test
    public void equivalentProfileObjectsDoNotInvalidateThemeEdits()
    {
        OverheadsSetupsTest.Fixture f = new OverheadsSetupsTest.Fixture();
        String preset = f.setups.themePreset(OverheadsHitsplatStyle.MODERN, OverheadsHitsplatMotion.FIXED, f.session);
        OverheadsSetups.Preview preview = f.setups.preview(preset, f.session, true);
        ConfigProfile refreshed = mock(ConfigProfile.class);
        when(refreshed.getId()).thenReturn(1L);
        when(f.manager.getProfile()).thenReturn(refreshed);
        assertEquals(1, f.setups.apply(preview));
        f.setups.saveCurrent("After refresh", false, f.session, true);
        when(refreshed.getId()).thenReturn(2L);
        assertThrows(IllegalStateException.class, () -> f.setups.saveCurrent("Stale", false, f.session, true));
    }
}
