package org.xinyo.subtitle.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;

import java.util.List;

public interface DouBanApiService {

    SearchResultVO search(String keyword, Integer start, Integer count);

    List<Subject> searchByKeyword(String title);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    List<Subject> save(List<SubjectVO> subjects);
}
