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
    public void inheritedActorInterfacesUseTheirOwnRuneLiteConfigGroup() throws Exception
    {
        for (Class<?> type : new Class<?>[]{OverheadsProfiles.PlayersConfig.class, OverheadsProfiles.PartyConfig.class, OverheadsProfiles.NpcsConfig.class, OverheadsProfiles.UniversalConfig.class})
        {
            ConfigManager manager = mock(ConfigManager.class);
            Constructor<?> constructor = Class.forName("net.runelite.client.config.ConfigInvocationHandler")
                .getDeclaredConstructor(ConfigManager.class);
            constructor.setAccessible(true);
            InvocationHandler handler = (InvocationHandler) constructor.newInstance(manager);
            OverheadsConfig config = (OverheadsConfig) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
            assertEquals(new OverheadsConfig() {}.healthBarOpacity(), config.healthBarOpacity());
            String group = type.getAnnotation(net.runelite.client.config.ConfigGroup.class).value();
            verify(manager).getConfiguration(group, "healthBarOpacity");
            assertNotEquals(OverheadsConfig.GROUP, group);
            for (Method method : OverheadsConfig.class.getMethods())
            {
                if (method.getAnnotation(ConfigItem.class) != null && method.getParameterCount() == 0)
                { assertNotNull(method.getName(), method.invoke(config)); }
            }
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
