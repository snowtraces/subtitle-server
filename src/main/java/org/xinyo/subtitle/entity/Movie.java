package org.xinyo.subtitle.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("movie")
public class Movie {
    @TableId
    private Integer id;
    private String title;
    private String director;
    private String mainActor;
    private String area;
    private String summary;
    private Integer year;
}
