package com.improvedoverheads;

import static org.junit.Assert.assertEquals;

import net.runelite.api.gameval.SpriteID;
import org.junit.Test;

public class OverheadsHeadIconRendererTest
{
	@Test
	public void scalesIconsAcrossDynamicRenderRange()
	{
		assertEquals(2, OverheadsHeadIconRenderer.scaled(24, 10));
		assertEquals(24, OverheadsHeadIconRenderer.scaled(24, 100));
		assertEquals(96, OverheadsHeadIconRenderer.scaled(24, 500));
	}

	@Test
	public void perspectiveMagnificationShrinksAndEnlargesConfiguredScale()
	{
		assertEquals(50, OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 384, 2048.0f));
		assertEquals(100, OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 384, 1024.0f));
		assertEquals(200, OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 384, 512.0f));
		assertEquals(150, OverheadsHeadIconRenderer.effectiveScalePercent(
			150, true, 384, 1024.0f));
	}

	@Test
	public void extendedOuterZoomCanShrinkBelowConfiguredScaleFloor()
	{
		assertEquals(25, OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 384, 4096.0f));
	}

	@Test
	public void disabledZoomScalingPreservesConfiguredPixelScale()
	{
		assertEquals(50, OverheadsHeadIconRenderer.effectiveScalePercent(
			10, false, 384, 512.0f));
		assertEquals(125, OverheadsHeadIconRenderer.effectiveScalePercent(
			125, false, 384, 4096.0f));
		assertEquals(200, OverheadsHeadIconRenderer.effectiveScalePercent(
			500, false, 384, 512.0f));
	}

	@Test
	public void invalidProjectionFallsBackToConfiguredPixelScale()
	{
		assertEquals(120, OverheadsHeadIconRenderer.effectiveScalePercent(
			120, true, 0, 1024.0f));
		assertEquals(120, OverheadsHeadIconRenderer.effectiveScalePercent(
			120, true, 384, Float.NaN));
		assertEquals(120, OverheadsHeadIconRenderer.effectiveScalePercent(
			120, true, 384, Float.POSITIVE_INFINITY));
		assertEquals(120, OverheadsHeadIconRenderer.effectiveScalePercent(
			120, true, 384, 49.0f));
	}

	@Test
	public void zoomScalingUsesSafeRenderBounds()
	{
		assertEquals(10, OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 1, Float.MAX_VALUE));
		assertEquals(400, OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 10_000, 50.0f));
	}

	@Test
	public void stacksAboveEarlierActorUiBeforeApplyingUserOffset()
	{
		assertEquals(
			118,
			OverheadsHeadIconRenderer.stackBottomY(
				120,
				OverheadsHealthBarRenderer.NO_OCCUPIED_TOP,
				0,
				2));
		assertEquals(98, OverheadsHeadIconRenderer.stackBottomY(120, 100, 0, 2));
		assertEquals(108, OverheadsHeadIconRenderer.stackBottomY(120, 100, -10, 2));
	}

	@Test
	public void configuredOffsetsStayConstantAcrossProjectedActorPositions()
	{
		assertHeadIconOffset(210, 380);
		assertHeadIconOffset(470, 125);
	}

	@Test
	public void spacingMovesTheFirstIconAwayFromContentBelowIt()
	{
		assertEquals(
			100,
			OverheadsHeadIconRenderer.stackBottomY(
				120,
				100,
				0,
				0));
		assertEquals(
			92,
			OverheadsHeadIconRenderer.stackBottomY(
				120,
				100,
				0,
				8));
	}

	@Test
	public void fractionalScalingKeepsCroppedSpriteInsideItsCanvas()
	{
		int canvasWidth = OverheadsHeadIconRenderer.scaled(3, 150);
		int imageX = OverheadsHeadIconRenderer.scaledEdge(1, 150);
		int imageWidth = OverheadsHeadIconRenderer.scaledSpan(1, 2, 150);

		assertEquals(4, canvasWidth);
		assertEquals(1, imageX);
		assertEquals(3, imageWidth);
		assertEquals(canvasWidth, imageX + imageWidth);
	}

	@Test
	public void zoomedOutScalingKeepsCroppedSpriteInsideItsCanvas()
	{
		int percent = OverheadsHeadIconRenderer.effectiveScalePercent(
			100, true, 384, 4096.0f);
		int canvasWidth = OverheadsHeadIconRenderer.scaled(3, percent);
		int imageX = OverheadsHeadIconRenderer.scaledEdge(1, percent);
		int imageWidth = OverheadsHeadIconRenderer.scaledSpan(1, 2, percent);

		assertEquals(25, percent);
		assertEquals(1, canvasWidth);
		assertEquals(0, imageX);
		assertEquals(1, imageWidth);
		assertEquals(canvasWidth, imageX + imageWidth);
	}

	@Test
	public void spacingSeparatesEachFollowingIcon()
	{
		assertEquals(80, OverheadsHeadIconRenderer.nextIconBottomY(80, 0));
		assertEquals(72, OverheadsHeadIconRenderer.nextIconBottomY(80, 8));
		assertEquals(60, OverheadsHeadIconRenderer.nextIconBottomY(80, 99));
	}

	@Test
	public void mapsStandardOverheadPrayersToActiveResourcePackSprites()
	{
		assertEquals(SpriteID.Prayeron.PROTECT_FROM_MELEE,
			OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(0));
		assertEquals(SpriteID.Prayeron.PROTECT_FROM_MISSILES,
			OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(1));
		assertEquals(SpriteID.Prayeron.PROTECT_FROM_MAGIC,
			OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(2));
		assertEquals(SpriteID.Prayeron.RETRIBUTION,
			OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(3));
		assertEquals(SpriteID.Prayeron.SMITE,
			OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(4));
		assertEquals(SpriteID.Prayeron.REDEMPTION,
			OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(5));
		assertEquals(-1, OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(-1));
		assertEquals(-1, OverheadsHeadIconRenderer.resourcePackPrayerSpriteId(6));
	}

	private static void assertHeadIconOffset(int anchorX, int anchorY)
	{
		assertEquals(
			10,
			OverheadsHeadIconRenderer.iconLeft(anchorX, 24, 10)
				- OverheadsHeadIconRenderer.iconLeft(anchorX, 24, 0));
		assertEquals(
			-10,
			OverheadsHeadIconRenderer.stackBottomY(
				anchorY,
				OverheadsHealthBarRenderer.NO_OCCUPIED_TOP,
				10,
				2)
				- OverheadsHeadIconRenderer.stackBottomY(
					anchorY,
					OverheadsHealthBarRenderer.NO_OCCUPIED_TOP,
					0,
					2));
		assertEquals(
			-10,
			OverheadsHeadIconRenderer.stackBottomY(
				anchorY,
				anchorY - 20,
				10,
				2)
				- OverheadsHeadIconRenderer.stackBottomY(
					anchorY,
					anchorY - 20,
					0,
					2));
	}
}
