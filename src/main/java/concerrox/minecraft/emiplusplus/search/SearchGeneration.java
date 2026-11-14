package concerrox.minecraft.emiplusplus.search;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Generation counter for EMI search results. Every result set EMI accepts gets a
 * new generation; grouped results may only be published while their generation
 * is still the latest one. Guarded by the EmiSearch class monitor, the same lock
 * EMI uses in EmiSearch.apply.
 */
public final class SearchGeneration {
    private static final Map<Object, Long> ACCEPTED = Collections.synchronizedMap(new WeakHashMap<>());
    private static long latest;

    private SearchGeneration() {}

    /** Called inside EMI's synchronized apply, right after it stored the worker's results. */
    public static void accept(Object worker) {
        latest++;
        ACCEPTED.put(worker, latest);
    }

    /** Generation EMI gave this worker, or 0 when EMI discarded its results. */
    public static long of(Object worker) {
        Long g = ACCEPTED.remove(worker);
        return g == null ? 0 : g;
    }

    /** Must be called while holding the EmiSearch class monitor. */
    public static boolean isLatest(long generation) {
        return generation != 0 && generation == latest;
    }
}
