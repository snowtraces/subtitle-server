package org.xinyo.subtitle.task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SubtitleSpiderThreadPool {
    private static SubtitleSpiderThreadPool instance = new SubtitleSpiderThreadPool();
    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(1, 1 , 10, TimeUnit.MINUTES,
                    new LinkedBlockingDeque<>(10000), new ThreadPoolExecutor.AbortPolicy());

    public static SubtitleSpiderThreadPool getInstance() {
        return instance;
    }

    public void submitTask(Runnable task){
        executor.submit(task);
    }
}
