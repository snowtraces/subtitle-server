package org.xinyo.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xinyo.subtitle.entity.douban.Subject;

import java.util.List;

@Repository
public interface SubjectMapper extends BaseMapper<Subject> {
    void plusDownloadTimesBySubtitleId(String subtitleId);

    List<Subject> listHot();
}
