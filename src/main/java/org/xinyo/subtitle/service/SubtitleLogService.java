package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.SubtitleLog;

import java.util.List;

public interface SubtitleLogService {
    void doLog(SubtitleLog subtitleLog);

    List<SubtitleLog> listAll();

    boolean isNeedUpdate(String subjectId);
}
