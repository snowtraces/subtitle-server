package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.mapper.SearchHistoryMapper;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.task.DoubanSearchThread;
import org.xinyo.subtitle.task.DoubanSearchThreadPool;

import java.time.LocalDateTime;

@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory>
        implements SearchHistoryService {
    @Override
    @Transactional
    public void add(SearchHistory searchHistory) {
        Integer total = searchHistory.getTotal();
        if (total == null || total == 0) {
            return;
        }

        super.save(searchHistory);
    }

    @Override
    public void update(SearchHistory searchHistory) {
        Integer times = searchHistory.getSearchTimes();
        searchHistory.setSearchTimes(times == null ? 0 : times + 1);
        searchHistory.setUpdateTime(LocalDateTime.now());

        super.saveOrUpdate(searchHistory);
    }

    @Override
    public boolean isSearched(String keyword) {
        SearchHistory byId = super.getById(keyword);
        return byId != null;
    }

    @Override
    public void timesIncr(String keyword) {
        baseMapper.timesIncr(keyword);
    }
}
