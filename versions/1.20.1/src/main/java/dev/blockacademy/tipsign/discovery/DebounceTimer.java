package dev.blockacademy.tipsign.discovery;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 5-second debounce timer for rapid edits.
 * Uses AtomicReference<ScheduledFuture> for thread-safe cancel/reschedule.
 */
public class DebounceTimer {

    private final ScheduledExecutorService executor;
    private final long delayMs;
    private final AtomicReference<ScheduledFuture<?>> pending = new AtomicReference<>();

    public DebounceTimer(ScheduledExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    /**
     * Schedule a task, cancelling any previously pending one.
     */
    public void schedule(Runnable task) {
        ScheduledFuture<?> old = pending.getAndSet(
            executor.schedule(() -> {
                pending.set(null);
                task.run();
            }, delayMs, TimeUnit.MILLISECONDS)
        );
        if (old != null) {
            old.cancel(false);
        }
    }

    /**
     * Cancel any pending task.
     */
    public void cancel() {
        ScheduledFuture<?> old = pending.getAndSet(null);
        if (old != null) {
            old.cancel(false);
        }
    }
}
