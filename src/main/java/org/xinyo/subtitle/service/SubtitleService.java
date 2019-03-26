package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.SRTSubtitleUnit;

import java.util.List;

public interface SubtitleService {


    List<SRTSubtitleUnit> readSubtitle(List<String> lines);
}
