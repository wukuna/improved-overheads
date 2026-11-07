package com.improvedoverheads;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.regenmeter.RegenMeterPlugin;

@Singleton
final class OverheadsLocalTimers
{
    private final Client client;
    private final OverheadsConfig config;
    private final PluginManager plugins;
    private final ItemManager items;
    private int poisonAt = -1;
    private double regen = -1;

    @Inject
    OverheadsLocalTimers(Client client, PluginManager plugins, ItemManager items, OverheadsConfig config)
    {
        this.client = client;
        this.config = config;
        this.plugins = plugins;
        this.items = items;
    }

    void observeHit(int type)
    {
        if (type == HitsplatID.POISON || type == HitsplatID.VENOM) { poisonAt = client.getTickCount(); }
    }

    void poisonChanged()
    {
        if (client.getVarpValue(VarPlayerID.POISON) <= 0) { poisonAt = -1; }
    }

    void tick()
    {
        regen = config.regenDisplay() == OverheadsBarDisplay.HIDDEN ? -1 : readRegen();
        poisonChanged();
    }

    double poisonRemaining()
    {
        int elapsed = client.getTickCount() - poisonAt;
        return poisonAt < 0 || client.getVarpValue(VarPlayerID.POISON) <= 0 || elapsed < 0 || elapsed >= 30
            ? -1 : 1 - elapsed / 30.0;
    }

    double regenRemaining() { return regen; }

    private double readRegen()
    {
        if (client.getBoostedSkillLevel(Skill.HITPOINTS) >= client.getRealSkillLevel(Skill.HITPOINTS)
            || client.getBoostedSkillLevel(Skill.HITPOINTS) <= 0 || hasIndependentCapeTimer() || client.isPrayerActive(Prayer.RP_REJUVENATION)) { return -1; }
        for (Plugin plugin : plugins.getPlugins())
        {
            if (plugin instanceof RegenMeterPlugin && plugins.isPluginActive(plugin))
            {
                double fraction = ((RegenMeterPlugin) plugin).getHitpointsPercentage();
                return Double.isFinite(fraction) && fraction >= 0 && fraction < 1 ? 1 - fraction : -1;
            }
        }
        return -1;
    }

    int regenTicks() { return client.isPrayerActive(Prayer.RAPID_HEAL) ? 50 : 100; }

    private boolean hasIndependentCapeTimer()
    {
        if (client.isPrayerActive(Prayer.RAPID_HEAL)) { return false; }
        ItemContainer worn = client.getItemContainer(InventoryID.EQUIPMENT);
        Item cape = worn == null ? null : worn.getItem(1);
        if (cape == null || cape.getId() < 0) { return false; }
        String name = items.getItemComposition(cape.getId()).getName();
        return name != null && (name.startsWith("Hitpoints cape") || name.toLowerCase(java.util.Locale.ROOT).contains("max cape"));
    }

    void clear() { poisonAt = -1; regen = -1; }
}
