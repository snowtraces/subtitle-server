package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.util.RequestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DouBanApiServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements DouBanApiService {
    private static final String SEARCH_URL = "http://api.douban.com/v2/movie/search?q=%s&start=%d&count=%d&apikey=0df993c66c0c636e29ecbb5344252a4a";

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Override
    public SearchResultVO search(String keyword, Integer start, Integer count) {
        String url = String.format(SEARCH_URL, keyword, start, count);
        String json = RequestUtils.requestText(url);
        Gson gson = new Gson();
        SearchResultVO searchResult = gson.fromJson(json, SearchResultVO.class);

        return searchResult;
    }

    @Override
    public List<Subject> searchByKeyword(String title) {
        List<Subject> subjects = new ArrayList<>();

        // 1. 判断是否存在本地数据
        boolean isSearched = searchHistoryService.isSearched(title);
        if (isSearched) {
            // a 查询本地
            QueryWrapper<Subject> wrapper = new QueryWrapper<>();
            wrapper.like("title", title)
                    .or().like("casts", title)
                    .or().like("directors", title);
            wrapper.last("limit 10");
            subjects = super.list(wrapper);
            searchHistoryService.timesIncr(title);
            System.out.println("LOCAL");
        } else {
            // b 查豆瓣
            SearchResultVO searchResult = search(title, 0, 10);
            subjects = save(searchResult.getSubjects());
            searchHistoryService.add(new SearchHistory(title, searchResult.getTotal()));
            System.out.println("DOUBAN");
        }

        return subjects;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Subject> save(List<SubjectVO> subjects) {
        if (subjects == null || subjects.size() == 0) {
            return null;
        }

        List<Subject> subjectList = subjects.stream()
                .map(Subject::new)
                .collect(Collectors.toList());

        List<Subject> newList = subjectList.stream().filter(o -> super.getById(o.getId()) == null).collect(Collectors.toList());
        super.saveBatch(newList);
        return subjectList;
    }
}
