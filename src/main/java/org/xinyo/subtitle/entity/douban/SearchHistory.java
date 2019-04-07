package org.xinyo.subtitle.entity.douban;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("douban_search_history")
public class SearchHistory {
    @TableId
    private String keyword;
    private LocalDateTime updateTime;
    private Integer searchTimes;
    private Integer total;

    public SearchHistory(){}

    public SearchHistory(String keyword, Integer total) {
        this.keyword = keyword;
        this.total = total;
    }
}
