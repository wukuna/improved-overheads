package com.improvedoverheads;

final class OverheadsLayout
{
	private static final double[][] CLOCKWISE_DIRECTIONS = {
		{1.0, 0.5},
		{0.0, 1.0},
		{-1.0, 0.5},
		{-1.0, -0.5},
		{0.0, -1.0},
		{1.0, -0.5}
	};

	private OverheadsLayout()
	{
	}

	static OverheadsOffset offsetFor(int position, int width, int height, int edgeCut)
	{
		return offsetFor(position, width, height, edgeCut, OverheadsLayoutShape.HEXAGONAL, OverheadsLayoutDirection.CLOCKWISE);
	}

	static OverheadsOffset offsetFor(
		int position,
		int width,
		int height,
		int edgeCut,
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection)
	{
		return offsetFor(position, width, height, edgeCut, layoutShape, layoutDirection, 0.0, 0.0);
	}

	static OverheadsOffset offsetFor(
		int position,
		int width,
		int height,
		int edgeCut,
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		double xSpacing,
		double ySpacing)
	{
		return offsetFor(position, width, height, edgeCut, layoutShape, layoutDirection, OverheadsLayoutBehavior.INCREMENTAL, xSpacing, ySpacing, 0);
	}

	static OverheadsOffset offsetFor(
		int position,
		int width,
		int height,
		int edgeCut,
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		OverheadsLayoutBehavior layoutBehavior,
		double xSpacing,
		double ySpacing,
		int minRadius)
	{
		return offsetForAbsolutePosition(
			absolutePositionFor(position, minRadius, layoutShape),
			width,
			height,
			edgeCut,
			layoutShape,
			layoutDirection,
			layoutBehavior,
			xSpacing,
			ySpacing);
	}

	private static OverheadsOffset offsetForAbsolutePosition(
		int position,
		int width,
		int height,
		int edgeCut,
		OverheadsLayoutShape layoutShape,
		OverheadsLayoutDirection layoutDirection,
		OverheadsLayoutBehavior layoutBehavior,
		double xSpacing,
		double ySpacing)
	{
		if (position <= 0)
		{
			return new OverheadsOffset(0, 0);
		}

		if (layoutShape == OverheadsLayoutShape.DIAMOND)
		{
			return diamondOffsetFor(position, width, height, edgeCut, layoutDirection, layoutBehavior, xSpacing, ySpacing);
		}

		if (layoutShape == OverheadsLayoutShape.GRID)
		{
			return gridOffsetFor(position, width, height, layoutDirection, layoutBehavior, xSpacing, ySpacing);
		}

		if (layoutShape == OverheadsLayoutShape.X)
		{
			return xOffsetFor(position, width, height, edgeCut, layoutDirection, layoutBehavior, xSpacing, ySpacing);
		}

		int indexOnRing = position - 1;
		int radius = 1;
		while (indexOnRing >= radius * 6)
		{
			indexOnRing -= radius * 6;
			radius++;
		}

		if (layoutBehavior == OverheadsLayoutBehavior.SYMMETRIC)
		{
			indexOnRing = symmetricIndexToBaseIndex(indexOnRing, radius * 6);
		}

		if (layoutDirection == OverheadsLayoutDirection.COUNTERCLOCKWISE)
		{
			indexOnRing = counterclockwiseIndexToClockwiseIndex(indexOnRing, radius);
		}

		xSpacing = normalizeSpacing(xSpacing);
		ySpacing = normalizeSpacing(ySpacing);
		double sideSpacing = width - edgeCut + xSpacing;
		double verticalSpacing = height + ySpacing;
		double x = 0;
		double y = -verticalSpacing * radius;
		int remaining = indexOnRing;

		for (int side = 0; side < CLOCKWISE_DIRECTIONS.length && remaining > 0; side++)
		{
			int steps = Math.min(remaining, radius);
			x += CLOCKWISE_DIRECTIONS[side][0] * sideSpacing * steps;
			y += CLOCKWISE_DIRECTIONS[side][1] * verticalSpacing * steps;
			remaining -= steps;
		}

		return new OverheadsOffset((int) Math.round(x), (int) Math.round(y));
	}

	private static OverheadsOffset diamondOffsetFor(
		int position,
		int width,
		int height,
		int edgeCut,
		OverheadsLayoutDirection layoutDirection,
		OverheadsLayoutBehavior layoutBehavior,
		double xSpacing,
		double ySpacing)
	{
		int indexOnRing = position - 1;
		int radius = 1;
		while (indexOnRing >= radius * 4)
		{
			indexOnRing -= radius * 4;
			radius++;
		}

		if (layoutBehavior == OverheadsLayoutBehavior.SYMMETRIC)
		{
			indexOnRing = symmetricIndexToBaseIndex(indexOnRing, radius * 4);
		}

		if (layoutDirection == OverheadsLayoutDirection.COUNTERCLOCKWISE)
		{
			indexOnRing = diamondCounterclockwiseIndexToClockwiseIndex(indexOnRing, radius);
		}

		double sideSpacing = width - edgeCut + normalizeSpacing(xSpacing);
		double verticalSpacing = height + normalizeSpacing(ySpacing);
		if (indexOnRing < radius)
		{
			double step = indexOnRing;
			return new OverheadsOffset((int) Math.round(step * sideSpacing), (int) Math.round((-radius + step) * verticalSpacing));
		}

		if (indexOnRing < radius * 2)
		{
			double step = indexOnRing - radius;
			return new OverheadsOffset((int) Math.round((radius - step) * sideSpacing), (int) Math.round(step * verticalSpacing));
		}

		if (indexOnRing < radius * 3)
		{
			double step = indexOnRing - radius * 2;
			return new OverheadsOffset((int) Math.round(-step * sideSpacing), (int) Math.round((radius - step) * verticalSpacing));
		}

		double step = indexOnRing - radius * 3;
		return new OverheadsOffset((int) Math.round((-radius + step) * sideSpacing), (int) Math.round(-step * verticalSpacing));
	}

	private static OverheadsOffset gridOffsetFor(
		int position,
		int width,
		int height,
		OverheadsLayoutDirection layoutDirection,
		OverheadsLayoutBehavior layoutBehavior,
		double xSpacing,
		double ySpacing)
	{
		int indexOnRing = position - 1;
		int radius = 1;
		while (indexOnRing >= radius * 8)
		{
			indexOnRing -= radius * 8;
			radius++;
		}

		if (layoutBehavior == OverheadsLayoutBehavior.SYMMETRIC)
		{
			indexOnRing = symmetricIndexToBaseIndex(indexOnRing, radius * 8);
		}

		if (layoutDirection == OverheadsLayoutDirection.COUNTERCLOCKWISE)
		{
			indexOnRing = gridCounterclockwiseIndexToClockwiseIndex(indexOnRing, radius);
		}

		double horizontalSpacing = width + normalizeSpacing(xSpacing);
		double verticalSpacing = height + normalizeSpacing(ySpacing);
		double x = 0;
		double y = -radius;
		int remaining = indexOnRing;

		if (remaining > 0)
		{
			int steps = Math.min(remaining, radius);
			x += steps;
			remaining -= steps;
		}

		if (remaining > 0)
		{
			int steps = Math.min(remaining, radius * 2);
			y += steps;
			remaining -= steps;
		}

		if (remaining > 0)
		{
			int steps = Math.min(remaining, radius * 2);
			x -= steps;
			remaining -= steps;
		}

		if (remaining > 0)
		{
			int steps = Math.min(remaining, radius * 2);
			y -= steps;
			remaining -= steps;
		}

		if (remaining > 0)
		{
			x += remaining;
		}

		return new OverheadsOffset((int) Math.round(x * horizontalSpacing), (int) Math.round(y * verticalSpacing));
	}

	private static OverheadsOffset xOffsetFor(
		int position,
		int width,
		int height,
		int edgeCut,
		OverheadsLayoutDirection layoutDirection,
		OverheadsLayoutBehavior layoutBehavior,
		double xSpacing,
		double ySpacing)
	{
		int indexOnRing = (position - 1) % 4;
		int radius = (position - 1) / 4 + 1;

		if (layoutBehavior == OverheadsLayoutBehavior.SYMMETRIC)
		{
			indexOnRing = symmetricIndexToBaseIndex(indexOnRing, 4);
		}

		if (layoutDirection == OverheadsLayoutDirection.COUNTERCLOCKWISE)
		{
			indexOnRing = xCounterclockwiseIndexToClockwiseIndex(indexOnRing);
		}

		double sideSpacing = width - edgeCut + normalizeSpacing(xSpacing);
		double verticalSpacing = height + normalizeSpacing(ySpacing);
		switch (indexOnRing)
		{
			case 0:
				return new OverheadsOffset((int) Math.round(radius * sideSpacing), (int) Math.round(-radius * verticalSpacing));
			case 1:
				return new OverheadsOffset((int) Math.round(radius * sideSpacing), (int) Math.round(radius * verticalSpacing));
			case 2:
				return new OverheadsOffset((int) Math.round(-radius * sideSpacing), (int) Math.round(radius * verticalSpacing));
			default:
				return new OverheadsOffset((int) Math.round(-radius * sideSpacing), (int) Math.round(-radius * verticalSpacing));
		}
	}

	private static double normalizeSpacing(double spacing)
	{
		if (Double.isNaN(spacing) || Double.isInfinite(spacing))
		{
			return 0.0;
		}

		spacing = Math.max(-64.0, Math.min(64.0, spacing));
		return Math.round(spacing);
	}

	static int slotLimit(int maxRadius)
	{
		return slotLimit(maxRadius, OverheadsLayoutShape.HEXAGONAL);
	}

	static int slotLimit(int maxRadius, OverheadsLayoutShape layoutShape)
	{
		return slotLimit(0, maxRadius, layoutShape);
	}

	static int slotLimit(int minRadius, int maxRadius, OverheadsLayoutShape layoutShape)
	{
		if (maxRadius <= 0)
		{
			return Integer.MAX_VALUE;
		}

		minRadius = Math.max(0, minRadius);
		int outerRadius = maxRadius - 1;
		if (minRadius > outerRadius)
		{
			return 0;
		}

		long slots = slotsThroughRadius(outerRadius, layoutShape) - slotsBeforeRadius(minRadius, layoutShape);
		return (int) Math.min(slots, Integer.MAX_VALUE);
	}

	static boolean isPositionWithinRadiusLimit(int position, int maxRadius)
	{
		return isPositionWithinRadiusLimit(position, maxRadius, OverheadsLayoutShape.HEXAGONAL);
	}

	static boolean isPositionWithinRadiusLimit(int position, int maxRadius, OverheadsLayoutShape layoutShape)
	{
		return isPositionWithinRadiusLimit(position, 0, maxRadius, layoutShape);
	}

	static boolean isPositionWithinRadiusLimit(int position, int minRadius, int maxRadius, OverheadsLayoutShape layoutShape)
	{
		int limit = slotLimit(minRadius, maxRadius, layoutShape);
		return position >= 0 && (limit == Integer.MAX_VALUE || position < limit);
	}

	static int radiusForPosition(int position, int minRadius, OverheadsLayoutShape layoutShape)
	{
		return absoluteRadiusForPosition(absolutePositionFor(position, minRadius, layoutShape), layoutShape);
	}

	private static int absolutePositionFor(int position, int minRadius, OverheadsLayoutShape layoutShape)
	{
		long absolutePosition = slotsBeforeRadius(Math.max(0, minRadius), layoutShape) + Math.max(0, position);
		return (int) Math.min(absolutePosition, Integer.MAX_VALUE);
	}

	private static long slotsBeforeRadius(int radius, OverheadsLayoutShape layoutShape)
	{
		if (radius <= 0)
		{
			return 0;
		}

		return slotsThroughRadius(radius - 1, layoutShape);
	}

	private static long slotsThroughRadius(int radius, OverheadsLayoutShape layoutShape)
	{
		if (layoutShape == OverheadsLayoutShape.DIAMOND)
		{
			return diamondSlotsThroughRadius(radius);
		}

		if (layoutShape == OverheadsLayoutShape.GRID)
		{
			return gridSlotsThroughRadius(radius);
		}

		if (layoutShape == OverheadsLayoutShape.X)
		{
			return xSlotsThroughRadius(radius);
		}

		return hexSlotsThroughRadius(radius);
	}

	private static int absoluteRadiusForPosition(int position, OverheadsLayoutShape layoutShape)
	{
		if (position <= 0)
		{
			return 0;
		}

		int indexOnRing = position - 1;
		if (layoutShape == OverheadsLayoutShape.X)
		{
			return indexOnRing / 4 + 1;
		}

		int radius = 1;
		int ringMultiplier = ringSizeMultiplier(layoutShape);
		while (indexOnRing >= radius * ringMultiplier)
		{
			indexOnRing -= radius * ringMultiplier;
			radius++;
		}

		return radius;
	}

	private static int ringSizeMultiplier(OverheadsLayoutShape layoutShape)
	{
		if (layoutShape == OverheadsLayoutShape.DIAMOND)
		{
			return 4;
		}

		if (layoutShape == OverheadsLayoutShape.GRID)
		{
			return 8;
		}

		return 6;
	}

	private static long hexSlotsThroughRadius(int radius)
	{
		if (radius <= 0)
		{
			return 1;
		}

		return 1L + 3L * radius * (radius + 1L);
	}

	private static long diamondSlotsThroughRadius(int radius)
	{
		if (radius <= 0)
		{
			return 1;
		}

		return 1L + 2L * radius * (radius + 1L);
	}

	private static long gridSlotsThroughRadius(int radius)
	{
		if (radius <= 0)
		{
			return 1;
		}

		long diameter = 2L * radius + 1L;
		return diameter * diameter;
	}

	private static long xSlotsThroughRadius(int radius)
	{
		if (radius <= 0)
		{
			return 1;
		}

		return 1L + 4L * radius;
	}

	private static int symmetricIndexToBaseIndex(int indexOnRing, int ringSize)
	{
		int halfRing = ringSize / 2;
		if (indexOnRing % 2 == 0)
		{
			return indexOnRing / 2;
		}

		return halfRing + indexOnRing / 2;
	}

	private static int counterclockwiseIndexToClockwiseIndex(int indexOnRing, int radius)
	{
		if (indexOnRing == 0)
		{
			return 0;
		}

		return radius * 6 - indexOnRing;
	}

	private static int diamondCounterclockwiseIndexToClockwiseIndex(int indexOnRing, int radius)
	{
		if (indexOnRing == 0)
		{
			return 0;
		}

		return radius * 4 - indexOnRing;
	}

	private static int gridCounterclockwiseIndexToClockwiseIndex(int indexOnRing, int radius)
	{
		if (indexOnRing == 0)
		{
			return 0;
		}

		return radius * 8 - indexOnRing;
	}

	private static int xCounterclockwiseIndexToClockwiseIndex(int indexOnRing)
	{
		if (indexOnRing == 0)
		{
			return 0;
		}

		return 4 - indexOnRing;
	}
}
