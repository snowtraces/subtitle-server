package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.SubjectService;
import org.xinyo.subtitle.task.SubtitleSpiderThread;
import org.xinyo.subtitle.task.SubtitleSpiderThreadPool;
import org.xinyo.subtitle.util.BloomFilterUtils;

import java.util.List;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    @Override
    public Subject getById(String id) {
        Subject byId = super.getById(id);

        // 下载字幕
        boolean mightContainSubtitle = BloomFilterUtils.mightContainSubtitle(id);
        if (!mightContainSubtitle) {
            BloomFilterUtils.pushSubtitle(id);
            SubtitleSpiderThreadPool.getInstance().submitTask(new SubtitleSpiderThread(byId));
        }

        return byId;
    }

    @Override
    public List<Subject> getTopBySize(int size) {
        // TODO 热门统计逻辑
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
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
}
