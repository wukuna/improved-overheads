package com.improvedoverheads;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsConfigProxyTest
{
    @Test
    public void actorReadsStayIsolatedFromRuneLitesSharedMethodCache() throws Exception
    {
        java.util.Map<String, String> values = new java.util.HashMap<>();
        ConfigManager manager = mock(ConfigManager.class, call ->
        {
            Object[] args = call.getArguments();
            if (call.getMethod().getName().equals("stringToObject") || call.getMethod().getName().equals("getConfiguration")
                && args.length >= 3 && args[args.length - 1] instanceof java.lang.reflect.Type) return call.callRealMethod();
            if (call.getMethod().getName().equals("getConfiguration") && args.length == 3) return values.get(args[0] + "." + args[2]);
            return RETURNS_DEFAULTS.answer(call);
        });
        when(manager.getConfiguration(anyString(), anyString())).thenAnswer(call -> values.get(call.getArgument(0) + "." + call.getArgument(1)));
        values.put(OverheadsConfig.GROUP + ".healthTextColor", Integer.toString(java.awt.Color.RED.getRGB()));
        values.put(OverheadsProfiles.Scope.NPCS.group + ".healthTextColor", Integer.toString(java.awt.Color.BLUE.getRGB()));
        Constructor<?> constructor = Class.forName("net.runelite.client.config.ConfigInvocationHandler").getDeclaredConstructor(ConfigManager.class);
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler) constructor.newInstance(manager);
        OverheadsConfig self = (OverheadsConfig) Proxy.newProxyInstance(OverheadsConfig.class.getClassLoader(), new Class<?>[]{OverheadsConfig.class}, handler);
        OverheadsProfiles profiles = new OverheadsProfiles(mock(net.runelite.api.Client.class), manager, self);
        assertEquals(java.awt.Color.RED, self.healthTextColor());
        assertEquals(java.awt.Color.BLUE, profiles.config(OverheadsProfiles.Scope.NPCS).healthTextColor());
        values.put(OverheadsProfiles.Scope.NPCS.group + ".healthTextColor", Integer.toString(java.awt.Color.GREEN.getRGB()));
        profiles.invalidate(OverheadsProfiles.Scope.NPCS.group);
        assertEquals(java.awt.Color.GREEN, profiles.config(OverheadsProfiles.Scope.NPCS).healthTextColor());
        assertEquals(java.awt.Color.RED, self.healthTextColor());
        for (Method method : OverheadsConfig.class.getMethods())
        {
            if (method.getAnnotation(ConfigItem.class) != null && !method.getName().equals("hideSidePanel"))
                assertEquals(OverheadsScopedConfig.class, OverheadsScopedConfig.class.getMethod(method.getName()).getDeclaringClass());
        }
    }

    @Test
    public void savedHitsplatSidesAndNativeDeserializeThroughRuneLite() throws Exception
    {
        for (OverheadsHitsplatPosition position : OverheadsHitsplatPosition.values())
        {
            ConfigManager manager = mock(ConfigManager.class, call -> call.getMethod().getName().equals("stringToObject")
                ? call.callRealMethod() : RETURNS_DEFAULTS.answer(call));
            when(manager.getConfiguration(OverheadsConfig.GROUP, "hitsplatPosition")).thenReturn(position.name());
            Constructor<?> constructor = Class.forName("net.runelite.client.config.ConfigInvocationHandler")
                .getDeclaredConstructor(ConfigManager.class);
            constructor.setAccessible(true);
            InvocationHandler handler = (InvocationHandler) constructor.newInstance(manager);
            OverheadsConfig config = (OverheadsConfig) Proxy.newProxyInstance(OverheadsConfig.class.getClassLoader(),
                new Class<?>[]{OverheadsConfig.class}, handler);
            assertEquals(position, config.hitsplatPosition());
        }
    }

    @Test
    public void settingsAndPanelWorkThroughRuneLiteConfigProxy() throws Exception
    {
        ConfigManager manager = mock(ConfigManager.class);
        Constructor<?> constructor = Class.forName("net.runelite.client.config.ConfigInvocationHandler")
            .getDeclaredConstructor(ConfigManager.class);
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler) constructor.newInstance(manager);
        OverheadsConfig config = (OverheadsConfig) Proxy.newProxyInstance(OverheadsConfig.class.getClassLoader(),
            new Class<?>[]{OverheadsConfig.class}, handler);
        OverheadsConfig defaults = new OverheadsConfig() {};
        for (Method method : OverheadsConfig.class.getMethods())
        {
            if (method.getAnnotation(ConfigItem.class) != null && method.getParameterCount() == 0)
            {
                assertEquals(method.getName(), method.invoke(defaults), method.invoke(config));
            }
        }
        SwingUtilities.invokeAndWait(() -> new OverheadsPanel(config, manager,
            mock(ColorPickerManager.class)).rebuild());
    }
}
