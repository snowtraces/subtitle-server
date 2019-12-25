package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.douban.SearchHistory;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.service.SearchHistoryService;
import org.xinyo.subtitle.task.DoubanSearchThread;
import org.xinyo.subtitle.task.DoubanSearchThreadPool;
import org.xinyo.subtitle.util.BloomFilterUtils;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.RequestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class DouBanApiServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements DouBanApiService {
    /**
     * 备用地址 [douban.uieee.com]
     * super key [0df993c66c0c636e29ecbb5344252a4a, 0b2bdeda43b5688921839c8ecb20399b ]
     * common key [0dad551ec0f84ed02907ff5c42e8ec70, 0bcf52793711959c236df76ba534c0d4]
     */
    private static final String API_KEY = "0dad551ec0f84ed02907ff5c42e8ec70";
    private static final String SEARCH_URL = "http://api.douban.com/v2/movie/search?q=%s&start=%d&count=%d&apikey=" + API_KEY;
    private static final String DETAIL_URL = "http://api.douban.com/v2/movie/subject/%s?apikey=" + API_KEY;
    private static final String POSTER_URL = "http://img3.doubanio.com/view/photo/s_ratio_poster/public/%s.webp?apikey=" + API_KEY;
    private static final String POSTER_URL_S = "https://img3.doubanio.com/view/subject/s/public/%s.webp?apikey=" + API_KEY;

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

        log.info("开始查询电影……");
        String url = String.format(SEARCH_URL, keyword, start, count);
        String json = RequestUtils.requestText(url);
        return new Gson().fromJson(json, SearchResultVO.class);
    }

    @Override
    public SubjectVO searchDetail(Subject subject) {

        log.info("开始查询详情……[{}]", subject.getTitle());
        String url = String.format(DETAIL_URL, subject.getId());
        String json = RequestUtils.requestText(url);

        return new Gson().fromJson(json, SubjectVO.class);
    }

    @Override
    public boolean fetchPoster(Subject subject) {
        String imgId = subject.getImgId();
        String url = imgId.startsWith("s") ? String.format(POSTER_URL_S, imgId) : String.format(POSTER_URL, imgId);

        log.info("开始读取海报……[{}]", subject.getTitle());
        List<String> idPath = FileUtils.separateString(subject.getId(), 1, 5);

        String path = FileUtils.createPath(FileUtils.basePath + "poster", idPath);
        boolean isSuccess = RequestUtils.fetchBinary(url, path);

        return isSuccess;
    }

    @Override
    public List<Subject> searchByKeyword(String title) {
        List<Subject> subjects = null;

        // 1. 判断是否存在本地数据
        boolean needUpdate = true;
        boolean isSearched = BloomFilterUtils.mightContainSearch(title);
        if (isSearched) {
            needUpdate = searchHistoryService.isNeedUpdate(title);
        }
        if (!needUpdate) {
            // a 查询本地
            QueryWrapper<Subject> wrapper = new QueryWrapper<>();
            wrapper.select("id", "title", "original_title", "year", "aka", "img_id", "countries",
                    "rating", "genres", "genres", "casts", "directors");
            wrapper.like("title", title)
                    .or().like("original_title", title)
                    .or().like("aka", title)
                    .or().like("casts", title)
                    .or().like("directors", title);
            wrapper.orderByDesc("ratings_count", "rating");
            wrapper.last("limit 10");
            subjects = super.list(wrapper);
            searchHistoryService.timesIncr(title);
            log.info("LOCAL");
        } else {
            BloomFilterUtils.pushSearch(title);
            // b 查豆瓣
            SearchResultVO searchResult = search(title, 0, 10);
            if (searchResult != null) {
                searchResult.setTitle(title);
                List<SubjectVO> subjectVOs = searchResult.getSubjects();
                if (subjectVOs != null && subjectVOs.size() > 0) {
                    subjects = subjectVOs.stream().map(Subject::new).collect(Collectors.toList());
                }
                searchHistoryService.update(new SearchHistory(title, searchResult.getTotal()));
                DoubanSearchThreadPool.getInstance().submitTask(new DoubanSearchThread(searchResult));
            }
            log.info("DOUBAN");
        }

        return subjects;
    }
}
