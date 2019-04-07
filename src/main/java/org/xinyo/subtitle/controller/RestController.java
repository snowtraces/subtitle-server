package org.xinyo.subtitle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectServcie;

import java.util.List;

@Component
public class RestController {

    @Autowired
    private SubjectServcie subjectServcie;

    @Autowired
    private DouBanApiService douBanApiService;

    /**
     * 关键字搜索，search框提示
     */
    @RestMapping("/api/searchMovies")
    public Object searchMovies(Subject subject) {
        List<Subject> subjects = douBanApiService.searchByKeyword(subject.getTitle());

        return subjects;
    }

    /**
     * 关键字搜索，搜索列表
     */
    @RestMapping("/api/listMovies")
    public Object listMovies(Subject subject) {
        List<Subject> subjects = douBanApiService.searchByKeyword(subject.getTitle());
        return subjects;
    }

    @RestMapping("/api/movie")
    public Object getMovie(@Param("id")String id) {
        Subject subject = subjectServcie.getById(id);
        return subject;
    }
}
