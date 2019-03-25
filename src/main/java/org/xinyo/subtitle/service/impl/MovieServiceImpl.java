package org.xinyo.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xinyo.subtitle.entity.Movie;
import org.xinyo.subtitle.mapper.MovieMapper;
import org.xinyo.subtitle.service.MovieService;

import java.util.List;

@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements MovieService {

    @Override
    public List<Movie> listByKeyword(String title) {
        QueryWrapper<Movie> wrapper = new QueryWrapper<>();
        wrapper.like("title", title);
        wrapper.last("limit 10");

        return baseMapper.selectList(wrapper);
    }

    @Override
    public Object getById(String id) {
        return super.getById(id);
    }
}
