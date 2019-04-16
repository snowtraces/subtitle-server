package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.SRTSubtitleUnit;
import org.xinyo.subtitle.entity.Subtitle;

import java.util.List;

public interface SubtitleService {


    List<SRTSubtitleUnit> readSubtitle(List<String> lines);

    boolean add(Subtitle subtitle);

    List<Subtitle> getBySubjectId(String id);

    List<Subtitle> listBySubjectId(String id);
}
