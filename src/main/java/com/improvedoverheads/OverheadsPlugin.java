package com.improvedoverheads;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.party.messages.PartyData;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ChatboxInput;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.itemstats.ItemStatPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Improved Overheads",
    description = "Customize chat, bars, hitsplats, prayer icons and corner badges for your player, party members, other players and NPCs.",
    tags = {"overhead", "chat", "health", "hitsplat", "prayer", "skull"},
    conflicts = {"Customize a Lot", "Custom HP Bar", "Advanced HP Bar", "Always On HP Bars", "No 0's", "Nameplates"}
)
@PluginDependency(ItemStatPlugin.class)
public class OverheadsPlugin extends Plugin
{
    private static final int GAME_CYCLE_MILLIS = 20;
    @Inject private OverheadsPartyState party;
    @Inject private OverheadsReleaseNotice releaseNotice;
    @Inject private OverheadsLocalTimers timers;
    @Inject private OverheadsConfig config;
    @Inject private OverheadsProfiles profiles;
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private OverlayManager overlayManager;
    @Inject private OverheadsOverlay overlay;
    @Inject private OverheadsSettings settings;
    @Inject private OverheadsActorUiVisibilityTracker actorUiVisibilityTracker;
    @Inject private OverheadsLocalChatEffectTracker localChatEffectTracker;
    @Inject private ClientToolbar toolbar;
    @Inject private OverheadsPanel panel;
    private NavigationButton navigation;
    private final Map<Actor, CopyOnWriteArrayList<OverheadsHitsplat>> hitsplats = new ConcurrentHashMap<>();
    private final Map<Actor, TargetClassification> targetClassifications = new ConcurrentHashMap<>();
    private long nextSequence;
    private volatile boolean running;
    private volatile long lifecycle;
    private final java.util.concurrent.atomic.AtomicBoolean refreshQueued = new java.util.concurrent.atomic.AtomicBoolean();


    OverheadsConfig configFor(Actor actor) { return profiles == null || actor == null ? config : profiles.config(actor); }
    OverheadsSettings settingsFor(Actor actor) { return profiles == null || actor == null ? settings : profiles.settings(actor); }

    Map<Actor, CopyOnWriteArrayList<OverheadsHitsplat>> getHitsplats()
    {
        return hitsplats;
    }

    @Override
    protected synchronized void startUp()
    {
        if (profiles != null) { profiles.initialize(); }
        running = true;
        if (releaseNotice != null) { releaseNotice.startUp(); }
        long generation = ++lifecycle;
        panel.rebuild();
        panel.setPartyRefresh(() -> clientThread.invokeLater(() -> { if (running && party != null) { party.requestRefresh(); } }));
        panel.setReflow(() -> clientThread.invokeLater(() -> { if (running) { overlay.reflow(); } }));
        navigation = NavigationButton.builder().tooltip("Improved Overheads")
            .icon(OverheadsPanel.icon()).priority(5).panel(panel).build();
        toolbar.addNavigation(navigation);
        overlayManager.add(overlay);
        clientThread.invokeLater(() ->
        {
            synchronized (this)
            {
                if (running && lifecycle == generation)
                {
                    clearState();
                    actorUiVisibilityTracker.enable();
                }
            }
        });
    }

    @Override
    protected synchronized void shutDown()
    {
        running = false;
        if (releaseNotice != null) { releaseNotice.shutDown(); }
        long generation = ++lifecycle;
        actorUiVisibilityTracker.disable();
        overlayManager.remove(overlay);
        toolbar.removeNavigation(navigation);
        panel.closeEditors();
        panel.setReflow(() -> {});
        panel.setPartyRefresh(() -> {});
        clientThread.invokeLater(() ->
        {
            if (lifecycle == generation)
            {
                clearState();
            }
        });
    }

    private void clearState() { clearState(true); }

    private void clearState(boolean resetSession)
    {
        if (resetSession && party != null) { party.clear(); party.refresh(this, running); }
        if (timers != null) { timers.clear(); }
        actorUiVisibilityTracker.setNativeExceptions((profiles == null ? config : profiles.config(OverheadsProfiles.Scope.NPCS)).nativeNpcUiIds());
        actorUiVisibilityTracker.clear();
        localChatEffectTracker.clear();
        overlay.clearCaches();
        overlay.clearVitals();
        hitsplats.clear();
        targetClassifications.clear();
        nextSequence = 0;
    }

    @Subscribe
    public void onPartyChanged(PartyChanged event)
    {
        if (party != null) { party.clear(); }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (timers != null && event.getVarpId() == VarPlayerID.POISON) { timers.poisonChanged(); }
    }

    @Subscribe(priority = -1)
    public void onPartyData(PartyData event)
    {
        long generation = lifecycle;
        long receivedParty = party == null ? 0 : party.id();
        long receivedSession = party == null ? -1 : party.session();
        clientThread.invokeLater(() ->
        {
            if (running && lifecycle == generation && party != null) { party.accept(event, receivedParty, receivedSession); }
        });
    }

    @Subscribe(priority = -1)
    public void onGameTick(GameTick event)
    {
        if (party != null) { party.refresh(this, running); }
        if (timers != null) { timers.tick(); }
        overlay.gameTick();
        cleanupExpiredHitsplats();
        actorUiVisibilityTracker.discardBefore(client.getGameCycle() - 1);
    }

    @Subscribe
    public void onStatChanged(net.runelite.api.events.StatChanged event)
    {
        if (running) overlay.statChanged(event);
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        overlay.clientTick();
    }

    @Subscribe
    public void onPluginChanged(PluginChanged event)
    {
        if (event.getPlugin() != this)
        {
            clientThread.invokeLater(() ->
            {
                if (running)
                {
                    actorUiVisibilityTracker.moveToEnd();
                    overlay.clearCaches();
                }
            });
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (profiles != null) profiles.invalidate(event.getGroup());
        if (OverheadsProfiles.containsGroup(event.getGroup()) && !OverheadsSetups.STORAGE_KEY.equals(event.getKey())
            && !OverheadsSetups.THEMES_KEY.equals(event.getKey())
            && refreshQueued.compareAndSet(false, true))
        {
            clientThread.invokeLater(() ->
            {
                refreshQueued.set(false);
                if (running)
                {
                    if (party != null) { party.refresh(this, running); }
                    actorUiVisibilityTracker.setNativeExceptions((profiles == null ? config : profiles.config(OverheadsProfiles.Scope.NPCS)).nativeNpcUiIds());
                    overlay.clearCaches();
                    targetClassifications.clear();
                    cleanupExpiredHitsplats();
                }
            });
            SwingUtilities.invokeLater(() -> { if (running) { panel.refresh(); } });
        }
    }

    @Subscribe
    public void onProfileChanged(ProfileChanged event)
    {
        if (profiles != null) { profiles.initialize(); }
        panel.invalidateSetupSession();
        clientThread.invokeLater(() -> { if (running) { clearState(); } });
        SwingUtilities.invokeLater(() -> { if (running) { panel.closeEditors(); panel.rebuild(); } });
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() != GameState.LOGGED_IN)
        {
            clearState(event.getGameState() != GameState.LOADING);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        removeActor(event.getNpc());
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned event)
    {
        removeActor(event.getPlayer());
    }

    private void removeActor(Actor actor)
    {
        actorUiVisibilityTracker.remove(actor);
        localChatEffectTracker.remove(actor);
        overlay.removeActor(actor);
        hitsplats.remove(actor);
        targetClassifications.remove(actor);
    }

    @Subscribe(priority = Float.NEGATIVE_INFINITY)
    public void onChatboxInput(ChatboxInput event)
    {
        localChatEffectTracker.recordOutgoing(event.getValue(), event.getChatType(),
            event.isConsumed(), client.getGameCycle());
    }

    @Subscribe(priority = Float.NEGATIVE_INFINITY)
    public void onOverheadTextChanged(OverheadTextChanged event)
    {
        localChatEffectTracker.recordOverhead(event.getActor(), client.getLocalPlayer(),
            event.getActor().getOverheadText(), client.getGameCycle());
    }

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Actor actor = event.getActor();
        OverheadsSettings settings = settingsFor(actor);
		Hitsplat hitsplat = event.getHitsplat();
		if (actor == null || hitsplat == null)
		{
			return;
		}
        if (timers != null && actor == client.getLocalPlayer()) { timers.observeHit(hitsplat.getHitsplatType()); }
		overlay.recordHealthBarDamage(actor, hitsplat.getHitsplatType(), hitsplat.getAmount());
		boolean mine = shouldTreatAsMine(hitsplat);
		int amount = hitsplat.getAmount();
		if (shouldDisableHitsplatsForActor(actor)
			|| settings.hideZeroHitsplats() && amount == 0
			|| settings.onlyDisplayMine() && !mine)
		{
			return;
		}

		int gameCycle = client.getGameCycle();
		OverheadsHitmarkDefinition definition = overlay.getDefinition(hitsplat.getHitsplatType());
		if (definition == null)
		{
			return;
		}
		int appearedOnGameCycle = OverheadsOverlay.nativeAppearanceGameCycle(
			definition,
			hitsplat.getDisappearsOnGameCycle(),
			gameCycle);

		addTrackedHitsplat(
			actor,
			hitsplat.getHitsplatType(),
			amount,
			mine,
			appearedOnGameCycle,
			gameCycle);
	}

	private OverheadsHitsplat addTrackedHitsplat(
		Actor actor,
		int hitsplatType,
		int amount,
		boolean mine,
		int appearedOnGameCycle,
		int gameCycle)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		int fadeInCycles = fadeInCycles(actor);
		int fullOpacityCycles = fullOpacityCycles(actor);
		int fadeOutCycles = fadeOutCycles(actor);
		int fullOpacityStartsOnGameCycle = appearedOnGameCycle + fadeInCycles;
		int fadeOutStartsOnGameCycle = fullOpacityStartsOnGameCycle + fullOpacityCycles;
		int expiresOnGameCycle = fadeOutStartsOnGameCycle + fadeOutCycles;
		if (expiresOnGameCycle <= gameCycle)
		{
			return null;
		}

		CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats = hitsplats.computeIfAbsent(actor, ignored -> new CopyOnWriteArrayList<>());
		boolean bossLike = isBossLikeTarget(actor);
		int minRadius = minRadiusFor(actor, bossLike);
		int maxRadius = maxRadiusFor(actor, bossLike);
		actorHitsplats.removeIf(activeHitsplat -> shouldRemoveFromTracking(
			actor,
			activeHitsplat,
			gameCycle,
			minRadius,
			maxRadius));
		int maxHitsplats = config.maxHitsplats() <= 0 ? 128 : Math.min(128, config.maxHitsplats());
		int position = config.reuseOldHitsplatSlots()
			? evictForTimedReuse(actor, actorHitsplats, gameCycle, mine)
			: -1;
		if (position < 0)
		{
			position = getAvailablePosition(actor, actorHitsplats, maxHitsplats, gameCycle);
		}
		if (position < 0 && mine && settings.prioritizeMine())
		{
			position = evictForPrioritizedMine(actor, actorHitsplats, maxHitsplats, gameCycle);
		}

		if (position < 0)
		{
			return null;
		}
		OverheadsHitsplat trackedHitsplat = new OverheadsHitsplat(
				hitsplatType,
				amount,
				position,
				appearedOnGameCycle,
				reuseAgeStartsOnGameCycle(gameCycle, appearedOnGameCycle),
				fullOpacityStartsOnGameCycle,
				fadeOutStartsOnGameCycle,
				expiresOnGameCycle,
				nextSequence++,
				mine);
		actorHitsplats.add(trackedHitsplat);
		return trackedHitsplat;
	}

	private void cleanupExpiredHitsplats()
	{
		int gameCycle = client.getGameCycle();
		hitsplats.entrySet().removeIf(entry ->
		{
			Actor actor = entry.getKey();
			boolean bossLike = isBossLikeTarget(actor);
			int minRadius = minRadiusFor(actor, bossLike);
			int maxRadius = maxRadiusFor(actor, bossLike);
			entry.getValue().removeIf(hitsplat -> shouldRemoveFromTracking(
				actor,
				hitsplat,
				gameCycle,
				minRadius,
				maxRadius));
			if (entry.getValue().isEmpty())
			{
				targetClassifications.remove(entry.getKey());
				return true;
			}
			return false;
		});
	}

	private int getAvailablePosition(
		Actor actor,
		CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats,
		int maxHitsplats,
		int gameCycle)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		if (config.hitsplatMotion() == OverheadsHitsplatMotion.FIXED && settings.layoutBehavior() == OverheadsLayoutBehavior.RANDOM)
		{
			return getRandomAvailablePosition(actor, actorHitsplats, maxHitsplats, gameCycle);
		}

		return getLowestAvailablePosition(actor, actorHitsplats, maxHitsplats, gameCycle);
	}

	private int getLowestAvailablePosition(
		Actor actor,
		CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats,
		int maxHitsplats,
		int gameCycle)
	{
		boolean bossLike = isBossLikeTarget(actor);
		int limit = positionLimit(actor, actorHitsplats.size(), maxHitsplats, bossLike);
		for (int position = 0; position < limit; position++)
		{
			if (!isPositionTaken(actorHitsplats, position, gameCycle))
			{
				return position;
			}
		}

		return -1;
	}

	private int getRandomAvailablePosition(
		Actor actor,
		CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats,
		int maxHitsplats,
		int gameCycle)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		boolean bossLike = isBossLikeTarget(actor);
		int limit = hardPositionLimit(actor, maxHitsplats, bossLike);
		int minRadius = minRadiusFor(actor, bossLike);
		for (int position = 0; position < limit; )
		{
			int radius = OverheadsLayout.radiusForPosition(position, minRadius, settings.layoutShape());
			List<Integer> availablePositions = new ArrayList<>();
			while (position < limit && OverheadsLayout.radiusForPosition(position, minRadius, settings.layoutShape()) == radius)
			{
				if (!isPositionTaken(actorHitsplats, position, gameCycle))
				{
					availablePositions.add(position);
				}
				position++;
			}

			if (!availablePositions.isEmpty())
			{
				return availablePositions.get(ThreadLocalRandom.current().nextInt(availablePositions.size()));
			}
		}

		return -1;
	}

	private static boolean isPositionTaken(CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats, int position, int gameCycle)
	{
		for (OverheadsHitsplat hitsplat : actorHitsplats)
		{
			if (!hitsplat.isExpired(gameCycle) && hitsplat.getPosition() == position)
			{
				return true;
			}
		}

		return false;
	}

	static boolean shouldTreatAsMine(Hitsplat hitsplat)
	{
		return hitsplat.isMine();
	}

	private int evictForTimedReuse(Actor actor,
		CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats,
		int gameCycle,
		boolean mine)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		OverheadsHitsplat victim = findTimedReuseVictim(
			actorHitsplats,
			gameCycle,
			reuseWindowCycles(config.hitsplatReuseInterval()),
			mine,
			settings.prioritizeMine());
		if (victim == null || !actorHitsplats.remove(victim))
		{
			return -1;
		}

		return victim.getPosition();
	}

	static OverheadsHitsplat findTimedReuseVictim(
		List<OverheadsHitsplat> actorHitsplats,
		int gameCycle,
		int minimumAgeCycles,
		boolean incomingMine,
		boolean prioritizeMine)
	{
		if (incomingMine && prioritizeMine)
		{
			OverheadsHitsplat nonMine = findOldestReusableHitsplat(
				actorHitsplats,
				gameCycle,
				minimumAgeCycles,
				false);
			if (nonMine != null)
			{
				return nonMine;
			}
		}

		return findOldestReusableHitsplat(
			actorHitsplats,
			gameCycle,
			minimumAgeCycles,
			true);
	}

	private static OverheadsHitsplat findOldestReusableHitsplat(
		List<OverheadsHitsplat> actorHitsplats,
		int gameCycle,
		int minimumAgeCycles,
		boolean includeMine)
	{
		OverheadsHitsplat oldest = null;
		for (OverheadsHitsplat hitsplat : actorHitsplats)
		{
			long age = (long) gameCycle - hitsplat.getReuseAgeStartsOnGameCycle();
			if (age < Math.max(1, minimumAgeCycles)
				|| !includeMine && hitsplat.isMine())
			{
				continue;
			}

			if (oldest == null
				|| hitsplat.getReuseAgeStartsOnGameCycle() < oldest.getReuseAgeStartsOnGameCycle()
				|| hitsplat.getReuseAgeStartsOnGameCycle() == oldest.getReuseAgeStartsOnGameCycle()
					&& hitsplat.getSequence() < oldest.getSequence())
			{
				oldest = hitsplat;
			}
		}

		return oldest;
	}

	static int reuseWindowCycles(int milliseconds)
	{
		long clampedMilliseconds = Math.max(0L, (long) milliseconds);
		long cycles = (clampedMilliseconds + GAME_CYCLE_MILLIS - 1L) / GAME_CYCLE_MILLIS;
		return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, cycles));
	}

	static int reuseAgeStartsOnGameCycle(int eventGameCycle, int appearedOnGameCycle)
	{
		return Math.max(eventGameCycle, appearedOnGameCycle);
	}

	private int evictForPrioritizedMine(Actor actor, CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats, int maxHitsplats, int gameCycle)
	{
		OverheadsHitsplat victim = findOldestEvictableHitsplat(actor, actorHitsplats, maxHitsplats, gameCycle, false);
		if (victim == null)
		{
			victim = findOldestEvictableHitsplat(actor, actorHitsplats, maxHitsplats, gameCycle, true);
		}

		if (victim == null)
		{
			return -1;
		}

		actorHitsplats.remove(victim);
		return victim.getPosition();
	}

	private OverheadsHitsplat findOldestEvictableHitsplat(
		Actor actor,
		CopyOnWriteArrayList<OverheadsHitsplat> actorHitsplats,
		int maxHitsplats,
		int gameCycle,
		boolean includeMine)
	{
		OverheadsHitsplat oldest = null;
		boolean bossLike = isBossLikeTarget(actor);
		int minRadius = minRadiusFor(actor, bossLike);
		int maxRadius = maxRadiusFor(actor, bossLike);
		for (OverheadsHitsplat hitsplat : actorHitsplats)
		{
			if (shouldRemoveFromTracking(
				actor,
				hitsplat,
				gameCycle,
				minRadius,
				maxRadius)
				|| maxHitsplats > 0 && hitsplat.getPosition() >= maxHitsplats
				|| !includeMine && hitsplat.isMine())
			{
				continue;
			}

			if (oldest == null || hitsplat.getSequence() < oldest.getSequence())
			{
				oldest = hitsplat;
			}
		}

		return oldest;
	}

	private boolean shouldRemoveFromTracking(
		Actor actor,
		OverheadsHitsplat hitsplat,
		int gameCycle,
		int minRadius,
		int maxRadius)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		return hitsplat.isExpired(gameCycle)
			|| hitsplat.isOutsidePositionLimit(config.maxHitsplats())
			|| shouldDisableHitsplatsForActor(actor)
			|| settings.hideZeroHitsplats() && hitsplat.getAmount() == 0
			|| settings.onlyDisplayMine() && !hitsplat.isMine()
			|| !OverheadsLayout.isPositionWithinRadiusLimit(
				hitsplat.getPosition(),
				minRadius,
				maxRadius,
				settings.layoutShape());
	}

	boolean shouldDisableHitsplatsForActor(Actor actor)
	{
        OverheadsConfig config = configFor(actor);
		Player localPlayer = client.getLocalPlayer();
		if (actor == localPlayer)
		{
			return config.disableMyHitsplats();
		}

		if (actor instanceof NPC)
		{
			return config.disableEnemyHitsplats();
		}

		if (actor instanceof Player)
		{
			return config.disableAllyHitsplats();
		}

		return false;
	}

	boolean isBossLikeTarget(Actor actor)
	{
        OverheadsConfig config = configFor(actor);
		if (actor == null)
		{
			return false;
		}

		int footprintSize = actor.getFootprintSize();
		int publicHealthScale = actor.getHealthScale();
		int actorDefinitionId = actor instanceof NPC ? ((NPC) actor).getId() : -1;
		TargetClassification cached = targetClassifications.get(actor);
		if (cached != null
			&& canReuseTargetClassification(
				cached.footprintSize,
				cached.publicHealthScale,
				cached.actorDefinitionId,
				footprintSize,
				publicHealthScale,
				actorDefinitionId))
		{
			return cached.bossLike;
		}

		boolean detected = detectsBossLikeTarget(
			config.largeTargetDetection(),
			footprintSize,
			publicHealthScale,
			config.largeTargetSize(),
			config.largeTargetHealthScale());
		targetClassifications.put(actor, new TargetClassification(
			footprintSize,
			publicHealthScale,
			actorDefinitionId,
			detected));
		return detected;
	}

	static boolean canReuseTargetClassification(
		int cachedFootprintSize,
		int cachedPublicHealthScale,
		int cachedActorDefinitionId,
		int footprintSize,
		int publicHealthScale,
		int actorDefinitionId)
	{
		return cachedFootprintSize == footprintSize
			&& cachedActorDefinitionId == actorDefinitionId
			&& (publicHealthScale <= 0 || cachedPublicHealthScale == publicHealthScale);
	}

	int minRadiusFor(boolean bossLike) { return minRadiusFor(null, bossLike); }
	int maxRadiusFor(boolean bossLike) { return maxRadiusFor(null, bossLike); }

	int minRadiusFor(Actor actor, boolean bossLike)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		return bossLike && config.hitsplatMotion() == OverheadsHitsplatMotion.FIXED ? settings.minRadius() : 0;
	}

	int maxRadiusFor(Actor actor, boolean bossLike)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		return bossLike && config.hitsplatMotion() == OverheadsHitsplatMotion.FIXED ? settings.maxRadius() : 0;
	}

	static boolean detectsBossLikeTarget(
		OverheadsTargetDetection detection,
		int footprintSize,
		int publicHealthScale,
		int footprintThreshold,
		int healthScaleThreshold)
	{
		boolean footprintMatch = footprintSize >= Math.max(1, footprintThreshold);
		boolean healthScaleMatch = publicHealthScale >= Math.max(100, healthScaleThreshold);
		OverheadsTargetDetection effectiveDetection = detection == null
			? OverheadsTargetDetection.EITHER
			: detection;
		switch (effectiveDetection)
		{
			case FOOTPRINT:
				return footprintMatch;
			case HEALTH_SCALE:
				return healthScaleMatch;
			case EITHER:
			default:
				return footprintMatch || healthScaleMatch;
		}
	}

	private int positionLimit(Actor actor, int activeHitsplatCount, int maxHitsplats, boolean bossLike)
	{
        OverheadsSettings settings = settingsFor(actor);
		int limit = maxHitsplats <= 0 ? activeHitsplatCount + 1 : maxHitsplats;
		int radiusLimit = OverheadsLayout.slotLimit(
			minRadiusFor(actor, bossLike),
			maxRadiusFor(actor, bossLike),
			settings.layoutShape());
		if (radiusLimit != Integer.MAX_VALUE)
		{
			limit = Math.min(limit, radiusLimit);
		}

		return Math.max(0, limit);
	}

	private int hardPositionLimit(Actor actor, int maxHitsplats, boolean bossLike)
	{
        OverheadsSettings settings = settingsFor(actor);
		int limit = maxHitsplats <= 0 ? 128 : Math.min(128, maxHitsplats);
		int radiusLimit = OverheadsLayout.slotLimit(
			minRadiusFor(actor, bossLike),
			maxRadiusFor(actor, bossLike),
			settings.layoutShape());
		if (radiusLimit != Integer.MAX_VALUE)
		{
			limit = Math.min(limit, radiusLimit);
		}

		return Math.max(0, limit);
	}

	private int fadeInCycles(Actor actor)
	{
        OverheadsSettings settings = settingsFor(actor);
		return Math.max(0, settings.fadeInDuration() / GAME_CYCLE_MILLIS);
	}

	private int fullOpacityCycles(Actor actor)
	{
        OverheadsSettings settings = settingsFor(actor);
		return Math.max(0, settings.fullOpacityDuration() / GAME_CYCLE_MILLIS);
	}

	private int fadeOutCycles(Actor actor)
	{
        OverheadsSettings settings = settingsFor(actor);
		return Math.max(0, settings.fadeOutDuration() / GAME_CYCLE_MILLIS);
	}

	private static final class TargetClassification
	{
		private final int footprintSize;
		private final int publicHealthScale;
		private final int actorDefinitionId;
		private final boolean bossLike;

		private TargetClassification(
			int footprintSize,
			int publicHealthScale,
			int actorDefinitionId,
			boolean bossLike)
		{
			this.footprintSize = footprintSize;
			this.publicHealthScale = publicHealthScale;
			this.actorDefinitionId = actorDefinitionId;
			this.bossLike = bossLike;
		}
	}

	@Provides
	OverheadsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OverheadsConfig.class);
	}
}
