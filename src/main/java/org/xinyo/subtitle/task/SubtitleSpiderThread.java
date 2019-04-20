package org.xinyo.subtitle.task;

import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.service.SpiderService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;

public class SubtitleSpiderThread implements Runnable, Serializable {
    private static final SpiderService spiderService = SpringContextHolder.getBean(SpiderService.class);

    private Subject subject;
    public SubtitleSpiderThread(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void run() {

        // 1. 执行爬虫
        System.err.println("开始下载字幕……");
        spiderService.doCrawl(subject);

    }
}
