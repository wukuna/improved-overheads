package com.improvedoverheads;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ProfileChanged;

@Singleton
public final class OverheadsReleaseNotice
{
    static final String RELEASE = "2026-11-07-initial-overheads";
    static final String SEEN_KEY = "releaseNoticeId";
    private static final String[] UPDATES = {
        "Customize overhead chat, health bars, hitsplats and prayer icons separately for yourself, party members, other players and NPCs.",
        "Choose numbers, bars or both for Health, Prayer, Run and Special displays.",
        "Add corner prayer badges; empty row spacing collapses while the overhead is active and returns when it turns off.",
        "Remove native prayer backgrounds only, or remove all backgrounds while keeping the prayer symbols.",
        "HP stays below prayer icons. Fresh installs use native HP and hitsplat positions; saved placements are preserved.",
        "Style healing and prayer-restoration splats independently.",
        "Save and share setups and hitsplat themes, with Undo for the last applied changes.",
        "Find detailed controls behind the new head-and-prayer sidebar icon; hide the icon through regular settings if preferred.",
    };

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ConfigManager configManager;
    @Inject private ChatMessageManager chat;
    @Inject private net.runelite.client.eventbus.EventBus eventBus;
    private volatile boolean running;
    private volatile int generation;

    public synchronized void startUp() { if (running) return; running = true; generation++; eventBus.register(this); schedule(); }
    public synchronized void shutDown() { if (!running) return; running = false; generation++; eventBus.unregister(this); }

    private void schedule()
    {
        final int scheduledGeneration = generation;
        clientThread.invokeLater(() -> {
            if (running && generation == scheduledGeneration) showIfNeeded();
        });
    }

    private void showIfNeeded()
    {
        if (client.getGameState() != GameState.LOGGED_IN
            || RELEASE.equals(configManager.getConfiguration(OverheadsConfig.GROUP, SEEN_KEY)))
            return;
        send("Updates:<br>* " + String.join("<br>* ", UPDATES));
        configManager.setConfiguration(OverheadsConfig.GROUP, SEEN_KEY, RELEASE);
    }

    private void send(String text)
    {
        String message = new ChatMessageBuilder().append(ChatColorType.NORMAL).append("[")
            .append(ChatColorType.HIGHLIGHT).append("Improved Overheads")
            .append(ChatColorType.NORMAL).append("] ").build() + text;
        chat.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE)
            .runeLiteFormattedMessage(message).build());
    }

    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        if (running && event.getGameState() == GameState.LOGGED_IN) schedule();
    }

    @Subscribe public void onProfileChanged(ProfileChanged event)
    {

        generation++;
        if (running) schedule();
    }
}
