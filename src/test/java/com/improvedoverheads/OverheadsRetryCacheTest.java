package com.improvedoverheads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class OverheadsRetryCacheTest
{
	@Test
	public void retryableFailureIsCachedBrieflyThenCanRecover()
	{
		AtomicInteger loads = new AtomicInteger();
		OverheadsRetryCache<Integer, String> cache = new OverheadsRetryCache<>(10);

		assertNull(cache.get(1, 0, ignored -> loads.getAndIncrement() == 0 ? null : "loaded", Objects::isNull));
		assertNull(cache.get(1, 9, ignored -> "unexpected", Objects::isNull));
		assertEquals("loaded", cache.get(1, 10, ignored -> loads.getAndIncrement() == 0 ? null : "loaded", Objects::isNull));
		assertEquals("loaded", cache.get(1, 100, ignored -> "unexpected", Objects::isNull));
		assertEquals(2, loads.get());
	}

	@Test
	public void clearForcesSuccessfulValuesToReload()
	{
		AtomicInteger loads = new AtomicInteger();
		OverheadsRetryCache<Integer, Integer> cache = new OverheadsRetryCache<>(10);

		assertEquals(Integer.valueOf(1), cache.get(1, 0, ignored -> loads.incrementAndGet(), ignored -> false));
		cache.clear();
		assertEquals(Integer.valueOf(2), cache.get(1, 1, ignored -> loads.incrementAndGet(), ignored -> false));
	}
}
