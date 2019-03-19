package org.xinyo.subtitle.service;

import org.xinyo.subtitle.entity.Person;

@org.springframework.stereotype.Service
public class Service {
    public void test(Person person) {
        System.out.println("test service!!!" + person);
    }
}
