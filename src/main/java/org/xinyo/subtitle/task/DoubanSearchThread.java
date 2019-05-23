package org.xinyo.subtitle.task;

import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.service.SubjectService;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class DoubanSearchThread implements Runnable, Serializable {
    private DouBanApiService douBanApiService = SpringContextHolder.getBean(DouBanApiService.class);
    private SubjectService subjectServcie = SpringContextHolder.getBean(SubjectService.class);
    private SearchHistoryService searchHistoryService = SpringContextHolder.getBean(SearchHistoryService.class);

    private final static int count = 20;
    private static int MAX_PAGE = 5;

    private SearchResultVO searchResultVO;

    public DoubanSearchThread(SearchResultVO searchResultVO) {
        this.searchResultVO = searchResultVO;
    }

    @Override
    public void run() {
        Integer total = searchResultVO.getTotal();
        String keyword = searchResultVO.getTitle();
        if (total == null || total == 0) {
            return;
        }

//        // a.分页查询
//        int maxPage = Math.min(MAX_PAGE, total / count + (total % count == 0 ? 0 : 1));
//        for (int i = 0; i < maxPage; i++) {
//            SearchResultVO search = douBanApiService.search(keyword, i * count, count);
//            List<Subject> subjects = search.getSubjects().stream()
//                    .map(Subject::new)
//                    .collect(Collectors.toList());
//
//            // 1. 保存结果
//            subjectServcie.save(subjects);
//
//            // 2. 发起海报和详情线程
//            subjects.forEach(s ->{
//                DoubanApiThreadPool.getInstance().submitTask(new DoubanPosterThread(s));
//                DoubanApiThreadPool.getInstance().submitTask(new DoubanDetailThread(s));
//            });
//        }

        // b.不分页查询
        List<Subject> subjects = searchResultVO.getSubjects().stream()
                .map(Subject::new)
                .collect(Collectors.toList());

        // 1. 保存结果
        subjectServcie.save(subjects);

        // 2. 发起海报和详情线程
        subjects.forEach(s ->{
            DoubanApiThreadPool.getInstance().submitTask(new DoubanPosterThread(s));
            DoubanApiThreadPool.getInstance().submitTask(new DoubanDetailThread(s));
        });

    }
}
