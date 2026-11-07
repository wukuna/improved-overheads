package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OverheadsLayoutTest
{
	@Test
	public void firstHexRingStartsTopAndMovesClockwiseWithoutGaps()
	{
		assertOffset(0, 0, 0);
		assertOffset(1, 0, -20);
		assertOffset(2, 23, -10);
		assertOffset(3, 23, 10);
		assertOffset(4, 0, 20);
		assertOffset(5, -23, 10);
		assertOffset(6, -23, -10);
	}

	@Test
	public void secondHexRingExpandsAfterFirstRing()
	{
		assertOffset(7, 0, -40);
		assertOffset(8, 23, -30);
		assertOffset(9, 46, -20);
	}

	@Test
	public void symmetricBehaviorPairsOppositeHexSlots()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 2, 0, 20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 3, 23, -10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 4, -23, 10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 5, 23, 10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 6, -23, -10);
	}

	@Test
	public void symmetricBehaviorPairsOppositeHexSlotsOnLargerRings()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 7, 0, -40);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 8, 0, 40);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 9, 23, -30);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 10, -23, 30);
	}

	@Test
	public void counterclockwiseDirectionStartsTopAndMovesCounterclockwise()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 2, -23, -10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 3, -23, 10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 4, 0, 20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 5, 23, 10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 6, 23, -10);
	}

	@Test
	public void counterclockwiseDirectionExtendsTheSamePatternOnLargerRings()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 7, 0, -40);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 8, -23, -30);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 9, -46, -20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 10, -46, 0);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 11, -46, 20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 12, -23, 30);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, 13, 0, 40);
	}

	@Test
	public void spacingCanBeControlledPerAxis()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 1, 0.0, 5.2, 0, -25);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 2, 4.6, 0.0, 28, -10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 2, 4.6, 5.2, 28, -12);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 2, 4.2, 4.2, 27, -12);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 1, -3.0, -4.0, 0, -16);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 2, -3.0, -4.0, 20, -8);
	}

	@Test
	public void symmetricBehaviorMirrorsWithCounterclockwiseDirection()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 2, 0, 20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 3, -23, -10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 4, 23, 10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 5, -23, 10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.COUNTERCLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 6, 23, -10);
	}

	@Test
	public void diamondShapeUsesFourCornerRings()
	{
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 2, 23, 0);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 3, 0, 20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 4, -23, 0);
	}

	@Test
	public void diamondShapeFillsLargerRingEdges()
	{
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 5, 0, -40);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 6, 23, -20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 7, 46, 0);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 8, 23, 20);
	}

	@Test
	public void diamondShapeCanRunCounterclockwise()
	{
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.COUNTERCLOCKWISE, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.COUNTERCLOCKWISE, 2, -23, 0);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.COUNTERCLOCKWISE, 3, 0, 20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.COUNTERCLOCKWISE, 4, 23, 0);
	}

	@Test
	public void diamondShapeCanUseSymmetricBehavior()
	{
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 2, 0, 20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 3, 23, 0);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 4, -23, 0);
	}

	@Test
	public void minRadiusSkipsInnerDiamondSlots()
	{
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 0, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 1, 1, 23, 0);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 2, 1, 0, 20);
		assertOffset(OverheadsLayoutShape.DIAMOND, OverheadsLayoutDirection.CLOCKWISE, 3, 1, -23, 0);
	}

	@Test
	public void minRadiusSkipsInnerHexSlots()
	{
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 0, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 1, 1, 23, -10);
		assertOffset(OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE, 6, 1, 0, -40);
	}

	@Test
	public void gridShapeUsesSquareRings()
	{
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 0, 0, 0);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 2, 30, -20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 3, 30, 0);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 4, 30, 20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 5, 0, 20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 6, -30, 20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 7, -30, 0);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 8, -30, -20);
	}

	@Test
	public void gridShapeExpandsToLargerSquareRings()
	{
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 9, 0, -40);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 10, 30, -40);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 11, 60, -40);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, 12, 60, -20);
	}

	@Test
	public void gridShapeCanRunCounterclockwise()
	{
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.COUNTERCLOCKWISE, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.COUNTERCLOCKWISE, 2, -30, -20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.COUNTERCLOCKWISE, 3, -30, 0);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.COUNTERCLOCKWISE, 4, -30, 20);
	}

	@Test
	public void gridShapeCanUseSymmetricBehavior()
	{
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 1, 0, -20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 2, 0, 20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 3, 30, -20);
		assertOffset(OverheadsLayoutShape.GRID, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 4, -30, 20);
	}

	@Test
	public void xShapeUsesDiagonalArms()
	{
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 0, 0, 0);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 1, 23, -20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 2, 23, 20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 3, -23, 20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 4, -23, -20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 5, 46, -40);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 6, 46, 40);
	}

	@Test
	public void xShapeCanRunCounterclockwise()
	{
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.COUNTERCLOCKWISE, 1, 23, -20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.COUNTERCLOCKWISE, 2, -23, -20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.COUNTERCLOCKWISE, 3, -23, 20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.COUNTERCLOCKWISE, 4, 23, 20);
	}

	@Test
	public void xShapeCanUseSymmetricBehavior()
	{
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 1, 23, -20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 2, -23, 20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 3, 23, 20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, OverheadsLayoutBehavior.SYMMETRIC, 4, -23, -20);
	}

	@Test
	public void minRadiusSkipsInnerXSlots()
	{
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 0, 1, 23, -20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 1, 1, 23, 20);
		assertOffset(OverheadsLayoutShape.X, OverheadsLayoutDirection.CLOCKWISE, 4, 1, 46, -40);
	}

	@Test
	public void slotLimitUsesMaxRadius()
	{
		assertEquals(Integer.MAX_VALUE, OverheadsLayout.slotLimit(0));
		assertEquals(1, OverheadsLayout.slotLimit(1));
		assertEquals(7, OverheadsLayout.slotLimit(2));
		assertEquals(19, OverheadsLayout.slotLimit(3));
		assertTrue(OverheadsLayout.isPositionWithinRadiusLimit(0, 1));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(1, 1));
		assertTrue(OverheadsLayout.isPositionWithinRadiusLimit(6, 2));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(7, 2));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(-1, 1));
	}

	@Test
	public void diamondSlotLimitUsesFourSlotsPerRing()
	{
		assertEquals(Integer.MAX_VALUE, OverheadsLayout.slotLimit(0, OverheadsLayoutShape.DIAMOND));
		assertEquals(1, OverheadsLayout.slotLimit(1, OverheadsLayoutShape.DIAMOND));
		assertEquals(5, OverheadsLayout.slotLimit(2, OverheadsLayoutShape.DIAMOND));
		assertEquals(13, OverheadsLayout.slotLimit(3, OverheadsLayoutShape.DIAMOND));
		assertTrue(OverheadsLayout.isPositionWithinRadiusLimit(4, 2, OverheadsLayoutShape.DIAMOND));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(5, 2, OverheadsLayoutShape.DIAMOND));
	}

	@Test
	public void slotLimitCanSkipInnerRadii()
	{
		assertEquals(4, OverheadsLayout.slotLimit(1, 2, OverheadsLayoutShape.DIAMOND));
		assertEquals(12, OverheadsLayout.slotLimit(1, 3, OverheadsLayoutShape.DIAMOND));
		assertEquals(6, OverheadsLayout.slotLimit(1, 2, OverheadsLayoutShape.HEXAGONAL));
		assertEquals(8, OverheadsLayout.slotLimit(1, 2, OverheadsLayoutShape.GRID));
		assertEquals(4, OverheadsLayout.slotLimit(1, 2, OverheadsLayoutShape.X));
		assertEquals(0, OverheadsLayout.slotLimit(2, 1, OverheadsLayoutShape.DIAMOND));
		assertTrue(OverheadsLayout.isPositionWithinRadiusLimit(3, 1, 2, OverheadsLayoutShape.DIAMOND));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(4, 1, 2, OverheadsLayoutShape.DIAMOND));
	}

	@Test
	public void gridSlotLimitUsesEightSlotsPerRing()
	{
		assertEquals(Integer.MAX_VALUE, OverheadsLayout.slotLimit(0, OverheadsLayoutShape.GRID));
		assertEquals(1, OverheadsLayout.slotLimit(1, OverheadsLayoutShape.GRID));
		assertEquals(9, OverheadsLayout.slotLimit(2, OverheadsLayoutShape.GRID));
		assertEquals(25, OverheadsLayout.slotLimit(3, OverheadsLayoutShape.GRID));
		assertTrue(OverheadsLayout.isPositionWithinRadiusLimit(8, 2, OverheadsLayoutShape.GRID));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(9, 2, OverheadsLayoutShape.GRID));
	}

	@Test
	public void xSlotLimitUsesFourSlotsPerRing()
	{
		assertEquals(Integer.MAX_VALUE, OverheadsLayout.slotLimit(0, OverheadsLayoutShape.X));
		assertEquals(1, OverheadsLayout.slotLimit(1, OverheadsLayoutShape.X));
		assertEquals(5, OverheadsLayout.slotLimit(2, OverheadsLayoutShape.X));
		assertEquals(9, OverheadsLayout.slotLimit(3, OverheadsLayoutShape.X));
		assertTrue(OverheadsLayout.isPositionWithinRadiusLimit(4, 2, OverheadsLayoutShape.X));
		assertFalse(OverheadsLayout.isPositionWithinRadiusLimit(5, 2, OverheadsLayoutShape.X));
	}

	@Test
	public void radiusForPositionUsesSkippedRadii()
	{
		assertEquals(1, OverheadsLayout.radiusForPosition(0, 1, OverheadsLayoutShape.DIAMOND));
		assertEquals(1, OverheadsLayout.radiusForPosition(3, 1, OverheadsLayoutShape.DIAMOND));
		assertEquals(2, OverheadsLayout.radiusForPosition(4, 1, OverheadsLayoutShape.DIAMOND));
		assertEquals(1, OverheadsLayout.radiusForPosition(7, 0, OverheadsLayoutShape.GRID));
		assertEquals(1, OverheadsLayout.radiusForPosition(8, 0, OverheadsLayoutShape.GRID));
		assertEquals(2, OverheadsLayout.radiusForPosition(9, 0, OverheadsLayoutShape.GRID));
		assertEquals(1, OverheadsLayout.radiusForPosition(3, 0, OverheadsLayoutShape.X));
		assertEquals(1, OverheadsLayout.radiusForPosition(4, 0, OverheadsLayoutShape.X));
		assertEquals(2, OverheadsLayout.radiusForPosition(5, 0, OverheadsLayoutShape.X));
		assertEquals(2, OverheadsLayout.radiusForPosition(4, 1, OverheadsLayoutShape.X));
	}

	private static void assertOffset(int position, int expectedX, int expectedY)
	{
		OverheadsOffset offset = OverheadsLayout.offsetFor(position, 30, 20, 7);
		assertEquals(expectedX, offset.getX());
		assertEquals(expectedY, offset.getY());
	}

	private static void assertOffset(
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		int position,
		int expectedX,
		int expectedY)
	{
		OverheadsOffset offset = OverheadsLayout.offsetFor(position, 30, 20, 7, layoutShape, layoutDirection);
		assertEquals(expectedX, offset.getX());
		assertEquals(expectedY, offset.getY());
	}

	private static void assertOffset(
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		OverheadsLayoutBehavior layoutBehavior,
		int position,
		int expectedX,
		int expectedY)
	{
		OverheadsOffset offset = OverheadsLayout.offsetFor(position, 30, 20, 7, layoutShape, layoutDirection, layoutBehavior, 0.0, 0.0, 0);
		assertEquals(expectedX, offset.getX());
		assertEquals(expectedY, offset.getY());
	}

	private static void assertOffset(
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		int position,
		int minRadius,
		int expectedX,
		int expectedY)
	{
		OverheadsOffset offset = OverheadsLayout.offsetFor(position, 30, 20, 7, layoutShape, layoutDirection, OverheadsLayoutBehavior.INCREMENTAL, 0.0, 0.0, minRadius);
		assertEquals(expectedX, offset.getX());
		assertEquals(expectedY, offset.getY());
	}

	private static void assertOffset(
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		int position,
		double xSpacing,
		double ySpacing,
		int expectedX,
		int expectedY)
	{
		OverheadsOffset offset = OverheadsLayout.offsetFor(position, 30, 20, 7, layoutShape, layoutDirection, xSpacing, ySpacing);
		assertEquals(expectedX, offset.getX());
		assertEquals(expectedY, offset.getY());
	}
}
