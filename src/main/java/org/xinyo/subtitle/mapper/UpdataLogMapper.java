package org.xinyo.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xinyo.subtitle.entity.douban.UpdateLog;

public interface UpdataLogMapper extends BaseMapper<UpdateLog> {
    void emptyPosterUpdateTime(String subjectId);
}
