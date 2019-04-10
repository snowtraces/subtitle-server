package org.xinyo.subtitle.task;

import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;

public class DoubanSearchThread implements Runnable, Serializable {
    private DouBanApiService douBanApiService = SpringContextHolder.getBean(DouBanApiService.class);
    private SearchHistoryService searchHistoryService = SpringContextHolder.getBean(SearchHistoryService.class);

    private final static int count = 20;
    private static int MAX_PAGE = 5;

    private SearchHistory searchHistory;

    public DoubanSearchThread(SearchHistory searchHistory) {
        this.searchHistory = searchHistory;
    }

    @Override
    public void run() {
        Integer total = searchHistory.getTotal();
        if (total == null || total == 0) {
            return;
        }

        // 执行查询
        int maxPage = Math.min(MAX_PAGE, total / count + (total % count == 0 ? 0 : 1));
        String keyword = searchHistory.getKeyword();
        for (int i = 0; i < maxPage; i++) {
            SearchResultVO search = douBanApiService.search(keyword, i * count, count);
            if (i == 0) {
                searchHistory.setTotal(search.getTotal());
            }

            // 保存结果
            douBanApiService.save(search.getSubjects());
        }

        // 更新历史记录
        searchHistoryService.update(searchHistory);
    }
}
