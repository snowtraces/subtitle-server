package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.SubtitleLog;
import org.xinyo.subtitle.mapper.SubtitleLogMapper;
import org.xinyo.subtitle.service.SubtitleLogService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubtitleLogServiceImpl extends ServiceImpl<SubtitleLogMapper, SubtitleLog> implements SubtitleLogService {
    @Override
    public void doLog(SubtitleLog subtitleLog) {
        super.saveOrUpdate(subtitleLog);
    }

    @Override
    public List<SubtitleLog> listAll() {
        return super.list();
    }

    @Override
    public boolean isNeedUpdate(String subjectId) {
        QueryWrapper<SubtitleLog> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", subjectId);

        List<SubtitleLog> list = super.list(wrapper);

        if (list == null || list.size() == 0) {
            return true;
        } else {
            LocalDateTime updateTime = list.get(0).getUpdateTime();
            return updateTime.isBefore(LocalDateTime.now().minusDays(7));
        }
    }
}
