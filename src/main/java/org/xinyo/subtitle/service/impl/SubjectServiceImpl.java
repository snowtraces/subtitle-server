package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.SubtitleLog;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.SubjectService;
import org.xinyo.subtitle.service.SubtitleLogService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.task.SubtitleSpiderThread;
import org.xinyo.subtitle.task.SubtitleSpiderThreadPool;

import java.util.List;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {
    @Autowired
    private SubtitleLogService subtitleLogService;
    @Autowired
    private SubtitleService subtitleService;

    @Override
    public Subject getById(String id) {
        Subject byId = super.getById(id);

        // 下载字幕
        boolean isNeedUpdate = subtitleLogService.checkIsNeedUpdate(id);
        if (isNeedUpdate) {
            SubtitleSpiderThreadPool.getInstance().submitTask(new SubtitleSpiderThread(byId));
            subtitleLogService.doLog(new SubtitleLog(byId.getId()));
        }

        return byId;
    }

    @Override
    public List<Subject> getTopBySize(int size) {
        // TODO 热门统计逻辑
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.gt("ratings_count", 10000);
        wrapper.orderByDesc("rating");
        wrapper.last("limit " + size);
        List<Subject> list = super.list(wrapper);

        return list;
    }

    @Override
    @Transactional
    public boolean save(List<Subject> subjects) {
        if (subjects == null || subjects.size() == 0) {
            return true;
        }

        return super.saveOrUpdateBatch(subjects);
    }

    @Override
    @Transactional
    public boolean saveDetail(SubjectVO subjectVO) {
        Subject subject = new Subject(subjectVO);
        return super.updateById(subject);
    }

    @Override
    public Subject getBySubtitleId(String subtitleId) {
        String subjectId = subtitleService.getSubjectIdById(subtitleId);

        return super.getById(subjectId);
    }
}
