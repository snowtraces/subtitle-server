package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;

import java.util.List;

public interface DouBanApiService {

    SearchResultVO search(String keyword, Integer start, Integer count);

    List<Subject> searchByKeyword(String title);

    List<Subject> save(List<SubjectVO> subjects);

    void fetchPoster(Subject imgId);
}
