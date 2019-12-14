package org.xinyo.subtitle.task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SubtitleFileThreadPool {
    private static SubtitleFileThreadPool instance = new SubtitleFileThreadPool();
    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(2, 2 , 10, TimeUnit.MINUTES,
                    new LinkedBlockingDeque<>(10000), new ThreadPoolExecutor.AbortPolicy());

    public static SubtitleFileThreadPool getInstance() {
        return instance;
    }

    public void submitTask(Runnable task){
        executor.submit(task);
    }
}
