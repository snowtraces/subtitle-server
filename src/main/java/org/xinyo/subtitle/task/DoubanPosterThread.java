package org.xinyo.subtitle.task;

import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.UpdateLog;
import org.xinyo.subtitle.service.UpdateLogService;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.RequestUtils;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 读取海报到本地
 */
public class DoubanPosterThread implements Runnable, Serializable {
    private static final String POSTER_URL = "http://img1.doubanio.com/view/photo/s_ratio_poster/public/%s.webp?apikey=0df993c66c0c636e29ecbb5344252a4a";
    private static final String POSTER_URL_S = "https://img3.doubanio.com/view/subject/s/public/%s.webp?apikey=0df993c66c0c636e29ecbb5344252a4a";

    private Subject subject;
    private String url;
    private static final String DEFAULT_PREFIX = "movie_default";
    private UpdateLogService updateLogService = SpringContextHolder.getBean(UpdateLogService.class);

    public DoubanPosterThread(Subject subject) {
        this.subject = subject;
        String imgId = subject.getImgId();
        this.url = imgId.startsWith("s") ? String.format(POSTER_URL_S, imgId) : String.format(POSTER_URL, imgId);
    }

    @Override
    public void run() {
        if (url.contains(DEFAULT_PREFIX)) {
            return;
        }

        // 1. 查询日志
        UpdateLog oldLog = updateLogService.getBySubjectId(subject.getId());
        if (oldLog != null && oldLog.getPosterUpdateTime() != null) {
            return;
        }

        // 2. 读取海报
        System.err.println("开始读取海报……");
        String bathPath = "/Users/CHENG/CODE/Projects/subtitle-angular/src/assets";
        String path = FileUtils.createPosterPath(bathPath);

        RequestUtils.fetchBinary(url, path);

        // 3. 写日志
        UpdateLog log = new UpdateLog();
        log.setSubjectId(subject.getId());
        log.setPosterUpdateTime(LocalDateTime.now());
        updateLogService.doLog(log);
    }
}
