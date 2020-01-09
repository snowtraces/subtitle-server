package org.xinyo.subtitle.entity;

import lombok.Data;

/**
 * @author CHENG
 */
@Data
public class PageParams {
    private Integer pageIndex;
    private Integer pageSize = 10;
    private String filter;
}
