package org.xinyo.subtitle.entity.douban.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultVO {
    private Integer count; // 页长
    private Integer start; // 起始偏移
    private Integer total;

    private List<SubjectVO> subjects;
    private String title; // 说明
}
