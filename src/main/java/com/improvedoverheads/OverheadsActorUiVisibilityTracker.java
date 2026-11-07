package com.improvedoverheads;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import net.runelite.api.Renderable;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;

@Singleton
final class OverheadsActorUiVisibilityTracker
{
	private final Client client;
	private final RenderCallbackManager renderCallbackManager;
	private final Map<Actor, Integer> acceptedOnGameCycle = new ConcurrentHashMap<>();
	private final Map<Actor, Integer> nativeUiOnGameCycle = new ConcurrentHashMap<>();
	private final RenderCallback observer = new RenderCallback()
	{
		@Override
		public boolean addEntity(Renderable renderable, boolean ui)
		{
			boolean active = enabled;
			boolean actor = renderable instanceof Actor;
			boolean requiresNativeUi = active
				&& ui
				&& actor
				&& (requiresNativeUi((Actor) renderable) || nativeException((Actor) renderable));
			if (active && ui && actor)
			{
				Actor acceptedActor = (Actor) renderable;
				Integer gameCycle = currentGameCycleValue();
				acceptedOnGameCycle.put(acceptedActor, gameCycle);
				if (requiresNativeUi)
				{
					nativeUiOnGameCycle.put(acceptedActor, gameCycle);
				}
				else
				{
					nativeUiOnGameCycle.remove(acceptedActor);
				}
			}
			if (shouldSuppressActorUi(active, ui, actor, requiresNativeUi))
			{
				return false;
			}

			return true;
		}
	};

    private Set<Integer> nativeExceptions = Collections.emptySet();

    void setNativeExceptions(String value)
    {
        Set<Integer> ids = new HashSet<>();
        if (value != null && value.length() <= 4096)
        {
            for (String part : value.split(","))
            {
                try
                {
                    int id = Integer.parseInt(part.trim());
                    if (id >= 0) { ids.add(id); }
                }
                catch (NumberFormatException ignored) { }
            }
        }
        nativeExceptions = ids;
    }

    boolean nativeException(Actor actor)
    {
        if (!(actor instanceof NPC)) { return false; }
        NPC npc = (NPC) actor;
        NPCComposition composition = npc.getTransformedComposition();
        return nativeExceptions.contains(npc.getId())
            || composition != null && nativeExceptions.contains(composition.getId());
    }

	private volatile boolean enabled;
	private volatile int cachedGameCycle = Integer.MIN_VALUE;
	private volatile Integer cachedGameCycleValue = Integer.MIN_VALUE;

	@Inject
	OverheadsActorUiVisibilityTracker(
		Client client,
		RenderCallbackManager renderCallbackManager)
	{
		this.client = client;
		this.renderCallbackManager = renderCallbackManager;
	}

	synchronized void enable()
	{
		if (!enabled)
		{
			renderCallbackManager.register(observer);
			enabled = true;
		}
	}

	synchronized void moveToEnd()
	{
		if (!enabled)
		{
			return;
		}

		renderCallbackManager.unregister(observer);
		renderCallbackManager.register(observer);
		clear();
	}

	synchronized void disable()
	{
		if (enabled)
		{
			enabled = false;
			renderCallbackManager.unregister(observer);
		}
		clear();
	}

	void copyAcceptedInto(Collection<Actor> destination, int gameCycle)
	{
		if (destination == null || !enabled)
		{
			return;
		}

		for (Map.Entry<Actor, Integer> entry : acceptedOnGameCycle.entrySet())
		{
			if (acceptedOn(entry.getValue(), gameCycle))
			{
				destination.add(entry.getKey());
			}
		}
	}

	void remove(Actor actor)
	{
		if (actor != null)
		{
			acceptedOnGameCycle.remove(actor);
			nativeUiOnGameCycle.remove(actor);
		}
	}

	boolean retainedNativeUi(Actor actor, int gameCycle)
	{
		return actor != null
			&& acceptedOn(nativeUiOnGameCycle.getOrDefault(actor, Integer.MIN_VALUE), gameCycle);
	}

	void discardBefore(int oldestGameCycle)
	{
		acceptedOnGameCycle.entrySet().removeIf(entry -> entry.getValue() < oldestGameCycle);
		nativeUiOnGameCycle.entrySet().removeIf(entry -> entry.getValue() < oldestGameCycle);
	}

	void clear()
	{
		acceptedOnGameCycle.clear();
		nativeUiOnGameCycle.clear();
		cachedGameCycleValue = Integer.MIN_VALUE;
		cachedGameCycle = Integer.MIN_VALUE;
	}

	private Integer currentGameCycleValue()
	{
		int gameCycle = client.getGameCycle();
		if (cachedGameCycle == gameCycle)
		{
			return cachedGameCycleValue;
		}

		synchronized (this)
		{
			if (cachedGameCycle != gameCycle)
			{
				cachedGameCycleValue = gameCycle;
				cachedGameCycle = gameCycle;
			}
			return cachedGameCycleValue;
		}
	}

	static boolean acceptedOn(int observedGameCycle, int gameCycle)
	{
		return observedGameCycle == gameCycle;
	}

	static boolean shouldSuppressActorUi(
		boolean enabled,
		boolean ui,
		boolean actor,
		boolean requiresNativeUi)
	{
		return enabled && ui && actor && !requiresNativeUi;
	}

	static boolean requiresNativeUi(Actor actor)
	{
        if (!(actor instanceof NPC)) { return false; }
        NPC npc = (NPC) actor;
        NPCComposition composition = npc.getTransformedComposition();
        return isSpecialBarCompatibilityNpc(npc.getId())
            || composition != null && isSpecialBarCompatibilityNpc(composition.getId());
	}

	static boolean isSpecialBarCompatibilityNpc(int npcId)
	{
		return usesDoomChargeBar(npcId)
			|| npcId == NpcID.TOA_KEPHRI_BOSS_SHIELDED
			|| npcId == NpcID.TOA_KEPHRI_BOSS_WEAK
			|| npcId == NpcID.TOA_KEPHRI_BOSS_ENRAGE
			|| npcId == NpcID.TOA_KEPHRI_BOSS_DEAD
			|| npcId == NpcID.YAMA_VOIDFLARE
			|| npcId == NpcID.ENT_TOTEMS_SITE_ICON
			|| npcId == NpcID.GATHERING_EVENT_SAPLING_NPC_HPBAR_1X1
			|| npcId == NpcID.GATHERING_EVENT_SAPLING_NPC_HPBAR_2X2
			|| npcId == NpcID.RAIDS_ICEDEMON_NONCOMBAT
			|| npcId == NpcID.MM_LAB_TABLE_BAR
			|| npcId == NpcID.WHISPERER
			|| npcId == NpcID.WHISPERER_MELEE
			|| npcId == NpcID.WHISPERER_QUEST
			|| npcId == NpcID.WHISPERER_MELEE_QUEST
			|| npcId == NpcID.VERZIK_PILLAR_NPC
			|| npcId == NpcID.VERZIK_COLLAPSING_PILLAR_NPC
			|| npcId == NpcID.VERZIK_STORY_PILLAR_NPC
			|| npcId == NpcID.VERZIK_STORY_COLLAPSING_PILLAR_NPC
			|| npcId == NpcID.VERZIK_HARD_PILLAR_NPC
			|| npcId == NpcID.VERZIK_HARD_COLLAPSING_PILLAR_NPC;
	}


	static boolean usesDoomChargeBar(int npcId)
	{
		return npcId == NpcID.DOM_BOSS
			|| npcId == NpcID.DOM_BOSS_SHIELDED
			|| npcId == NpcID.DOM_BOSS_BURROWED;
	}
}
