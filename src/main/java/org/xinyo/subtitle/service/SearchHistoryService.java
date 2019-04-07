package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.douban.SearchHistory;

public interface SearchHistoryService {
    void add(SearchHistory searchHistory);

    void update(SearchHistory searchHistory);

    boolean isSearched(String keyword);

    void timesIncr(String keyword);
}
