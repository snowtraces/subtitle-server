package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.SearchHistory;

import java.util.List;

public interface SearchHistoryService {
    void add(SearchHistory searchHistory);

    void update(SearchHistory searchHistory);

    boolean isSearched(String keyword);

    void timesIncr(String keyword);

    List<SearchHistory> listAll();

    boolean isNeedUpdate(String keyword);
}
