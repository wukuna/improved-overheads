package com.improvedoverheads;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OverheadsPresentationTest
{
    @Test
    public void osrsBarsFillFromTheSelectedEndInBothOrientations()
    {
        BufferedImage front = new BufferedImage(30, 5, BufferedImage.TYPE_INT_ARGB);
        BufferedImage back = new BufferedImage(30, 5, BufferedImage.TYPE_INT_ARGB);
        Graphics2D f = front.createGraphics();
        f.setColor(Color.GREEN);
        f.fillRect(0, 0, 30, 5);
        f.dispose();
        Graphics2D b = back.createGraphics();
        b.setColor(Color.RED);
        b.fillRect(0, 0, 30, 5);
        b.dispose();
        for (OverheadsHealthBarFillDirection direction : OverheadsHealthBarFillDirection.values())
        {
            boolean vertical = direction == OverheadsHealthBarFillDirection.TOP_TO_BOTTOM
                || direction == OverheadsHealthBarFillDirection.BOTTOM_TO_TOP;
            BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            OverheadsHealthBarRenderer.drawOsrsBar(graphics,
                new Rectangle(0, 0, vertical ? 5 : 30, vertical ? 30 : 5),
                vertical, 0.5, direction, front, back);
            graphics.dispose();
            boolean startsFilled = direction == OverheadsHealthBarFillDirection.TOP_TO_BOTTOM
                || direction == OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
            assertEquals(startsFilled ? Color.GREEN.getRGB() : Color.RED.getRGB(), image.getRGB(2, 2));
            assertEquals(startsFilled ? Color.RED.getRGB() : Color.GREEN.getRGB(),
                image.getRGB(vertical ? 2 : 28, vertical ? 28 : 2));
        }
        BufferedImage tinted = OverheadsHealthBarRenderer.tintSprite(front, Color.CYAN);
        assertEquals(Color.CYAN.getRGB(), tinted.getRGB(10, 2));
        assertEquals(Color.GREEN.getRGB(), front.getRGB(10, 2));
    }

    @Test
    public void importedBarDimensionsCannotOverflowOrAllocateUnboundedImages()
    {
        assertEquals(2400, OverheadsHealthBarRenderer.barDimension(Double.MAX_VALUE, 10, 200, Double.MAX_VALUE));
        assertEquals(10, OverheadsHealthBarRenderer.barDimension(Double.NaN, 10, 200, Double.NaN));
        assertEquals(2, OverheadsHealthBarRenderer.barDimension(-100, 2, 30, -20));
    }

    @Test
    public void sideBarsRemainBesideThePlayerAndZoomIsBounded()
    {
        Point top = new Point(200, 100);
        Point middle = new Point(200, 150);
        Point feet = new Point(200, 200);
        Rectangle left = OverheadsPlacement.bounds(top, middle, feet, OverheadsPosition.LEFT, 10, 60, 0, 0);
        Rectangle right = OverheadsPlacement.bounds(top, middle, feet, OverheadsPosition.RIGHT, 10, 60, 0, 0);
        Rectangle below = OverheadsPlacement.bounds(top, middle, feet, OverheadsPosition.BELOW, 60, 10, 0, 0);
        assertEquals(new Rectangle(172, 120, 10, 60), left);
        assertEquals(new Rectangle(218, 120, 10, 60), right);
        assertTrue(below.y > feet.getY());
        assertEquals(2.0, OverheadsPlacement.zoomFactor(750, 1000), 0.001);
        assertEquals(1.0, OverheadsPlacement.zoomFactor(750, Float.NaN), 0);
        assertEquals(4.0, OverheadsPlacement.zoomFactor(10000, 50), 0);
    }

    @Test
    public void prayerFramesRecolorWithoutChangingTheSharedSprite()
    {
        BufferedImage original = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        original.setRGB(5, 5, Color.WHITE.getRGB());
        OverheadsSprite source = new OverheadsSprite(original, 0, 0);
        BufferedImage square = OverheadsPrayerIconStyle.create(source, OverheadsIconShape.SQUARE,
            Color.BLUE, Color.RED, 100).getImage();
        BufferedImage round = OverheadsPrayerIconStyle.create(source, OverheadsIconShape.ROUND,
            Color.GREEN, Color.RED, 100).getImage();
        assertEquals(Color.BLUE.getRGB(), square.getRGB(2, 2));
        assertEquals(0, round.getRGB(0, 0) >>> 24);
        assertEquals(Color.WHITE.getRGB(), original.getRGB(5, 5));
        assertEquals(0, original.getRGB(2, 2));
        assertTrue(OverheadsPrayerIconStyle.create(source, OverheadsIconShape.SQUARE,
            Color.BLUE, Color.RED, 200).getWidth() > square.getWidth());
    }

    @Test
    public void allHealthDisplayModesRenderAndZeroBarOpacityKeepsNumbers()
    {
        Fixture fixture = new Fixture();
        for (OverheadsBarDisplay display : OverheadsBarDisplay.values())
        {
            fixture.config.display = display;
            assertEquals(display != OverheadsBarDisplay.HIDDEN, pixels(fixture.render()) > 0);
        }
        fixture.config.opacity = 0;
        fixture.config.display = OverheadsBarDisplay.BAR_ONLY;
        assertEquals(0, pixels(fixture.render()));
        fixture.config.display = OverheadsBarDisplay.BOTH;
        assertTrue(pixels(fixture.render()) > 0);
        fixture.config.display = OverheadsBarDisplay.NUMBERS_ONLY;
        assertTrue(pixels(fixture.render()) > 0);
    }

    @Test
    public void prayerCanRenderWhenHealthIsHiddenAndReactsToActivePrayer()
    {
        Fixture fixture = new Fixture();
        fixture.config.health = false;
        fixture.config.prayer = OverheadsPrayerVisibility.ACTIVE;
        assertEquals(0, pixels(fixture.render()));
        when(fixture.client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(true);
        assertTrue(pixels(fixture.render()) > 0);
        when(fixture.client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(false);
        assertEquals(0, pixels(fixture.render()));
        fixture.config.prayer = OverheadsPrayerVisibility.ALWAYS;
        for (OverheadsBarDisplay display : OverheadsBarDisplay.values())
        {
            fixture.config.prayerDisplay = display;
            assertEquals(display != OverheadsBarDisplay.HIDDEN, pixels(fixture.render()) > 0);
        }
    }

    @Test
    public void verticalAndHorizontalBarsHonorEverySide()
    {
        Fixture fixture = new Fixture();
        fixture.config.display = OverheadsBarDisplay.BAR_ONLY;
        for (OverheadsPosition position : OverheadsPosition.values())
        {
            fixture.config.position = position;
            fixture.config.orientation = OverheadsOrientation.HORIZONTAL;
            Rectangle horizontal = pixelBounds(fixture.render());
            fixture.config.orientation = OverheadsOrientation.VERTICAL;
            Rectangle vertical = pixelBounds(fixture.render());
            assertTrue(horizontal.width > horizontal.height);
            assertTrue(vertical.height > vertical.width);
            if (position == OverheadsPosition.LEFT) { assertTrue(vertical.getMaxX() < 200); }
            if (position == OverheadsPosition.RIGHT) { assertTrue(vertical.x > 200); }
        }
    }

    private static int pixels(BufferedImage image)
    {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                if ((image.getRGB(x, y) >>> 24) != 0) { count++; }
            }
        }
        return count;
    }

    private static Rectangle pixelBounds(BufferedImage image)
    {
        Rectangle bounds = null;
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                if ((image.getRGB(x, y) >>> 24) != 0)
                {
                    if (bounds == null) { bounds = new Rectangle(x, y, 1, 1); }
                    else { bounds.add(x, y); }
                }
            }
        }
        assertNotNull(bounds);
        return bounds;
    }

    static class Fixture
    {
        final Client client = mock(Client.class);
        final Player player = mock(Player.class);
        final Settings config = new Settings();
        final OverheadsHealthBarRenderer renderer = new OverheadsHealthBarRenderer(client, config);

        Fixture()
        {
            when(client.getLocalPlayer()).thenReturn(player);
            when(client.getRealSkillLevel(Skill.HITPOINTS)).thenReturn(99);
            when(client.getBoostedSkillLevel(Skill.HITPOINTS)).thenReturn(73);
            when(client.getRealSkillLevel(Skill.PRAYER)).thenReturn(99);
            when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(62);
            when(player.getHealthRatio()).thenReturn(22);
            when(player.getHealthScale()).thenReturn(30);
            when(player.getLogicalHeight()).thenReturn(100);
            when(player.getCanvasImageLocation(any(), anyInt())).thenAnswer(call ->
                new Point(200, 200 - (Integer) call.getArgument(1)));
        }

        BufferedImage render()
        {
            BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            renderer.render(graphics, player, new Point(200, 100), 100);
            graphics.dispose();
            return image;
        }
    }

    static class Settings implements OverheadsConfig
    {
        OverheadsBarDisplay display = OverheadsBarDisplay.BOTH;
        OverheadsBarDisplay prayerDisplay = OverheadsBarDisplay.BOTH;
        OverheadsPosition position = OverheadsPosition.ABOVE;
        OverheadsOrientation orientation = OverheadsOrientation.HORIZONTAL;
        OverheadsPrayerVisibility prayer = OverheadsPrayerVisibility.NEVER;
        int opacity = 100;
        boolean health = true;
        @Override public OverheadsBarDisplay selfHealthDisplay() { return display; }
        @Override public OverheadsBarDisplay prayerDisplay() { return prayerDisplay; }
        @Override public OverheadsHealthPosition healthPosition() { return OverheadsHealthPosition.valueOf(position.name()); }
        @Override public OverheadsOrientation healthOrientation() { return orientation; }
        @Override public OverheadsPrayerVisibility prayerVisibility() { return prayer; }
        @Override public int healthBarOpacity() { return opacity; }
        @Override public boolean healthBarsEnabled() { return health; }
    }
}
