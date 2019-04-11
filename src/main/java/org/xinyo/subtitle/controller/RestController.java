package org.xinyo.subtitle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectService;

import java.util.List;

@Component
public class RestController {

    private final SubjectService subjectServcie;

    private final DouBanApiService douBanApiService;

    @Autowired
    public RestController(SubjectService subjectServcie, DouBanApiService douBanApiService) {
        this.subjectServcie = subjectServcie;
        this.douBanApiService = douBanApiService;
    }

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
        return douBanApiService.searchByKeyword(subject.getTitle());
    }

    /**
     * 根据id查询
     */
    @RestMapping("/api/movie")
    public Object getMovie(@Param("id")String id) {
        return subjectServcie.getById(id);
    }

    @RestMapping("/api/listTopMovies")
    public Object listTopMovies(){
        return subjectServcie.getTop16();
    }
}
