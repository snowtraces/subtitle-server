package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.Subject;

import java.util.List;

public interface SubjectServcie {
    Subject getById(String id);

    List<Subject> getTop16();
}
