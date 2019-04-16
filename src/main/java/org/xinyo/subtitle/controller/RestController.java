package org.xinyo.subtitle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectService;
import org.xinyo.subtitle.service.SubtitleService;

import java.util.List;

@Component
public class RestController {

    private final SubjectService subjectService;
    private final DouBanApiService douBanApiService;
    private final SubtitleService subtitleService;

    @Autowired
    public RestController(
            SubjectService subjectService,
            DouBanApiService douBanApiService,
            SubtitleService subtitleService
    ) {
        this.subjectService = subjectService;
        this.douBanApiService = douBanApiService;
        this.subtitleService = subtitleService;
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
        return subjectService.getById(id);
    }

    @RestMapping("/api/listTopMovies")
    public Object listTopMovies(){
        return subjectService.getTopBySize(24);
    }

    @RestMapping("/api/listSubtitles")
    public Object listSubtitles(@Param("id")String id) {
        List<Subtitle> list = subtitleService.listBySubjectId(id);
        return list;
    }
}
