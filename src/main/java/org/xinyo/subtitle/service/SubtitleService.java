package org.xinyo.subtitle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xinyo.subtitle.entity.Subtitle;

import java.util.List;

public interface SubtitleService extends IService<Subtitle> {

    boolean add(Subtitle subtitle);

    List<Subtitle> getBySubjectId(String id);

    List<Subtitle> listBySubjectId(String id);

    List<Subtitle> listAll();

    String getSubjectIdById(String subtitleId);

    boolean plusDownloadTimes(String subtitleId);

    void updateWeight(String subtitleId, List<String> fileNameList);

    List<Subtitle> listWithWeightBySubjectId(String id);
}
