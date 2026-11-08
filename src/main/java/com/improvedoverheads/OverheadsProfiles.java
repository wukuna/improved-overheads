package com.improvedoverheads;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.config.ConfigManager;

@Singleton
public final class OverheadsProfiles
{
    enum Scope
    {
        SELF("My player", OverheadsConfig.GROUP),
        PLAYERS("Other players", "improvedoverheadsplayers"),
        NPCS("NPCs", "improvedoverheadsnpcs"),
        PARTY("Party", "improvedoverheadsparty");

        final String label;
        final String group;
        Scope(String label, String group) { this.label = label; this.group = group; }
        @Override public String toString() { return label; }
    }


    @Inject private OverheadsPartyState party;
    private final Client client;
    private final ConfigManager manager;
    private final Map<Scope, OverheadsConfig> configs = new EnumMap<>(Scope.class);
    private final Map<Scope, OverheadsSettings> settings = new EnumMap<>(Scope.class);

    @Inject
    OverheadsProfiles(Client client, ConfigManager manager, OverheadsConfig self)
    {
        this.client = client;
        this.manager = manager;
        configs.put(Scope.SELF, self);
        configs.put(Scope.PLAYERS, new OverheadsScopedConfig(manager, Scope.PLAYERS.group));
        configs.put(Scope.NPCS, new OverheadsScopedConfig(manager, Scope.NPCS.group));
        configs.put(Scope.PARTY, new OverheadsScopedConfig(manager, Scope.PARTY.group));
        configs.forEach((scope, config) -> settings.put(scope, new OverheadsSettings(config)));
    }

    Scope scope(Actor actor)
    {
        return actor == client.getLocalPlayer() ? Scope.SELF : actor instanceof NPC ? Scope.NPCS
            : actor != null && party != null && party.contains(actor.getName()) ? Scope.PARTY : Scope.PLAYERS;
    }

    OverheadsConfig config(Scope scope) { return configs.get(scope); }
    OverheadsConfig config(Actor actor) { return config(scope(actor)); }
    OverheadsSettings settings(Actor actor) { return settings.get(scope(actor)); }

    void invalidate(String group)
    {
        for (Scope scope : Scope.values())
            if (scope.group.equals(group) && configs.get(scope) instanceof OverheadsScopedConfig)
                ((OverheadsScopedConfig) configs.get(scope)).invalidate();
    }

    static boolean containsGroup(String group)
    {
        for (Scope scope : Scope.values()) if (scope.group.equals(group)) return true;
        return false;
    }

    void initialize()
    {
        configs.values().forEach(value -> { if (value instanceof OverheadsScopedConfig) ((OverheadsScopedConfig) value).invalidate(); });
        if (!"1".equals(manager.getConfiguration(Scope.SELF.group, "prayerSettingsVersion")))
        {
            String[][] keys = {{"prayerRestorePreview", "restorePreview"}, {"prayerRestorePreviewColor", "restorePreviewColor"}, {"prayerLowColor", "lowResourceColor"}};
            OverheadsSetupSchema old = new OverheadsSetupSchema(config(Scope.SELF));
            for (String[] pair : keys)
            {
                if (manager.getConfiguration(Scope.SELF.group, pair[0]) == null)
                { manager.setConfiguration(Scope.SELF.group, pair[0], old.entries.get(pair[1]).getter.get()); }
            }
            manager.setConfiguration(Scope.SELF.group, "prayerSettingsVersion", "1");
        }
        for (Scope scope : Scope.values())
        {
            if (scope == Scope.SELF || "1".equals(manager.getConfiguration(scope.group, "actorSettingsVersion"))) continue;
            OverheadsSetupSchema source = new OverheadsSetupSchema(config(scope == Scope.PARTY ? Scope.PLAYERS : Scope.SELF));
            for (OverheadsSetupSchema.Entry<?> entry : source.entries.values())
            {
                if (entry.key.startsWith("prayerIconCombat") || entry.key.startsWith("prayerIconRow2")) continue;
                if (entry.key.equals("partyDisplay") || manager.getConfiguration(scope.group, entry.key) != null) continue;
                Object value = entry.getter.get();
                if (scope == Scope.PLAYERS && (entry.key.equals("prayerDisplay") || entry.key.equals("runDisplay") || entry.key.equals("specialDisplay")))
                {
                    value = config(Scope.SELF).partyDisplay() == OverheadsBarDisplay.HIDDEN ? OverheadsBarDisplay.BOTH : config(Scope.SELF).partyDisplay();
                }
                manager.setConfiguration(scope.group, entry.key, value);
            }
            manager.setConfiguration(scope.group, "actorSettingsVersion", "1");
        }
        initializePrayerGroups();
        initializePrayerRowBackgrounds();
    }

    private void initializePrayerGroups()
    {
        String[][] keys = {{"prayerIconCombatScalePercent", "prayerIconScalePercent"}, {"prayerIconCombatPosition", "prayerIconPosition"},
            {"prayerIconCombatXOffset", "prayerIconXOffset"}, {"prayerIconCombatYOffset", "prayerIconYOffset"}};
        for (Scope scope : Scope.values())
        {
            if ("1".equals(manager.getConfiguration(scope.group, "prayerIconGroupsVersion"))) continue;
            OverheadsSetupSchema schema = new OverheadsSetupSchema(config(scope));
            for (String[] pair : keys)
                if (manager.getConfiguration(scope.group, pair[0]) == null)
                    manager.setConfiguration(scope.group, pair[0], schema.entries.get(pair[1]).getter.get());
            manager.setConfiguration(scope.group, "prayerIconGroupsVersion", "1");
            invalidate(scope.group);
        }
    }

    private void initializePrayerRowBackgrounds()
    {
        String[][] keys = {{"prayerIconRow2BackgroundEnabled", "showPrayerIconBackground"},
            {"prayerIconRow2Shape", "prayerIconShape"},
            {"prayerIconRow2Background", "prayerIconBackground"},
            {"prayerIconRow2Border", "prayerIconBorder"},
            {"prayerIconRow2TracksPoints", "prayerIconBackgroundTracksPoints"},
            {"prayerIconRow2EmptyBackground", "prayerIconEmptyBackground"}};
        for (Scope scope : Scope.values())
        {
            if ("1".equals(manager.getConfiguration(scope.group, "prayerRowBackgroundVersion"))) continue;
            OverheadsSetupSchema schema = new OverheadsSetupSchema(config(scope));
            for (String[] pair : keys)
                if (manager.getConfiguration(scope.group, pair[0]) == null)
                    manager.setConfiguration(scope.group, pair[0], schema.entries.get(pair[1]).getter.get());
            manager.setConfiguration(scope.group, "prayerRowBackgroundVersion", "1");
            invalidate(scope.group);
        }
    }

    static String actorKey(String key, Scope scope)
    {
        switch (key)
        {
            case "showNpcOverheadChat": case "overheadChatEnabled": return scope == Scope.NPCS ? "showNpcOverheadChat" : "overheadChatEnabled";
            case "selfHealthDisplay": case "otherHealthDisplay": case "npcHealthDisplay":
                return scope == Scope.NPCS ? "npcHealthDisplay" : (scope == Scope.PLAYERS || scope == Scope.PARTY) ? "otherHealthDisplay" : "selfHealthDisplay";
            case "selfNameVisibility": case "playerNameVisibility": case "npcNameVisibility":
                return scope == Scope.NPCS ? "npcNameVisibility" : (scope == Scope.PLAYERS || scope == Scope.PARTY) ? "playerNameVisibility" : "selfNameVisibility";
            case "disableMyHitsplats": case "disableAllyHitsplats": case "disableEnemyHitsplats":
                return scope == Scope.NPCS ? "disableEnemyHitsplats" : (scope == Scope.PLAYERS || scope == Scope.PARTY) ? "disableAllyHitsplats" : "disableMyHitsplats";
            default: return key;
        }
    }

}
