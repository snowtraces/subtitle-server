package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectService;

import java.util.List;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    @Autowired
    private DouBanApiService douBanApiService;

    @Override
    public Subject getById(String id) {
        return super.getById(id);
    }

    @Override
    public List<Subject> getTop16() {
        // TODO 热门统计逻辑
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("rating");
        wrapper.last("limit 16");
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
