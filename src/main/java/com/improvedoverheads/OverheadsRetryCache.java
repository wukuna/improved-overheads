package com.improvedoverheads;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

final class OverheadsRetryCache<K, V>
{
	private final Map<K, Entry<V>> entries = new HashMap<>();
	private final int retryCycles;

	OverheadsRetryCache(int retryCycles)
	{
		this.retryCycles = Math.max(1, retryCycles);
	}

	V get(K key, int gameCycle, Function<K, V> loader, Predicate<V> shouldRetry)
	{
		Entry<V> cached = entries.get(key);
		if (cached != null && (!shouldRetry.test(cached.value) || gameCycle < cached.retryOnGameCycle))
		{
			return cached.value;
		}

		V value = loader.apply(key);
		entries.put(key, new Entry<>(value, gameCycle + retryCycles));
		return value;
	}

	void clear()
	{
		entries.clear();
	}

	private static final class Entry<V>
	{
		private final V value;
		private final int retryOnGameCycle;

		private Entry(V value, int retryOnGameCycle)
		{
			this.value = value;
			this.retryOnGameCycle = retryOnGameCycle;
		}
	}
}
