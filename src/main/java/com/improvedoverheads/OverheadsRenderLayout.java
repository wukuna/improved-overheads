package com.improvedoverheads;

import java.awt.FontMetrics;

final class OverheadsRenderLayout
{
	private static final int DEFAULT_HEIGHT = 20;

	private final int firstX;
	private final int secondX;
	private final int middleX;
	private final int lastX;
	private final int spriteY;
	private final int textX;
	private final int textBaseline;
	private final int middleRepeatCount;
	private final int width;
	private final int height;

	private OverheadsRenderLayout(
		int firstX,
		int secondX,
		int middleX,
		int lastX,
		int spriteY,
		int textX,
		int textBaseline,
		int middleRepeatCount,
		int width,
		int height)
	{
		this.firstX = firstX;
		this.secondX = secondX;
		this.middleX = middleX;
		this.lastX = lastX;
		this.spriteY = spriteY;
		this.textX = textX;
		this.textBaseline = textBaseline;
		this.middleRepeatCount = middleRepeatCount;
		this.width = width;
		this.height = height;
	}

	static OverheadsRenderLayout create(
		OverheadsSprite first,
		OverheadsSprite middle,
		OverheadsSprite second,
		OverheadsSprite last,
		String text,
		FontMetrics metrics,
		int textYOffset)
	{
		return create(first, middle, second, last, text, metrics, textYOffset, 100);
	}

	static OverheadsRenderLayout create(
		OverheadsSprite first,
		OverheadsSprite middle,
		OverheadsSprite second,
		OverheadsSprite last,
		String text,
		FontMetrics metrics,
		int textYOffset,
		int scalePercent)
	{
		scalePercent = clampScalePercent(scalePercent);
		int textWidth = metrics.stringWidth(text);
		int firstWidth = width(first, scalePercent);
		int middleWidth = width(middle, scalePercent);
		int secondWidth = width(second, scalePercent);

		int repeatCount = 0;
		if (middleWidth > 0)
		{
			repeatCount = second == null && last == null ? 1 : textWidth / middleWidth + 1;
		}

		int cursorX = 0;
		int firstX = cursorX - offsetX(first, scalePercent);
		cursorX += firstWidth + scaleDimension(2, scalePercent);

		int secondX = cursorX - offsetX(second, scalePercent);
		cursorX += secondWidth;

		int middleX = cursorX - offsetX(middle, scalePercent);
		int textX = cursorX;
		if (middleWidth > 0)
		{
			int repeatedWidth = repeatCount * middleWidth;
			cursorX += repeatedWidth;
			textX += (repeatedWidth - textWidth) / 2;
		}
		else
		{
			cursorX += textWidth;
		}

		int lastX = cursorX - offsetX(last, scalePercent);
		boolean textOnly = allNull(first, middle, second, last);
		int scaledDefaultHeight = scaleDimension(DEFAULT_HEIGHT, scalePercent);
		int scaledTextYOffset = scaleCoordinate(textYOffset, scalePercent);
		int textShadowOffset = scaleDimension(1, scalePercent);
		int textBaseline = textOnly
			? scaledTextYOffset + (scaledDefaultHeight - metrics.getHeight()) / 2 + metrics.getAscent()
			: scaledTextYOffset + scaleDimension(15, scalePercent);

		Bounds bounds = new Bounds();
		bounds.include(first, firstX, 0, scalePercent);
		bounds.include(second, secondX, 0, scalePercent);
		for (int i = 0; i < repeatCount; i++)
		{
			bounds.include(middle, middleX + i * middleWidth, 0, scalePercent);
		}
		bounds.include(last, lastX, 0, scalePercent);
		if (!text.isEmpty())
		{
			bounds.include(
				textX,
				textBaseline - metrics.getAscent(),
				textX + Math.max(1, textWidth) + textShadowOffset,
				textBaseline + metrics.getDescent() + textShadowOffset);
		}

		if (!bounds.isInitialized())
		{
			bounds.include(0, 0, 1, scaledDefaultHeight);
		}

		int translateX = -Math.floorDiv(bounds.getMinX() + bounds.getMaxX(), 2);
		int translateY = textOnly
			? -scaledDefaultHeight / 2
			: -Math.floorDiv(bounds.getMinY() + bounds.getMaxY(), 2);
		int translatedMinY = bounds.getMinY() + translateY;
		int translatedMaxY = bounds.getMaxY() + translateY;
		int centeredHeight = 2 * Math.max(Math.abs(translatedMinY), Math.abs(translatedMaxY));
		int measuredHeight = bounds.getMaxY() - bounds.getMinY();
		if (textOnly && textYOffset != 0)
		{
			measuredHeight = Math.max(measuredHeight, centeredHeight);
		}
		return new OverheadsRenderLayout(
			firstX + translateX,
			secondX + translateX,
			middleX + translateX,
			lastX + translateX,
			translateY,
			textX + translateX,
			textBaseline + translateY,
			repeatCount,
			bounds.getMaxX() - bounds.getMinX(),
			measuredHeight);
	}

	int getFirstX()
	{
		return firstX;
	}

	static OverheadsRenderLayout singleSprite(OverheadsSprite sprite, String text, FontMetrics metrics, int scale)
	{
		if (sprite == null)
		{
			return create(null, null, null, null, text, metrics, 0, scale);
		}
		int width = width(sprite, scale);
		int height = scaleDimension(sprite.getHeight(), scale);
		int textWidth = metrics.stringWidth(text);
		int baseline = (metrics.getAscent() - metrics.getDescent()) / 2;
		return new OverheadsRenderLayout(-width / 2 - offsetX(sprite, scale), 0, 0, 0,
			-height / 2 - scaleCoordinate(sprite.getOffsetY(), scale), -textWidth / 2, baseline, 0,
			Math.max(width, textWidth + 1), Math.max(height, metrics.getAscent() + metrics.getDescent() + 1));
	}

	int getSecondX()
	{
		return secondX;
	}

	int getMiddleX()
	{
		return middleX;
	}

	int getLastX()
	{
		return lastX;
	}

	int getSpriteY()
	{
		return spriteY;
	}

	int getTextX()
	{
		return textX;
	}

	int getTextBaseline()
	{
		return textBaseline;
	}

	int getMiddleRepeatCount()
	{
		return middleRepeatCount;
	}

	int getWidth()
	{
		return width;
	}

	int getHeight()
	{
		return height;
	}

	static int clampScalePercent(int scalePercent)
	{
		return Math.max(50, Math.min(200, scalePercent));
	}

	static int scaleCoordinate(int value, int scalePercent)
	{
		return Math.round(value * clampScalePercent(scalePercent) / 100.0f);
	}

	static int scaleDimension(int value, int scalePercent)
	{
		return value <= 0 ? 0 : Math.max(1, scaleCoordinate(value, scalePercent));
	}

	static int width(OverheadsSprite sprite, int scalePercent)
	{
		return sprite == null ? 0 : scaleDimension(sprite.getWidth(), scalePercent);
	}

	private static int offsetX(OverheadsSprite sprite, int scalePercent)
	{
		return sprite == null ? 0 : scaleCoordinate(sprite.getOffsetX(), scalePercent);
	}

	private static boolean allNull(
		OverheadsSprite first,
		OverheadsSprite middle,
		OverheadsSprite second,
		OverheadsSprite last)
	{
		return first == null && middle == null && second == null && last == null;
	}

	private static final class Bounds
	{
		private int minX;
		private int minY;
		private int maxX;
		private int maxY;
		private boolean initialized;

		private void include(OverheadsSprite sprite, int x, int y, int scalePercent)
		{
			if (sprite == null)
			{
				return;
			}

			int left = x + scaleCoordinate(sprite.getOffsetX(), scalePercent);
			int top = y + scaleCoordinate(sprite.getOffsetY(), scalePercent);
			include(
				left,
				top,
				left + scaleDimension(sprite.getWidth(), scalePercent),
				top + scaleDimension(sprite.getHeight(), scalePercent));
		}

		private void include(int left, int top, int right, int bottom)
		{
			if (!initialized)
			{
				minX = left;
				minY = top;
				maxX = right;
				maxY = bottom;
				initialized = true;
				return;
			}

			minX = Math.min(minX, left);
			minY = Math.min(minY, top);
			maxX = Math.max(maxX, right);
			maxY = Math.max(maxY, bottom);
		}

		private boolean isInitialized()
		{
			return initialized;
		}

		private int getMinX()
		{
			return minX;
		}

		private int getMinY()
		{
			return minY;
		}

		private int getMaxX()
		{
			return maxX;
		}

		private int getMaxY()
		{
			return maxY;
		}
	}
}
