package com.improvedoverheads;

final class OverheadsHitsplat
{
	private final int hitsplatType;
	private final int amount;
	private final int position;
	private final int appearsOnGameCycle;
	private final int reuseAgeStartsOnGameCycle;
	private final int fullOpacityStartsOnGameCycle;
	private final int fadeOutStartsOnGameCycle;
	private final int expiresOnGameCycle;
	private final long sequence;
	private final boolean mine;

	OverheadsHitsplat(
		int hitsplatType,
		int amount,
		int position,
		int appearsOnGameCycle,
		int reuseAgeStartsOnGameCycle,
		int fullOpacityStartsOnGameCycle,
		int fadeOutStartsOnGameCycle,
		int expiresOnGameCycle,
		long sequence,
		boolean mine)
	{
		this.hitsplatType = hitsplatType;
		this.amount = amount;
		this.position = position;
		this.appearsOnGameCycle = appearsOnGameCycle;
		this.reuseAgeStartsOnGameCycle = reuseAgeStartsOnGameCycle;
		this.fullOpacityStartsOnGameCycle = fullOpacityStartsOnGameCycle;
		this.fadeOutStartsOnGameCycle = fadeOutStartsOnGameCycle;
		this.expiresOnGameCycle = expiresOnGameCycle;
		this.sequence = sequence;
		this.mine = mine;
	}

	int getHitsplatType()
	{
		return hitsplatType;
	}

	int getAmount()
	{
		return amount;
	}

	int getPosition()
	{
		return position;
	}

	int getReuseAgeStartsOnGameCycle()
	{
		return reuseAgeStartsOnGameCycle;
	}

	long getSequence()
	{
		return sequence;
	}

	boolean isMine()
	{
		return mine;
	}

	boolean isExpired(int gameCycle)
	{
		return expiresOnGameCycle <= gameCycle;
	}

	boolean isOutsidePositionLimit(int maxHitsplats)
	{
		return maxHitsplats > 0 && position >= maxHitsplats;
	}

	float getAlpha(int gameCycle)
	{
		if (gameCycle < appearsOnGameCycle)
		{
			return 0.0f;
		}

		if (gameCycle < fullOpacityStartsOnGameCycle)
		{
			return progressBetween(gameCycle, appearsOnGameCycle, fullOpacityStartsOnGameCycle);
		}

		if (gameCycle < fadeOutStartsOnGameCycle)
		{
			return 1.0f;
		}

		if (expiresOnGameCycle <= fadeOutStartsOnGameCycle)
		{
			return 0.0f;
		}

		return 1.0f - progressBetween(gameCycle, fadeOutStartsOnGameCycle, expiresOnGameCycle);
	}

    float getProgress(int gameCycle)
    {
        return progressBetween(gameCycle, appearsOnGameCycle, expiresOnGameCycle);
    }

	private static float progressBetween(int gameCycle, int startsOnGameCycle, int endsOnGameCycle)
	{
		int cycles = endsOnGameCycle - startsOnGameCycle;
		if (cycles <= 0)
		{
			return 1.0f;
		}

		float progress = (gameCycle - startsOnGameCycle) / (float) cycles;
		return Math.max(0.0f, Math.min(1.0f, progress));
	}
}
