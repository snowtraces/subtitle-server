package org.xinyo.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xinyo.subtitle.entity.Subtitle;

public interface SubtitleMapper extends BaseMapper<Subtitle> {
    void plusDownloadTimes(String subtitleId);
}
