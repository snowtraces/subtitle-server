package org.xinyo.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xinyo.subtitle.entity.Subtitle;

import java.util.List;

public interface SubtitleMapper extends BaseMapper<Subtitle> {
    void plusDownloadTimes(String subtitleId);

    List<Subtitle> listMovieWithWeightBySubjectId(String subjectId);
}
