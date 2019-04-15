package org.xinyo.subtitle.task;

import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.service.SpiderService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;
import java.util.List;

public class SubtitleSpiderThread implements Runnable, Serializable {
    private static final SpiderService spiderService = SpringContextHolder.getBean(SpiderService.class);
    private static final SubtitleService subtitleService = SpringContextHolder.getBean(SubtitleService.class);

    private Subject subject;
    public SubtitleSpiderThread(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void run() {
        // 1. 判断记录
        List<Subtitle> list = subtitleService.getBySubjectId(subject.getId());
        if (list != null && list.size() > 0) {
            return;
        }

        // 2. 执行爬虫
        System.err.println("开始下载字幕……");
        spiderService.doCrawl(subject);
    }
}
