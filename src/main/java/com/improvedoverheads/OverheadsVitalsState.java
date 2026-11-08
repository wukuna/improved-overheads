package com.improvedoverheads;

import java.util.function.LongSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.plugins.itemstats.StatsChanges;

@Singleton
final class OverheadsVitalsState
{
    private final Client client;
    private final OverheadsConfig config;
    private final ItemStatChangesService itemStats;
    private final LongSupplier clock;
    private int healthRestore;
    private int prayerRestore;
    private int previousPrayer = -1;
    private long tickStarted;
    private long prayerLostAt;
    private long refreshedAt;
    private int hoveredItem = -1;

    @Inject
    OverheadsVitalsState(Client client, OverheadsConfig config, ItemStatChangesService itemStats)
    {
        this(client, config, itemStats, System::nanoTime);
    }

    OverheadsVitalsState(Client client, OverheadsConfig config, ItemStatChangesService itemStats, LongSupplier clock)
    {
        this.client = client;
        this.config = config;
        this.itemStats = itemStats;
        this.clock = clock;
    }

    void gameTick()
    {
        long now = clock.getAsLong();
        tickStarted = now;
        int prayer = client.getBoostedSkillLevel(Skill.PRAYER);
        if (previousPrayer >= 0 && prayer < previousPrayer)
        {
            prayerLostAt = now;
        }
        previousPrayer = prayer;
        refreshedAt = 0;
    }

    void clientTick()
    {
        int item = hoveredInventoryItem();
        if (item < 0 || itemStats == null || (!config.restorePreview() && !config.prayerRestorePreview()))
        {
            healthRestore = 0;
            prayerRestore = 0;
            hoveredItem = -1;
            return;
        }
        long now = clock.getAsLong();
        if (item == hoveredItem && refreshedAt != 0 && now - refreshedAt < 100_000_000L)
        {
            return;
        }
        healthRestore = 0;
        prayerRestore = 0;
        hoveredItem = item;
        refreshedAt = now;
        Effect effect = itemStats.getItemStatChanges(item);
        if (effect == null)
        {
            return;
        }
        StatsChanges changes = effect.calculate(client);
        if (changes == null || changes.getStatChanges() == null)
        {
            return;
        }
        for (StatChange change : changes.getStatChanges())
        {
            if (change == null || change.getStat() == null)
            {
                continue;
            }
            if (Skill.HITPOINTS.getName().equals(change.getStat().getName()))
            {
                healthRestore = Math.max(0, change.getRelative());
            }
            else if (Skill.PRAYER.getName().equals(change.getStat().getName()))
            {
                prayerRestore = Math.max(0, change.getRelative());
            }
        }
    }

    private int hoveredInventoryItem()
    {
        if (client.isMenuOpen() || client.getMenu() == null)
        {
            return -1;
        }
        MenuEntry[] entries = client.getMenu().getMenuEntries();
        if (entries == null || entries.length == 0)
        {
            return -1;
        }
        Widget widget = entries[entries.length - 1].getWidget();
        return widget != null && widget.getId() == InterfaceID.Inventory.ITEMS ? widget.getItemId() : -1;
    }

    int restore(boolean prayer)
    {
        return prayer ? config.prayerRestorePreview() ? prayerRestore : 0 : config.restorePreview() ? healthRestore : 0;
    }

    double tickProgress()
    {
        return tickStarted == 0 ? -1 : Math.max(0, Math.min(1, (clock.getAsLong() - tickStarted) / 600_000_000.0));
    }

    boolean prayerJustDrained()
    {
        return prayerLostAt != 0 && clock.getAsLong() - prayerLostAt < 600_000_000L;
    }

    void clear()
    {
        healthRestore = 0;
        prayerRestore = 0;
        previousPrayer = -1;
        tickStarted = 0;
        prayerLostAt = 0;
        refreshedAt = 0;
        hoveredItem = -1;
    }
}
