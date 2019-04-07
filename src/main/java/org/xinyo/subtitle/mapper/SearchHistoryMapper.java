package org.xinyo.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.xinyo.subtitle.entity.douban.SearchHistory;

@Repository
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    void timesIncr(String keyword);
}
