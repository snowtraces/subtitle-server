package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.SubtitleLog;
import org.xinyo.subtitle.mapper.SubtitleLogMapper;
import org.xinyo.subtitle.service.SubtitleLogService;

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
}
