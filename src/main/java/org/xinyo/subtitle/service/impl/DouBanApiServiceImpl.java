package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.RequestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DouBanApiServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements DouBanApiService {
    private static final String SEARCH_URL = "http://api.douban.com/v2/movie/search?q=%s&start=%d&count=%d&apikey=0df993c66c0c636e29ecbb5344252a4a";
    private static final String DETAIL_URL = "http://api.douban.com/v2/movie/subject/%s?apikey=0df993c66c0c636e29ecbb5344252a4a";
    private static final String POSTER_URL = "http://img3.doubanio.com/view/photo/s_ratio_poster/public/%s.webp?apikey=0df993c66c0c636e29ecbb5344252a4a";
    private static final String POSTER_URL_S = "https://img3.doubanio.com/view/subject/s/public/%s.webp?apikey=0df993c66c0c636e29ecbb5344252a4a";

    private final SearchHistoryService searchHistoryService;

    @Autowired
    public DouBanApiServiceImpl(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @Override
    public SearchResultVO search(String keyword, Integer start, Integer count) {
        try {
            keyword = URLEncoder.encode(keyword, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.err.println("开始查询电影……");
        String url = String.format(SEARCH_URL, keyword, start, count);
        String json = RequestUtils.requestText(url);
        return  new Gson().fromJson(json, SearchResultVO.class);
    }

    @Override
    public SubjectVO searchDetail(Subject subject) {

        System.err.println("开始查询详情……[" + subject.getTitle() + "]");
        String url = String.format(DETAIL_URL, subject.getId());
        String json = RequestUtils.requestText(url);

        return new Gson().fromJson(json, SubjectVO.class);
    }

    @Override
    public boolean fetchPoster(Subject subject) {
        String imgId = subject.getImgId();
        String url = imgId.startsWith("s") ? String.format(POSTER_URL_S, imgId) : String.format(POSTER_URL, imgId);

        System.err.println("开始读取海报……[" + subject.getTitle() + "]");
        String bathPath = "/Users/CHENG/CODE/Projects/subtitle-angular/src/assets";
        String path = FileUtils.createPosterPath(bathPath);
        boolean isSuccess = RequestUtils.fetchBinary(url, path);

        return isSuccess;
    }

    @Override
    public List<Subject> searchByKeyword(String title) {
        List<Subject> subjects = null;

        // 1. 判断是否存在本地数据
        boolean isSearched = searchHistoryService.isSearched(title);
        if (isSearched) {
            // a 查询本地
            QueryWrapper<Subject> wrapper = new QueryWrapper<>();
            wrapper.like("title", title)
                    .or().like("original_title", title)
                    .or().like("casts", title)
                    .or().like("directors", title);
            wrapper.orderByDesc("rating");
            wrapper.last("limit 10");
            subjects = super.list(wrapper);
            searchHistoryService.timesIncr(title);
            System.out.println("LOCAL");
        } else {
            // b 查豆瓣
            SearchResultVO searchResult = search(title, 0, 10);
            
            if (searchResult != null) {
                List<SubjectVO> subjectVOs = searchResult.getSubjects();
                if (subjectVOs != null && subjectVOs.size() > 0) {
                    subjects = subjectVOs.stream().map(Subject::new).collect(Collectors.toList());
                }
                searchHistoryService.add(new SearchHistory(title, searchResult.getTotal()));
            }
            System.out.println("DOUBAN");
        }

        return subjects;
    }
}
