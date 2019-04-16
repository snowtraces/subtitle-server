package org.xinyo.subtitle.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.xinyo.subtitle.entity.SubtitleLog;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.service.SubtitleLogService;

import java.util.List;

/**
 * 布隆过滤工具类
 */
public class BloomFilterUtils {
    private static final int FILTER_SIZE = 10000;
    private static BloomFilter doubanFilter = null; // 豆瓣搜索过滤器
    private static BloomFilter subtitleFilter = null; // 字幕爬虫过滤器
    private static SearchHistoryService searchHistoryService = SpringContextHolder.getBean(SearchHistoryService.class);
    private static SubtitleLogService subtitleLogService = SpringContextHolder.getBean(SubtitleLogService.class);

    public static void initFilter() {
        // 1. 新建过滤器
        doubanFilter = BloomFilter.create(Funnels.byteArrayFunnel(), FILTER_SIZE, 0.001);
        subtitleFilter = BloomFilter.create(Funnels.byteArrayFunnel(), FILTER_SIZE * 5, 0.001);

        // 2. 初始化数据
        List<SearchHistory> searchHistories = searchHistoryService.listAll();
        if (searchHistories != null && searchHistories.size() > 0) {
            searchHistories.forEach(h -> doubanFilter.put(h.getKeyword().getBytes()));
        }

        List<SubtitleLog> subtitleLogs = subtitleLogService.listAll();
        if (subtitleLogs != null && subtitleLogs.size() > 0) {
            subtitleLogs.forEach(log -> subtitleFilter.put(log.getSubjectId().getBytes()));
        }

        System.err.println("BloomFilter 初始化完毕");
    }

    public static void pushSearch(String input) {
        doubanFilter.put(input.getBytes());
    }

    public static void pushSubtitle(String input) {
        subtitleFilter.put(input.getBytes());
    }

    /**
     * 判断是否包含
     *
     * @param input
     * @return true:已存在， false:不存在
     */
    public static boolean mightContainSearch(String input) {
        return doubanFilter.mightContain(input.getBytes());
    }

    public static boolean mightContainSubtitle(String input) {
        return subtitleFilter.mightContain(input.getBytes());
    }

}
