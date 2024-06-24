package io.hhplus.tdd.util;


import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
public class TaskUtil {
    private final ConcurrentHashMap<Long, Task> taskMap = new ConcurrentHashMap<>();

    public Future<?> executeTask(long userId, Runnable task) {
        return taskMap.computeIfAbsent(userId, k -> new Task()).execute(task);
    }

}
