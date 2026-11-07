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
