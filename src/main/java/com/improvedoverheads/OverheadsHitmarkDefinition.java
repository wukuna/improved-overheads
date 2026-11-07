package com.improvedoverheads;

import java.nio.charset.Charset;
import net.runelite.api.Client;
import net.runelite.api.HitsplatID;
import net.runelite.api.gameval.SpriteID;

final class OverheadsHitmarkDefinition
{
	static final int TRANSFORM_UNAVAILABLE = Integer.MIN_VALUE;

	private static final int HITMARK_CONFIG_ARCHIVE = 32;
	private static final Charset CP1252 = Charset.forName("windows-1252");

	private final int firstSpriteId;
	private final int middleSpriteId;
	private final int secondSpriteId;
	private final int lastSpriteId;
	private final int textColor;
	private final String textTemplate;
	private final int duration;
	private final int textYOffset;
	private final int transformVarbit;
	private final int transformVarp;
	private final int[] transforms;
	private final boolean fallbackDefinition;

	private OverheadsHitmarkDefinition(
		int firstSpriteId,
		int middleSpriteId,
		int secondSpriteId,
		int lastSpriteId,
		int textColor,
		String textTemplate,
		int duration,
		int textYOffset,
		int transformVarbit,
		int transformVarp,
		int[] transforms,
		boolean fallbackDefinition)
	{
		this.firstSpriteId = firstSpriteId;
		this.middleSpriteId = middleSpriteId;
		this.secondSpriteId = secondSpriteId;
		this.lastSpriteId = lastSpriteId;
		this.textColor = textColor;
		this.textTemplate = textTemplate;
		this.duration = duration;
		this.textYOffset = textYOffset;
		this.transformVarbit = transformVarbit;
		this.transformVarp = transformVarp;
		this.transforms = transforms;
		this.fallbackDefinition = fallbackDefinition;
	}

	static OverheadsHitmarkDefinition load(Client client, int hitsplatType)
	{
		try
		{
			byte[] data = client.getIndexConfig().loadData(HITMARK_CONFIG_ARCHIVE, hitsplatType);
			return decode(data, hitsplatType);
		}
		catch (RuntimeException ex)
		{
			return fallback(hitsplatType);
		}
	}

	static OverheadsHitmarkDefinition decode(byte[] data, int hitsplatType)
	{
		if (data == null)
		{
			return fallback(hitsplatType);
		}

		DecodedDefinition decoded = new DecodedDefinition();
		Buffer buffer = new Buffer(data);
		while (true)
		{
			int opcode = buffer.readUnsignedByte();
			if (opcode == 0)
			{
				return decoded.toDefinition();
			}

			decoded.decodeNext(buffer, opcode);
		}
	}

	boolean hasTransforms()
	{
		return transforms != null;
	}

	boolean hasRenderableContent()
	{
		return firstSpriteId >= 0
			|| middleSpriteId >= 0
			|| secondSpriteId >= 0
			|| lastSpriteId >= 0
			|| !textTemplate.isEmpty();
	}

	boolean isFallbackDefinition()
	{
		return fallbackDefinition;
	}

	int getTransformedType(Client client)
	{
		if (transforms == null)
		{
			return -1;
		}

		int selector = -1;
		try
		{
			if (transformVarbit != -1)
			{
				selector = client.getVarbitValue(transformVarbit);
			}
			else if (transformVarp != -1)
			{
				selector = client.getVarpValue(transformVarp);
			}
		}
		catch (RuntimeException ex)
		{
			return TRANSFORM_UNAVAILABLE;
		}

		return getTransformedTypeForSelector(selector);
	}

	int getTransformedTypeForSelector(int selector)
	{
		if (transforms == null)
		{
			return -1;
		}

		int index = selector >= 0 && selector < transforms.length - 1
			? selector
			: transforms.length - 1;
		return transforms[index];
	}

	String formatAmount(int amount)
	{
		String formatted = textTemplate;
		while (true)
		{
			int index = formatted.indexOf("%1");
			if (index < 0)
			{
				return formatted;
			}

			formatted = formatted.substring(0, index) + amount + formatted.substring(index + 2);
		}
	}

	int getFirstSpriteId()
	{
		return firstSpriteId;
	}

	int getMiddleSpriteId()
	{
		return middleSpriteId;
	}

	int getSecondSpriteId()
	{
		return secondSpriteId;
	}

	int getLastSpriteId()
	{
		return lastSpriteId;
	}

	int getTextColor()
	{
		return textColor;
	}

	int getDuration()
	{
		return duration;
	}

	int appearanceGameCycle(int disappearsOnGameCycle)
	{
		return disappearsOnGameCycle - duration;
	}

	int getTextYOffset()
	{
		return textYOffset;
	}

	private static OverheadsHitmarkDefinition fallback(int hitsplatType)
	{
		return new OverheadsHitmarkDefinition(
			fallbackSpriteId(hitsplatType),
			-1,
			-1,
			-1,
			0xFFFFFF,
			"%1",
			0,
			0,
			-1,
			-1,
			null,
			true);
	}

	private static int fallbackSpriteId(int hitsplatType)
	{
		switch (hitsplatType)
		{
			case HitsplatID.BLOCK_ME:
			case HitsplatID.BLOCK_OTHER:
				return SpriteID.Hitmark.HITSPLAT_BLUE_MISS;
			case HitsplatID.POISON:
				return SpriteID.Hitmark.HITSPLAT_GREEN_POISON;
			case HitsplatID.VENOM:
				return SpriteID.Hitmark.HITSPLAT_DARK_GREEN_VENOM;
			case HitsplatID.DOOM:
				return SpriteID.Hitmark.COLOSSEUM_DOOM;
			case HitsplatID.BURN:
				return SpriteID.Hitmark.BURN_DAMAGE;
			default:
				return SpriteID.Hitmark._1;
		}
	}

	private static final class DecodedDefinition
	{
		private int firstSpriteId = -1;
		private int middleSpriteId = -1;
		private int secondSpriteId = -1;
		private int lastSpriteId = -1;
		private int textColor = 0xFFFFFF;
		private String textTemplate = "";
		private int duration = 70;
		private int textYOffset;
		private int transformVarbit = -1;
		private int transformVarp = -1;
		private int[] transforms;

		private void decodeNext(Buffer buffer, int opcode)
		{
			switch (opcode)
			{
				case 1:
					buffer.readNullableLargeSmart();
					break;
				case 2:
					textColor = buffer.readMedium();
					break;
				case 3:
					firstSpriteId = buffer.readNullableLargeSmart();
					break;
				case 4:
					secondSpriteId = buffer.readNullableLargeSmart();
					break;
				case 5:
					middleSpriteId = buffer.readNullableLargeSmart();
					break;
				case 6:
					lastSpriteId = buffer.readNullableLargeSmart();
					break;
				case 7:
					buffer.readShort();
					break;
				case 8:
					textTemplate = buffer.readStringCp1252NullCircumfixed();
					break;
				case 9:
					duration = buffer.readUnsignedShort();
					break;
				case 10:
					buffer.readShort();
					break;
				case 11:
					break;
				case 12:
					buffer.readUnsignedByte();
					break;
				case 13:
					textYOffset = buffer.readShort();
					break;
				case 14:
					buffer.readUnsignedShort();
					break;
				case 15:
				case 16:
					break;
				case 17:
				case 18:
					decodeTransforms(buffer, opcode == 18);
					break;
				default:
					throw new IllegalArgumentException("Unknown hitmark opcode " + opcode);
			}
		}

		private void decodeTransforms(Buffer buffer, boolean hasFallback)
		{
			transformVarbit = normalizeUnsignedShort(buffer.readUnsignedShort());
			transformVarp = normalizeUnsignedShort(buffer.readUnsignedShort());
			int fallbackTransform = -1;
			if (hasFallback)
			{
				fallbackTransform = normalizeUnsignedShort(buffer.readUnsignedShort());
			}

			int count = buffer.readUnsignedByte();
			transforms = new int[count + 2];
			for (int i = 0; i <= count; i++)
			{
				transforms[i] = normalizeUnsignedShort(buffer.readUnsignedShort());
			}
			transforms[count + 1] = fallbackTransform;
		}

		private OverheadsHitmarkDefinition toDefinition()
		{
			return new OverheadsHitmarkDefinition(
				firstSpriteId,
				middleSpriteId,
				secondSpriteId,
				lastSpriteId,
				textColor,
				textTemplate,
				duration,
				textYOffset,
				transformVarbit,
				transformVarp,
				transforms,
				false);
		}

		private static int normalizeUnsignedShort(int value)
		{
			return value == 65535 ? -1 : value;
		}
	}

	private static final class Buffer
	{
		private final byte[] data;
		private int offset;

		private Buffer(byte[] data)
		{
			this.data = data;
		}

		private int readUnsignedByte()
		{
			return data[offset++] & 0xFF;
		}

		private int readUnsignedShort()
		{
			offset += 2;
			return ((data[offset - 2] & 0xFF) << 8) | (data[offset - 1] & 0xFF);
		}

		private int readShort()
		{
			int value = readUnsignedShort();
			return value > 32767 ? value - 65536 : value;
		}

		private int readMedium()
		{
			offset += 3;
			return ((data[offset - 3] & 0xFF) << 16)
				| ((data[offset - 2] & 0xFF) << 8)
				| (data[offset - 1] & 0xFF);
		}

		private int readInt()
		{
			offset += 4;
			return ((data[offset - 4] & 0xFF) << 24)
				| ((data[offset - 3] & 0xFF) << 16)
				| ((data[offset - 2] & 0xFF) << 8)
				| (data[offset - 1] & 0xFF);
		}

		private int readNullableLargeSmart()
		{
			if (data[offset] < 0)
			{
				return readInt() & Integer.MAX_VALUE;
			}

			int value = readUnsignedShort();
			return value == 32767 ? -1 : value;
		}

		private String readStringCp1252NullCircumfixed()
		{
			if (readUnsignedByte() != 0)
			{
				throw new IllegalStateException("Expected circumfixed string prefix");
			}

			int start = offset;
			while (data[offset++] != 0)
			{

			}

			int length = offset - start - 1;
			return length == 0 ? "" : new String(data, start, length, CP1252);
		}
	}
}
