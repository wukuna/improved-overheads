package com.improvedoverheads;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.runelite.api.Point;
import org.junit.Test;

public class OverheadsOverlayAnchorTest
{
	@Test
	public void sharedAnchorUsesNativeUpperUiHeightPaddingForEveryActorSize()
	{
		assertEquals(47, OverheadsOverlay.actorProjectionHeight(32));
		assertEquals(111, OverheadsOverlay.actorProjectionHeight(96));
		assertEquals(335, OverheadsOverlay.actorProjectionHeight(320));
	}

	@Test
	public void npcDefinitionHeightOverridesLogicalHeightWhenAvailable()
	{
		assertEquals(72, OverheadsOverlay.selectBaseOverheadHeight(240, 72));
		assertEquals(240, OverheadsOverlay.selectBaseOverheadHeight(
			240,
			OverheadsNpcOverheadHeight.NO_OVERRIDE));
		assertEquals(240, OverheadsOverlay.selectBaseOverheadHeight(
			240,
			OverheadsNpcOverheadHeight.LOAD_FAILED));
		assertEquals(240, OverheadsOverlay.selectBaseOverheadHeight(
			240,
			OverheadsNpcOverheadHeight.DECODE_FAILED));
	}

	@Test
	public void projectedNativeOriginReceivesOnlyTheNativeCanvasLayoutCorrection()
	{
		Point anchor = OverheadsOverlay.nativeStackAnchor(new Point(210, 380));

		assertEquals(new Point(210, 384), anchor);
		assertEquals(377, OverheadsHealthBarRenderer.barTop(anchor.getY(), 5, 0));
		assertEquals(
			382,
			OverheadsHeadIconRenderer.stackBottomY(
				anchor.getY(),
				OverheadsHealthBarRenderer.NO_OCCUPIED_TOP,
				0,
				2));
		assertNull(OverheadsOverlay.nativeStackAnchor(null));
	}

	@Test
	public void projectsOnlyActorsWithVisibleOverheadUi()
	{
		assertTrue(OverheadsOverlay.needsActorTop(true, false, false));
		assertTrue(OverheadsOverlay.needsActorTop(false, true, false));
		assertTrue(OverheadsOverlay.needsActorTop(false, false, true));
		assertTrue(OverheadsOverlay.needsActorTop(true, true, true));
		assertFalse(OverheadsOverlay.needsActorTop(false, false, false));
	}

	@Test
	public void overheadStackIsTranslationInvariantAcrossActorTopPositions()
	{
		assertArrayEquals(relativeStack(380), relativeStack(125));

		int actorTopY = 380;
		int chatTop = OverheadsOverheadChatRenderer.chatLaneTop(
			actorTopY,
			12,
			OverheadsOverheadChatEffect.STATIC);
		int healthTop = OverheadsHealthBarRenderer.barTop(chatTop, 5, 0);
		int iconBottom = OverheadsHeadIconRenderer.stackBottomY(
			chatTop,
			healthTop,
			0,
			2);
		assertTrue(healthTop + 5 <= chatTop - 2);
		assertTrue(iconBottom <= healthTop - 2);
	}

	private static int[] relativeStack(int actorTopY)
	{
		int chatTop = OverheadsOverheadChatRenderer.chatLaneTop(
			actorTopY,
			12,
			OverheadsOverheadChatEffect.STATIC);
		int chatBaseline = OverheadsOverheadChatRenderer.chatBaseline(
			actorTopY,
			0);
		int healthTop = OverheadsHealthBarRenderer.barTop(chatTop, 5, 0);
		int iconBottom = OverheadsHeadIconRenderer.stackBottomY(
			chatTop,
			healthTop,
			0,
			2);
		return new int[]{
			chatTop - actorTopY,
			chatBaseline - actorTopY,
			healthTop - actorTopY,
			iconBottom - actorTopY
		};
	}
}
