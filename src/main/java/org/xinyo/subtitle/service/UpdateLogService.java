package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.UpdateLog;

public interface UpdateLogService {
    void doLog(UpdateLog log);

    UpdateLog getBySubjectId(String id);

    void emptyPosterUpdateTime(String id);
}
