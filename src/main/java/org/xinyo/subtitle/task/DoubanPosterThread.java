package org.xinyo.subtitle.task;

import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.RequestUtils;

import java.io.Serializable;

/**
 * 读取海报到本地
 */
public class DoubanPosterThread implements Runnable, Serializable {

    private String url;
    private static final String DEFAULT_PREFIX = "movie_default";

    public DoubanPosterThread(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        if (url.contains(DEFAULT_PREFIX)) {
            return;
        }

        System.err.println("开始读取海报……");
        String path = FileUtils.createPosterPath();

        RequestUtils.fetchBinary(url, path);

        // TODO 写读取记录
    }
}
