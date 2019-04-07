package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.Subject;

import java.util.List;

public interface DouBanApiService {

    List<Subject> searchByKeyword(String title);

}
