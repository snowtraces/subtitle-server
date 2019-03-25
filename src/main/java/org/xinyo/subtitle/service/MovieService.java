package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.Movie;

import java.util.List;

public interface MovieService {
    List<Movie> listByKeyword(String title);

    Object getById(String id);
}
