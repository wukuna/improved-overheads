package com.improvedoverheads;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.party.PartyDataService;
import net.runelite.client.party.PartyDataType;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.messages.PartyData;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;

@Singleton
final class OverheadsPartyState
{
    private static final PartyDataType[] TYPES = {PartyDataType.CHARACTER_NAME, PartyDataType.ENERGY, PartyDataType.SPEC};
    private static final Skill[] SKILLS = {Skill.HITPOINTS, Skill.PRAYER};
    private final Client client;
    private final OverheadsConfig config;
    private final PartyService party;
    private final PartyDataService data;
    private final Map<Long, MemberState> states = new HashMap<>();
    private boolean subscribed;
    private long partyId;
    private volatile long session;
    private long lastRefresh;
    private boolean refreshPending;

    static final class Value
    {
        final int current;
        final int maximum;
        final int received;

        Value(int current, int maximum, int received)
        {
            this.current = current;
            this.maximum = maximum;
            this.received = received;
        }
    }

    private static final class MemberState
    {
        private final PartyMember identity;
        private final Value[] values = new Value[4];
        private final int[] bases = {-1, -1};
        private final int[] boosts = {-1, -1};

        private MemberState(PartyMember identity) { this.identity = identity; }
    }

    @Inject
    OverheadsPartyState(Client client, OverheadsConfig config, PartyService party, PartyDataService data)
    {
        this.client = client;
        this.config = config;
        this.party = party;
        this.data = data;
    }

    void refresh(Plugin owner, boolean running)
    {
        boolean wanted = running && config.partyDisplay() != OverheadsBarDisplay.HIDDEN;
        if (wanted != subscribed)
        {
            subscribed = wanted;
            clear();
            if (wanted)
            {
                data.subscribe(owner, TYPES);
                data.subscribe(owner, SKILLS);
            }
            else
            {
                data.unsubscribe(owner, TYPES);
                data.unsubscribe(owner, SKILLS);
            }
        }
        syncParty();
        if (wanted && refreshPending) { requestRefresh(); }
    }

    void requestRefresh()
    {
        if (!subscribed || !party.isInParty() || party.getLocalMember() == null
            || client.getGameState() != GameState.LOGGED_IN) { return; }
        long now = System.nanoTime();
        if (lastRefresh != 0 && now - lastRefresh < 10_000_000_000L) { return; }
        lastRefresh = now;
        party.send(new UserSync());
        refreshPending = false;
    }

    private void syncParty()
    {
        long current = party.getPartyId();
        if (partyId != current) { clear(); }
        states.entrySet().removeIf(entry -> party.getMemberById(entry.getKey()) != entry.getValue().identity
            || !entry.getValue().identity.isLoggedIn());
    }

    boolean contains(String name)
    {
        return name != null && party.isInParty() && party.getMemberByDisplayName(name) != null;
    }

    long id() { return party.getPartyId(); }
    long session() { return session; }

    void accept(PartyData message, long receivedParty, long receivedSession)
    {
        syncParty();
        if (!subscribed || client.getGameState() != GameState.LOGGED_IN || !party.isInParty() || receivedParty != partyId || receivedSession != session) { return; }
        PartyMember member = party.getMemberById(message.getMemberId());
        if (member == null || !member.isLoggedIn() || member == party.getLocalMember()) { return; }
        MemberState state = states.computeIfAbsent(member.getMemberId(), ignored -> new MemberState(member));
        int tick = client.getTickCount();
        int[] base = message.getStatBase();
        int[] boost = message.getStatBoost();
        for (int i = 0; i < SKILLS.length; i++)
        {
            int index = SKILLS[i].ordinal();
            boolean changed = false;
            if (base != null && (index >= base.length || base[index] <= 0 || base[index] > 126))
            {
                state.bases[i] = -1;
                state.boosts[i] = -1;
                state.values[i] = null;
            }
            if (base != null && index < base.length && base[index] > 0 && base[index] <= 126)
            {
                state.bases[i] = base[index];
                changed = true;
            }
            if (boost != null && index < boost.length && boost[index] >= 0 && boost[index] <= 255)
            {
                state.boosts[i] = boost[index];
                changed = true;
            }
            if (changed && state.bases[i] > 0 && state.boosts[i] >= 0)
            {
                state.values[i] = new Value(state.boosts[i], state.bases[i], tick);
            }
        }
        setPercent(state, 2, message.getRunEnergy(), tick);
        setPercent(state, 3, message.getSpecEnergy(), tick);
    }

    private static void setPercent(MemberState state, int index, Integer value, int tick)
    {
        if (value != null && value >= 0 && value <= 100) { state.values[index] = new Value(value, 100, tick); }
    }

    Value value(Player player, int index)
    {
        if (partyId != party.getPartyId()) { clear(); }
        if (!subscribed || player == client.getLocalPlayer() || player.getName() == null) { return null; }
        PartyMember member = party.getMemberByDisplayName(player.getName());
        MemberState state = member == null || !member.isLoggedIn() ? null : states.get(member.getMemberId());
        if (state != null && state.identity != member) { state = null; }
        Value value = state == null || index < 0 || index >= 4 ? null : state.values[index];
        if (value == null) { return null; }
        int age = client.getTickCount() - value.received;
        int lifetime = Math.max(5, Math.min(300, config.partyDataLifetime())) * 5 / 3;
        return age >= 0 && age <= lifetime ? value : null;
    }

    void clear() { states.clear(); partyId = party.getPartyId(); session++; refreshPending = true; }
}
