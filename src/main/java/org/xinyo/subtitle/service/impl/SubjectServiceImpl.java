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
        boolean isNeedUpdate = subtitleLogService.isNeedUpdate(id);
        if (isNeedUpdate) {
            SubtitleSpiderThreadPool.getInstance().submitTask(new SubtitleSpiderThread(byId));
            subtitleLogService.doLog(new SubtitleLog(byId.getId()));
        }

        return byId;
    }

    @Override
    public List<Subject> getTopBySize(int size) {
        // movie
        QueryWrapper movieWrapper = buildWrapper(size / 2);
        movieWrapper.select("id", "title", "img_id", "rating");
        movieWrapper.eq("subtype", "movie");
        List<Subject> movies = super.list(movieWrapper);

        // tv
        QueryWrapper tvWrapper = buildWrapper(size / 4);
        tvWrapper.select("id", "title", "img_id", "rating");
        tvWrapper.eq("subtype", "tv");
        List<Subject> tvs = super.list(tvWrapper);
        movies.addAll(tvs);

        return movies;
    }

    private QueryWrapper buildWrapper(int size) {
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.gt("ratings_count", 10000);
        wrapper.orderByDesc("rating");
        wrapper.last("limit " + size);

        return wrapper;
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

    @Override
    public void plusDownloadTimesBySubtitleId(String subtitleId) {
        baseMapper.plusDownloadTimesBySubtitleId(subtitleId);
    }

    @Override
    public List<Subject> listHot() {
        return baseMapper.listHot();
    }

    @Override
    public String getSubtypeById(String subjectId) {
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.select("subtype");
        wrapper.eq("id", subjectId);

        Subject one = super.getOne(wrapper);
        return one.getSubtype();
    }
}
