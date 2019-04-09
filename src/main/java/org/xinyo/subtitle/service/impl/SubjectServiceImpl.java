package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectServcie;

import java.util.List;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectServcie {

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
        wrapper.orderByDesc("year");
        wrapper.last("limit 16");
        List<Subject> list = super.list(wrapper);

        list.forEach(s -> douBanApiService.fetchPoster(s.getImgId()));

        return list;
    }
}
