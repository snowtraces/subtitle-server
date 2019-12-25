package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.mapper.SubtitleMapper;
import org.xinyo.subtitle.service.SubjectService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.WeightUtils;

import java.util.List;

@Service
public class SubtitleServiceImpl extends ServiceImpl<SubtitleMapper, Subtitle> implements SubtitleService {

    @Autowired
    private SubjectService subjectService;

    @Override
    public boolean add(Subtitle subtitle) {
        return super.save(subtitle);
    }

    @Override
    public List<Subtitle> getBySubjectId(String id) {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", id);
        return super.list(wrapper);
    }

    @Override
    public List<Subtitle> listBySubjectId(String id) {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", id);

        return super.list(wrapper);
    }

    @Override
    public List<Subtitle> listAll() {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.select("source_id", "subject_id");
        return super.list(wrapper);
    }

    @Override
    public String getSubjectIdById(String subtitleId) {
        QueryWrapper<Subtitle> wrapper = new QueryWrapper<>();
        wrapper.select("subject_id");
        wrapper.eq("id", subtitleId);
        wrapper.last("limit 1");

        Subtitle one = super.getOne(wrapper);

        return one == null ? null : one.getSubjectId();
    }

    @Override
    public boolean plusDownloadTimes(String subtitleId) {
        baseMapper.plusDownloadTimes(subtitleId);
        subjectService.plusDownloadTimesBySubtitleId(subtitleId);
        return true;
    }

    @Override
    public void updateWeight(String subtitleId, List<String> fileNameList) {
        Subtitle subtitle = new Subtitle();
        subtitle.setId(subtitleId);
        subtitle.setWeight(WeightUtils.getWeight(fileNameList));

        super.updateById(subtitle);
    }

    @Override
    public List<Subtitle> listWithWeightBySubjectId(String subjectId) {
        // 1. 判断movie/tv
        String subtype = subjectService.getSubtypeById(subjectId);

        // 2. 根据类型查询
        if ("movie".equals(subtype)) {
            return baseMapper.listMovieWithWeightBySubjectId(subjectId);
        } else {
            // TODO tv权重过滤逻辑
            return listBySubjectId(subjectId);
        }
    }
}
