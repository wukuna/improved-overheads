package com.improvedoverheads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OverheadsTargetDetectionTest
{
	@Test
	public void footprintModeUsesOnlyTheConfiguredFootprintThreshold()
	{
		assertFalse(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.FOOTPRINT,
			1,
			200,
			2,
			40));
		assertTrue(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.FOOTPRINT,
			2,
			1,
			2,
			40));
	}

	@Test
	public void publicHealthScaleModeClampsLegacyThresholdsBelowOneHundred()
	{
		assertFalse(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.HEALTH_SCALE,
			8,
			99,
			2,
			40));
		assertTrue(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.HEALTH_SCALE,
			1,
			100,
			2,
			40));
	}

	@Test
	public void eitherModeCatchesLargeFootprintsAndOneTileHighScaleTargets()
	{
		assertTrue(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.EITHER,
			2,
			30,
			2,
			40));
		assertTrue(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.EITHER,
			1,
			100,
			2,
			40));
		assertFalse(OverheadsPlugin.detectsBossLikeTarget(
			OverheadsTargetDetection.EITHER,
			1,
			30,
			2,
			40));
	}

	@Test
	public void nullModeFallsBackToEitherAndThresholdsAreClamped()
	{
		assertTrue(OverheadsPlugin.detectsBossLikeTarget(
			null,
			1,
			1,
			0,
			0));
	}

	@Test
	public void classificationSurvivesMissingHealthButNotAnNpcTransform()
	{
		assertTrue(OverheadsPlugin.canReuseTargetClassification(
			2, 100, 42,
			2, -1, 42));
		assertFalse(OverheadsPlugin.canReuseTargetClassification(
			2, 100, 42,
			1, -1, 42));
		assertFalse(OverheadsPlugin.canReuseTargetClassification(
			2, 100, 42,
			2, -1, 43));
		assertFalse(OverheadsPlugin.canReuseTargetClassification(
			2, 100, 42,
			2, 30, 42));
	}
}
