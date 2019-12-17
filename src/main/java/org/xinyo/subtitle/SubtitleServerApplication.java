package org.xinyo.subtitle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xinyo.subtitle.netty.HttpServer;
import org.xinyo.subtitle.util.BloomFilterUtils;
import org.xinyo.subtitle.util.RarUtils;

@SpringBootApplication
@MapperScan("org.xinyo.subtitle.mapper")
public class SubtitleServerApplication implements CommandLineRunner {

    private final HttpServer httpServer;

    @Value("${custom.rarPath}")
    private String rarPath;

    @Autowired
    public SubtitleServerApplication(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public static void main(String[] args) {
        SpringApplication.run(SubtitleServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        BloomFilterUtils.initFilter();
        RarUtils.initRarPath(rarPath);
        httpServer.start();
    }
}
