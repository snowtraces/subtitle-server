package org.xinyo.subtitle.task;

import org.xinyo.subtitle.entity.SubtitleLog;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.service.SpiderService;
import org.xinyo.subtitle.service.SubtitleLogService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;

public class SubtitleSpiderThread implements Runnable, Serializable {
    private static final SpiderService spiderService = SpringContextHolder.getBean(SpiderService.class);
    private static final SubtitleLogService subtitleLogService = SpringContextHolder.getBean(SubtitleLogService.class);

    private Subject subject;
    public SubtitleSpiderThread(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void run() {

        // 1. 执行爬虫
        System.err.println("开始下载字幕……");
        spiderService.doCrawl(subject);

        // 2. 写日志
        subtitleLogService.doLog(new SubtitleLog(subject.getId()));
    }
}