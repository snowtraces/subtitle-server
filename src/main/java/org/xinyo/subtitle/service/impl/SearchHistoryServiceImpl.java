package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.mapper.SearchHistoryMapper;
import org.xinyo.subtitle.service.SearchHistoryService;

import java.time.LocalDateTime;
import java.util.List;

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
    @Transactional
    public void update(SearchHistory searchHistory) {

        String keyword = searchHistory.getKeyword();
        SearchHistory byId = super.getById(keyword);

        Integer times = byId.getSearchTimes();
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

    @Override
    public List<SearchHistory> listAll() {
        return super.list();
    }

    @Override
    public boolean isNeedUpdate(String keyword) {
        QueryWrapper<SearchHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("keyword", keyword);
        wrapper.last("limit 1");

        SearchHistory one = super.getOne(wrapper);

        if (one == null) {
            return true;
        } else {
            LocalDateTime updateTime = one.getUpdateTime();
            if (updateTime.isBefore(LocalDateTime.now().minusDays(7))) {
                return true;
            }
        }

        return false;
    }
}
