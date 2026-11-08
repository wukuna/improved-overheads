package com.improvedoverheads;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsProfilesTest
{
    @Test
    public void partyMigratesFromOtherPlayersButEditsAndMembershipStayIndependent() throws Exception
    {
        Fixture f = new Fixture();
        f.put(OverheadsProfiles.Scope.PLAYERS, "healthBarOpacity", 35);
        f.put(OverheadsProfiles.Scope.PLAYERS, "healthBarDamageTrailHold", 650);
        f.profiles.initialize();
        assertEquals(35, f.profiles.config(OverheadsProfiles.Scope.PARTY).healthBarOpacity());
        assertEquals(650, f.profiles.config(OverheadsProfiles.Scope.PARTY).healthBarDamageTrailHold());
        f.put(OverheadsProfiles.Scope.PARTY, "healthBarOpacity", 81);
        f.put(OverheadsProfiles.Scope.PARTY, "healthBarDamageTrailHold", 300);
        f.profiles.initialize();
        assertEquals(81, f.profiles.config(OverheadsProfiles.Scope.PARTY).healthBarOpacity());
        assertEquals(35, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).healthBarOpacity());
        assertEquals(650, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).healthBarDamageTrailHold());
        OverheadsPartyTest.Fixture shared = new OverheadsPartyTest.Fixture(false);
        java.lang.reflect.Field field = OverheadsProfiles.class.getDeclaredField("party");
        field.setAccessible(true);
        field.set(f.profiles, shared.state);
        assertEquals(OverheadsProfiles.Scope.PARTY, f.profiles.scope(shared.player));
        assertEquals(OverheadsProfiles.Scope.SELF, f.profiles.scope(f.player));
        assertEquals(OverheadsProfiles.Scope.NPCS, f.profiles.scope(mock(NPC.class)));
        when(shared.party.isInParty()).thenReturn(false);
        assertEquals(OverheadsProfiles.Scope.PLAYERS, f.profiles.scope(shared.player));
        verifyNoInteractions(shared.data);
    }

    @Test
    public void olderScopedSetupsCopyPartyPreferencesWithoutChangingSharingConsent()
    {
        Fixture f = new Fixture();
        f.profiles.initialize();
        OverheadsSetups setups = f.setups();
        JsonObject root = new Gson().fromJson(setups.exportCurrent("Legacy", setups.session()), JsonObject.class);
        JsonObject settings = new JsonObject();
        settings.addProperty("players.healthBarOpacity", 45);
        root.add("settings", settings);
        setups.apply(setups.preview(root.toString(), setups.session()));
        assertEquals(45, f.profiles.config(OverheadsProfiles.Scope.PARTY).healthBarOpacity());
        assertEquals(OverheadsBarDisplay.HIDDEN, f.self.partyDisplay());
        f.put(OverheadsProfiles.Scope.PARTY, "healthBarOpacity", 72);
        setups.apply(setups.preview(setups.copyGroup(3, 2, setups.session()), setups.session()));
        assertEquals(72, f.profiles.config(OverheadsProfiles.Scope.NPCS).healthBarOpacity());
        assertEquals(45, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).healthBarOpacity());
    }

    @Test
    public void themePresetAndGroupCopyTargetTheSelectedActorOnly()
    {
        Fixture f = new Fixture();
        f.profiles.initialize();
        OverheadsSetups setups = f.setups();
        setups.setThemeScope(OverheadsProfiles.Scope.NPCS);
        String theme = setups.themePreset(OverheadsHitsplatStyle.MODERN, OverheadsHitsplatMotion.SCROLL_UP, setups.session());
        setups.apply(setups.preview(theme, setups.session(), true));
        assertEquals(OverheadsHitsplatStyle.MODERN, f.profiles.config(OverheadsProfiles.Scope.NPCS).hitsplatStyle());
        assertEquals(OverheadsHitsplatStyle.OSRS, f.self.hitsplatStyle());
        f.put(OverheadsProfiles.Scope.NPCS, "healthBarOpacity", 32);
        f.put(OverheadsProfiles.Scope.NPCS, "npcHealthDisplay", OverheadsBarDisplay.NUMBERS_ONLY);
        setups.apply(setups.preview(setups.copyGroup(2, 1, setups.session()), setups.session()));
        assertEquals(32, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).healthBarOpacity());
        assertEquals(OverheadsBarDisplay.NUMBERS_ONLY, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).otherHealthDisplay());
        assertNotEquals(32, f.self.healthBarOpacity());
        assertEquals(OverheadsBarDisplay.HIDDEN, f.self.partyDisplay());
    }

    @Test
    public void overlayUsesOtherPlayerChatSettingsInsteadOfMyPlayerSettings()
    {
        Fixture f = new Fixture();
        f.profiles.initialize();
        OverheadsRenderPipelineTest.Fixture render = new OverheadsRenderPipelineTest.Fixture();
        when(render.client.getLocalPlayer()).thenReturn(mock(Player.class));
        OverheadsProfiles profiles = new OverheadsProfiles(render.client, f.manager, f.self);
        render.overlay.configureProfiles(profiles);
        render.health.configureProfiles(profiles);
        render.icons.configureProfiles(profiles);
        f.put(OverheadsProfiles.Scope.PLAYERS, "healthBarsEnabled", false);
        f.put(OverheadsProfiles.Scope.PLAYERS, "headIconsEnabled", false);
        f.put(OverheadsProfiles.Scope.PLAYERS, "overheadChatEnabled", false);
        when(render.player.getOverheadText()).thenReturn("Other player message");
        when(render.player.getOverheadCycle()).thenReturn(150);
        assertNull(OverheadsRenderPipelineTest.pixelBounds(render.render()));
        f.put(OverheadsProfiles.Scope.PLAYERS, "overheadChatEnabled", true);
        f.put(OverheadsProfiles.Scope.PLAYERS, "overheadChatColor", Color.RED);
        BufferedImage image = render.render();
        assertNotNull(OverheadsRenderPipelineTest.pixelBounds(image));
        boolean red = false;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
        { if (image.getRGB(x, y) == Color.RED.getRGB()) red = true; }
        assertTrue(red);
    }

    @Test
    public void migrationPreservesExistingChoicesAndRunsOncePerProfile()
    {
        Fixture f = new Fixture();
        f.put(OverheadsProfiles.Scope.SELF, "healthBarOpacity", 37);
        f.put(OverheadsProfiles.Scope.SELF, "lowResourceColor", Color.ORANGE);
        f.put(OverheadsProfiles.Scope.SELF, "partyDisplay", OverheadsBarDisplay.BOTH);
        f.put(OverheadsProfiles.Scope.PLAYERS, "healthBarOpacity", 82);
        f.put(OverheadsProfiles.Scope.PLAYERS, "runDisplay", OverheadsBarDisplay.NUMBERS_ONLY);
        f.profiles.initialize();
        assertEquals(37, f.profiles.config(OverheadsProfiles.Scope.NPCS).healthBarOpacity());
        assertEquals(82, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).healthBarOpacity());
        assertEquals(OverheadsBarDisplay.NUMBERS_ONLY, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).runDisplay());
        assertEquals(Color.ORANGE, f.self.prayerLowColor());
        assertNull(f.get(OverheadsProfiles.Scope.PLAYERS, "partyDisplay"));
        f.put(OverheadsProfiles.Scope.SELF, "healthBarOpacity", 5);
        f.profiles.initialize();
        assertEquals(37, f.profiles.config(OverheadsProfiles.Scope.NPCS).healthBarOpacity());
        f.values.clear();
        f.put(OverheadsProfiles.Scope.SELF, "healthBarOpacity", 63);
        f.profiles.initialize();
        assertEquals(63, f.profiles.config(OverheadsProfiles.Scope.NPCS).healthBarOpacity());
    }

    @Test
    public void universalIsExplicitAndOnlyAppliesTheChosenComponent()
    {
        Fixture f = new Fixture();
        f.profiles.initialize();
        f.put(OverheadsProfiles.Scope.UNIVERSAL, "healthBarOpacity", 12);
        f.put(OverheadsProfiles.Scope.UNIVERSAL, "selfHealthDisplay", OverheadsBarDisplay.NUMBERS_ONLY);
        f.put(OverheadsProfiles.Scope.UNIVERSAL, "overheadChatColor", Color.RED);
        f.put(OverheadsProfiles.Scope.UNIVERSAL, "prayerOpacity", 7);
        assertNotEquals(12, f.self.healthBarOpacity());
        f.profiles.applyUniversal(OverheadsComponent.HEALTH);
        for (OverheadsProfiles.Scope scope : new OverheadsProfiles.Scope[]{OverheadsProfiles.Scope.SELF, OverheadsProfiles.Scope.PLAYERS, OverheadsProfiles.Scope.NPCS, OverheadsProfiles.Scope.PARTY})
        {
            assertEquals(12, f.profiles.config(scope).healthBarOpacity());
            assertNotEquals(Color.RED, f.profiles.config(scope).overheadChatColor());
            assertNotEquals(7, f.profiles.config(scope).prayerOpacity());
        }
        assertEquals(OverheadsBarDisplay.NUMBERS_ONLY, f.profiles.config(OverheadsProfiles.Scope.NPCS).npcHealthDisplay());
        assertEquals(OverheadsBarDisplay.NUMBERS_ONLY, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).otherHealthDisplay());
        f.put(OverheadsProfiles.Scope.SELF, "healthBarOpacity", 90);
        assertEquals(12, f.profiles.config(OverheadsProfiles.Scope.PLAYERS).healthBarOpacity());
    }

    @Test
    public void setupRoundTripUndoAndLegacyImportsKeepActorScopesAndConsent()
    {
        Fixture from = new Fixture();
        from.profiles.initialize();
        from.put(OverheadsProfiles.Scope.SELF, "healthBarOpacity", 21);
        from.put(OverheadsProfiles.Scope.PLAYERS, "healthBarOpacity", 41);
        from.put(OverheadsProfiles.Scope.NPCS, "healthBarOpacity", 61);
        from.put(OverheadsProfiles.Scope.PARTY, "healthBarOpacity", 81);
        from.put(OverheadsProfiles.Scope.UNIVERSAL, "overheadChatColor", Color.BLUE);
        from.put(OverheadsProfiles.Scope.SELF, "partyDisplay", OverheadsBarDisplay.BOTH);
        OverheadsSetups exporter = from.setups();
        String json = exporter.exportCurrent("Independent", exporter.session());
        assertFalse(json.contains("partyDisplay"));
        Fixture to = new Fixture();
        to.profiles.initialize();
        OverheadsSetups importer = to.setups();
        JsonObject before = importer.schema.snapshot();
        OverheadsSetups.Preview preview = importer.preview(json, importer.session());
        importer.apply(preview);
        assertEquals(exporter.schema.snapshot(), importer.schema.snapshot());
        assertEquals(OverheadsBarDisplay.HIDDEN, to.self.partyDisplay());
        importer.apply(importer.reverse(preview));
        assertEquals(before, importer.schema.snapshot());
        JsonObject legacy = new Gson().fromJson(json, JsonObject.class);
        legacy.addProperty("version", 1);
        JsonObject settings = new JsonObject();
        settings.addProperty("healthBarOpacity", 29);
        settings.addProperty("partyDisplay", "BOTH");
        legacy.add("settings", settings);
        importer.apply(importer.preview(legacy.toString(), importer.session()));
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        { assertEquals(29, to.profiles.config(scope).healthBarOpacity()); }
        assertEquals(OverheadsBarDisplay.HIDDEN, to.self.partyDisplay());
    }

    @Test
    public void healthRenderingDoesNotBleedOpacityBetweenActors()
    {
        Fixture f = new Fixture();
        f.profiles.initialize();
        Player other = mock(Player.class);
        NPC npc = mock(NPC.class);
        for (Actor actor : new Actor[]{f.player, other, npc})
        {
            when(actor.getHealthRatio()).thenReturn(15);
            when(actor.getHealthScale()).thenReturn(30);
            when(actor.getCanvasImageLocation(any(), anyInt())).thenReturn(new Point(120, 160));
        }
        when(f.client.getRealSkillLevel(Skill.HITPOINTS)).thenReturn(99);
        when(f.client.getBoostedSkillLevel(Skill.HITPOINTS)).thenReturn(50);
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        {
            f.put(scope, "healthBarStyle", OverheadsHealthBarStyle.SOLID);
            f.put(scope, "selfHealthDisplay", OverheadsBarDisplay.BAR_ONLY);
            f.put(scope, "otherHealthDisplay", OverheadsBarDisplay.BAR_ONLY);
            f.put(scope, "npcHealthDisplay", OverheadsBarDisplay.BAR_ONLY);
            f.put(scope, "prayerDisplay", OverheadsBarDisplay.HIDDEN);
            f.put(scope, "healthBarOpacity", 100);
        }
        f.put(OverheadsProfiles.Scope.SELF, "healthBarOpacity", 0);
        OverheadsHealthBarRenderer renderer = new OverheadsHealthBarRenderer(f.client, f.self);
        renderer.configureProfiles(f.profiles);
        assertEquals(0, pixels(renderer, f.player));
        assertTrue(pixels(renderer, other) > 0);
        assertTrue(pixels(renderer, npc) > 0);
        f.put(OverheadsProfiles.Scope.PLAYERS, "healthBarOpacity", 0);
        assertEquals(0, pixels(renderer, other));
        assertTrue(pixels(renderer, npc) > 0);
        f.put(OverheadsProfiles.Scope.SELF, "selfHealthDisplay", OverheadsBarDisplay.NUMBERS_ONLY);
        assertTrue(pixels(renderer, f.player) > 0);
    }

    private static int pixels(OverheadsHealthBarRenderer renderer, Actor actor)
    {
        BufferedImage image = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        renderer.render(graphics, actor, new Point(120, 120), 120, null);
        graphics.dispose();
        int count = 0;
        for (int y = 0; y < 240; y++) for (int x = 0; x < 240; x++) if ((image.getRGB(x, y) >>> 24) != 0) count++;
        return count;
    }

    static final class Fixture
    {
        final Map<String, Object> values = new HashMap<>();
        final ConfigManager manager = mock(ConfigManager.class);
        final Client client = mock(Client.class);
        final Player player = mock(Player.class);
        final OverheadsConfig self = config(OverheadsConfig.class, OverheadsProfiles.Scope.SELF);
        final OverheadsProfiles profiles;

        Fixture()
        {
            when(client.getLocalPlayer()).thenReturn(player);
            when(manager.getConfig(OverheadsProfiles.PlayersConfig.class)).thenReturn(config(OverheadsProfiles.PlayersConfig.class, OverheadsProfiles.Scope.PLAYERS));
            when(manager.getConfig(OverheadsProfiles.PartyConfig.class)).thenReturn(config(OverheadsProfiles.PartyConfig.class, OverheadsProfiles.Scope.PARTY));
            when(manager.getConfig(OverheadsProfiles.NpcsConfig.class)).thenReturn(config(OverheadsProfiles.NpcsConfig.class, OverheadsProfiles.Scope.NPCS));
            when(manager.getConfig(OverheadsProfiles.UniversalConfig.class)).thenReturn(config(OverheadsProfiles.UniversalConfig.class, OverheadsProfiles.Scope.UNIVERSAL));
            when(manager.getConfiguration(anyString(), anyString())).thenAnswer(call ->
            {
                Object value = values.get(call.getArgument(0) + "." + call.getArgument(1));
                return value == null ? null : value.toString();
            });
            doAnswer(call -> { values.put(call.getArgument(0) + "." + call.getArgument(1), call.getArgument(2)); return null; })
                .when(manager).setConfiguration(anyString(), anyString(), any(Object.class));
            doAnswer(call -> { values.put(call.getArgument(0) + "." + call.getArgument(1), call.getArgument(2)); return null; })
                .when(manager).setConfiguration(anyString(), anyString(), anyString());
            profiles = new OverheadsProfiles(client, manager, self);
        }

        <T extends OverheadsConfig> T config(Class<T> type, OverheadsProfiles.Scope scope)
        {
            return mock(type, call ->
            {
                ConfigItem item = call.getMethod().getAnnotation(ConfigItem.class);
                Object value = item == null ? null : values.get(scope.group + "." + item.keyName());
                return value == null ? call.callRealMethod() : value;
            });
        }

        void put(OverheadsProfiles.Scope scope, String key, Object value) { values.put(scope.group + "." + key, value); }
        Object get(OverheadsProfiles.Scope scope, String key) { return values.get(scope.group + "." + key); }
        OverheadsSetups setups()
        {
            OverheadsSetups setups = new OverheadsSetups(self, manager, new Gson());
            setups.configureProfiles(profiles);
            return setups;
        }
    }
}
