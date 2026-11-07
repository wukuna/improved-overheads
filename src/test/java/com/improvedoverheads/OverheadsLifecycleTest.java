package com.improvedoverheads;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsLifecycleTest
{
    @Test
    public void queuedStartCannotReenableAfterShutdownAndProfilesClearState() throws Exception
    {
        OverheadsPlugin plugin = new OverheadsPlugin();
        Client client = mock(Client.class);
        Player player = mock(Player.class);
        when(client.getGameCycle()).thenReturn(30);
        ClientThread clientThread = mock(ClientThread.class);
        List<Runnable> tasks = new ArrayList<>();
        doAnswer(call -> { tasks.add(call.getArgument(0)); return null; }).when(clientThread).invokeLater(any(Runnable.class));
        RenderCallbackManager renders = new RenderCallbackManager();
        OverheadsActorUiVisibilityTracker visibility = new OverheadsActorUiVisibilityTracker(client, renders);
        OverheadsConfig config = new OverheadsConfig() {};
        ConfigManager manager = mock(ConfigManager.class);
        OverheadsPanel panel = new OverheadsPanel(config, manager, mock(ColorPickerManager.class));
        OverheadsOverlay overlay = mock(OverheadsOverlay.class);
        OverheadsCompatibilityTest.inject(plugin, "client", client);
        OverheadsCompatibilityTest.inject(plugin, "config", config);
        OverheadsCompatibilityTest.inject(plugin, "clientThread", clientThread);
        OverheadsCompatibilityTest.inject(plugin, "toolbar", mock(ClientToolbar.class));
        OverheadsCompatibilityTest.inject(plugin, "overlayManager", mock(OverlayManager.class));
        OverheadsCompatibilityTest.inject(plugin, "overlay", overlay);
        OverheadsCompatibilityTest.inject(plugin, "panel", panel);
        OverheadsCompatibilityTest.inject(plugin, "actorUiVisibilityTracker", visibility);
        OverheadsCompatibilityTest.inject(plugin, "localChatEffectTracker", new OverheadsLocalChatEffectTracker());
        OverheadsCompatibilityTest.inject(plugin, "settings", new OverheadsSettings(config));
        SwingUtilities.invokeAndWait(() -> { plugin.startUp(); plugin.shutDown(); });
        drain(tasks);
        assertTrue(renders.addEntity(player, true));
        SwingUtilities.invokeAndWait(plugin::startUp);
        drain(tasks);
        assertFalse(renders.addEntity(player, true));
        plugin.onProfileChanged(new ProfileChanged());
        drain(tasks);
        List<net.runelite.api.Actor> accepted = new ArrayList<>();
        visibility.copyAcceptedInto(accepted, 30);
        assertTrue(accepted.isEmpty());
        SwingUtilities.invokeAndWait(plugin::shutDown);
        drain(tasks);
        assertTrue(renders.addEntity(player, true));
        verify(overlay, atLeast(2)).clearCaches();
        verify(manager, atLeastOnce()).getProfile();
        verifyNoMoreInteractions(manager);
    }

    private static void drain(List<Runnable> tasks)
    {
        List<Runnable> pending = new ArrayList<>(tasks);
        tasks.clear();
        pending.forEach(Runnable::run);
    }
}
