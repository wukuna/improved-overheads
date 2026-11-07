package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.client.config.FontType;
import org.junit.Test;

public class OverheadsOverheadChatRendererTest
{
	@Test
	public void formattingIsRemovedWithoutLosingPrintableJagexCharacters()
	{
		assertEquals("<3 > 2",
			OverheadsOverheadChatRenderer.displayText("<lt>3 <gt> 2"));
		assertEquals("<tag>",
			OverheadsOverheadChatRenderer.displayText("<col=ff0000><lt>tag<gt></col>"));
		assertEquals("<col=ff0000>",
			OverheadsOverheadChatRenderer.displayText("<lt>col=ff0000<gt>"));
		assertEquals("CafÃ© â˜… & 'quotes'",
			OverheadsOverheadChatRenderer.displayText("CafÃ© â˜… & 'quotes'"));
		assertEquals("Meet @ bank",
			OverheadsOverheadChatRenderer.displayText("Meet <at> bank"));
		assertEquals("Literal <at> stays literal",
			OverheadsOverheadChatRenderer.displayText("Literal <lt>at<gt> stays literal"));
		assertEquals("non-breaking-hyphen",
			OverheadsOverheadChatRenderer.displayText("non<nbh>breaking<nbh>hyphen"));
	}

	@Test
	public void capturesEveryConfiguredStyleOnceForTheFrame()
	{
		Color textColor = new Color(10, 20, 30, 40);
		Color shadowColor = new Color(50, 60, 70, 80);
		FontType font = new FontType()
			.withFamily("Dialog")
			.withSize(19)
			.withBold(true)
			.withItalic(true);
		OverheadsConfig config = new OverheadsConfig()
		{
			@Override
			public boolean showNpcOverheadChat()
			{
				return false;
			}

			@Override
			public FontType overheadChatFont()
			{
				return font;
			}

			@Override
			public Color overheadChatColor()
			{
				return textColor;
			}

			@Override
			public boolean overheadChatShadow()
			{
				return false;
			}

			@Override
			public Color overheadChatShadowColor()
			{
				return shadowColor;
			}

			@Override
			public OverheadsOverheadChatEffect overheadChatEffect()
			{
				return OverheadsOverheadChatEffect.SHAKE;
			}

			@Override
			public int overheadChatXOffset()
			{
				return -7;
			}

			@Override
			public int overheadChatYOffset()
			{
				return 9;
			}
		};

		OverheadsOverheadChatRenderer.Style style =
			OverheadsOverheadChatRenderer.captureStyle(config);
		assertTrue(style.shouldRender(false));
		assertFalse(style.shouldRender(true));
		assertEquals(19, style.getFont().getSize());
		assertTrue(style.getFont().isItalic());
		assertSame(textColor, style.getColor());
		assertFalse(style.hasShadow());
		assertSame(shadowColor, style.getShadowColor());
		assertEquals(OverheadsOverheadChatEffect.SHAKE, style.getFallbackEffect());
		assertEquals(-7, style.getXOffset());
		assertEquals(9, style.getYOffset());
	}

	@Test
	public void reservesTheChatLaneOnlyForVisibleMessages()
	{
		OverheadsLocalChatEffectTracker tracker =
			new OverheadsLocalChatEffectTracker();
		Object actor = new Object();
		OverheadsOverheadChatRenderer.Style style =
			OverheadsOverheadChatRenderer.captureStyle(new OverheadsConfig()
			{
			});

		assertTrue(OverheadsOverheadChatRenderer.hasRenderableMessage(
			null,
			tracker.messageStateFor(actor, "Hello", 100, 0, 150),
			style,
			Color.YELLOW));
		assertFalse(OverheadsOverheadChatRenderer.hasRenderableMessage(
			null,
			tracker.messageStateFor(actor, "Hello", 0, 0, 150),
			style,
			Color.YELLOW));
		assertFalse(OverheadsOverheadChatRenderer.hasRenderableMessage(
			null,
			tracker.messageStateFor(actor, null, 100, 0, 150),
			style,
			Color.YELLOW));
		assertFalse(OverheadsOverheadChatRenderer.hasRenderableMessage(
			null,
			tracker.messageStateFor(actor, "<col=ff0000></col>", 100, 0, 150),
			style,
			Color.YELLOW));

		OverheadsOverheadChatRenderer.Style invisibleStyle =
			OverheadsOverheadChatRenderer.captureStyle(new OverheadsConfig()
			{
				@Override
				public boolean overheadChatShadow()
				{
					return false;
				}
			});
		assertFalse(OverheadsOverheadChatRenderer.hasRenderableMessage(
			null,
			tracker.messageStateFor(actor, "Hello", 100, 0, 150),
			invisibleStyle,
			new Color(0, 0, 0, 0)));
	}

	@Test
	public void relationshipColorsUseGroupIronThenFriendThenClanPrecedence()
	{
		Color fallback = new Color(1, 2, 3, 4);
		Color friend = new Color(0xFFA5FF40, true);
		Color clan = new Color(0xFF40CFFF, true);
		Color groupIron = new Color(0xFFFF4040, true);
		OverheadsConfig config = new OverheadsConfig()
		{
			@Override
			public Color overheadChatColor()
			{
				return fallback;
			}

			@Override
			public boolean overheadChatRelationshipColors()
			{
				return true;
			}

			@Override
			public Color overheadChatFriendColor()
			{
				return friend;
			}

			@Override
			public Color overheadChatClanColor()
			{
				return clan;
			}

			@Override
			public Color overheadChatGroupIronColor()
			{
				return groupIron;
			}
		};
		OverheadsOverheadChatRenderer.Style style =
			OverheadsOverheadChatRenderer.captureStyle(config);

		assertSame(groupIron,
			OverheadsOverheadChatRenderer.relationshipColor(style, true, true, true));
		assertSame(friend,
			OverheadsOverheadChatRenderer.relationshipColor(style, false, true, true));
		assertSame(clan,
			OverheadsOverheadChatRenderer.relationshipColor(style, false, false, true));
		assertSame(fallback,
			OverheadsOverheadChatRenderer.relationshipColor(style, false, false, false));

		OverheadsOverheadChatRenderer.Style disabled =
			OverheadsOverheadChatRenderer.captureStyle(new OverheadsConfig()
			{
				@Override
				public Color overheadChatColor()
				{
					return fallback;
				}

				@Override
				public boolean overheadChatRelationshipColors()
				{
					return false;
				}
			});
		assertSame(fallback,
			OverheadsOverheadChatRenderer.relationshipColor(disabled, true, true, true));
	}

	@Test
	public void rgbaTextAndShadowReachThePixelRenderer()
	{
		Color textColor = new Color(220, 10, 20, 128);
		OverheadsConfig textConfig = new OverheadsConfig()
		{
			@Override
			public Color overheadChatColor()
			{
				return textColor;
			}

			@Override
			public boolean overheadChatShadow()
			{
				return false;
			}
		};
		BufferedImage textImage = drawStaticText(
			OverheadsOverheadChatRenderer.captureStyle(textConfig),
			textColor);
		int textPixel = mostOpaquePixel(textImage);
		assertTrue((textPixel >>> 24) > 0 && (textPixel >>> 24) <= 128);
		assertTrue((textPixel >> 16 & 0xFF) > (textPixel & 0xFF));

		Color shadowColor = new Color(5, 30, 210, 96);
		OverheadsConfig shadowConfig = new OverheadsConfig()
		{
			@Override
			public Color overheadChatColor()
			{
				return new Color(0, 0, 0, 0);
			}

			@Override
			public Color overheadChatShadowColor()
			{
				return shadowColor;
			}
		};
		OverheadsOverheadChatRenderer.Style shadowStyle =
			OverheadsOverheadChatRenderer.captureStyle(shadowConfig);
		BufferedImage shadowImage = drawStaticText(shadowStyle, shadowStyle.getColor());
		int shadowPixel = mostOpaquePixel(shadowImage);
		assertTrue((shadowPixel >>> 24) > 0 && (shadowPixel >>> 24) <= 96);
		assertTrue((shadowPixel & 0xFF) > (shadowPixel >> 16 & 0xFF));
	}

	@Test
	public void stretchedPatternColorsReachTheMovingGlyphRenderer()
	{
		OverheadsLocalChatEffectTracker tracker =
			new OverheadsLocalChatEffectTracker();
		Object local = new Object();
		tracker.recordOutgoing("pattern15:wave:MM", 0, false, 100);
		tracker.recordOverhead(local, local, "MM", 101);
		Color colors = tracker.colorFor(local, "MM", Color.YELLOW, 101);
		assertTrue(colors instanceof OverheadsLocalChatEffectTracker.PerGlyphColor);

		OverheadsConfig config = new OverheadsConfig()
		{
			@Override
			public boolean overheadChatShadow()
			{
				return false;
			}
		};
		OverheadsOverheadChatRenderer.Style style =
			OverheadsOverheadChatRenderer.captureStyle(config);
		BufferedImage image = drawText(
			style,
			colors,
			"MM",
			OverheadsOverheadChatEffect.WAVE);
		assertTrue(containsRgb(image, 0xE40303));
		assertTrue(containsRgb(image, 0x24408E));
	}

	@Test
	public void anchorsTextBelowHealthAndIconsWithLocalUserOffsets()
	{
		assertEquals(
			110,
			OverheadsOverheadChatRenderer.chatLaneTop(
				120,
				12,
				OverheadsOverheadChatEffect.STATIC));
		assertEquals(
			122,
			OverheadsOverheadChatRenderer.chatBaseline(
				120,
				0));
		assertEquals(
			127,
			OverheadsOverheadChatRenderer.chatBaseline(
				120,
				-5));
	}

	@Test
	public void animatedTextKeepsItsTopInsideTheReservedChatLane()
	{
		int actorTopY = 120;
		int ascent = 12;
		for (OverheadsOverheadChatEffect effect : OverheadsOverheadChatEffect.values())
		{
			int laneTop = OverheadsOverheadChatRenderer.chatLaneTop(
				actorTopY,
				ascent,
				effect);
			int baseline = OverheadsOverheadChatRenderer.chatBaseline(
				actorTopY,
				0);
			assertEquals(
				laneTop,
				OverheadsOverheadChatRenderer.effectBounds(
					10,
					baseline,
					40,
					ascent,
					3,
					effect).y);
		}
	}

	@Test
	public void configuredOffsetsStayConstantAcrossProjectedActorPositions()
	{
		assertChatOffset(210, 380);
		assertChatOffset(470, 125);
	}

	@Test
	public void shiftsOverlappingChatAboveExistingText()
	{
		int baseline = OverheadsOverheadChatRenderer.collisionFreeBaseline(
			10,
			100,
			50,
			10,
			3,
			Arrays.asList(new Rectangle(0, 88, 80, 16)));

		assertEquals(82, baseline);
		assertEquals(
			100,
			OverheadsOverheadChatRenderer.collisionFreeBaseline(
				100,
				100,
				20,
				10,
				3,
				Arrays.asList(new Rectangle(0, 88, 20, 16))));
	}

	@Test
	public void collisionShiftJumpsOverTheActorsOwnUiStack()
	{
		int baseline = OverheadsOverheadChatRenderer.collisionFreeBaseline(
			10,
			122,
			50,
			10,
			3,
			OverheadsOverheadChatEffect.STATIC,
			80,
			110,
			Arrays.asList(new Rectangle(0, 105, 80, 10)));
		Rectangle bounds = OverheadsOverheadChatRenderer.effectBounds(
			10,
			baseline,
			50,
			10,
			3,
			OverheadsOverheadChatEffect.STATIC);

		assertEquals(74, baseline);
		assertTrue(bounds.y + bounds.height <= 78);
	}

	@Test
	public void collisionShiftClearsTheOwnUiStackForEveryEffect()
	{
		for (OverheadsOverheadChatEffect effect : OverheadsOverheadChatEffect.values())
		{
			Rectangle preferredBounds = OverheadsOverheadChatRenderer.effectBounds(
				10,
				200,
				50,
				10,
				3,
				effect);
			Rectangle blocker = new Rectangle(
				preferredBounds.x,
				preferredBounds.y,
				preferredBounds.width,
				Math.max(1, preferredBounds.height / 2));
			int baseline = OverheadsOverheadChatRenderer.collisionFreeBaseline(
				10,
				200,
				50,
				10,
				3,
				effect,
				130,
				190,
				Arrays.asList(blocker));
			Rectangle resolvedBounds = OverheadsOverheadChatRenderer.effectBounds(
				10,
				baseline,
				50,
				10,
				3,
				effect);

			assertTrue(
				effect.name(),
				resolvedBounds.y + resolvedBounds.height <= 128);
		}
	}

	@Test
	public void ownUiCollisionGuardTranslatesWithTheProjectedStack()
	{
		int baseline = OverheadsOverheadChatRenderer.collisionFreeBaseline(
			10,
			122,
			50,
			10,
			3,
			OverheadsOverheadChatEffect.WAVE,
			80,
			110,
			Arrays.asList(new Rectangle(0, 105, 80, 10)));
		int translated = OverheadsOverheadChatRenderer.collisionFreeBaseline(
			10,
			422,
			50,
			10,
			3,
			OverheadsOverheadChatEffect.WAVE,
			380,
			410,
			Arrays.asList(new Rectangle(0, 405, 80, 10)));

		assertEquals(300, translated - baseline);
	}

	@Test
	public void collisionShiftDoesNotReserveAnAbsentUiStack()
	{
		assertEquals(
			99,
			OverheadsOverheadChatRenderer.collisionFreeBaseline(
				10,
				122,
				50,
				10,
				3,
				OverheadsOverheadChatEffect.STATIC,
				OverheadsHealthBarRenderer.NO_OCCUPIED_TOP,
				110,
				Arrays.asList(new Rectangle(0, 105, 80, 10))));
	}

	@Test
	public void ordersCollisionBoundsAndHandlesAnUnsortedCallerDefensively()
	{
		List<Rectangle> ordered = new ArrayList<>();
		OverheadsOverheadChatRenderer.addOccupiedBounds(
			ordered,
			new Rectangle(0, 72, 80, 16));
		OverheadsOverheadChatRenderer.addOccupiedBounds(
			ordered,
			new Rectangle(0, 100, 80, 16));
		OverheadsOverheadChatRenderer.addOccupiedBounds(
			ordered,
			new Rectangle(0, 88, 80, 16));
		assertEquals(100, ordered.get(0).y);
		assertEquals(88, ordered.get(1).y);
		assertEquals(72, ordered.get(2).y);

		assertEquals(
			66,
			OverheadsOverheadChatRenderer.collisionFreeBaseline(
				10,
				100,
				50,
				10,
				3,
				Arrays.asList(
					new Rectangle(0, 72, 80, 16),
					new Rectangle(0, 88, 80, 16))));
	}

	@Test
	public void reservesTheFullAnimatedEnvelopeForCollisionChecks()
	{
		assertEquals(
			new Rectangle(5, 85, 61, 24),
			OverheadsOverheadChatRenderer.effectBounds(
				10,
				100,
				50,
				10,
				3,
				OverheadsOverheadChatEffect.WAVE_2));
		assertEquals(
			new Rectangle(-15, 90, 101, 14),
			OverheadsOverheadChatRenderer.effectBounds(
				10,
				100,
				50,
				10,
				3,
				OverheadsOverheadChatEffect.SCROLL));

		int baseline = OverheadsOverheadChatRenderer.collisionFreeBaseline(
			10,
			100,
			50,
			10,
			3,
			OverheadsOverheadChatEffect.WAVE,
			Arrays.asList(new Rectangle(0, 88, 80, 16)));
		assertEquals(77, baseline);
	}

	@Test
	public void animatedOffsetsAreDeterministicAndBounded()
	{
		int waveX = OverheadsOverheadChatRenderer.effectXOffset(
			OverheadsOverheadChatEffect.WAVE_2,
			4,
			1234);
		int waveY = OverheadsOverheadChatRenderer.effectYOffset(
			OverheadsOverheadChatEffect.WAVE_2,
			4,
			1234,
			100);

		assertEquals(
			waveX,
			OverheadsOverheadChatRenderer.effectXOffset(
				OverheadsOverheadChatEffect.WAVE_2,
				4,
				1234));
		assertEquals(
			waveY,
			OverheadsOverheadChatRenderer.effectYOffset(
				OverheadsOverheadChatEffect.WAVE_2,
				4,
				1234,
				100));
		assertTrue(Math.abs(waveX) <= 5);
		assertTrue(Math.abs(waveY) <= 5);
		assertEquals(7, OverheadsOverheadChatRenderer.shakeAmplitude(100));
		assertEquals(0, OverheadsOverheadChatRenderer.shakeAmplitude(44));
	}

	@Test
	public void animatedPhaseDoesNotJumpAtTheFormerModuloBoundary()
	{
		int before = OverheadsOverheadChatRenderer.effectYOffset(
			OverheadsOverheadChatEffect.WAVE,
			0,
			9999,
			100);
		int after = OverheadsOverheadChatRenderer.effectYOffset(
			OverheadsOverheadChatEffect.WAVE,
			0,
			10000,
			100);

		assertTrue(Math.abs(after - before) <= 2);
	}

	@Test
	public void clippedEffectsTraverseTheirWindowsOverTheMessageLifetime()
	{
		assertEquals(170, OverheadsOverheadChatRenderer.scrollTextX(100, 40, 100));
		assertEquals(30, OverheadsOverheadChatRenderer.scrollTextX(100, 40, 0));
		assertEquals(-20, OverheadsOverheadChatRenderer.slideYOffset(100));
		assertEquals(0, OverheadsOverheadChatRenderer.slideYOffset(80));
		assertEquals(0, OverheadsOverheadChatRenderer.slideYOffset(20));
		assertEquals(20, OverheadsOverheadChatRenderer.slideYOffset(0));
	}

	@Test
	public void playerEffectsUseTheFullOneHundredFiftyCycleLifetime()
	{
		assertEquals(
			170,
			OverheadsOverheadChatRenderer.scrollTextX(100, 40, 150, 150));
		assertEquals(
			124,
			OverheadsOverheadChatRenderer.scrollTextX(100, 40, 100, 150));
		assertEquals(
			30,
			OverheadsOverheadChatRenderer.scrollTextX(100, 40, 0, 150));
		assertEquals(
			-20,
			OverheadsOverheadChatRenderer.slideYOffset(150, 150));
		assertEquals(
			0,
			OverheadsOverheadChatRenderer.slideYOffset(100, 150));
		assertEquals(
			20,
			OverheadsOverheadChatRenderer.slideYOffset(0, 150));
		assertEquals(
			7,
			OverheadsOverheadChatRenderer.shakeAmplitude(150, 150));
		assertEquals(
			0,
			OverheadsOverheadChatRenderer.shakeAmplitude(94, 150));
	}

	@Test
	public void effectsHaveReadableLabels()
	{
		assertEquals("Static", OverheadsOverheadChatEffect.STATIC.toString());
		assertEquals("Wave", OverheadsOverheadChatEffect.WAVE.toString());
		assertEquals("Wave 2", OverheadsOverheadChatEffect.WAVE_2.toString());
		assertEquals("Shake", OverheadsOverheadChatEffect.SHAKE.toString());
		assertEquals("Scroll", OverheadsOverheadChatEffect.SCROLL.toString());
		assertEquals("Slide", OverheadsOverheadChatEffect.SLIDE.toString());
	}

	private static void assertChatOffset(int anchorX, int anchorY)
	{
		assertEquals(
			10,
			OverheadsOverheadChatRenderer.chatLeft(anchorX, 40, 10)
				- OverheadsOverheadChatRenderer.chatLeft(anchorX, 40, 0));
		assertEquals(
			-10,
			OverheadsOverheadChatRenderer.chatBaseline(
				anchorY,
				10)
				- OverheadsOverheadChatRenderer.chatBaseline(
					anchorY,
					0));
		assertEquals(
			10,
			anchorY - OverheadsOverheadChatRenderer.chatLaneTop(
				anchorY,
				12,
				OverheadsOverheadChatEffect.STATIC));
	}

	private static BufferedImage drawStaticText(
		OverheadsOverheadChatRenderer.Style style,
		Color textColor)
	{
		return drawText(
			style,
			textColor,
			"M",
			OverheadsOverheadChatEffect.STATIC);
	}

	private static BufferedImage drawText(
		OverheadsOverheadChatRenderer.Style style,
		Color textColor,
		String text,
		OverheadsOverheadChatEffect effect)
	{
		BufferedImage image = new BufferedImage(80, 40, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			graphics.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setFont(style.getFont());
			FontMetrics metrics = graphics.getFontMetrics();
			GlyphVector glyphs = OverheadsOverheadChatRenderer.createAnimatedGlyphs(
				graphics,
				text,
				effect,
				123,
				100,
				150);
			OverheadsOverheadChatRenderer.drawStyledEffect(
				graphics,
				text,
				8,
				28,
				metrics.stringWidth(text),
				metrics,
				effect,
				glyphs,
				100,
				100,
				style,
				textColor);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	private static boolean containsRgb(BufferedImage image, int expectedRgb)
	{
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int pixel = image.getRGB(x, y);
				if ((pixel >>> 24) != 0 && (pixel & 0x00FFFFFF) == expectedRgb)
				{
					return true;
				}
			}
		}
		return false;
	}

	private static int mostOpaquePixel(BufferedImage image)
	{
		int mostOpaque = 0;
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int pixel = image.getRGB(x, y);
				if ((pixel >>> 24) > (mostOpaque >>> 24))
				{
					mostOpaque = pixel;
				}
			}
		}
		return mostOpaque;
	}
}
