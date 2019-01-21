package hotchemi.com.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link hotchemi.com.github.LruCache}.
 *
 * @author Shintaro Katafuchi
 */
public class LruCacheTest {

    private static final String A = "A";

    private static final String B = "B";

    private static final String C = "C";

    private static final String D = "D";

    private static final String E = "E";

    private LruCache<String, String> cache;

    private static void assertMiss(LruCache<String, String> cache, String key) {
        assertNull(cache.get(key));
    }

    private static void assertHit(LruCache<String, String> cache, String key, String value) {
        assertThat(cache.get(key), is(value));
    }

    private static void assertSnapshot(LruCache<String, String> cache, String... keysAndValues) {
        List<String> actualKeysAndValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : cache.snapshot().entrySet()) {
            actualKeysAndValues.add(entry.getKey());
            actualKeysAndValues.add(entry.getValue());
        }
        assertEquals(Arrays.asList(keysAndValues), actualKeysAndValues);
    }

    @After
    public void tearDown() {
        cache.clear();
        cache = null;
    }

//    @Test
//    public void defaultMemorySize() {
//        assertThat(cache.getMaxMemorySize(), is(3 * 1024 * 1024));
//    }

    @Test
    public void logic() {
        cache = new LruCache<>(3);
        Assert.assertEquals(0, cache.getMemorySize());
        cache.put("a", A);
        assertHit(cache, "a", A);
        cache.put("b", B);
        assertHit(cache, "a", A);
        Assert.assertEquals(12, cache.getMemorySize());
        assertHit(cache, "b", B);
        assertSnapshot(cache, "a", A, "b", B);

        cache.put("c", C);
        assertHit(cache, "a", A);
        assertHit(cache, "b", B);
        assertHit(cache, "c", C);
        assertSnapshot(cache, "a", A, "b", B, "c", C);

        Assert.assertEquals(18, cache.getMemorySize());

        cache.put("d", D);
        assertMiss(cache, "a");
        assertHit(cache, "b", B);
        assertHit(cache, "c", C);
        assertHit(cache, "d", D);
        assertHit(cache, "b", B);
        assertHit(cache, "c", C);
        assertSnapshot(cache, "d", D, "b", B, "c", C);

        Assert.assertEquals(18, cache.getMemorySize());

        cache.put("e", E);
        assertMiss(cache, "d");
        assertMiss(cache, "a");
        assertHit(cache, "e", E);
        assertHit(cache, "b", B);
        assertHit(cache, "c", C);
        assertSnapshot(cache, "e", E, "b", B, "c", C);
    }

    @Test
    public void concurrentLogic() throws InterruptedException {
        cache = new LruCache<>(1_000);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch countDownLatch = new CountDownLatch(2);
        Runnable runnableA = () -> {
            for(int i = 0; i < 100; i++) {
                cache.put(String.valueOf(i), String.valueOf(i));
                cache.get(String.valueOf(i));
            }
            countDownLatch.countDown();
        };

        executorService.execute(runnableA);
        executorService.execute(runnableA);

        countDownLatch.await(1, TimeUnit.MINUTES); // waits for executions to be over

        Assert.assertTrue(cache.getMemorySize() == 1200);
    }

    @Test
    public void constructorDoesNotAllowZeroCacheSize() {
        try {
            new LruCache(0);
            fail();
        } catch (IllegalArgumentException expected) {
            //nothing
        }
    }

    @Test
    public void evictionWithSingletonCache() {
        LruCache<String, String> cache = new LruCache<>(1);
        cache.put("a", A);
        cache.put("b", B);
        assertSnapshot(cache, "b", B);
    }

    /**
     * Replacing the value for a key doesn't cause an eviction but it does bring the replaced entry to
     * the front of the queue.
     */
    @Test
    public void putCauseEviction() {
        cache = new LruCache<>(3);
        cache.put("a", A);
        cache.put("b", B);
        cache.put("c", C);
        cache.put("b", D);
        assertSnapshot(cache, "a", A, "c", C, "b", D);
    }

    @Test
    public void clear() {
        cache = new LruCache<>(3);
        cache.put("a", "a");
        cache.put("b", "b");
        cache.put("c", "c");
        cache.clear();
        assertThat(cache.snapshot().size(), is(0));
    }

}