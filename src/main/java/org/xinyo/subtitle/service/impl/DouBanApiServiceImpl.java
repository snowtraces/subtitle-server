package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xinyo.subtitle.entity.douban.Subject;
import org.xinyo.subtitle.entity.douban.vo.SearchResultVO;
import org.xinyo.subtitle.entity.douban.vo.SubjectVO;
import org.xinyo.subtitle.mapper.SubjectMapper;
import org.xinyo.subtitle.service.DouBanApiService;
import org.xinyo.subtitle.util.RequestUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DouBanApiServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements DouBanApiService {
    private static final String SEARCH_URL = "http://api.douban.com/v2/movie/search?q=%s&start=0&count=10&apikey=0df993c66c0c636e29ecbb5344252a4a";

    @Override
    public List<Subject> searchByKeyword(String title) {

        String url = String.format(SEARCH_URL, title);
        String json = RequestUtils.requestText(url);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SearchResultVO searchResult = gson.fromJson(json, SearchResultVO.class);
        List<Subject> subjects = save(searchResult.getSubjects());

        return subjects;
    }

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
