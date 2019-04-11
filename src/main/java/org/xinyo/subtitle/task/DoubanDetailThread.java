package org.xinyo.subtitle.task;

import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.UpdateLog;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectServcie;
import org.xinyo.subtitle.service.UpdateLogService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询电影详情
 */
public class DoubanDetailThread implements Runnable, Serializable {


    private Subject subject;
    private UpdateLogService updateLogService = SpringContextHolder.getBean(UpdateLogService.class);
    private DouBanApiService douBanApiService = SpringContextHolder.getBean(DouBanApiService.class);
    private SubjectServcie subjectServcie = SpringContextHolder.getBean(SubjectServcie.class);

    public DoubanDetailThread(Subject subject) {
        this.subject = subject;
    }

    @Override
    @Transactional
    public void run() {

        // 1. 查询日志
        UpdateLog oldLog = updateLogService.getBySubjectId(subject.getId());
        if (oldLog != null && oldLog.getBaseUpdateTime() != null) {
            return;
        }

        // 2. 读取详情
        SubjectVO subjectVO = douBanApiService.searchDetail(subject);
        boolean saveResult = subjectServcie.saveDetail(subjectVO);

        // 3. 写日志
        if (saveResult) {
            UpdateLog log = new UpdateLog();
            log.setSubjectId(subject.getId());
            log.setBaseUpdateTime(LocalDateTime.now());
            updateLogService.doLog(log);
        }
    }
}
