package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.douban.UpdateLog;
import org.xinyo.subtitle.mapper.UpdataLogMapper;
import org.xinyo.subtitle.service.UpdateLogService;
@Service
public class UpdateLogServiceImpl extends ServiceImpl<UpdataLogMapper, UpdateLog> implements UpdateLogService {
    @Override
    public void doLog(UpdateLog log) {
        super.saveOrUpdate(log);
    }

    @Override
    public UpdateLog getBySubjectId(String id) {
        return super.getById(id);
    }
}
