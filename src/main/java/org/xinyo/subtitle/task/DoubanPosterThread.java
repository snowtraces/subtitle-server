package org.xinyo.subtitle.task;

import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.UpdateLog;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.UpdateLogService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 读取海报到本地
 */
public class DoubanPosterThread implements Runnable, Serializable {
    private Subject subject;
    private static final String DEFAULT_PREFIX = "default";

    private DouBanApiService douBanApiService = SpringContextHolder.getBean(DouBanApiService.class);
    private UpdateLogService updateLogService = SpringContextHolder.getBean(UpdateLogService.class);

    public DoubanPosterThread(Subject subject) {
        this.subject = subject;
    }

    @Override
    @Transactional
    public void run() {
        if (subject.getImgId().contains(DEFAULT_PREFIX)) {
            return;
        }

        // 1. 查询日志
        UpdateLog oldLog = updateLogService.getBySubjectId(subject.getId());
        if (oldLog != null && oldLog.getPosterUpdateTime() != null) {
            return;
        }

        // 2. 读取海报
        boolean isSuccess = douBanApiService.fetchPoster(subject);

        // 3. 写日志
        if (isSuccess) {
            UpdateLog log = new UpdateLog();
            log.setSubjectId(subject.getId());
            log.setPosterUpdateTime(LocalDateTime.now());
            updateLogService.doLog(log);
        }
    }
}
