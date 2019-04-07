package org.xinyo.subtitle.task;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DoubanSearchThreadPool {
    private static DoubanSearchThreadPool instance = new DoubanSearchThreadPool();
    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(1, 1, 1, TimeUnit.HOURS,
                    new LinkedBlockingDeque<>(10000), new ThreadPoolExecutor.AbortPolicy());

    public static DoubanSearchThreadPool getInstance() {
        return instance;
    }

    public Future submitTask(Runnable task){
        return executor.submit(task);
    }
}
