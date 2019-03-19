package org.xinyo.subtitle.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.Person;
import org.xinyo.subtitle.netty.annotation.Param;
import org.xinyo.subtitle.netty.annotation.Reference;
import org.xinyo.subtitle.netty.annotation.RestMapping;
import org.xinyo.subtitle.service.Service;

@Component
public class RestController {

    @Reference
    @Autowired
    private Service service;

    @RestMapping("/data")
    public String data(@Param("name") String name, Person person){
        service.test(person);
        Gson gson = new Gson();
        return gson.toJson(person);
    }

    @RestMapping("/api/searchMovies")
    public String searchMovies(@Param("term") String term, Person person){
        service.test(person);
        Gson gson = new Gson();
        return gson.toJson(person);
    }
}
