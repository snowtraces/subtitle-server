package org.xinyo.subtitle.entity.douban.vo;

import lombok.Data;

import java.util.Map;

@Data
public class PersonVO {
    private String id;
    private String name;
    private Map<String, String> avatars;
}
