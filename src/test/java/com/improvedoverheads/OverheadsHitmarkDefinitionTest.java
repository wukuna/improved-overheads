package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class OverheadsHitmarkDefinitionTest
{
	@Test
	public void decodesSpritesTextColorTemplateAndYOffset()
	{
		byte[] data = {
			2, 0x12, 0x34, 0x56,
			3, 0x01, 0x41,
			4, 0x01, 0x42,
			5, 0x01, 0x43,
			6, 0x01, 0x44,
			8, 0, 'H', 'i', 't', ' ', '%', '1', '!', 0,
			12, 1,
			13, (byte) 0xFF, (byte) 0xFB,
			0
		};

		OverheadsHitmarkDefinition definition = OverheadsHitmarkDefinition.decode(data, 0);

		assertEquals(321, definition.getFirstSpriteId());
		assertEquals(322, definition.getSecondSpriteId());
		assertEquals(323, definition.getMiddleSpriteId());
		assertEquals(324, definition.getLastSpriteId());
		assertEquals(0x123456, definition.getTextColor());
		assertEquals(-5, definition.getTextYOffset());
		assertEquals("Hit 27!", definition.formatAmount(27));
		assertFalse(definition.hasTransforms());
	}

	@Test
	public void opcode17PreservesSelectedMinusOneTransform()
	{
		byte[] data = {
			17,
			0, 7,
			(byte) 0xFF, (byte) 0xFF,
			1,
			0, 100,
			(byte) 0xFF, (byte) 0xFF,
			0
		};
		OverheadsHitmarkDefinition definition = OverheadsHitmarkDefinition.decode(data, 0);

		assertTrue(definition.hasTransforms());
		assertEquals(100, definition.getTransformedTypeForSelector(0));
		assertEquals(-1, definition.getTransformedTypeForSelector(1));
		assertEquals(-1, definition.getTransformedTypeForSelector(99));
		assertEquals(
			OverheadsHitmarkDefinition.TRANSFORM_UNAVAILABLE,
			definition.getTransformedType(null));
	}

	@Test
	public void opcode18UsesItsExplicitFallbackTransform()
	{
		byte[] data = {
			18,
			(byte) 0xFF, (byte) 0xFF,
			0, 9,
			0, 44,
			0,
			0, 55,
			0
		};
		OverheadsHitmarkDefinition definition = OverheadsHitmarkDefinition.decode(data, 0);

		assertEquals(55, definition.getTransformedTypeForSelector(0));
		assertEquals(44, definition.getTransformedTypeForSelector(1));
	}

	@Test
	public void opcode9DecodesDuration()
	{
		OverheadsHitmarkDefinition definition =
			OverheadsHitmarkDefinition.decode(new byte[]{9, 0x01, 0x2C, 0}, 0);

		assertEquals(300, definition.getDuration());
		assertEquals(700, definition.appearanceGameCycle(1000));
		assertEquals(700, OverheadsOverlay.nativeAppearanceGameCycle(definition, 1000, 900));
	}

	@Test
	public void cacheFailureUsesEventTimingSoReplacementRemainsVisible()
	{
		OverheadsHitmarkDefinition fallback =
			OverheadsHitmarkDefinition.decode(null, 0);

		assertTrue(fallback.isFallbackDefinition());
		assertEquals(900, OverheadsOverlay.nativeAppearanceGameCycle(fallback, 1000, 900));
		assertEquals(900, OverheadsOverlay.nativeAppearanceGameCycle(null, 1000, 900));
	}

	@Test
	public void validSpriteLessDefinitionStaysEmpty()
	{
		OverheadsHitmarkDefinition definition = OverheadsHitmarkDefinition.decode(new byte[]{0}, 0);

		assertEquals(-1, definition.getFirstSpriteId());
		assertEquals(-1, definition.getMiddleSpriteId());
		assertEquals(-1, definition.getSecondSpriteId());
		assertEquals(-1, definition.getLastSpriteId());
		assertEquals("", definition.formatAmount(27));
		assertEquals(70, definition.getDuration());
		assertFalse(definition.hasRenderableContent());
		assertFalse(definition.isFallbackDefinition());
	}

	@Test
	public void resolvesNestedTransformsAndPreservesHiddenTarget()
	{
		OverheadsHitmarkDefinition first = transformFallback(2);
		OverheadsHitmarkDefinition second = transformFallback(3);
		OverheadsHitmarkDefinition finalDefinition =
			OverheadsHitmarkDefinition.decode(new byte[]{8, 0, '%', '1', 0, 0}, 3);
		Map<Integer, OverheadsHitmarkDefinition> definitions = new HashMap<>();
		definitions.put(1, first);
		definitions.put(2, second);
		definitions.put(3, finalDefinition);

		assertSame(
			finalDefinition,
			OverheadsOverlay.resolveDefinition(1, null, definitions::get));
		assertNull(OverheadsOverlay.resolveDefinition(
			4,
			null,
			ignored -> transformFallback(-1)));
	}

	@Test
	public void acceptsNativeNoOpOpcodes()
	{
		OverheadsHitmarkDefinition definition =
			OverheadsHitmarkDefinition.decode(new byte[]{15, 16, 0}, 0);

		assertFalse(definition.isFallbackDefinition());
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsUnknownOpcodes()
	{
		OverheadsHitmarkDefinition.decode(new byte[]{99, 0}, 0);
	}

	private static OverheadsHitmarkDefinition transformFallback(int target)
	{
		int encodedTarget = target < 0 ? 65535 : target;
		return OverheadsHitmarkDefinition.decode(new byte[]{
			18,
			(byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF,
			(byte) (encodedTarget >>> 8), (byte) encodedTarget,
			0,
			(byte) 0xFF, (byte) 0xFF,
			0
		}, 0);
	}
}
