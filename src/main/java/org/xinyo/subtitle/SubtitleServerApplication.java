package org.xinyo.subtitle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xinyo.subtitle.netty.HttpServer;

@SpringBootApplication
@MapperScan("org.xinyo.subtitle.mapper")
public class SubtitleServerApplication implements CommandLineRunner {

    @Autowired
    private HttpServer httpServer;

    public static void main(String[] args) {
//        new SpringApplicationBuilder(SubtitleServerApplication.class).web(WebApplicationType.NONE).run(args);
        SpringApplication.run(SubtitleServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        httpServer.start();
    }
}
