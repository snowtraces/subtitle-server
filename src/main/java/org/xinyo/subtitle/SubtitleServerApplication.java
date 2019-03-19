package org.xinyo.subtitle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.xinyo.subtitle.netty.HttpServer;
import org.xinyo.subtitle.service.Service;

@SpringBootApplication
public class SubtitleServerApplication implements CommandLineRunner {

    @Autowired
    private HttpServer httpServer;

    public static void main(String[] args) {
        new SpringApplicationBuilder(SubtitleServerApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        httpServer.start();
    }
}
