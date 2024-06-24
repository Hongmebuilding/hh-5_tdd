package io.hhplus.tdd.util;

import java.util.concurrent.*;

public class Task {
    private final ThreadPoolExecutor executor;

    BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
    public Task() {
        this.executor = new ThreadPoolExecutor(
                1,
                1,
                0L, TimeUnit.MILLISECONDS,
                q
        );
    }

    public Future<?> execute(Runnable task) {
        return executor.submit(task);
    }

    public void executeRunnable(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }
}