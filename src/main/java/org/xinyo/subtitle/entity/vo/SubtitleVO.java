package org.xinyo.subtitle.entity.vo;

import lombok.Data;

@Data
public class SubtitleVO {
    private String id;
    private String title;
    private String subjectId;
    private String language; // [其他][德][法][韩][日][英][繁][简]
    private String type; // 1.翻译 2.官方 3.听译 4.机翻
    private String version;
    private String remark;
    private String token;
}
