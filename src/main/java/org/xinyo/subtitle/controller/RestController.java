package org.xinyo.subtitle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.Subtitle;
import org.xinyo.subtitle.entity.SubtitleFile;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SubjectService;
import org.xinyo.subtitle.service.SubtitleFileService;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.HotSubjectCache;

import java.util.List;

@Component
public class RestController {

    private final SubjectService subjectService;
    private final DouBanApiService douBanApiService;
    private final SubtitleService subtitleService;
    private final SubtitleFileService subtitleFileService;

    @Autowired
    public RestController(
            SubjectService subjectService,
            DouBanApiService douBanApiService,
            SubtitleService subtitleService,
            SubtitleFileService subtitleFileService
    ) {
        this.subjectService = subjectService;
        this.douBanApiService = douBanApiService;
        this.subtitleService = subtitleService;
        this.subtitleFileService = subtitleFileService;
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
    @RestMapping("/api/movie/${id}")
    public Object getMovie(String id) {
        return subjectService.getById(id);
    }

    @RestMapping("/api/movieBySubtitleId")
    public Object getMovieBySubtitleId(@Param("subtitleId")String subtitleId) {
        return subjectService.getBySubtitleId(subtitleId);
    }

    @RestMapping("/api/listTopMovies")
    public Object listTopMovies(){
        return HotSubjectCache.getInstance().getHotList();
//        return subjectService.getTopBySize(32);
    }

    @RestMapping("/api/listSubtitles")
    public Object listSubtitles(@Param("id")String id) {
        List<Subtitle> list = subtitleService.listWithWeightBySubjectId(id);
//        List<Subtitle> list = subtitleService.listBySubjectId(id);
        return list;
    }

    @RestMapping("/api/getSubtitleById")
    public Object getSubtitleById(@Param("id")String subtitleId) {
        return subtitleService.getById(subtitleId);
    }

    @RestMapping("/api/listSubtitleFile")
    public Object listSubtitleFile(@Param("subtitleId")String subtitleId) {
        List<SubtitleFile> subtitleFiles = subtitleFileService.listBySubtitleId(subtitleId);
        return subtitleFiles;
    }

    @RestMapping("/api/doDownload")
    public Object doDownload(@Param("subtitleId")String subtitleId) {
        return subtitleService.plusDownloadTimes(subtitleId);
    }

}
