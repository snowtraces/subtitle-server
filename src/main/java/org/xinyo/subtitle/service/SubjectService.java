package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;

import java.util.List;

public interface SubjectService {
    Subject getById(String id);

    List<Subject> getTopBySize(int size);

    boolean save(List<Subject> subjects);

    boolean saveDetail(SubjectVO subjectVO);
}
