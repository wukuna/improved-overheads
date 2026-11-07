package com.improvedoverheads;

import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.SpritePixels;

final class OverheadsSprite
{
	private final BufferedImage image;
	private final int width;
	private final int height;
	private final int maxWidth;
	private final int maxHeight;
	private final int offsetX;
	private final int offsetY;
	private volatile BufferedImage xbr2xImage;

	OverheadsSprite(
		BufferedImage image,
		int offsetX,
		int offsetY)
	{
		this(image, image.getWidth(), image.getHeight(), offsetX, offsetY);
	}

	OverheadsSprite(
		BufferedImage image,
		int maxWidth,
		int maxHeight,
		int offsetX,
		int offsetY)
	{
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.maxWidth = Math.max(this.width, maxWidth);
		this.maxHeight = Math.max(this.height, maxHeight);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	static OverheadsSprite load(Client client, int spriteId)
	{
		if (spriteId < 0)
		{
			return null;
		}

		try
		{
			SpritePixels[] sprites = client.getSprites(client.getIndexSprites(), spriteId, 0);
			if (sprites == null || sprites.length == 0 || sprites[0] == null)
			{
				return null;
			}

			SpritePixels sprite = sprites[0];
			BufferedImage image = sprite.toBufferedImage();
			if (image == null)
			{
				return null;
			}

			return new OverheadsSprite(
				image,
				sprite.getMaxWidth(),
				sprite.getMaxHeight(),
				sprite.getOffsetX(),
				sprite.getOffsetY());
		}
		catch (RuntimeException ex)
		{
			return null;
		}
	}

	BufferedImage getImage()
	{
		return image;
	}

	BufferedImage getImageForScaling(
		OverheadsSpriteScalingMode scalingMode,
		int targetWidth,
		int targetHeight)
	{
		if (scalingMode != OverheadsSpriteScalingMode.XBR
			|| targetWidth <= width && targetHeight <= height)
		{
			return image;
		}

		BufferedImage scaled = xbr2xImage;
		if (scaled == null)
		{
			synchronized (this)
			{
				scaled = xbr2xImage;
				if (scaled == null)
				{
					scaled = OverheadsXbrScaler.scale2x(image);
					xbr2xImage = scaled;
				}
			}
		}
		return scaled;
	}

	int getWidth()
	{
		return width;
	}

	int getHeight()
	{
		return height;
	}

	int getMaxWidth()
	{
		return maxWidth;
	}

	int getMaxHeight()
	{
		return maxHeight;
	}

	int getOffsetX()
	{
		return offsetX;
	}

	int getOffsetY()
	{
		return offsetY;
	}
}
