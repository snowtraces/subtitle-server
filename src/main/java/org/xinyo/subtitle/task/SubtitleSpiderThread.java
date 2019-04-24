package org.xinyo.subtitle.task;

import lombok.extern.log4j.Log4j2;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.service.SpiderService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;

@Log4j2
public class SubtitleSpiderThread implements Runnable, Serializable {
    private static final SpiderService spiderService = SpringContextHolder.getBean(SpiderService.class);

    private Subject subject;
    public SubtitleSpiderThread(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void run() {

        // 1. 执行爬虫
        spiderService.doCrawl(subject);

    }
}
