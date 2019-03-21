package org.xinyo.subtitle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.Movie;
import org.xinyo.subtitle.entity.Person;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.annotation.Reference;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.MovieService;
import org.xinyo.subtitle.service.Service;

@Component
public class RestController {

    @Reference
    @Autowired
    private Service service;

    @Autowired
    private MovieService movieService;

    @RestMapping("/data")
    public Object data(@Param("name") String name, Person person) {
        service.test(person);
        return person;
    }

    @RestMapping("/api/searchMovies")
    public Object searchMovies(Movie movie) {
        return movieService.listByKeyword(movie.getTitle());
    }

    @RestMapping("/api/listMovies")
    public Object listMovies(Movie movie) {
        return movieService.listByKeyword(movie.getTitle());
    }
}
