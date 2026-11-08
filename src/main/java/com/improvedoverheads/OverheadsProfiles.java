package com.improvedoverheads;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;

@Singleton
public final class OverheadsProfiles
{
    enum Scope
    {
        SELF("My player", OverheadsConfig.GROUP),
        PLAYERS("Other players", "improvedoverheadsplayers"),
        NPCS("NPCs", "improvedoverheadsnpcs"),
        PARTY("Party", "improvedoverheadsparty"),
        UNIVERSAL("Universal", "improvedoverheadsuniversal");

        final String label;
        final String group;
        Scope(String label, String group) { this.label = label; this.group = group; }
        @Override public String toString() { return label; }
    }

    @ConfigGroup("improvedoverheadsplayers")
    public interface PlayersConfig extends OverheadsConfig {}
    @ConfigGroup("improvedoverheadsnpcs")
    public interface NpcsConfig extends OverheadsConfig {}
    @ConfigGroup("improvedoverheadsparty")
    public interface PartyConfig extends OverheadsConfig {}
    @ConfigGroup("improvedoverheadsuniversal")
    public interface UniversalConfig extends OverheadsConfig {}

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
        configs.put(Scope.PLAYERS, manager.getConfig(PlayersConfig.class));
        configs.put(Scope.NPCS, manager.getConfig(NpcsConfig.class));
        configs.put(Scope.PARTY, manager.getConfig(PartyConfig.class));
        configs.put(Scope.UNIVERSAL, manager.getConfig(UniversalConfig.class));
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

    static boolean containsGroup(String group)
    {
        for (Scope scope : Scope.values()) if (scope.group.equals(group)) return true;
        return false;
    }

    void initialize()
    {
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
    }

    void applyUniversal(OverheadsComponent component)
    {
        OverheadsSetupSchema source = new OverheadsSetupSchema(config(Scope.UNIVERSAL));
        for (Scope scope : new Scope[]{Scope.SELF, Scope.PLAYERS, Scope.NPCS, Scope.PARTY})
        {
            for (OverheadsSetupSchema.Entry<?> entry : source.entries.values())
            {
                if (entry.key.equals("partyDisplay") || OverheadsComponent.forKey(entry.key) != component) continue;
                String sourceKey = universalKey(entry.key);
                Object value = source.entries.get(sourceKey).getter.get();
                if (entry.key.equals("healthBarSolidWidth")) { value = (double) config(Scope.UNIVERSAL).playerHealthWidth(); }
                if (entry.key.equals("healthBarHeight")) { value = (double) Math.min(20, config(Scope.UNIVERSAL).playerHealthHeight()); }
                manager.setConfiguration(scope.group, entry.key, value);
            }
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

    private static String universalKey(String key)
    {
        switch (key)
        {
            case "otherHealthDisplay": case "npcHealthDisplay": return "selfHealthDisplay";
            case "showNpcOverheadChat": return "overheadChatEnabled";
            case "playerNameVisibility": case "npcNameVisibility": return "selfNameVisibility";
            case "disableAllyHitsplats": case "disableEnemyHitsplats": return "disableMyHitsplats";
            default: return key;
        }
    }
}
