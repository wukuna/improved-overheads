package com.improvedoverheads;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.config.FontType;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsSetupsTest
{
    @Test
    public void everyAppearanceSettingIncludingDefaultsCanRoundTripAcrossProfiles()
    {
        Fixture from = new Fixture();
        from.values.put("healthTextColor", new Color(40, 50, 60, 70));
        from.values.put("healthTextFont", new FontType().withFamily("Dialog").withSize(27).withBold(true).withItalic(true));
        from.values.put("healthOrientation", OverheadsOrientation.VERTICAL);
        from.values.put("healthBarBorderThickness", 2.5);
        from.values.put("nameXOffset", -90);
        from.values.put("xSpacing", 7);
        String json = from.setups.exportCurrent("Across accounts", from.session);
        Fixture to = new Fixture();
        to.values.put("hideZeroHitsplats", true);
        OverheadsSetups.Preview preview = to.setups.preview(json, to.session);
        assertTrue(preview.changes.size() >= 6);
        assertEquals(preview.changes.size(), to.setups.apply(preview));
        assertEquals(from.setups.schema.snapshot(), to.setups.schema.snapshot());
        assertEquals(new Color(40, 50, 60, 70), to.values.get("healthTextColor"));
        assertFalse(to.raw.containsKey(OverheadsSetups.STORAGE_KEY));
        for (java.lang.reflect.Method method : OverheadsConfig.class.getDeclaredMethods())
        {
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            if (item != null && !item.keyName().equals("hideSidePanel"))
            {
                assertTrue(item.keyName(), from.setups.schema.entries.containsKey(item.keyName()));
            }
        }
    }

    @Test
    public void unknownFieldsNeverBecomeConfigurationWrites()
    {
        Fixture f = new Fixture();
        JsonObject json = f.document();
        JsonObject settings = json.getAsJsonObject("settings");
        settings.addProperty("nameXOffset", 33);
        settings.addProperty("runelite.plugin.enabled", true);
        settings.addProperty("savedSetups", "bad");
        settings.addProperty("futureOption", true);
        json.addProperty("futureMetadata", "ignored");
        OverheadsSetups.Preview preview = f.setups.preview(json.toString(), f.session);
        assertEquals(3, preview.ignored);
        assertEquals(1, f.setups.apply(preview));
        assertEquals(33, f.values.get("nameXOffset"));
        assertEquals(1, f.raw.size());
    }

    @Test
    public void invalidKnownSettingRejectsTheWholePreviewBeforeWritingAnything()
    {
        Fixture f = new Fixture();
        for (String invalid : new String[]{"\"true\"", "null", "1", "{}"})
        {
            JsonObject document = f.document();
            document.getAsJsonObject("settings").addProperty("nameXOffset", 33);
            document.getAsJsonObject("settings").add("avoidOverlap", new Gson().fromJson(invalid, com.google.gson.JsonElement.class));
            assertThrows(IllegalArgumentException.class, () -> f.setups.preview(document.toString(), f.session));
        }
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("componentGap", 999);
        assertThrows(IllegalArgumentException.class, () -> f.setups.preview(document.toString(), f.session));
        document.getAsJsonObject("settings").addProperty("componentGap", 2.5);
        assertThrows(IllegalArgumentException.class, () -> f.setups.preview(document.toString(), f.session));
        assertTrue(f.raw.isEmpty());
    }

    @Test
    public void stalePreviewRejectsConflictingChangesButAllowsUnrelatedOnes()
    {
        Fixture f = new Fixture();
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("nameXOffset", 33);
        OverheadsSetups.Preview preview = f.setups.preview(document.toString(), f.session);
        f.values.put("runOpacity", 15);
        assertEquals(1, f.setups.apply(preview));
        assertEquals(15, f.values.get("runOpacity"));
        f.raw.clear();
        f.values.put("nameXOffset", 44);
        assertThrows(IllegalStateException.class, () -> f.setups.apply(preview));
        assertTrue(f.raw.isEmpty());
    }

    @Test
    public void profileOrLifecycleChangeInvalidatesSavedSetupActionsAndPreviews()
    {
        Fixture f = new Fixture();
        JsonObject document = f.document();
        document.getAsJsonObject("settings").addProperty("componentGap", 8);
        OverheadsSetups.Preview preview = f.setups.preview(document.toString(), f.session);
        when(f.manager.getProfile()).thenReturn(mock(ConfigProfile.class));
        assertThrows(IllegalStateException.class, () -> f.setups.apply(preview));
        assertThrows(IllegalStateException.class, () -> f.setups.saveCurrent("stale", false, f.session));
        f.session = f.setups.session();
        f.setups.invalidate();
        assertThrows(IllegalStateException.class, () -> f.setups.exportCurrent("stale", f.session));
        assertTrue(f.raw.isEmpty());
    }

    @Test
    public void savesAreNamedBoundedAndRequireExplicitReplacement()
    {
        Fixture f = new Fixture();
        f.setups.saveCurrent("Bossing", false, f.session);
        assertEquals(1, f.setups.saved(f.session).size());
        f.values.put("componentGap", 8);
        assertThrows(IllegalArgumentException.class, () -> f.setups.saveCurrent("Bossing", false, f.session));
        f.setups.saveCurrent("Bossing", true, f.session);
        f.values.put("componentGap", 0);
        OverheadsSetups.Preview saved = f.setups.preview(f.setups.saved(f.session).get("Bossing"), f.session);
        assertEquals(1, f.setups.apply(saved));
        assertEquals(8, f.values.get("componentGap"));
        for (int i = 0; i < 11; i++)
        {
            f.setups.saveCurrent("Setup " + i, false, f.session);
        }
        assertThrows(IllegalArgumentException.class, () -> f.setups.saveCurrent("Too many", false, f.session));
        f.setups.delete("Bossing", f.session);
        assertEquals(11, f.setups.saved(f.session).size());
    }

    @Test
    public void copyingGroupOptionsLeavesSharedAppearanceAndOtherGroupsAlone()
    {
        Fixture f = new Fixture();
        f.values.put("selfHealthDisplay", OverheadsBarDisplay.NUMBERS_ONLY);
        f.values.put("selfNameVisibility", OverheadsVisibility.ALL);
        f.values.put("disableMyHitsplats", true);
        JsonObject before = f.setups.schema.snapshot();
        OverheadsSetups.Preview preview = f.setups.preview(f.setups.copyGroup(0, 2, f.session), f.session);
        assertEquals(3, f.setups.apply(preview));
        assertEquals(OverheadsBarDisplay.NUMBERS_ONLY, f.values.get("npcHealthDisplay"));
        assertEquals(before.get("otherHealthDisplay"), f.setups.schema.snapshot().get("otherHealthDisplay"));
        assertEquals(before.get("healthBarFrontColor"), f.setups.schema.snapshot().get("healthBarFrontColor"));
    }

    @Test
    public void malformedDuplicateOversizedAndUnsupportedJsonIsRejected()
    {
        Fixture f = new Fixture();
        for (String json : new String[]{"{} {}", "{bad:true}", "{\"a\":1,\"a\":2}", "{\"a\":NaN}", "{\"a\":1,}", "[]",
            "{\"a\":" + "[".repeat(15) + "1" + "]".repeat(15) + "}", " ".repeat(OverheadsSetups.MAX_JSON + 1)})
        {
            assertThrows(json.substring(0, Math.min(40, json.length())), IllegalArgumentException.class,
                () -> f.setups.preview(json, f.session));
        }
        JsonObject document = f.document();
        document.addProperty("version", 3);
        assertThrows(IllegalArgumentException.class, () -> f.setups.preview(document.toString(), f.session));
        assertTrue(f.raw.isEmpty());
    }

    static final class Fixture
    {
        final Map<String, Object> values = new HashMap<>();
        final Map<String, String> raw = new HashMap<>();
        final Map<String, String> methods = new HashMap<>();
        final ConfigManager manager = mock(ConfigManager.class);
        final OverheadsConfig config = mock(OverheadsConfig.class, call ->
            values.containsKey(call.getMethod().getName()) ? values.get(call.getMethod().getName()) : call.callRealMethod());
        final OverheadsSetups setups = new OverheadsSetups(config, manager, new Gson());
        OverheadsSetups.Session session;

        Fixture()
        {
            for (java.lang.reflect.Method method : OverheadsConfig.class.getDeclaredMethods())
            {
                ConfigItem item = method.getAnnotation(ConfigItem.class);
                if (item != null)
                {
                    methods.put(item.keyName(), method.getName());
                }
            }
            ConfigProfile profile = mock(ConfigProfile.class);
            when(profile.getId()).thenReturn(1L);
            when(manager.getProfile()).thenReturn(profile);
            when(manager.getConfiguration(eq(OverheadsConfig.GROUP), anyString())).thenAnswer(call -> raw.get(call.getArgument(1)));
            doAnswer(call ->
            {
                assertEquals(OverheadsConfig.GROUP, call.getArgument(0));
                String key = call.getArgument(1);
                Object value = call.getArgument(2);
                values.put(methods.getOrDefault(key, key), value);
                raw.put(key, String.valueOf(value));
                return null;
            }).when(manager).setConfiguration(anyString(), anyString(), any(Object.class));
            doAnswer(call ->
            {
                assertEquals(OverheadsConfig.GROUP, call.getArgument(0));
                raw.put(call.getArgument(1), call.getArgument(2));
                return null;
            }).when(manager).setConfiguration(anyString(), anyString(), anyString());
            session = setups.session();
        }

        JsonObject document()
        {
            return OverheadsSetups.readObject(setups.exportCurrent("Test", session), OverheadsSetups.MAX_JSON);
        }
    }
}
