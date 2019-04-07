package org.xinyo.subtitle.entity.douban.vo;

import lombok.Data;

import java.util.Map;

@Data
public class SubjectVO {
    private String id;
    private String title;
    private String original_title;
    private String subtype;
    private String year;

    private Map<String, String> images;
    private String[] genres;
    private Map<String, Object> rating;

    private PersonVO[] casts;
    private PersonVO[] directors;
}
