package org.xinyo.subtitle.task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DoubanApiThreadPool {
    private static DoubanApiThreadPool instance = new DoubanApiThreadPool();
    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(1, 2, 10, TimeUnit.MINUTES,
                    new LinkedBlockingDeque<>(10000), new ThreadPoolExecutor.AbortPolicy());

    public static DoubanApiThreadPool getInstance() {
        return instance;
    }

    public void submitTask(Runnable task){
        executor.submit(task);
    }
}
