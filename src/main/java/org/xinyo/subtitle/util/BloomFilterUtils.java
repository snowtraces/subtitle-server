package org.xinyo.subtitle.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.service.SearchHistoryService;

import java.util.List;

/**
 * 布隆过滤工具类
 */
public class BloomFilterUtils {
    private static final int FILTER_SIZE = 10000;
    private static BloomFilter bloomFilter = null;
    private static SearchHistoryService searchHistoryService = SpringContextHolder.getBean(SearchHistoryService.class);

    public static void initFilter() {
        // 1. 新建过滤器
        bloomFilter = BloomFilter.create(Funnels.byteArrayFunnel(), FILTER_SIZE, 0.001);

        // 2. 初始化数据
        List<SearchHistory> searchHistories = searchHistoryService.listAll();
        if (searchHistories != null && searchHistories.size() > 0) {
            searchHistories.forEach(h -> bloomFilter.put(h.getKeyword().getBytes()));
        }

        System.err.println("BloomFilter 初始化完毕");
    }

    public static void push(String input) {
        bloomFilter.put(input.getBytes());
    }

    /**
     * 判断是否包含
     *
     * @param input
     * @return true:已存在， false:不存在
     */
    public static boolean checkIsExist(String input) {
        return bloomFilter.mightContain(input.getBytes());
    }

}
