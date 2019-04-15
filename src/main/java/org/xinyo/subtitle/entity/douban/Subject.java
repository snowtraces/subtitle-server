package org.xinyo.subtitle.entity.douban;

import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.xinyo.subtitle.entity.douban.vo.PersonVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@TableName("douban_subject")
public class Subject {
    private String id;
    private String title;
    private String originalTitle;
    private String subtype;
    private Integer year;
    private String aka;
    private String countries;

    private Integer seasonsCount;
    private String episodesCount;
    private String currentSeason;
    private Integer ratingsCount;

    private String imgId;
    private String genres;
    private Integer rating;

    private String casts;
    private String directors;

    private String summary;

    public Subject(){}
    public Subject(SubjectVO subjectVO){
        BeanUtils.copyProperties(subjectVO, this);

        this.year = Strings.isNullOrEmpty(subjectVO.getYear()) ? null : Integer.valueOf(subjectVO.getYear());
        this.originalTitle = subjectVO.getOriginal_title();
        this.imgId = extractImgId(subjectVO);
        this.genres = extractStringList(subjectVO.getGenres());
        this.rating = extractRating(subjectVO);
        this.casts = extractPerson(subjectVO.getCasts());
        this.directors = extractPerson(subjectVO.getDirectors());

        this.seasonsCount = subjectVO.getSeasons_count();
        this.episodesCount = subjectVO.getEpisodes_count();
        this.currentSeason = subjectVO.getCurrent_season();
        this.ratingsCount = subjectVO.getRatings_count();

        this.aka = extractStringList(subjectVO.getAka());
        this.countries = extractStringList(subjectVO.getCountries());

        normalize();
    }

    private void normalize() {
        if (!Strings.isNullOrEmpty(summary)) {
            summary = summary.replace("©豆瓣", "");
        }
    }

    private String extractPerson(PersonVO[] personVOS) {
        if (personVOS == null) {
            return null;
        }

        List<String> names = Arrays.stream(personVOS).map(PersonVO::getName).collect(Collectors.toList());
        return String.join("/", names);
    }

    private Integer extractRating(SubjectVO subjectVO) {
        Map<String, Object> rating = subjectVO.getRating();
        if (rating == null) {
            return null;
        }
        double average = (double) rating.get("average");
        return (int) (average * 100);
    }

    private String extractImgId(SubjectVO subjectVO) {
        Map<String, String> images = subjectVO.getImages();
        if (images == null) {
            return null;
        }
        String small = images.get("small"); // https://img3.doubanio.com/view/photo/s_ratio_poster/public/p2551118196.webp
        return small.substring(small.lastIndexOf("/") + 1, small.lastIndexOf("."));
    }

    private String extractStringList(String[] source) {
        if (source == null) {
            return null;
        } else {
            return String.join("/", source);
        }
    }

}
